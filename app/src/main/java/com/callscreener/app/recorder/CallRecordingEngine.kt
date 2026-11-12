package com.callscreener.app.recorder

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import com.callscreener.app.root.RootPrivilegeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
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
    private var recordingJob: Job? = null
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

        try {
            val recordingsDir = getRecordingsDir()
            val cleanNumber = callerNumber.replace(Regex("[^0-9+]"), "").ifBlank { "Unknown" }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val wavFile = File(recordingsDir, "Call_${cleanNumber}_${timestamp}.wav")
            currentFile = wavFile

            scope.launch {
                unmaskRootAudioMixer(isSimulated)
            }

            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 2
            val record = createAudioRecord(bufferSize)

            if (record == null || record.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "Failed to initialize AudioRecord for call recording")
                record?.release()
                return null
            }

            audioRecord = record
            record.startRecording()
            isRecording = true
            Log.i(TAG, "Call recording started -> ${wavFile.absolutePath}")

            recordingJob = scope.launch {
                writeWavAudioStream(wavFile, record, bufferSize)
            }

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
            recordingJob?.cancel()
            recordingJob = null

            audioRecord?.apply {
                try {
                    stop()
                } catch (_: Exception) {}
                release()
            }
            audioRecord = null

            val file = currentFile
            if (file != null && file.exists() && file.length() > 44) {
                finalizeWavHeader(file)
                Log.i(TAG, "Call recording stopped and finalized: ${file.name} (${file.length()} bytes)")
            }

            return file
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping call recording", e)
            return currentFile
        }
    }

    private fun createAudioRecord(bufferSize: Int): AudioRecord? {
        val audioSources = intArrayOf(
            MediaRecorder.AudioSource.VOICE_CALL,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.MIC
        )

        for (source in audioSources) {
            try {
                val record = AudioRecord(
                    source,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
                )
                if (record.state == AudioRecord.STATE_INITIALIZED) {
                    Log.i(TAG, "Successfully initialized AudioRecord with source: $source")
                    return record
                }
                record.release()
            } catch (e: Exception) {
                Log.w(TAG, "Source $source not supported: ${e.message}")
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

    private fun writeWavAudioStream(wavFile: File, record: AudioRecord, bufferSize: Int) {
        val buffer = ByteArray(bufferSize)
        var totalAudioLen = 0L

        try {
            FileOutputStream(wavFile).use { fos ->
                fos.write(ByteArray(44))

                while (isRecording && recordingJob?.isActive == true) {
                    val readBytes = record.read(buffer, 0, buffer.size)
                    if (readBytes > 0) {
                        fos.write(buffer, 0, readBytes)
                        totalAudioLen += readBytes
                    }
                }
            }
        } catch (e: Exception) {
            if (isRecording) {
                Log.e(TAG, "Error writing WAV stream", e)
            }
        }
    }

    private fun finalizeWavHeader(wavFile: File) {
        val totalAudioLen = wavFile.length() - 44
        val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = SAMPLE_RATE * channels * 2
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
                raf.writeInt(Integer.reverseBytes(SAMPLE_RATE))
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
            val name = file.nameWithoutExtension
            val parts = name.split("_")
            val number = if (parts.size >= 2) parts[1] else "Unknown"
            val sizeKb = file.length() / 1024
            val sizeFormatted = if (sizeKb > 1024) String.format(Locale.US, "%.1f MB", sizeKb / 1024.0) else "$sizeKb KB"

            val audioBytes = (file.length() - 44).coerceAtLeast(0)
            val durationSeconds = audioBytes / (SAMPLE_RATE * 2)

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
