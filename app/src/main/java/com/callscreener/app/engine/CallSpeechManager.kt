package com.callscreener.app.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.callscreener.app.model.VoiceGender
import com.callscreener.app.model.VoicePersona
import com.callscreener.app.service.CallScreeningInCallService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class CallSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    private data class PendingSpeech(val text: String, val isSimulated: Boolean)
    private val pendingQueue = mutableListOf<PendingSpeech>()

    val injector = RootCallAudioInjector(context)
    var useDirectRootInjection: Boolean = false
    var isSimulatorMode: Boolean = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    var onSpeechFinished: (() -> Unit)? = null
    var onAudioFileReadyForMonitor: ((File) -> Unit)? = null
    var onAudioFileMonitorStop: (() -> Unit)? = null

    private val utteranceListeners = ConcurrentHashMap<String, (Boolean) -> Unit>()

    private val _activePersona = MutableStateFlow(VoicePersona.DEFAULT)
    val activePersona: StateFlow<VoicePersona> = _activePersona.asStateFlow()

    private var activeFocusRequest: AudioFocusRequest? = null

    init {
        ensureTtsInitialized()
    }

    private fun ensureTtsInitialized() {
        if (textToSpeech == null) {
            try {
                textToSpeech = TextToSpeech(context.applicationContext, this, "com.google.android.tts")
            } catch (e: Exception) {
                try {
                    textToSpeech = TextToSpeech(context.applicationContext, this)
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to instantiate TextToSpeech", e2)
                }
            }
        }
    }

    fun setVoicePersona(persona: VoicePersona) {
        _activePersona.value = persona
        applyVoiceForPersona(persona)
    }

    private fun applyVoiceForText(text: String, persona: VoicePersona) {
        val tts = textToSpeech ?: return
        tts.setPitch(persona.pitch)
        tts.setSpeechRate(persona.speed)

        try {
            val hasBengaliAssamese = text.any { it in '\u0980'..'\u09FF' }

            if (hasBengaliAssamese) {
                var res = tts.setLanguage(Locale("as", "IN"))
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    res = tts.setLanguage(Locale("bn", "IN"))
                }
                if (res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED) {
                    val available = tts.voices
                    val indicVoice = available?.firstOrNull { it.locale.language.equals("as", ignoreCase = true) || it.locale.language.equals("bn", ignoreCase = true) }
                    if (indicVoice != null) tts.voice = indicVoice
                    return
                }
            }

            applyVoiceForPersona(persona)
        } catch (e: Exception) {
            Log.w(TAG, "Error in applyVoiceForText", e)
            applyVoiceForPersona(persona)
        }
    }

    private fun applyVoiceForPersona(persona: VoicePersona) {
        val tts = textToSpeech ?: return
        tts.setPitch(persona.pitch)
        tts.setSpeechRate(persona.speed)

        try {
            val targetLocale = persona.locale
            val langResult = tts.setLanguage(targetLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Locale $targetLocale not fully supported, trying fallback")
                if (persona.isHindi) {
                    tts.setLanguage(Locale("hi", "IN"))
                } else {
                    tts.setLanguage(Locale.US)
                }
            }

            val availableVoices = tts.voices
            if (!availableVoices.isNullOrEmpty()) {
                val targetLangCode = persona.languageCode

                val langVoices = availableVoices.filter { voice ->
                    voice.locale.language.equals(targetLangCode, ignoreCase = true)
                }
                val candidateVoices = if (langVoices.isNotEmpty()) {
                    langVoices
                } else if (persona.isHindi) {
                    val indianVoices = availableVoices.filter {
                        it.locale.country.equals("IN", ignoreCase = true)
                    }
                    if (indianVoices.isNotEmpty()) indianVoices else availableVoices.filter {
                        it.locale.language.equals("en", ignoreCase = true)
                    }
                } else {
                    availableVoices.filter {
                        it.locale.language.equals("en", ignoreCase = true)
                    }
                }

                val matchingVoice = when (persona.gender) {
                    VoiceGender.FEMALE -> {
                        candidateVoices.firstOrNull { voice ->
                            val vName = voice.name.lowercase()
                            val matchesKw = persona.voiceKeywords.any { kw -> vName.contains(kw) }
                            val isNeural = isNeuralQuality(voice)
                            isFemaleVoiceName(vName) && matchesKw && isNeural
                        }
                        ?: candidateVoices.firstOrNull { voice ->
                            val vName = voice.name.lowercase()
                            val matchesKw = persona.voiceKeywords.any { kw -> vName.contains(kw) }
                            isFemaleVoiceName(vName) && matchesKw && !isNotInstalled(voice)
                        }
                        ?: candidateVoices.firstOrNull { voice ->
                            val vName = voice.name.lowercase()
                            isFemaleVoiceName(vName) && isNeuralQuality(voice)
                        }
                        ?: candidateVoices.firstOrNull { voice ->
                            val vName = voice.name.lowercase()
                            isFemaleVoiceName(vName) && !isNotInstalled(voice)
                        }
                        ?: candidateVoices.firstOrNull { voice ->
                            isFemaleVoiceName(voice.name.lowercase())
                        }
                        ?: candidateVoices.firstOrNull { !isMaleVoiceName(it.name.lowercase()) }
                        ?: candidateVoices.firstOrNull()
                    }
                    VoiceGender.MALE -> {
                        candidateVoices.firstOrNull { voice ->
                            val vName = voice.name.lowercase()
                            val matchesKw = persona.voiceKeywords.any { kw -> vName.contains(kw) }
                            isMaleVoiceName(vName) && matchesKw && isNeuralQuality(voice)
                        }
                        ?: candidateVoices.firstOrNull { voice ->
                            val vName = voice.name.lowercase()
                            val matchesKw = persona.voiceKeywords.any { kw -> vName.contains(kw) }
                            isMaleVoiceName(vName) && matchesKw && !isNotInstalled(voice)
                        }
                        ?: candidateVoices.firstOrNull { voice ->
                            val vName = voice.name.lowercase()
                            isMaleVoiceName(vName) && !isNotInstalled(voice)
                        }
                        ?: candidateVoices.firstOrNull { voice ->
                            isMaleVoiceName(voice.name.lowercase())
                        }
                        ?: availableVoices.firstOrNull { voice ->
                            val vName = voice.name.lowercase()
                            isMaleVoiceName(vName) && !isNotInstalled(voice)
                        }
                        ?: availableVoices.firstOrNull { voice ->
                            isMaleVoiceName(voice.name.lowercase())
                        }
                        ?: candidateVoices.firstOrNull()
                    }
                    VoiceGender.NEUTRAL -> candidateVoices.firstOrNull()
                }

                if (matchingVoice != null) {
                    tts.voice = matchingVoice
                    Log.i(TAG, "Assigned natural voice: ${matchingVoice.name} (quality=${matchingVoice.quality}, locale=${matchingVoice.locale}) for persona: ${persona.name}")
                }

                var targetPitch = persona.pitch
                if (persona.gender == VoiceGender.MALE) {
                    val isConfirmedMale = matchingVoice != null && isMaleVoiceName(matchingVoice.name.lowercase())
                    if (!isConfirmedMale) {
                        targetPitch = minOf(targetPitch, 0.62f)
                        Log.w(TAG, "No confirmed male voice on device; shifted pitch to deep masculine register: $targetPitch")
                    } else {
                        targetPitch = minOf(targetPitch, 0.85f)
                    }
                } else if (persona.gender == VoiceGender.FEMALE) {
                    val isConfirmedFemale = matchingVoice != null && isFemaleVoiceName(matchingVoice.name.lowercase())
                    if (!isConfirmedFemale) {
                        targetPitch = maxOf(targetPitch, 1.15f)
                        Log.w(TAG, "No confirmed female voice on device; shifted pitch to feminine register: $targetPitch")
                    }
                }
                tts.setPitch(targetPitch)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Voice selection fallback to default engine", e)
            try {
                tts.voice = null
            } catch (_: Exception) {}
        }
    }

    private fun isFemaleVoiceName(name: String): Boolean {
        return name.contains("female") || name.contains("#female") ||
                name.contains("-f-") || name.contains("-fem-") || name.contains("f00") ||
                name.contains("sfg") || name.contains("iob") || name.contains("tpf") ||
                name.contains("cfl") || name.contains("rjs") ||
                name.contains("hia") || name.contains("hie") || name.contains("hic") ||
                name.contains("woman") || name.contains("girl")
    }

    private fun isMaleVoiceName(name: String): Boolean {
        if (isFemaleVoiceName(name)) return false
        return name.contains("male") || name.contains("#male") || name.contains("-m-") ||
                name.contains("m00") || name.contains("hid") || name.contains("hib") ||
                name.contains("iom") || name.contains("iol") || name.contains("tpc") ||
                name.contains("-d-") || name.contains("-b-") || name.contains("hnd") ||
                name.contains("_male") || name.contains("man") || name.contains("boy")
    }

    private fun isNeuralQuality(voice: Voice): Boolean {
        val name = voice.name.lowercase()
        return name.contains("network") ||
                name.contains("neural") ||
                name.contains("wavenet") ||
                voice.quality >= Voice.QUALITY_HIGH
    }

    private fun isNotInstalled(voice: Voice): Boolean {
        return voice.features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) == true
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                val result = tts.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.language = Locale.US
                }

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                tts.setAudioAttributes(audioAttributes)

                applyVoiceForPersona(_activePersona.value)

                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        if (utteranceId != null && utteranceListeners.containsKey(utteranceId)) {
                            return
                        }
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        if (utteranceId != null) {
                            val listener = utteranceListeners.remove(utteranceId)
                            if (listener != null) {
                                listener(true)
                                return
                            }
                        }
                        _isSpeaking.value = false
                        abandonFocus()
                        onSpeechFinished?.invoke()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (utteranceId != null) {
                            val listener = utteranceListeners.remove(utteranceId)
                            if (listener != null) {
                                listener(false)
                                return
                            }
                        }
                        _isSpeaking.value = false
                        abandonFocus()
                        onSpeechFinished?.invoke()
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        if (utteranceId != null) {
                            val listener = utteranceListeners.remove(utteranceId)
                            if (listener != null) {
                                listener(false)
                                return
                            }
                        }
                        _isSpeaking.value = false
                        abandonFocus()
                        Log.e(TAG, "TTS Error: $errorCode on utterance: $utteranceId")
                        onSpeechFinished?.invoke()
                    }
                })

                isInitialized = true
                synchronized(pendingQueue) {
                    for (item in pendingQueue) {
                        speak(item.text, item.isSimulated)
                    }
                    pendingQueue.clear()
                }
            }
        } else {
            Log.e(TAG, "Failed to initialize TextToSpeech engine with status code: $status")
            isInitialized = false
        }
    }

    fun speak(text: String, isSimulated: Boolean = false) {
        val processedText = normalizeHinglishForTts(text, _activePersona.value)
        val localSim = isSimulated || isSimulatorMode
        if (!isInitialized || textToSpeech == null) {
            synchronized(pendingQueue) {
                pendingQueue.add(PendingSpeech(processedText, localSim))
            }
            ensureTtsInitialized()
            return
        }

        if (localSim) {
            speakInternal(processedText, true)
            return
        }

        _isSpeaking.value = true
        scope.launch {
            try {
                applyVoiceForText(processedText, _activePersona.value)

                val tts = textToSpeech
                if (tts == null) {
                    _isSpeaking.value = false
                    speakInternal(processedText, false)
                    return@launch
                }

                val baseDir = context.getExternalFilesDir(null) ?: context.cacheDir
                val ttsDir = File(baseDir, "tts_cache").apply { mkdirs() }
                val tempWavFile = File(ttsDir, "tts_syn_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.wav")

                val utteranceId = "inject_${UUID.randomUUID()}"
                val synthLatch = CompletableDeferred<Boolean>()

                utteranceListeners[utteranceId] = { success ->
                    synthLatch.complete(success)
                }

                val params = Bundle().apply {
                    putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_VOICE_CALL)
                    putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                }

                val synthResult = tts.synthesizeToFile(processedText, params, tempWavFile, utteranceId)
                Log.i(TAG, "synthesizeToFile queued result: $synthResult for: '$processedText' -> ${tempWavFile.absolutePath}")

                var synthSuccess = false
                if (synthResult == TextToSpeech.SUCCESS) {
                    synthSuccess = try {
                        withTimeoutOrNull(3500L) {
                            synthLatch.await()
                        } ?: false
                    } catch (_: Exception) {
                        false
                    }
                }
                utteranceListeners.remove(utteranceId)

                if (!synthSuccess) {
                    var waitMs = 0
                    var lastSize = -1L
                    var stableCount = 0
                    while (waitMs < 1500) {
                        delay(50L)
                        waitMs += 50
                        if (tempWavFile.exists() && tempWavFile.length() > 44) {
                            val currentSize = tempWavFile.length()
                            if (currentSize == lastSize) {
                                stableCount++
                                if (stableCount >= 2) {
                                    synthSuccess = true
                                    break
                                }
                            } else {
                                lastSize = currentSize
                                stableCount = 0
                            }
                        }
                    }
                }

                if (synthSuccess && tempWavFile.exists() && tempWavFile.length() > 44) {
                    Log.i(TAG, "Speech synthesized successfully (${tempWavFile.length()} bytes). Injecting into call uplink...")
                    onAudioFileReadyForMonitor?.invoke(tempWavFile)

                    injector.injectAudioFile(tempWavFile) { injected ->
                        onAudioFileMonitorStop?.invoke()
                        try { tempWavFile.delete() } catch (_: Exception) {}
                        Log.i(TAG, "In-call speech injection finished. Success = $injected")
                        _isSpeaking.value = false
                        onSpeechFinished?.invoke()
                    }
                } else {
                    Log.w(TAG, "synthesizeToFile failed to produce valid audio file; falling back to speakInternal()")
                    try { tempWavFile.delete() } catch (_: Exception) {}
                    speakInternal(processedText, false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in in-call speech synthesis injection", e)
                _isSpeaking.value = false
                speakInternal(processedText, false)
            }
        }
    }

    private fun normalizeHinglishForTts(input: String, persona: VoicePersona): String {
        if (!persona.isHindi) return input
        val devanagariCount = input.count { it in '\u0900'..'\u097F' }
        if (devanagariCount > input.length / 3) return input

        var text = input
        val replacements = listOf(
            Regex("(?i)\\bnamaste\\b") to "नमस्ते",
            Regex("(?i)\\bnamaskar\\b") to "नमस्कार",
            Regex("(?i)\\baap\\b") to "आप",
            Regex("(?i)\\bkaun\\b") to "कौन",
            Regex("(?i)\\bkaunsa\\b") to "कौनसा",
            Regex("(?i)\\bbol\\b") to "बोल",
            Regex("(?i)\\brahe\\b") to "रहे",
            Regex("(?i)\\braha\\b") to "रहा",
            Regex("(?i)\\brahi\\b") to "रही",
            Regex("(?i)\\bhain\\b") to "हैं",
            Regex("(?i)\\bhai\\b") to "है",
            Regex("(?i)\\bho\\b") to "हो",
            Regex("(?i)\\bmain\\b") to "मैं",
            Regex("(?i)\\bhum\\b") to "हम",
            Regex("(?i)\\btum\\b") to "तुम",
            Regex("(?i)\\bmera\\b") to "मेरा",
            Regex("(?i)\\bmeri\\b") to "मेरी",
            Regex("(?i)\\bhun\\b|\\bhoon\\b") to "हूँ",
            Regex("(?i)\\bkya\\b") to "क्या",
            Regex("(?i)\\bkyun\\b|\\bkyu\\b") to "क्यों",
            Regex("(?i)\\bkaam\\b") to "काम",
            Regex("(?i)\\bbaat\\b") to "बात",
            Regex("(?i)\\bkarni\\b|\\bkarna\\b") to "करनी",
            Regex("(?i)\\bkisse\\b") to "किससे",
            Regex("(?i)\\bkisko\\b") to "किसको",
            Regex("(?i)\\bkis\\b") to "किस",
            Regex("(?i)\\bliye\\b|\\blie\\b") to "लिए",
            Regex("(?i)\\bbatao\\b|\\bbtaye\\b|\\bbataiye\\b") to "बताइए",
            Regex("(?i)\\bsuniye\\b") to "सुनिए",
            Regex("(?i)\\bbolo\\b") to "बोलो",
            Regex("(?i)\\bbusy\\b") to "व्यस्त",
            Regex("(?i)\\babhi\\b") to "अभी",
            Regex("(?i)\\bbaad\\b") to "बाद",
            Regex("(?i)\\bmein\\b|\\bme\\b") to "में",
            Regex("(?i)\\bcall\\b") to "कॉल",
            Regex("(?i)\\bkaro\\b") to "करो",
            Regex("(?i)\\bkarein\\b") to "करें",
            Regex("(?i)\\bkijiye\\b|\\bkijiye ga\\b") to "कीजिए",
            Regex("(?i)\\bphone\\b") to "फ़ोन",
            Regex("(?i)\\bmat\\b") to "मत",
            Regex("(?i)\\bnahi\\b|\\bnahin\\b|\\bna\\b") to "नहीं",
            Regex("(?i)\\bhaan\\b|\\bha\\b") to "हाँ",
            Regex("(?i)\\btheek\\b|\\bthik\\b") to "ठीक",
            Regex("(?i)\\baccha\\b|\\bacchaa\\b") to "अच्छा",
            Regex("(?i)\\bdhanyawad\\b|\\bshukriya\\b|\\bthanks\\b") to "धन्यवाद",
            Regex("(?i)\\bmessage\\b") to "संदेश",
            Regex("(?i)\\bchhod\\b|\\bchhodna\\b") to "छोड़",
            Regex("(?i)\\bdo\\b|\\bde\\b|\\bdijiye\\b") to "दीजिए",
            Regex("(?i)\\bbhejo\\b|\\bbhejiye\\b") to "भेजिए",
            Regex("(?i)\\binterested\\b") to "रुचि",
            Regex("(?i)\\bchahiye\\b") to "चाहिए",
            Regex("(?i)\\bwrong\\b\\s*number") to "गलत नंबर",
            Regex("(?i)\\bwrong\\b") to "गलत",
            Regex("(?i)\\bnumber\\b") to "नंबर",
            Regex("(?i)\\bparcel\\b") to "पार्सल",
            Regex("(?i)\\bcourier\\b") to "कूरियर",
            Regex("(?i)\\bdelivery\\b") to "डिलीवरी",
            Regex("(?i)\\bdarwaze\\b|\\bdarwaja\\b") to "दरवाज़े",
            Regex("(?i)\\bpar\\b|\\bpe\\b") to "पर",
            Regex("(?i)\\brakh\\b") to "रख",
            Regex("(?i)\\bplease\\b|\\bkripya\\b") to "कृपया"
        )

        for ((pattern, devanagari) in replacements) {
            text = pattern.replace(text, devanagari)
        }
        return text
    }

    private fun speakInternal(text: String, isSimulated: Boolean) {
        val utteranceId = UUID.randomUUID().toString()
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (isSimulated) {
            try {
                audioManager.mode = AudioManager.MODE_NORMAL

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        )
                        .build()
                    activeFocusRequest = focusRequest
                    audioManager.requestAudioFocus(focusRequest)
                } else {
                    @Suppress("DEPRECATION")
                    audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to prepare media audio routing for Bluetooth earbuds", e)
            }
        }

        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(if (isSimulated) AudioAttributes.USAGE_MEDIA else AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build()
            textToSpeech?.setAudioAttributes(attributes)
        } catch (e: Exception) {
            Log.w(TAG, "Could not set audio attributes for TTS", e)
        }

        val stream = if (isSimulated) AudioManager.STREAM_MUSIC else AudioManager.STREAM_VOICE_CALL
        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, stream)
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            putString(TextToSpeech.Engine.KEY_PARAM_PAN, "0.0")
            putString("networkTts", "true")
        }

        applyVoiceForText(text, _activePersona.value)

        val result = textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        Log.i(TAG, "TTS speakInternal() isSimulated=$isSimulated, stream=$stream, result=$result for text: '$text'")

        if (result != TextToSpeech.SUCCESS) {
            Log.w(TAG, "TTS speak returned error $result, reinitializing engine...")
            isInitialized = false
            try {
                textToSpeech?.shutdown()
            } catch (e: Exception) {
                Log.w(TAG, "Error shutting down dead TTS engine", e)
            }
            textToSpeech = null
            synchronized(pendingQueue) {
                pendingQueue.add(PendingSpeech(text, isSimulated))
            }
            ensureTtsInitialized()
        }
    }

    private fun abandonFocus() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activeFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
            activeFocusRequest = null
        } catch (e: Exception) {
            Log.w(TAG, "Error abandoning audio focus", e)
        }
    }

    fun stop() {
        textToSpeech?.stop()
        onAudioFileMonitorStop?.invoke()
        abandonFocus()
        _isSpeaking.value = false
    }

    fun release() {
        stop()
        injector.release()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }

    companion object {
        private const val TAG = "CallSpeechManager"
    }
}
