package com.callscreener.app.engine

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.os.Process
import android.util.Log
import com.callscreener.app.root.RootPrivilegeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

enum class JammerMode(val displayName: String, val badge: String, val subtitle: String) {
    DAF_JAMMER(
        "Speech Jammer (DAF)",
        "STUTTER DAF",
        "200ms timing collision — induces speech motor stutter & arrest"
    ),
    INFINITE_ECHO(
        "Infinite Echo Chamber",
        "ECHO CHAMBER",
        "Multi-tap noise-free recursive acoustic reflections with warm low-pass damping"
    )
}

class SpeechJammerEngine private constructor(private val appContext: Context) {

    private val prefs = appContext.getSharedPreferences("call_screener_speech_jammer_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    companion object {
        private const val TAG = "SpeechJammerEngine"
        const val DSP_SAMPLE_RATE = 16000
        const val UPLINK_SAMPLE_RATE = 48000
        const val DEFAULT_DELAY_MS = 200
        const val DEFAULT_INTENSITY_GAIN = 1.35f
        const val DEFAULT_ECHO_DECAY = 0.68f

        @Volatile
        private var instance: SpeechJammerEngine? = null

        fun getInstance(context: Context): SpeechJammerEngine {
            return instance ?: synchronized(this) {
                instance ?: SpeechJammerEngine(context.applicationContext).also { instance = it }
            }
        }
    }

    private val _isJammerActive = MutableStateFlow(false)
    val isJammerActive: StateFlow<Boolean> = _isJammerActive.asStateFlow()

    private val _liveDisruptionLevel = MutableStateFlow(0.0f)
    val liveDisruptionLevel: StateFlow<Float> = _liveDisruptionLevel.asStateFlow()

    var isEnabled: Boolean
        get() = prefs.getBoolean("jammer_enabled", true)
        set(value) { prefs.edit().putBoolean("jammer_enabled", value).apply() }

    var mode: JammerMode
        get() {
            val saved = prefs.getString("jammer_mode", JammerMode.DAF_JAMMER.name)
            return try {
                JammerMode.valueOf(saved ?: JammerMode.DAF_JAMMER.name)
            } catch (_: Exception) {
                JammerMode.DAF_JAMMER
            }
        }
        set(value) {
            prefs.edit().putString("jammer_mode", value.name).apply()
            _activeModeFlow.value = value
            Log.i(TAG, "Jammer operational mode set to: ${value.name}")
        }

    private val _activeModeFlow = MutableStateFlow(mode)
    val activeModeFlow: StateFlow<JammerMode> = _activeModeFlow.asStateFlow()

    var echoDecay: Float
        get() = prefs.getFloat("jammer_echo_decay", DEFAULT_ECHO_DECAY)
        set(value) {
            val clamped = value.coerceIn(0.30f, 0.88f)
            prefs.edit().putFloat("jammer_echo_decay", clamped).apply()
        }

    var delayMs: Int
        get() = prefs.getInt("jammer_delay_ms", DEFAULT_DELAY_MS)
        set(value) {
            val clamped = value.coerceIn(100, 350)
            prefs.edit().putInt("jammer_delay_ms", clamped).apply()
            updateDelaySamples(clamped)
        }

    var intensityGain: Float
        get() = prefs.getFloat("jammer_intensity", DEFAULT_INTENSITY_GAIN)
        set(value) {
            val clamped = value.coerceIn(0.8f, 2.5f)
            prefs.edit().putFloat("jammer_intensity", clamped).apply()
        }

    var autoJamUnknown: Boolean
        get() = prefs.getBoolean("jammer_auto_unknown", false)
        set(value) { prefs.edit().putBoolean("jammer_auto_unknown", value).apply() }

    var isEchoUserVoiceEnabled: Boolean
        get() = prefs.getBoolean("jammer_echo_user_voice", false)
        set(value) {
            prefs.edit().putBoolean("jammer_echo_user_voice", value).apply()
            _isEchoUserVoiceFlow.value = value
            Log.i(TAG, "Speech Jammer Echo User Voice set to: $value")
        }

