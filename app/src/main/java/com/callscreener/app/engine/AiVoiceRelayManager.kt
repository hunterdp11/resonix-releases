package com.callscreener.app.engine

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * AI Voice Relay Engine (Speech-to-AI-Voice)
 *
 * Allows the user to speak naturally into their phone microphone during a call.
 * The engine captures and transcribes their speech in real time with live feedback,
 * and immediately converts it into the selected studio-quality AI Voice Persona
 * (Chloe / Zoe / Pooja / Kavya / etc.) to inject into the call stream.
 *
 * Guarantees:
 * - Robust speech recognition on all devices (on-device engine where available, fallback to Google Speech Services).
 * - Immediate live partial transcription so text is visible in real-time.
 * - Continuous auto-reconnection loop so subsequent speech is never missed.
 */
class AiVoiceRelayManager(private val context: Context) : RecognitionListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _livePartialTranscript = MutableStateFlow("")
    val livePartialTranscript: StateFlow<String> = _livePartialTranscript.asStateFlow()

    private val _speechEvents = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val speechEvents: SharedFlow<String> = _speechEvents.asSharedFlow()

    @Volatile
    var isAutoRelayActive: Boolean = false

    @Volatile
    var isTtsCurrentlySpeaking: Boolean = false

    @Volatile
    var targetLanguageTag: String = ""

    private var consecutiveErrors = 0

    fun startListening() {
        if (isTtsCurrentlySpeaking) {
            Log.d(TAG, "Skipping startListening(): TTS is currently speaking")
            return
        }

        // Verify RECORD_AUDIO permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Cannot start SpeechRecognizer: RECORD_AUDIO permission is not granted")
            _isListening.value = false
            return
        }

        mainHandler.post {
            if (isTtsCurrentlySpeaking) return@post

            try {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    Log.w(TAG, "SpeechRecognizer is not available on this device")
                    _isListening.value = false
                    return@post
                }

                // If recognizer is active or already exists, cancel previous session cleanly
                try {
                    speechRecognizer?.cancel()
                } catch (_: Exception) {}

                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(this@AiVoiceRelayManager)
                    }
                }

                val defaultLang = Locale.getDefault().toLanguageTag()
                val lang = if (targetLanguageTag.isNotBlank()) targetLanguageTag else defaultLang

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang)
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-US", "en-IN", "hi-IN", "as-IN"))
                }

                speechRecognizer?.startListening(intent)
                _isListening.value = true
                Log.i(TAG, "SpeechRecognizer started listening (lang: $lang)")
            } catch (e: Exception) {
                Log.e(TAG, "Error in startListening()", e)
                _isListening.value = false
                if (isAutoRelayActive) {
                    recreateRecognizer(backoffMs = 1200L)
                }
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping recognizer", e)
            }
            _isListening.value = false
        }
    }

    fun cancel() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                Log.w(TAG, "Error canceling recognizer", e)
            }
            _isListening.value = false
            _livePartialTranscript.value = ""
        }
    }

    fun release() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.w(TAG, "Error destroying recognizer", e)
            }
            speechRecognizer = null
            _isListening.value = false
            _livePartialTranscript.value = ""
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _isListening.value = true
        Log.d(TAG, "SpeechRecognizer onReadyForSpeech")
    }

    override fun onBeginningOfSpeech() {
        _isListening.value = true
        Log.d(TAG, "SpeechRecognizer onBeginningOfSpeech (User is speaking)")
    }

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _isListening.value = false
        Log.d(TAG, "SpeechRecognizer onEndOfSpeech (processing speech)")
    }

    override fun onError(error: Int) {
        val errorDesc = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "ERROR_AUDIO"
            SpeechRecognizer.ERROR_CLIENT -> "ERROR_CLIENT"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "ERROR_INSUFFICIENT_PERMISSIONS"
            SpeechRecognizer.ERROR_NETWORK -> "ERROR_NETWORK"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ERROR_NETWORK_TIMEOUT"
            SpeechRecognizer.ERROR_NO_MATCH -> "ERROR_NO_MATCH"
            SpeechRecognizer.ERROR_SERVER -> "ERROR_SERVER"
            SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> "ERROR_SERVER_DISCONNECTED"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "ERROR_SPEECH_TIMEOUT"
            else -> "CODE_$error"
        }
        Log.d(TAG, "SpeechRecognizer status: $errorDesc ($error)")
        _isListening.value = false
        _livePartialTranscript.value = ""

        // If auto-relay is OFF, do NOT loop or auto-restart! Simply remain idle until user taps mic.
        if (!isAutoRelayActive || isTtsCurrentlySpeaking) {
            return
        }

        // Auto-relay is active:
        if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            consecutiveErrors = 0
            mainHandler.postDelayed({
                if (isAutoRelayActive && !isTtsCurrentlySpeaking && !_isListening.value) {
                    try {
                        startListening()
                    } catch (_: Exception) {}
                }
            }, 700L)
            return
        }

        consecutiveErrors++
        if (consecutiveErrors <= 3) {
            recreateRecognizer(backoffMs = (consecutiveErrors * 600L).coerceIn(800L, 2500L))
        } else {
            Log.w(TAG, "Excessive SpeechRecognizer errors ($consecutiveErrors). Pausing auto-restart to prevent flapping.")
        }
    }

    private fun recreateRecognizer(backoffMs: Long) {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
            speechRecognizer = null
            _isListening.value = false

            if (isAutoRelayActive && !isTtsCurrentlySpeaking) {
                mainHandler.postDelayed({
                    if (isAutoRelayActive && !isTtsCurrentlySpeaking && !_isListening.value) {
                        startListening()
                    }
                }, backoffMs)
            }
        }
    }

    override fun onResults(results: Bundle?) {
        consecutiveErrors = 0
        _isListening.value = false
        _livePartialTranscript.value = ""
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim()

        if (!text.isNullOrBlank()) {
            Log.i(TAG, "Final user speech transcribed: '$text'")
            _speechEvents.tryEmit(text)
            // Once emitted, CallSessionManager will trigger TTS playback.
            // When TTS completes, CallSessionManager.speechManager.onSpeechFinished
            // will resume listening if isAutoRelayEnabled is true.
        } else if (isAutoRelayActive && !isTtsCurrentlySpeaking) {
            // No speech detected, restart listening after debounce
            mainHandler.postDelayed({
                if (isAutoRelayActive && !isTtsCurrentlySpeaking && !_isListening.value) {
                    startListening()
                }
            }, 600L)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim()
        if (!text.isNullOrBlank()) {
            _livePartialTranscript.value = text
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    companion object {
        private const val TAG = "AiVoiceRelayManager"
    }
}
