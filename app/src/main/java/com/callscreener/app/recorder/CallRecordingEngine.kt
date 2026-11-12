package com.callscreener.app.recorder

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import android.os.Process
import android.util.Log
import androidx.core.content.ContextCompat
import com.callscreener.app.root.RootPrivilegeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RecordedCallItem(
    val id: String,
    val file: File,
    val callerNumber: String,
    val timestamp: Long,
    val durationSeconds: Long,
    val fileSizeFormatted: String
)

class CallRecordingEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var currentFile: File? = null

    @Volatile
    var isRecording: Boolean = false
        private set

    companion object {
        private const val TAG = "CallRecordingEngine"
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    fun startRecording(callerNumber: String, isSimulated: Boolean): File? {
        if (isRecording) {
            Log.w(TAG, "Recording is already active")
            return currentFile
        }

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "RECORD_AUDIO permission is not granted")
            return null
        }

        try {
            val recordingsDir = getRecordingsDir()
            val cleanNumber = callerNumber.replace(Regex("[^0-9+]"), "").ifBlank { "Unknown" }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val wavFile = File(recordingsDir, "Call_${cleanNumber}_${timestamp}.wav")
            currentFile = wavFile

            scope.launch {
                unmaskRootAudioMixer(isSimulated)
            }

            val pair = createAudioRecord()
            if (pair == null) {
                Log.e(TAG, "Failed to initialize working AudioRecord for call recording")
                return null
            }

            val (record, rate) = pair
            audioRecord = record

            try {
                record.startRecording()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to startRecording", e)
                record.release()
                audioRecord = null
                return null
            }

            if (record.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                Log.e(TAG, "AudioRecord state is not RECORDING: ${record.recordingState}")
                record.release()
                audioRecord = null
                return null
            }

            isRecording = true
            Log.i(TAG, "Call recording started -> ${wavFile.name} at ${rate}Hz")

            recordingThread = Thread({
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
                writeWavAudioStream(wavFile, record, rate)
            }, "Resonix-AudioRecorder")
            recordingThread?.start()

            return wavFile
        } catch (e: Exception) {
            Log.e(TAG, "Error starting call recording", e)
            isRecording = false
            return null
        }
    }

    fun stopRecording(): File? {
        if (!isRecording) return currentFile
        isRecording = false

        try {
            audioRecord?.apply {
                try {
                    stop()
                } catch (_: Exception) {}
            }

            recordingThread?.join(1500)

            audioRecord?.apply {
                try {
                    release()
                } catch (_: Exception) {}
            }
            audioRecord = null
            recordingThread = null

            val file = currentFile
            return if (file != null && file.exists() && file.length() > 44) {
                Log.i(TAG, "Call recording stopped and verified: ${file.name} (${file.length()} bytes)")
                file
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping call recording", e)
            return currentFile
        }
    }

    private fun createAudioRecord(): Pair<AudioRecord, Int>? {
        val sampleRates = intArrayOf(44100, 16000, 48000, 8000)
        val audioSources = if (RootPrivilegeManager.isRootAvailable()) {
            intArrayOf(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                MediaRecorder.AudioSource.VOICE_CALL
            )
        } else {
            intArrayOf(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.AudioSource.VOICE_RECOGNITION
            )
        }

        for (rate in sampleRates) {
            val minBuf = AudioRecord.getMinBufferSize(rate, CHANNEL_CONFIG, AUDIO_FORMAT)
            if (minBuf <= 0) continue
            val bufferSize = (minBuf * 2).coerceAtLeast(4096)

            for (source in audioSources) {
                try {
                    val record = AudioRecord(
                        source,
                        rate,
                        CHANNEL_CONFIG,
                        AUDIO_FORMAT,
                        bufferSize
                    )
                    if (record.state == AudioRecord.STATE_INITIALIZED) {
                        try {
                            record.startRecording()
                            val testBuf = ByteArray(512)
                            val testRead = record.read(testBuf, 0, testBuf.size)
                            record.stop()
                            if (testRead >= 0) {
                                Log.i(TAG, "AudioRecord verified: source $source at ${rate}Hz (buf $bufferSize)")
                                return Pair(record, rate)
                            } else {
                                Log.w(TAG, "AudioRecord source $source read test returned code: $testRead")
                                record.release()
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "AudioRecord source $source test failed: ${e.message}")
                            record.release()
                        }
                    } else {
                        record.release()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Source $source at $rate not supported: ${e.message}")
                }
            }
        }
        return null
    }

    private suspend fun unmaskRootAudioMixer(isSimulated: Boolean) {
        if (isSimulated) return
        if (!RootPrivilegeManager.isRootAvailable() && !RootPrivilegeManager.isPrivilegedApp(context)) return

        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.setParameters("audio_capability=call_recording=1")
            audioManager?.setParameters("incall_music_enable=true")
            Log.i(TAG, "Root dual-side audio parameters configured")
        } catch (e: Exception) {
            Log.w(TAG, "Could not set root audio recording parameters", e)
        }
    }

    private fun writeWavAudioStream(wavFile: File, record: AudioRecord, sampleRate: Int) {
        val minBuf = AudioRecord.getMinBufferSize(sampleRate, CHANNEL_CONFIG, AUDIO_FORMAT)
        val buffer = ByteArray((minBuf * 2).coerceAtLeast(4096))
        var totalAudioLen = 0L
        var fos: FileOutputStream? = null

        try {
            fos = FileOutputStream(wavFile)
            fos.write(ByteArray(44))

            while (isRecording) {
                val readBytes = record.read(buffer, 0, buffer.size)
                if (readBytes > 0) {
                    fos.write(buffer, 0, readBytes)
                    totalAudioLen += readBytes
                } else if (readBytes < 0) {
                    Log.w(TAG, "AudioRecord read returned error code: $readBytes")
                    Thread.sleep(15)
                }
            }
            fos.flush()
        } catch (e: Exception) {
            if (isRecording) {
                Log.e(TAG, "Error writing WAV stream", e)
            }
        } finally {
            try {
                fos?.close()
            } catch (_: Exception) {}

            if (wavFile.exists()) {
                if (totalAudioLen > 0) {
                    finalizeWavHeader(wavFile, sampleRate, totalAudioLen)
                    Log.i(TAG, "WAV recording finalized: ${wavFile.name} ($totalAudioLen bytes)")
                } else {
                    try { wavFile.delete() } catch (_: Exception) {}
                }
            }
        }
    }

    private fun finalizeWavHeader(wavFile: File, sampleRate: Int, totalAudioLen: Long) {
        val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = sampleRate * channels * 2
        val blockAlign = channels * 2

        try {
            RandomAccessFile(wavFile, "rw").use { raf ->
                raf.seek(0)
                raf.writeBytes("RIFF")
                raf.writeInt(Integer.reverseBytes(totalDataLen.toInt()))
                raf.writeBytes("WAVE")
                raf.writeBytes("fmt ")
                raf.writeInt(Integer.reverseBytes(16))
                raf.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt())
                raf.writeShort(java.lang.Short.reverseBytes(channels.toShort()).toInt())
                raf.writeInt(Integer.reverseBytes(sampleRate))
                raf.writeInt(Integer.reverseBytes(byteRate))
                raf.writeShort(java.lang.Short.reverseBytes(blockAlign.toShort()).toInt())
                raf.writeShort(java.lang.Short.reverseBytes(16.toShort()).toInt())
                raf.writeBytes("data")
                raf.writeInt(Integer.reverseBytes(totalAudioLen.toInt()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finalizing WAV header", e)
        }
    }

    fun getRecordingsDir(): File {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS) ?: context.filesDir, "CallRecordings")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun getSavedRecordings(): List<RecordedCallItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<RecordedCallItem>()
        val dir = getRecordingsDir()
        val files = dir.listFiles { file -> file.extension.equals("wav", ignoreCase = true) } ?: emptyArray()

        files.sortByDescending { it.lastModified() }
        for (file in files) {
            val fileBytes = file.length()
            if (fileBytes <= 44) {
                try { file.delete() } catch (_: Exception) {}
                continue
            }

            val name = file.nameWithoutExtension
            val parts = name.split("_")
            val number = if (parts.size >= 2) parts[1] else "Unknown"
            val sizeKb = (fileBytes / 1024).coerceAtLeast(1)
            val sizeFormatted = if (sizeKb > 1024) String.format(Locale.US, "%.1f MB", sizeKb / 1024.0) else "$sizeKb KB"

            val audioBytes = (fileBytes - 44).coerceAtLeast(0)
            val durationSeconds = (audioBytes / (SAMPLE_RATE * 2)).coerceAtLeast(1)

            list.add(
                RecordedCallItem(
                    id = file.name,
                    file = file,
                    callerNumber = number,
                    timestamp = file.lastModified(),
                    durationSeconds = durationSeconds,
                    fileSizeFormatted = sizeFormatted
                )
            )
        }
        list
    }

    suspend fun deleteRecording(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file ${file.name}", e)
            false
        }
    }
}