    private val _isEchoUserVoiceFlow = MutableStateFlow(isEchoUserVoiceEnabled)
    val isEchoUserVoiceFlow: StateFlow<Boolean> = _isEchoUserVoiceFlow.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var userMicRecord: AudioRecord? = null
    private var telephonyUplinkTrack: AudioTrack? = null
    private var mediaMixerTrack: AudioTrack? = null
    private var monitorAudioTrack: AudioTrack? = null
    private var echoCanceler: AcousticEchoCanceler? = null
    private var processingThread: Thread? = null

    @Volatile
    private var isRunning = false

    @Volatile
    private var isTestMode = false

    private var originalMediaVolume = -1
    private var originalVoiceVolume = -1

    private val ringBufferSize = 32768
    private val ringBufferMask = ringBufferSize - 1
    private val ringBuffer = FloatArray(ringBufferSize)
    private var writePos = 0

    @Volatile
    private var delaySamples = (DEFAULT_DELAY_MS * DSP_SAMPLE_RATE) / 1000

    init {
        updateDelaySamples(delayMs)
    }

    private fun updateDelaySamples(ms: Int) {
        delaySamples = (ms * DSP_SAMPLE_RATE) / 1000
        Log.i(TAG, "Speech Jammer delay calibrated: ${ms}ms ($delaySamples samples @ ${DSP_SAMPLE_RATE}Hz)")
    }

    fun setEchoUserVoice(enabled: Boolean) {
        isEchoUserVoiceEnabled = enabled
        if (isRunning && !isTestMode) {
            if (enabled && userMicRecord == null) {
                val recordBufSize = max(
                    AudioRecord.getMinBufferSize(DSP_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 4,
                    8192
                )
                userMicRecord = createUserMicAudioRecord(recordBufSize)
                Log.i(TAG, "Dynamically started User Mic capture for Speech Jammer")
            } else if (!enabled && userMicRecord != null) {
                try {
                    userMicRecord?.stop()
                    userMicRecord?.release()
                } catch (_: Exception) {}
                userMicRecord = null
                Log.i(TAG, "Dynamically stopped User Mic capture for Speech Jammer")
            }
        }
    }

    @Synchronized
    fun startJammer(forTestLab: Boolean = false): Boolean {
        if (isRunning) {
            Log.w(TAG, "Speech Jammer is already active")
            return true
        }

        isTestMode = forTestLab
        Log.i(TAG, "Starting Speech Jammer (TestMode=$forTestLab, Delay=${delayMs}ms, Intensity=${intensityGain}x, Mode=${mode.name})")

        val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        if (!forTestLab) {
            unmaskInCallHardwareMixersSync()
            try {
                originalMediaVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: -1
                val maxMediaVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, maxMediaVol, 0)
                Log.i(TAG, "Boosted STREAM_MUSIC volume to $maxMediaVol for in-call injection")
            } catch (e: Exception) {
                Log.w(TAG, "Could not set STREAM_MUSIC volume: ${e.message}")
            }
            try {
                originalVoiceVolume = audioManager?.getStreamVolume(AudioManager.STREAM_VOICE_CALL) ?: -1
                val maxVoiceVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) ?: 7
                audioManager?.setStreamVolume(AudioManager.STREAM_VOICE_CALL, maxVoiceVol, 0)
                Log.i(TAG, "Boosted STREAM_VOICE_CALL volume to $maxVoiceVol for in-call feedback")
            } catch (_: Exception) {}
        }

