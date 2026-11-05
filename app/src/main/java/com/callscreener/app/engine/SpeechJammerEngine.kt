package com.callscreener.app.engine

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tanh

/**
 * Acoustic Speech Jammer Engine using Delayed Auditory Feedback (DAF).
 *
 * Disrupts the caller's brain speech-motor synchronization by capturing their incoming
 * vocal stream and feeding it back into the call uplink with a precise 180ms - 220ms delay.
 *
 * Neurological basis:
 * Human speech production requires bone/auditory feedback within <20ms.
 * When delayed by ~200ms, the brain's auditory cortex detects an artificial timing collision,
 * inducing involuntary stuttering, syllable repetition, severe slurring, or total speech arrest.
 */
class SpeechJammerEngine private constructor(private val appContext: Context) {

    private val prefs = appContext.getSharedPreferences("call_screener_speech_jammer_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    companion object {
        private const val TAG = "SpeechJammerEngine"
        const val SAMPLE_RATE = 16000
        const val DEFAULT_DELAY_MS = 200
        const val DEFAULT_INTENSITY_GAIN = 1.6f

        @Volatile
        private var instance: SpeechJammerEngine? = null

        fun getInstance(context: Context): SpeechJammerEngine {
            return instance ?: synchronized(this) {
                instance ?: SpeechJammerEngine(context.applicationContext).also { instance = it }
            }
        }
    }

    // Active state
    private val _isJammerActive = MutableStateFlow(false)
    val isJammerActive: StateFlow<Boolean> = _isJammerActive.asStateFlow()

    // Real-time audio disruption level (0.0f to 1.0f) for live UI pulsating visualizers
    private val _liveDisruptionLevel = MutableStateFlow(0.0f)
    val liveDisruptionLevel: StateFlow<Float> = _liveDisruptionLevel.asStateFlow()

