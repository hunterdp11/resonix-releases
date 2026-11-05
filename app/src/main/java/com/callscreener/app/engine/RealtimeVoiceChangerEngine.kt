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
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.os.Process
import android.util.Log
import com.callscreener.app.model.VoiceEffectType
import com.callscreener.app.model.VoiceGender
import com.callscreener.app.model.VoicePersona
import com.callscreener.app.root.RootPrivilegeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Ultra-Low-Latency Realtime DSP Voice Changer Engine
 *
 * Implements a synchronized dual-tap circular delay line pitch shifter
 * with fractional linear interpolation and complementary Hann windowing.
 *
 * Mathematical guarantee:
 * - Sum of Hann window weights (w1 + w2) = 1.000 identically at all samples (unity gain, zero tremolo).
 * - Tap wrap discontinuities occur strictly when window weight = 0 (zero clicks/pops).
 * - Maximum delay D = 480 samples @ 16kHz (30ms). Average latency = 15ms.
 * - Gentle low-pass formant smoothing filter removes shrill chipmunk artifacts,
 *   producing a rich, natural feminine vocal resonance.
 */
class RealtimeVoiceChangerEngine {

    private val sampleRate = 16000
    private val channelConfigIn = AudioFormat.CHANNEL_IN_MONO
    private val channelConfigOut = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var echoCanceler: AcousticEchoCanceler? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var gainControl: AutomaticGainControl? = null
    private var processingThread: Thread? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    @Volatile
    private var isRunning = false

    @Volatile
    private var isTestModeActive = false

    @Volatile
    private var currentPersona: VoicePersona = VoicePersona.DEFAULT

    @Volatile
    private var customPitchFactor: Float? = null

    private val _isVoiceChangerActive = MutableStateFlow(false)
    val isVoiceChangerActive: StateFlow<Boolean> = _isVoiceChangerActive.asStateFlow()

    // Real-time audio input level (smoothed RMS: 0.0f to 1.0f) for live UI visualizers
    private val _liveAudioLevel = MutableStateFlow(0.0f)
    val liveAudioLevel: StateFlow<Float> = _liveAudioLevel.asStateFlow()

    // Pitch shifter circular delay line state
    // D = 480 samples @ 16kHz = 30ms.
    private val maxDelay = 480
    private val ringBufferSize = 2048
    private val ringBufferMask = ringBufferSize - 1
    private val ringBuffer = FloatArray(ringBufferSize)
    private var writePos = 0
    private var delayTap1 = 0.0f

    // Acoustic formant filter states
    private var prevLowPassSample = 0.0f
    private var prevBassSample = 0.0f
    private var prevHpSample = 0.0f
    private var prevInputHp = 0.0f
    private var prevLpSample = 0.0f
    private var robotCarrierPhase = 0.0

    fun setPersona(persona: VoicePersona) {
        currentPersona = persona
        customPitchFactor = null
        Log.i(TAG, "Voice Changer persona switched to: ${persona.name} (pitch: ${persona.pitch}x, effect: ${persona.effectType})")
    }

    fun setCustomPitch(factor: Float?) {
        customPitchFactor = factor?.coerceIn(0.60f, 1.50f)
        Log.i(TAG, "Voice Changer custom pitch set to: $customPitchFactor")
    }

    fun getCurrentPersona(): VoicePersona = currentPersona
    fun getCustomPitch(): Float? = customPitchFactor