        val recordBufSize = max(
            AudioRecord.getMinBufferSize(DSP_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 4,
            8192
        )

        val record = createAudioRecord(recordBufSize)
        if (record == null) {
            Log.e(TAG, "Failed to initialize AudioRecord for Speech Jammer")
            return false
        }
        audioRecord = record

        if (!forTestLab && isEchoUserVoiceEnabled) {
            userMicRecord = createUserMicAudioRecord(recordBufSize)
            Log.i(TAG, "Initialized User Mic AudioRecord for two-way echo chamber (userMicRecord != null: ${userMicRecord != null})")
        } else {
            userMicRecord = null
        }

        if (AcousticEchoCanceler.isAvailable()) {
            try {
                echoCanceler = AcousticEchoCanceler.create(record.audioSessionId)?.apply {
                    enabled = false
                    Log.i(TAG, "AcousticEchoCanceler explicitly disabled for Speech Jammer")
                }
            } catch (e: Exception) {
                Log.w(TAG, "AcousticEchoCanceler disable notice: ${e.message}")
            }
        }

        val outChannelConfig = AudioFormat.CHANNEL_OUT_MONO
        val outEncoding = AudioFormat.ENCODING_PCM_16BIT
        val minTrackBuf = AudioTrack.getMinBufferSize(DSP_SAMPLE_RATE, outChannelConfig, outEncoding)
        val trackBufSize = max(minTrackBuf * 4, 16384)

        val audioFormat = AudioFormat.Builder()
            .setSampleRate(DSP_SAMPLE_RATE)
            .setChannelMask(outChannelConfig)
            .setEncoding(outEncoding)
            .build()

        if (forTestLab) {
            val testAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val mTrack = try {
                AudioTrack.Builder()
                    .setAudioAttributes(testAttrs)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(trackBufSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to build test lab monitor AudioTrack", e)
                null
            }

            if (mTrack != null && mTrack.state == AudioTrack.STATE_INITIALIZED) {
                try { mTrack.setVolume(1.0f) } catch (_: Exception) {}
                monitorAudioTrack = mTrack
                Log.i(TAG, "Test Lab monitor AudioTrack initialized @ 16kHz")
            }
            telephonyUplinkTrack = null
            mediaMixerTrack = null
        } else {
            val outputs = try {
                audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS) ?: emptyArray()
            } catch (_: Exception) {
                emptyArray<AudioDeviceInfo>()
            }
            val telephonyDevice = outputs.firstOrNull { it.type == AudioDeviceInfo.TYPE_TELEPHONY }

            val telephonyAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build()

            val tTrack = try {
                AudioTrack.Builder()
                    .setAudioAttributes(telephonyAttrs)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(trackBufSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to build telephony uplink AudioTrack", e)
                null
            }

            if (tTrack != null && tTrack.state == AudioTrack.STATE_INITIALIZED) {
                if (telephonyDevice != null) {
                    try {
                        val routed = tTrack.setPreferredDevice(telephonyDevice)
                        Log.i(TAG, "Speech Jammer telephony track routed to TYPE_TELEPHONY: $routed")
                    } catch (e: Exception) {
                        Log.w(TAG, "setPreferredDevice(TYPE_TELEPHONY) error: ${e.message}")
                    }
                } else {
                    Log.i(TAG, "telephonyDevice not found; operating on USAGE_VOICE_COMMUNICATION")
                }
                try { tTrack.setVolume(AudioTrack.getMaxVolume()) } catch (_: Exception) {}
                telephonyUplinkTrack = tTrack
            }

            val mediaAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build()

            val mmTrack = try {
                AudioTrack.Builder()
                    .setAudioAttributes(mediaAttrs)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(trackBufSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to build media mixer AudioTrack", e)
                null
            }

            if (mmTrack != null && mmTrack.state == AudioTrack.STATE_INITIALIZED) {
                try { mmTrack.setVolume(AudioTrack.getMaxVolume()) } catch (_: Exception) {}
                mediaMixerTrack = mmTrack
                Log.i(TAG, "Speech Jammer media mixer track initialized @ 16kHz for ALSA Incall_Music")
            }

            val commAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val monTrack = try {
                AudioTrack.Builder()
                    .setAudioAttributes(commAttrs)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(trackBufSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to build in-call monitor AudioTrack", e)
                null
            }

            if (monTrack != null && monTrack.state == AudioTrack.STATE_INITIALIZED) {
                try { monTrack.setVolume(0.92f) } catch (_: Exception) {}
                monitorAudioTrack = monTrack
                Log.i(TAG, "Speech Jammer in-call monitor track initialized @ 16kHz for local earpiece/speaker")
            }
        }

        if (telephonyUplinkTrack == null && mediaMixerTrack == null && monitorAudioTrack == null) {
            Log.e(TAG, "All AudioTracks failed to initialize")
            record.release()
            return false
        }

        synchronized(ringBuffer) {
            ringBuffer.fill(0.0f)
            writePos = 0
        }

        val preRoll = ShortArray(512)
        try {
            telephonyUplinkTrack?.apply {
                write(preRoll, 0, preRoll.size, AudioTrack.WRITE_NON_BLOCKING)
                play()
            }
            mediaMixerTrack?.apply {
                write(preRoll, 0, preRoll.size, AudioTrack.WRITE_NON_BLOCKING)
                play()
            }
            monitorAudioTrack?.apply {
                write(preRoll, 0, preRoll.size, AudioTrack.WRITE_NON_BLOCKING)
                play()
            }

            if (record.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                record.startRecording()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting AudioRecord/AudioTrack", e)
            stopJammer()
            return false
        }

        isRunning = true
        _isJammerActive.value = true

        processingThread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            runDafDspLoop(record)
        }, "SpeechJammerDspThread").apply {
            start()
        }

        Log.i(TAG, "Speech Jammer running with active multi-destination real-time DSP pipeline")
        return true
    }

    @Synchronized
    fun stopJammer() {
        if (!isRunning) return
        Log.i(TAG, "Stopping Speech Jammer")

        isRunning = false
        _isJammerActive.value = false
        _liveDisruptionLevel.value = 0.0f

        try {
            processingThread?.interrupt()
            processingThread = null
        } catch (_: Exception) {}

        try {
            audioRecord?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        audioRecord = null

        try {
            userMicRecord?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        userMicRecord = null

        try {
            telephonyUplinkTrack?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        telephonyUplinkTrack = null

        try {
            mediaMixerTrack?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        mediaMixerTrack = null

        try {
            monitorAudioTrack?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        monitorAudioTrack = null

        try {
            echoCanceler?.release()
        } catch (_: Exception) {}
        echoCanceler = null

        if (originalMediaVolume >= 0) {
            try {
                val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, originalMediaVolume, 0)
            } catch (_: Exception) {}
            originalMediaVolume = -1
        }

        if (originalVoiceVolume >= 0) {
            try {
                val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.setStreamVolume(AudioManager.STREAM_VOICE_CALL, originalVoiceVolume, 0)
            } catch (_: Exception) {}
            originalVoiceVolume = -1
        }

        if (!isTestMode) {
            restoreInCallHardwareMixers()
        }
    }

    private fun runDafDspLoop(record: AudioRecord) {
        val chunkSize = 256
        val inShorts = ShortArray(chunkSize)
        val micShorts = ShortArray(chunkSize)
        val outShorts = ShortArray(chunkSize)
        val inFloats = FloatArray(chunkSize)
        val outFloats = FloatArray(chunkSize)

        var hpfPrevIn = 0.0f
        var hpfPrevOut = 0.0f
        val hpfAlpha = 0.968f

        var lpfPrevOut = 0.0f
        val lpfAlpha = 0.62f

        var prevEchoDamped = 0.0f

        var rollingSpeechPeak = 0.25f
        var smoothLevel = 0.0f

        while (isRunning && !Thread.currentThread().isInterrupted) {
            val readCount = record.read(inShorts, 0, chunkSize)
            if (readCount <= 0) {
                if (readCount < 0) {
                    try { Thread.sleep(10) } catch (_: InterruptedException) { break }
                }
                continue
            }

            val micRec = userMicRecord
            val micReadCount = if (isEchoUserVoiceEnabled && micRec != null && micRec.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                try {
                    micRec.read(micShorts, 0, readCount, AudioRecord.READ_NON_BLOCKING)
                } catch (_: Exception) { 0 }
            } else 0

            val currentMode = mode
            val gain = intensityGain
            val currentDelay = delaySamples
            val decay = echoDecay

            var chunkEnergySum = 0.0f
            var chunkMaxAbs = 0.0f

            for (i in 0 until readCount) {
                val callerSample = inShorts[i].toFloat() / 32768.0f
                val micSample = if (i < micReadCount) (micShorts[i].toFloat() / 32768.0f) * 1.35f else 0.0f
                val combined = (callerSample + micSample).coerceIn(-1.0f, 1.0f)

                val hpfOut = hpfAlpha * (hpfPrevOut + combined - hpfPrevIn)
                hpfPrevIn = combined
                hpfPrevOut = hpfOut

                val lpfOut = lpfPrevOut + lpfAlpha * (hpfOut - lpfPrevOut)
                lpfPrevOut = lpfOut

                inFloats[i] = lpfOut
                val absVal = abs(lpfOut)
                chunkEnergySum += lpfOut * lpfOut
                if (absVal > chunkMaxAbs) chunkMaxAbs = absVal
            }

            val chunkRms = sqrt(chunkEnergySum / readCount)

            if (chunkMaxAbs > 0.015f) {
                rollingSpeechPeak = rollingSpeechPeak * 0.90f + chunkMaxAbs * 0.10f
                rollingSpeechPeak = rollingSpeechPeak.coerceIn(0.10f, 0.90f)
            } else {
                rollingSpeechPeak = max(rollingSpeechPeak * 0.999f, 0.12f)
            }

            val agcGain = (0.78f / rollingSpeechPeak).coerceIn(1.0f, 2.5f)

            var outEnergySum = 0.0f

            for (i in 0 until readCount) {
                val sampleIn = inFloats[i]

                if (currentMode == JammerMode.INFINITE_ECHO) {
                    val tap1Delay = currentDelay.coerceIn(800, 4800)
                    val tap2Delay = (currentDelay * 1.65f).toInt().coerceIn(1300, 8000)
                    val tap3Delay = (currentDelay * 2.35f).toInt().coerceIn(1850, 11500)

                    val s1 = ringBuffer[(writePos - tap1Delay) and ringBufferMask]
                    val s2 = ringBuffer[(writePos - tap2Delay) and ringBufferMask]
                    val s3 = ringBuffer[(writePos - tap3Delay) and ringBufferMask]

                    val rawEcho = (s1 * 0.75f + s2 * 0.50f + s3 * 0.35f) * decay

                    prevEchoDamped = 0.80f * rawEcho + 0.20f * prevEchoDamped

                    val feedbackCoeff = (decay * 0.80f).coerceIn(0.35f, 0.85f)
                    ringBuffer[writePos] = (sampleIn + prevEchoDamped * feedbackCoeff).coerceIn(-1.0f, 1.0f)
                    writePos = (writePos + 1) and ringBufferMask

                    val outEcho = prevEchoDamped * (gain * 1.85f)
                    val clampedOut = softKneeLimit(outEcho)

                    outFloats[i] = clampedOut
                    outShorts[i] = (clampedOut * 32767.0f).toInt().coerceIn(-32768, 32767).toShort()
                    outEnergySum += clampedOut * clampedOut

                } else {
                    ringBuffer[writePos] = sampleIn

                    val readPos = (writePos - currentDelay) and ringBufferMask
                    val delayedSample = ringBuffer[readPos]
                    writePos = (writePos + 1) and ringBufferMask

                    val amplified = delayedSample * agcGain * (gain * 1.85f)
                    val clampedOut = softKneeLimit(amplified)

                    outFloats[i] = clampedOut
                    outShorts[i] = (clampedOut * 32767.0f).toInt().coerceIn(-32768, 32767).toShort()
                    outEnergySum += clampedOut * clampedOut
                }
            }

            val chunkOutRms = sqrt(outEnergySum / readCount)

            try {
                telephonyUplinkTrack?.write(outShorts, 0, readCount, AudioTrack.WRITE_NON_BLOCKING)
            } catch (_: Exception) {}

            try {
                mediaMixerTrack?.write(outShorts, 0, readCount, AudioTrack.WRITE_NON_BLOCKING)
            } catch (_: Exception) {}

            try {
                monitorAudioTrack?.write(outShorts, 0, readCount, AudioTrack.WRITE_NON_BLOCKING)
            } catch (_: Exception) {}

            val liveLevel = max(chunkRms, chunkOutRms)
            smoothLevel = smoothLevel * 0.70f + liveLevel * 0.30f
            _liveDisruptionLevel.value = smoothLevel.coerceIn(0.0f, 1.0f)
        }
    }

    private fun softKneeLimit(x: Float): Float {
        val absX = abs(x)
        val knee = 0.65f
        val limit = 0.95f
        if (absX <= knee) return x
        val excess = absX - knee
        val compressed = knee + (excess * (limit - knee)) / (excess + (limit - knee))
        return if (x >= 0.0f) compressed.coerceAtMost(limit) else -compressed.coerceAtMost(limit)
    }

    private fun createAudioRecord(bufferSize: Int): AudioRecord? {
        val channelIn = AudioFormat.CHANNEL_IN_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT

        val candidateSources = if (isTestMode) {
            intArrayOf(
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                MediaRecorder.AudioSource.DEFAULT
            )
        } else {
            intArrayOf(
                MediaRecorder.AudioSource.VOICE_CALL,
                MediaRecorder.AudioSource.VOICE_DOWNLINK,
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.AudioSource.DEFAULT
            )
        }

        for (src in candidateSources) {
            try {
                @SuppressLint("MissingPermission")
                val record = AudioRecord(
                    src,
                    DSP_SAMPLE_RATE,
                    channelIn,
                    encoding,
                    bufferSize
                )
                if (record.state == AudioRecord.STATE_INITIALIZED) {
                    try {
                        if (record.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                            record.startRecording()
                        }
                        val testBuf = ShortArray(64)
                        var testRead = -1
                        for (attempt in 0..3) {
                            testRead = record.read(testBuf, 0, testBuf.size)
                            if (testRead > 0) break
                            try { Thread.sleep(25) } catch (_: Exception) {}
                        }
                        if (testRead >= 0) {
                            Log.i(TAG, "AudioRecord successfully verified & initialized with source=$src (readCount=$testRead)")
                            return record
                        } else {
                            Log.w(TAG, "AudioRecord source=$src test read returned error $testRead")
                            try { record.stop() } catch (_: Exception) {}
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "AudioRecord source=$src start test failed: ${e.message}")
                    }
                }
                record.release()
            } catch (e: Exception) {
                Log.w(TAG, "AudioRecord source=$src failed initialization: ${e.message}")
            }
        }

        Log.e(TAG, "Could not initialize any AudioRecord source for Speech Jammer")
        return null
    }

    private fun createUserMicAudioRecord(bufferSize: Int): AudioRecord? {
        val channelIn = AudioFormat.CHANNEL_IN_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT
        val micSources = intArrayOf(
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.DEFAULT
        )

        for (src in micSources) {
            try {
                @SuppressLint("MissingPermission")
                val record = AudioRecord(
                    src,
                    DSP_SAMPLE_RATE,
                    channelIn,
                    encoding,
                    bufferSize
                )
                if (record.state == AudioRecord.STATE_INITIALIZED) {
                    try {
                        if (record.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                            record.startRecording()
                        }
                        Log.i(TAG, "User Mic AudioRecord initialized & started with source=$src")
                        return record
                    } catch (e: Exception) {
                        Log.w(TAG, "User Mic AudioRecord start failed on source=$src: ${e.message}")
                    }
                }
                record.release()
            } catch (e: Exception) {
                Log.w(TAG, "User Mic AudioRecord source=$src failed: ${e.message}")
            }
        }
        Log.w(TAG, "Could not initialize User Mic AudioRecord on any candidate source")
        return null
    }

    private fun unmaskInCallHardwareMixersSync() {
        try {
            val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.mode = AudioManager.MODE_IN_CALL
            audioManager?.setParameters("incall_music_enable=true")
            audioManager?.setParameters("incall_music_enable=1")
            audioManager?.setParameters("audio_capability=call_recording=1")
            audioManager?.setParameters("voice_tx_mute=0")
            audioManager?.setParameters("voice_tx_mute=false")
            audioManager?.setParameters("voice_rx_mute=0")
            audioManager?.setParameters("voice_rx_mute=false")
        } catch (_: Exception) {}

        if (!RootPrivilegeManager.isRootAvailable()) return
        try {
            runBlocking(Dispatchers.IO) {
                RootPrivilegeManager.executeSuCommand("""
                    # 1. System permissions via root
                    pm grant com.resonix.app android.permission.CAPTURE_AUDIO_OUTPUT 2>/dev/null || true
                    pm grant com.resonix.app android.permission.MODIFY_AUDIO_ROUTING 2>/dev/null || true
                    appops set com.resonix.app RECORD_AUDIO allow 2>/dev/null || true
                    appops set com.resonix.app CAPTURE_AUDIO_OUTPUT allow 2>/dev/null || true
                    pm grant com.callscreener.app android.permission.CAPTURE_AUDIO_OUTPUT 2>/dev/null || true
                    pm grant com.callscreener.app android.permission.MODIFY_AUDIO_ROUTING 2>/dev/null || true
                    appops set com.callscreener.app RECORD_AUDIO allow 2>/dev/null || true
                    appops set com.callscreener.app CAPTURE_AUDIO_OUTPUT allow 2>/dev/null || true

                    # 2. Unmute cellular modem uplink completely across 4G/5G/VoLTE/VoWiFi
                    cmd telecom set-call-muted false 2>/dev/null || true
                    tinymix 'Voice Tx Mute' 0 2>/dev/null || true
                    tinymix 'Voice Tx Device Mute' 0 2>/dev/null || true
                    tinymix 'Voice Tx Volume' 100 2>/dev/null || true
                    tinymix 'Voip Tx Mute' 0 2>/dev/null || true
                    tinymix 'VoiceMMode1 Tx Mute' 0 2>/dev/null || true
                    tinymix 'VoiceMMode2 Tx Mute' 0 2>/dev/null || true
                    tinymix 'VoLTE Tx Mute' 0 2>/dev/null || true
                    tinymix 'VoWIFI Tx Mute' 0 2>/dev/null || true

                    # 3. Ensure physical mic is connected to Voice Tx (reversing any prior AI relay isolation)
                    tinymix 'Voice_Tx Mixer TX_Voice' 1 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer SLIM_0_TX_Voice' 1 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer PRI_TX_Voice' 1 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer TERT_MI2S_TX_Voice' 1 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer TX_CDC_DMA_TX_3' 1 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer SLIM_0_TX' 1 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer AFE_PCM_TX' 1 2>/dev/null || true

                    # 4. Route Qualcomm audio streams into modem uplink (Voice_Tx, VoLTE, VoWIFI, VoiceMMode1/2)
                    tinymix 'Incall_Music Audio Mixer MultiMedia1' 1 2>/dev/null || true
                    tinymix 'Incall_Music Audio Mixer MultiMedia2' 1 2>/dev/null || true
                    tinymix 'Incall_Music_2 Audio Mixer MultiMedia1' 1 2>/dev/null || true
                    tinymix 'Incall_Music_2 Audio Mixer MultiMedia2' 1 2>/dev/null || true
                    tinymix 'Incall_Music Audio Mixer MultiMedia1 Volume' 100 2>/dev/null || true
                    tinymix 'Incall_Music Audio Mixer MultiMedia2 Volume' 100 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer MultiMedia1' 1 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer MultiMedia2' 1 2>/dev/null || true
                    tinymix 'VoiceMMode1_Tx Mixer MultiMedia1' 1 2>/dev/null || true
                    tinymix 'VoiceMMode1_Tx Mixer MultiMedia2' 1 2>/dev/null || true
                    tinymix 'VoiceMMode2_Tx Mixer MultiMedia1' 1 2>/dev/null || true
                    tinymix 'VoiceMMode2_Tx Mixer MultiMedia2' 1 2>/dev/null || true
                    tinymix 'VoLTE_Tx Mixer MultiMedia1' 1 2>/dev/null || true
                    tinymix 'VoLTE_Tx Mixer MultiMedia2' 1 2>/dev/null || true
                    tinymix 'VoWIFI_Tx Mixer MultiMedia1' 1 2>/dev/null || true
                    tinymix 'VoWIFI_Tx Mixer MultiMedia2' 1 2>/dev/null || true

                    # 5. MediaTek Dimensity in-call playback routing
                    tinymix 'Audio_Incall_Playback' 1 2>/dev/null || true
                    tinymix 'Incall_Playback' 1 2>/dev/null || true
                    tinymix 'Speech_MD_Tx' 1 2>/dev/null || true
                    tinymix 'Speech_Tx' 1 2>/dev/null || true
                    tinymix 'Audio_Incall_Playback_DL' 1 2>/dev/null || true

                    # 6. Google Tensor / Samsung Exynos modem in-call uplink
                    tinymix 'Voice Call Uplink' 1 2>/dev/null || true
                    tinymix 'Voice Tx' 1 2>/dev/null || true
                    tinymix 'Uplink Mute' 0 2>/dev/null || true
                    tinymix 'Playback Incall Music' 1 2>/dev/null || true
                    tinymix 'Incall Playback' 1 2>/dev/null || true
                    tinymix 'Incall_Music' 1 2>/dev/null || true
                    tinymix 'Media_Tx' 1 2>/dev/null || true
                """.trimIndent())
            }
            Log.i(TAG, "Unmasked modem uplink ALSA mixers synchronously for Speech Jammer")
        } catch (e: Exception) {
            Log.w(TAG, "Root hardware mixer unmask notice: ${e.message}")
        }
    }

    private fun restoreInCallHardwareMixers() {
        if (!RootPrivilegeManager.isRootAvailable()) return
        scope.launch(Dispatchers.IO) {
            try {
                RootPrivilegeManager.executeSuCommand("""
                    tinymix 'Voice_Tx Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer MultiMedia2' 0 2>/dev/null || true
                    tinymix 'VoiceMMode1_Tx Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'VoiceMMode1_Tx Mixer MultiMedia2' 0 2>/dev/null || true
                    tinymix 'VoiceMMode2_Tx Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'VoiceMMode2_Tx Mixer MultiMedia2' 0 2>/dev/null || true
                    tinymix 'VoLTE_Tx Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'VoLTE_Tx Mixer MultiMedia2' 0 2>/dev/null || true
                    tinymix 'VoWIFI_Tx Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'VoWIFI_Tx Mixer MultiMedia2' 0 2>/dev/null || true
                    tinymix 'Incall_Music Audio Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'Incall_Music Audio Mixer MultiMedia2' 0 2>/dev/null || true
                    tinymix 'Incall_Music_2 Audio Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'Audio_Incall_Playback' 0 2>/dev/null || true
                    tinymix 'Incall_Playback' 0 2>/dev/null || true
                    tinymix 'Audio_Incall_Playback_DL' 0 2>/dev/null || true
                    tinymix 'Voice Call Uplink' 0 2>/dev/null || true
                    tinymix 'Playback Incall Music' 0 2>/dev/null || true
                    tinymix 'Media_Tx' 0 2>/dev/null || true
                """.trimIndent())
                Log.i(TAG, "Restored normal in-call ALSA mixers")
            } catch (_: Exception) {}
        }
    }
}
