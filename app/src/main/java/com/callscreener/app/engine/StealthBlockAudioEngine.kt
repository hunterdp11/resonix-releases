package com.callscreener.app.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.telecom.Call
import android.telecom.VideoProfile
import android.util.Log
import com.callscreener.app.root.RootPrivilegeManager
import com.callscreener.app.service.CallScreeningInCallService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

object StealthBlockAudioEngine {

    private const val TAG = "StealthBlockAudio"
    private const val SAMPLE_RATE = 48000
    private val scope = CoroutineScope(Dispatchers.IO)

    @Volatile
    private var isInitialized = false

    fun prewarm(context: Context) {
        if (isInitialized) return
        isInitialized = true
        scope.launch {
            try {
                val dir = File(context.cacheDir, "stealth_tones").apply { mkdirs() }
                ensureSitToneWav(dir)
                ensureFastBusyWav(dir)
                prewarmTtsAnnouncements(context.applicationContext, dir)
            } catch (e: Exception) {
                Log.w(TAG, "Prewarm stealth audio notice: ${e.message}")
            }
        }
    }

    fun handleStealthBlock(context: Context, call: Call, callerNumber: String, mode: BlockRejectMode) {
        scope.launch {
            try {
                Log.i(TAG, ">>> STEALTH BLOCK INTERCEPT: Number='$callerNumber', Mode='${mode.name}'")

                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                try {
                    audioManager.isMicrophoneMute = true
                    audioManager.setParameters("voice_tx_mute=1")
                    audioManager.setParameters("voice_tx_mute=true")
                    CallScreeningInCallService.setCallMute(true)
                } catch (_: Exception) {}

                RootPrivilegeManager.executeAsync {
                    val script = """
                        tinymix 'Voice Tx Mute' 1 2>/dev/null || true
                        tinymix 'Voice Tx Device Mute' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer TX_Voice' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer MultiMedia1' 1 2>/dev/null || true
                        tinymix 'Incall_Music Audio Mixer MultiMedia1' 1 2>/dev/null || true
                    """.trimIndent()
                    RootPrivilegeManager.executeSuCommand(script)
                }

                withContext(Dispatchers.Main) {
                    try {
                        call.answer(VideoProfile.STATE_AUDIO_ONLY)
                        Log.i(TAG, "Call answered in 0ms (ringback killed instantly on caller side)")
                    } catch (e: Exception) {
                        Log.w(TAG, "call.answer notice: ${e.message}")
                    }
                }

                when (mode) {
                    BlockRejectMode.INSTANT_CARRIER_DROP -> {
                        delay(35L)
                        disconnectAndDrop(context, call)
                    }

                    BlockRejectMode.FAST_BUSY -> {
                        delay(60L)
                        val dir = File(context.cacheDir, "stealth_tones").apply { mkdirs() }
                        val wavFile = ensureFastBusyWav(dir)
                        injectWavToCall(context, wavFile)
                        disconnectAndDrop(context, call)
                    }

                    BlockRejectMode.NUMBER_DOES_NOT_EXIST -> {
                        delay(60L)
                        val dir = File(context.cacheDir, "stealth_tones").apply { mkdirs() }
                        val announcementFile = File(dir, "carrier_not_exist_full.wav")
                        val fileToPlay = if (announcementFile.exists() && announcementFile.length() > 1000) {
                            announcementFile
                        } else {
                            ensureSitToneWav(dir)
                        }
                        injectWavToCall(context, fileToPlay)
                        disconnectAndDrop(context, call)
                    }

                    BlockRejectMode.SWITCHED_OFF_NOTICE -> {
                        delay(60L)
                        val dir = File(context.cacheDir, "stealth_tones").apply { mkdirs() }
                        val announcementFile = File(dir, "carrier_switched_off_full.wav")
                        val fileToPlay = if (announcementFile.exists() && announcementFile.length() > 1000) {
                            announcementFile
                        } else {
                            ensureSitToneWav(dir)
                        }
                        injectWavToCall(context, fileToPlay)
                        disconnectAndDrop(context, call)
                    }

                    BlockRejectMode.NOT_AVAILABLE_ANNOUNCEMENT -> {
                        delay(60L)
                        val dir = File(context.cacheDir, "stealth_tones").apply { mkdirs() }
                        val announcementFile = File(dir, "carrier_not_available_full.wav")
                        val fileToPlay = if (announcementFile.exists() && announcementFile.length() > 1000) {
                            announcementFile
                        } else {
                            ensureSitToneWav(dir)
                        }
                        injectWavToCall(context, fileToPlay)
                        disconnectAndDrop(context, call)
                    }

                    BlockRejectMode.FORWARD_TO_TARGET -> {
                        disconnectAndDrop(context, call)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in handleStealthBlock", e)
                disconnectAndDrop(context, call)
            }
        }
    }

    private fun disconnectAndDrop(context: Context, call: Call) {
        try {
            call.disconnect()
        } catch (_: Exception) {}
        val blockManager = CallBlockManager.getInstance(context)
        blockManager.executeRootZeroRingDrop()
        Log.i(TAG, "Stealth call terminated & modem dropped.")
    }

    fun generateSitTonePcm(): ByteArray {
        val bos = ByteArrayOutputStream()
        appendTone(bos, 950.0, 330)
        appendTone(bos, 1400.0, 330)
        appendTone(bos, 1800.0, 330)
        appendSilence(bos, 150)
        return bos.toByteArray()
    }

    fun generateFastBusyPcm(): ByteArray {
        val bos = ByteArrayOutputStream()
        for (i in 0 until 2) {
            appendDualTone(bos, 480.0, 620.0, 250)
            appendSilence(bos, 250)
        }
        return bos.toByteArray()
    }

    private fun appendTone(bos: ByteArrayOutputStream, freq: Double, durationMs: Int) {
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until numSamples) {
            val angle = 2.0 * PI * freq * i / SAMPLE_RATE
            val sample = (sin(angle) * 26000.0).toInt().coerceIn(-32768, 32767).toShort()
            buffer.putShort(sample)
        }
        bos.write(buffer.array())
    }

    private fun appendDualTone(bos: ByteArrayOutputStream, freq1: Double, freq2: Double, durationMs: Int) {
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until numSamples) {
            val s1 = sin(2.0 * PI * freq1 * i / SAMPLE_RATE)
            val s2 = sin(2.0 * PI * freq2 * i / SAMPLE_RATE)
            val sample = ((s1 + s2) * 0.5 * 26000.0).toInt().coerceIn(-32768, 32767).toShort()
            buffer.putShort(sample)
        }
        bos.write(buffer.array())
    }