    // Configurable Settings
    var isEnabled: Boolean
        get() = prefs.getBoolean("jammer_enabled", true)
        set(value) { prefs.edit().putBoolean("jammer_enabled", value).apply() }

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
            val clamped = value.coerceIn(1.0f, 2.8f)
            prefs.edit().putFloat("jammer_intensity", clamped).apply()
        }

    var autoJamUnknown: Boolean
        get() = prefs.getBoolean("jammer_auto_unknown", false)
        set(value) { prefs.edit().putBoolean("jammer_auto_unknown", value).apply() }

    // Audio Pipeline State
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var echoCanceler: AcousticEchoCanceler? = null
    private var gainControl: AutomaticGainControl? = null
    private var processingThread: Thread? = null

    @Volatile
    private var isRunning = false

    @Volatile
    private var isTestMode = false

    // DAF Circular Buffer: 32,768 samples @ 16kHz = ~2.04 seconds capacity
    private val ringBufferSize = 32768
    private val ringBufferMask = ringBufferSize - 1
    private val ringBuffer = FloatArray(ringBufferSize)
    private var writePos = 0

    @Volatile
    private var delaySamples = (DEFAULT_DELAY_MS * SAMPLE_RATE) / 1000

    init {
        updateDelaySamples(delayMs)
    }

    private fun updateDelaySamples(ms: Int) {
        delaySamples = (ms * SAMPLE_RATE) / 1000
        Log.i(TAG, "Speech Jammer delay calibrated: ${ms}ms ($delaySamples samples @ ${SAMPLE_RATE}Hz)")
    }

    /**
     * Start the real-time Speech Jammer during a live phone call or test lab.
     *
     * @param forTestLab If true, captures local mic for testing in headphones;
     *                   If false, captures telephony downlink (caller's voice) via root mixer.
     */
    @Synchronized
    fun startJammer(forTestLab: Boolean = false): Boolean {
        if (isRunning) {
            Log.w(TAG, "Speech Jammer is already active")
            return true
        }

        isTestMode = forTestLab
        Log.i(TAG, "Starting Speech Jammer (TestMode=$forTestLab, Delay=${delayMs}ms, Intensity=${intensityGain}x)")

        // 1. Unmask hardware in-call mixers via root if on active call
        if (!forTestLab) {
            unmaskInCallHardwareMixers()
        }

        // 2. Initialize AudioRecord
        val channelIn = AudioFormat.CHANNEL_IN_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT
        val minRecordBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, channelIn, encoding)
        val recordBufSize = max(minRecordBuf * 4, 4096)

        val recordSource = if (forTestLab) {
            MediaRecorder.AudioSource.VOICE_COMMUNICATION
        } else {
            MediaRecorder.AudioSource.VOICE_CALL // Dual-side / Downlink
        }

        val record = tryCreateAudioRecord(recordSource, recordBufSize)
        if (record == null) {
            Log.e(TAG, "Failed to initialize AudioRecord for Speech Jammer")
            return false
        }
        audioRecord = record

        // Hardware Echo Canceler on device
        try {
            if (AcousticEchoCanceler.isAvailable()) {
                echoCanceler = AcousticEchoCanceler.create(record.audioSessionId)?.apply {
                    enabled = true
                }
            }
        } catch (_: Exception) {}

        // 3. Initialize AudioTrack for real-time delayed injection
        val channelOut = AudioFormat.CHANNEL_OUT_MONO
        val minTrackBuf = AudioTrack.getMinBufferSize(SAMPLE_RATE, channelOut, encoding)
        val trackBufSize = max(minTrackBuf * 4, 4096)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(channelOut)
            .setEncoding(encoding)
            .build()

        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(trackBufSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to build AudioTrack", e)
            record.release()
            return false
        }

        if (track.state != AudioTrack.STATE_INITIALIZED) {
            Log.e(TAG, "AudioTrack not initialized")
            record.release()
            track.release()
            return false
        }
        audioTrack = track

        try {
            track.setVolume(AudioTrack.getMaxVolume())
        } catch (_: Exception) {}

        // Reset buffer
        synchronized(ringBuffer) {
            ringBuffer.fill(0.0f)
            writePos = 0
        }

        // Start I/O
        try {
            record.startRecording()
            track.play()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting AudioRecord/AudioTrack", e)
            stopJammer()
            return false
        }

        isRunning = true
        _isJammerActive.value = true

        // Launch real-time DSP worker thread with realtime audio priority
        processingThread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            runDafDspLoop(record, track)
        }, "SpeechJammerDspThread").apply {
            start()
        }

        Log.i(TAG, "Speech Jammer running with real-time DSP thread")
        return true
    }

    /**
     * Stop the Speech Jammer and restore normal call audio.
     */
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
            audioTrack?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        audioTrack = null

        try {
            echoCanceler?.release()
        } catch (_: Exception) {}
        echoCanceler = null
    }

    /**
     * Real-time DSP audio loop: reads caller audio, buffers with DAF delay,
     * amplifies formants, and writes directly into uplink.
     */
    private fun runDafDspLoop(record: AudioRecord, track: AudioTrack) {
        val chunkSize = 256 // 16ms chunk size @ 16kHz
        val inShorts = ShortArray(chunkSize)
        val outShorts = ShortArray(chunkSize)

        var smoothLevel = 0.0f

        while (isRunning && !Thread.currentThread().isInterrupted) {
            val readCount = record.read(inShorts, 0, chunkSize)
            if (readCount <= 0) continue

            val gain = intensityGain
            val currentDelay = delaySamples
            var chunkEnergy = 0.0f

            for (i in 0 until readCount) {
                val inputSample = inShorts[i].toFloat() / 32768.0f
                val absVal = abs(inputSample)
                if (absVal > chunkEnergy) chunkEnergy = absVal

                // 1. Write current input into circular delay line
                ringBuffer[writePos] = inputSample

                // 2. Read delayed sample from DAF delay tap
                val readPos = (writePos - currentDelay) and ringBufferMask
                val delayedSample = ringBuffer[readPos]

                // Advance write head
                writePos = (writePos + 1) and ringBufferMask

                // 3. Nonlinear Soft-Limiter / Acoustic Amplification (tanh curve)
                // Boost caller's voice so it penetrates bone conduction without harsh digital clipping
                val amplified = delayedSample * gain
                val compressed = tanh(amplified.toDouble()).toFloat()

                outShorts[i] = (compressed * 32767.0f).toInt().coerceIn(-32768, 32767).toShort()
            }

            // Write delayed audio to output stream
            track.write(outShorts, 0, readCount)

            // Update live UI disruption level
            smoothLevel = smoothLevel * 0.75f + chunkEnergy * 0.25f
            _liveDisruptionLevel.value = smoothLevel.coerceIn(0.0f, 1.0f)
        }
    }

    private fun tryCreateAudioRecord(preferredSource: Int, bufferSize: Int): AudioRecord? {
        val sources = intArrayOf(
            preferredSource,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.VOICE_CALL,
            MediaRecorder.AudioSource.MIC
        )

        for (src in sources.distinct()) {
            try {
                @SuppressLint("MissingPermission")
                val record = AudioRecord(
                    src,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
                if (record.state == AudioRecord.STATE_INITIALIZED) {
                    Log.i(TAG, "AudioRecord initialized using source=$src")
                    return record
                }
                record.release()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to initialize source=$src: ${e.message}")
            }
        }
        return null
    }

    private fun unmaskInCallHardwareMixers() {
        if (!RootPrivilegeManager.isRootAvailable()) return
        scope.launch {
            try {
                val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.setParameters("incall_music_enable=true")
                audioManager?.setParameters("audio_capability=call_recording=1")

                RootPrivilegeManager.executeSuCommand("""
                    tinymix 'Incall_Music Audio Mixer MultiMedia1' 1 2>/dev/null || true
                    tinymix 'Voice_Rx Audio Mixer MultiMedia1' 1 2>/dev/null || true
                    tinymix 'Voice Rx Mute' 0 2>/dev/null || true
                    tinymix 'Voice Tx Mute' 0 2>/dev/null || true
                """.trimIndent())
            } catch (e: Exception) {
                Log.w(TAG, "Root hardware mixer unmask notice: ${e.message}")
            }
        }
    }
}