    @SuppressLint("MissingPermission")
    fun start(
        streamType: Int = AudioManager.STREAM_MUSIC,
        context: Context? = null,
        isTestMode: Boolean = false
    ) {
        if (isRunning) return
        isTestModeActive = isTestMode

        val minBufIn = AudioRecord.getMinBufferSize(sampleRate, channelConfigIn, audioFormat)
        val minBufOut = AudioTrack.getMinBufferSize(sampleRate, channelConfigOut, audioFormat)
        val bufferSize = maxOf(minBufIn, minBufOut, 1024)

        // In active in-call mode (not test mode), isolate telephony audio routing via root:
        // 1. Unroute physical mic from Voice_Tx (so raw voice never reaches caller)
        // 2. Route MultiMedia1 to Voice_Tx (so caller receives only pitch-shifted voice)
        if (!isTestMode) {
            scope.launch {
                try {
                    val mixerCommands = """
                        tinymix 'Voice_Tx Mixer MultiMedia1' 1 2>/dev/null || true
                        tinymix 'Incall_Music Audio Mixer MultiMedia1' 1 2>/dev/null || true
                        tinymix 'Incall_Music Audio Mixer MultiMedia2' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer TX_CDC_DMA_TX_3' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer SLIM_0_TX' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer AFE_PCM_TX' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer PRI_TX_Voice' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer TERT_MI2S_TX_Voice' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer TX_Voice' 0 2>/dev/null || true
                    """.trimIndent()
                    RootPrivilegeManager.executeSuCommand(mixerCommands)
                } catch (e: Exception) {
                    Log.d(TAG, "In-call mixer commands skipped: ${e.message}")
                }
            }
        }

        try {
            // AudioRecord setup: MediaRecorder.AudioSource.MIC captures un-muted clean user speech
            var record: AudioRecord? = null
            val audioSources = if (isTestMode) {
                intArrayOf(
                    MediaRecorder.AudioSource.MIC,
                    MediaRecorder.AudioSource.DEFAULT
                )
            } else {
                intArrayOf(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    MediaRecorder.AudioSource.MIC,
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    MediaRecorder.AudioSource.DEFAULT
                )
            }

            for (src in audioSources) {
                try {
                    val candidate = AudioRecord(src, sampleRate, channelConfigIn, audioFormat, bufferSize * 2)
                    if (candidate.state == AudioRecord.STATE_INITIALIZED) {
                        record = candidate
                        Log.i(TAG, "AudioRecord initialized with audio source: $src (isTestMode: $isTestMode)")
                        break
                    } else {
                        candidate.release()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to initialize AudioRecord with source $src", e)
                }
            }

            if (record == null) {
                Log.e(TAG, "Could not initialize AudioRecord with any source")
                return
            }
            audioRecord = record

            // Attach hardware AEC, NoiseSuppressor, and AGC if supported
            val sessionId = record.audioSessionId
            if (sessionId != AudioRecord.ERROR_INVALID_OPERATION) {
                try {
                    if (AcousticEchoCanceler.isAvailable()) {
                        echoCanceler = AcousticEchoCanceler.create(sessionId)?.apply {
                            enabled = true
                            Log.i(TAG, "Hardware AcousticEchoCanceler enabled")
                        }
                    }
                    if (NoiseSuppressor.isAvailable()) {
                        noiseSuppressor = NoiseSuppressor.create(sessionId)?.apply {
                            enabled = true
                            Log.i(TAG, "Hardware NoiseSuppressor enabled")
                        }
                    }
                    if (AutomaticGainControl.isAvailable()) {
                        gainControl = AutomaticGainControl.create(sessionId)?.apply {
                            enabled = true
                            Log.i(TAG, "Hardware AutomaticGainControl enabled")
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error configuring hardware audiofx", e)
                }
            }

            // AudioTrack setup:
            // Test Mode -> STREAM_MUSIC / USAGE_MEDIA (plays directly into headphones or speakers)
            // In-Call Mode -> STREAM_VOICE_CALL / USAGE_VOICE_COMMUNICATION
            val audioAttributes = AudioAttributes.Builder()
                .setLegacyStreamType(streamType)
                .setUsage(
                    if (isTestMode || streamType == AudioManager.STREAM_MUSIC)
                        AudioAttributes.USAGE_MEDIA
                    else
                        AudioAttributes.USAGE_VOICE_COMMUNICATION
                )
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val audioFormatSpec = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(channelConfigOut)
                .setEncoding(audioFormat)
                .build()

            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormatSpec)
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            // In in-call mode, attempt routing directly to telephony device if available
            if (!isTestMode && context != null) {
                try {
                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                    val outputs = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                    val telephonyDevice = outputs?.firstOrNull { it.type == AudioDeviceInfo.TYPE_TELEPHONY }
                    if (telephonyDevice != null) {
                        track.setPreferredDevice(telephonyDevice)
                        Log.i(TAG, "Voice Changer AudioTrack routed to TYPE_TELEPHONY")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not set telephony device route", e)
                }
            }

            audioTrack = track
            track.setVolume(1.0f)

            // Reset circular delay line pointers
            writePos = 0
            delayTap1 = 0.0f
            ringBuffer.fill(0.0f)
            prevLowPassSample = 0.0f
            prevBassSample = 0.0f
            prevHpSample = 0.0f
            prevInputHp = 0.0f
            prevLpSample = 0.0f
            robotCarrierPhase = 0.0

            audioRecord?.startRecording()
            audioTrack?.play()
            isRunning = true
            _isVoiceChangerActive.value = true

            processingThread = Thread({
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
                processAudioLoop(bufferSize)
            }, "RealtimeVoiceDSP").apply { start() }

            Log.i(TAG, "Realtime Voice Changer DSP started (stream: $streamType, testMode: $isTestMode)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Realtime Voice Changer DSP", e)
            stop()
        }
    }

    fun stop() {
        isRunning = false
        _isVoiceChangerActive.value = false
        _liveAudioLevel.value = 0.0f

        processingThread?.interrupt()
        processingThread = null

        // In in-call mode, restore telephony mic uplink routing via root
        if (!isTestModeActive) {
            scope.launch {
                try {
                    val restoreCommands = """
                        tinymix 'Voice_Tx Mixer MultiMedia1' 0 2>/dev/null || true
                        tinymix 'Incall_Music Audio Mixer MultiMedia1' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer TX_CDC_DMA_TX_3' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer SLIM_0_TX' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer AFE_PCM_TX' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer PRI_TX_Voice' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer TERT_MI2S_TX_Voice' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer TX_Voice' 1 2>/dev/null || true
                    """.trimIndent()
                    RootPrivilegeManager.executeSuCommand(restoreCommands)
                } catch (_: Exception) {}
            }
        }

        try {
            echoCanceler?.release()
            echoCanceler = null
            noiseSuppressor?.release()
            noiseSuppressor = null
            gainControl?.release()
            gainControl = null
        } catch (_: Exception) {}

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord", e)
        }
        audioRecord = null

        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioTrack", e)
        }
        audioTrack = null
        isTestModeActive = false
        Log.i(TAG, "Realtime Voice Changer DSP stopped cleanly")
    }

    private fun processAudioLoop(bufferSize: Int) {
        // Small 20ms chunk (320 samples @ 16kHz) for ultra-low latency (<35ms round-trip)
        val chunkSamples = minOf(320, bufferSize / 2)
        val shortBuffer = ShortArray(chunkSamples)
        val outputBuffer = ShortArray(chunkSamples)

        while (isRunning && !Thread.currentThread().isInterrupted) {
            val readCount = audioRecord?.read(shortBuffer, 0, shortBuffer.size) ?: 0
            if (readCount > 0) {
                // Compute smoothed RMS level for UI audio level indicator
                var sumSq = 0.0
                for (k in 0 until readCount) {
                    val sampleVal = shortBuffer[k].toDouble()
                    sumSq += sampleVal * sampleVal
                }
                val rms = sqrt(sumSq / readCount) / 32768.0
                _liveAudioLevel.value = (rms * 3.5).toFloat().coerceIn(0.0f, 1.0f)

                val persona = currentPersona
                when (persona.effectType) {
                    VoiceEffectType.ROBOTIC_RING_MOD -> {
                        // Harmonic 65Hz ring modulation with DC bias for crisp metallic cyborg voice
                        val carrierFreq = 65.0
                        val phaseInc = (2.0 * Math.PI * carrierFreq) / sampleRate
                        for (k in 0 until readCount) {
                            val inVal = shortBuffer[k].toDouble()
                            val mod = 0.45 + 0.55 * sin(robotCarrierPhase)
                            robotCarrierPhase += phaseInc
                            if (robotCarrierPhase > 2.0 * Math.PI) robotCarrierPhase -= 2.0 * Math.PI
                            outputBuffer[k] = (inVal * mod * 1.30).toInt().coerceIn(-32768, 32767).toShort()
                        }
                    }

                    VoiceEffectType.WALKIE_TALKIE_RADIO -> {
                        // Dispatcher bandpass (high-pass at 350Hz, low-pass at 3200Hz) + radio distortion
                        for (k in 0 until readCount) {
                            val inSample = shortBuffer[k].toFloat()
                            val hp = 0.88f * (prevHpSample + inSample - prevInputHp)
                            prevHpSample = hp
                            prevInputHp = inSample
                            val bp = 0.65f * hp + 0.35f * prevLpSample
                            prevLpSample = bp
                            val driven = (bp * 2.2f).coerceIn(-24000f, 24000f)
                            val noise = ((Math.random() - 0.5) * 350).toFloat()
                            outputBuffer[k] = (driven + noise).toInt().coerceIn(-32768, 32767).toShort()
                        }
                    }

                    VoiceEffectType.DEEP_ANONYMOUS -> {
                        // Deep authoritative resonance (Marcus Deep Guard: pitch factor = 0.72x)
                        applyPitchShift(shortBuffer, outputBuffer, readCount, 0.72f)
                    }

                    VoiceEffectType.NONE -> {
                        val pitchShift = customPitchFactor ?: if (persona.gender == VoiceGender.FEMALE) {
                            persona.dspPitchFactor
                        } else if (persona.pitch != 1.0f) {
                            persona.pitch
                        } else {
                            1.0f
                        }

                        if (pitchShift != 1.0f) {
                            // High-definition natural female pitch shift (Chloe: 1.30x, Zoe: 1.32x, Mia: 1.28x)
                            applyPitchShift(shortBuffer, outputBuffer, readCount, pitchShift)
                        } else {
                            System.arraycopy(shortBuffer, 0, outputBuffer, 0, readCount)
                        }
                    }
                }

                audioTrack?.write(outputBuffer, 0, readCount)
            }
        }
    }

    /**
     * Synchronized Dual-Tap Circular Delay Line Pitch Shifter
     *
     * Mathematical properties:
     * 1. Output rate: t_read = t - delta(t) => dt_read/dt = 1 - d(delta)/dt = P.
     *    Hence delta1 advances by (1.0 - pitchFactor) each sample.
     * 2. Tap 2 is 180 degrees out of phase: delta2 = (delta1 + D / 2) % D.
     * 3. Hann window weights: W1 = 0.5 * (1 - cos(2*pi*delta1/D)), W2 = 0.5 * (1 + cos(2*pi*delta1/D)).
     *    W1 + W2 = 1.0 identically everywhere. Constant unity gain, zero tremolo.
     * 4. Wrap-around discontinuity occurs strictly when W = 0 => ZERO click/pop.
     * 5. Fractional delay with linear interpolation guarantees smooth sub-sample waveforms.
     * 6. Formant smoothing filter tames unnatural high frequencies (>3.8 kHz),
     *    yielding a rich, warm, and natural feminine vocal tone without chipmunk squeaks.
     */
    private fun applyPitchShift(
        input: ShortArray,
        output: ShortArray,
        length: Int,
        pitchFactor: Float
    ) {
        val factor = pitchFactor.coerceIn(0.60f, 1.45f)
        val rate = 1.0f - factor
        val halfD = maxDelay * 0.5f
        val twoPiOverD = (2.0 * Math.PI / maxDelay)

        for (i in 0 until length) {
            val inSample = input[i].toFloat()

            // 1. Write sample to ring buffer
            ringBuffer[writePos] = inSample

            // 2. Read Tap 1 with linear interpolation
            val d1 = delayTap1
            val dInt1 = d1.toInt()
            val frac1 = d1 - dInt1
            val idx1_0 = (writePos - dInt1) and ringBufferMask
            val idx1_1 = (idx1_0 - 1) and ringBufferMask
            val s1 = ringBuffer[idx1_0] * (1.0f - frac1) + ringBuffer[idx1_1] * frac1

            // Hann Window 1
            val phase1 = twoPiOverD * d1
            val cosP1 = cos(phase1).toFloat()
            val w1 = 0.5f * (1.0f - cosP1)

            // 3. Read Tap 2 (180 deg out of phase)
            val d2 = if (d1 >= halfD) d1 - halfD else d1 + halfD
            val dInt2 = d2.toInt()
            val frac2 = d2 - dInt2
            val idx2_0 = (writePos - dInt2) and ringBufferMask
            val idx2_1 = (idx2_0 - 1) and ringBufferMask
            val s2 = ringBuffer[idx2_0] * (1.0f - frac2) + ringBuffer[idx2_1] * frac2

            // Hann Window 2: cos(phase1 + pi) = -cos(phase1)
            val w2 = 0.5f * (1.0f + cosP1)

            // 4. Overlap-add
            var outVal = s1 * w1 + s2 * w2

            // 5. Formant shaping:
            if (factor > 1.0f) {
                // Natural Female Voice Warmth Filter:
                // Gentle 1st-order IIR low-pass filter (~3.8kHz cutoff)
                // Cuts high-frequency chipmunk sibilance while keeping consonants clear
                prevLowPassSample = 0.72f * outVal + 0.28f * prevLowPassSample
                outVal = prevLowPassSample
            } else if (factor < 1.0f) {
                // Deep Male Resonance Boost:
                prevBassSample = 0.40f * outVal + 0.60f * prevBassSample
                outVal = outVal * 0.85f + prevBassSample * 0.40f
            }

            // 6. Soft limiter to prevent digital clipping
            val normalized = (outVal / 32768.0f).coerceIn(-1.5f, 1.5f)
            val softClipped = if (normalized > 0.85f) {
                0.85f + (normalized - 0.85f) / (1.0f + (normalized - 0.85f) * (normalized - 0.85f))
            } else if (normalized < -0.85f) {
                -0.85f + (normalized + 0.85f) / (1.0f + (normalized + 0.85f) * (normalized + 0.85f))
            } else {
                normalized
            }

            output[i] = (softClipped * 32767.0f).toInt().coerceIn(-32768, 32767).toShort()

            // 7. Advance delay tap
            delayTap1 += rate
            if (delayTap1 >= maxDelay) {
                delayTap1 -= maxDelay
            } else if (delayTap1 < 0.0f) {
                delayTap1 += maxDelay
            }

            // 8. Advance circular buffer write position
            writePos = (writePos + 1) and ringBufferMask
        }
    }

    companion object {
        private const val TAG = "VoiceChangerEngine"
    }
}