    private fun appendSilence(bos: ByteArrayOutputStream, durationMs: Int) {
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val bytes = ByteArray(numSamples * 2)
        bos.write(bytes)
    }

    fun ensureSitToneWav(dir: File): File {
        val file = File(dir, "sit_tone.wav")
        if (!file.exists() || file.length() < 1000) {
            val pcm = generateSitTonePcm()
            writeWavFile(file, pcm, SAMPLE_RATE, 1)
        }
        return file
    }

    fun ensureFastBusyWav(dir: File): File {
        val file = File(dir, "fast_busy.wav")
        if (!file.exists() || file.length() < 1000) {
            val pcm = generateFastBusyPcm()
            writeWavFile(file, pcm, SAMPLE_RATE, 1)
        }
        return file
    }

    private fun writeWavFile(outFile: File, pcmData: ByteArray, sampleRate: Int, channels: Int) {
        val totalAudioLen = pcmData.size.toLong()
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * 2

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * 2).toByte()
        header[33] = 0
        header[34] = 16
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        FileOutputStream(outFile).use { fos ->
            fos.write(header)
            fos.write(pcmData)
        }
    }

    private suspend fun injectWavToCall(context: Context, wavFile: File) = withContext(Dispatchers.IO) {
        if (!wavFile.exists() || wavFile.length() < 100) return@withContext
        val injector = RootCallAudioInjector(context)
        injector.injectAudioFile(wavFile) { ok ->
            Log.d(TAG, "Stealth audio notice injected: $ok (${wavFile.name})")
        }
    }

    private fun prewarmTtsAnnouncements(context: Context, dir: File) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                scope.launch {
                    try {
                        val sitPcm = generateSitTonePcm()

                        synthesizeNotice(context, tts, dir, sitPcm,
                            "The number you have dialed does not exist. Please check the number and dial again.",
                            "carrier_not_exist_full.wav"
                        )

                        synthesizeNotice(context, tts, dir, sitPcm,
                            "The number you have dialed is currently switched off. Please try again later.",
                            "carrier_switched_off_full.wav"
                        )

                        synthesizeNotice(context, tts, dir, sitPcm,
                            "The number you have dialed is currently not available. Please check the number and dial again.",
                            "carrier_not_available_full.wav"
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Error prewarming TTS: ${e.message}")
                    } finally {
                        try { tts?.shutdown() } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    private suspend fun synthesizeNotice(
        context: Context,
        tts: TextToSpeech?,
        dir: File,
        sitPcm: ByteArray,
        speechText: String,
        outFilename: String
    ) = withContext(Dispatchers.IO) {
        val target = File(dir, outFilename)
        if (target.exists() && target.length() > 2000) return@withContext

        val tempWav = File(dir, "temp_tts_${System.currentTimeMillis()}.wav")
        try {
            val params = android.os.Bundle()
            val utteranceId = "stealth_tts_${System.currentTimeMillis()}"
            var completed = false

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) { completed = true }
                override fun onError(id: String?) { completed = true }
            })

            val res = tts?.synthesizeToFile(speechText, params, tempWav, utteranceId)
            if (res == TextToSpeech.SUCCESS) {
                var waited = 0
                while (!completed && waited < 40) {
                    delay(100)
                    waited++
                }

                if (tempWav.exists() && tempWav.length() > 44) {
                    val ttsPcm = readWavDataOnly(tempWav)
                    val merged = ByteArrayOutputStream().apply {
                        write(sitPcm)
                        write(ttsPcm)
                    }.toByteArray()
                    writeWavFile(target, merged, SAMPLE_RATE, 1)
                    Log.i(TAG, "Prewarmed authentic carrier notice: ${target.name} (${target.length()} bytes)")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to synthesize notice $outFilename: ${e.message}")
        } finally {
            try { tempWav.delete() } catch (_: Exception) {}
        }
    }

    private fun readWavDataOnly(wavFile: File): ByteArray {
        val fis = FileInputStream(wavFile)
        val header = ByteArray(44)
        fis.read(header)
        val data = ByteArray((wavFile.length() - 44).toInt())
        fis.read(data)
        fis.close()
        return data
    }
}
