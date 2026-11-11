package com.callscreener.app.engine

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.callscreener.app.model.AudioPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AudioPresetManager(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val presetsDir = File(context.filesDir, "audio_presets").apply { mkdirs() }

    private val _presets = MutableStateFlow<List<AudioPreset>>(emptyList())
    val presets: StateFlow<List<AudioPreset>> = _presets.asStateFlow()

    private var activePlayer: MediaPlayer? = null
    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    init {
        loadPresets()
    }

    fun loadPresets() {
        val savedJson = prefs.getString(KEY_ALL_PRESETS_ORDER, null)
        if (!savedJson.isNullOrBlank()) {
            try {
                val array = JSONArray(savedJson)
                val list = mutableListOf<AudioPreset>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        AudioPreset(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            textOrPath = obj.getString("textOrPath"),
                            isAudioFile = obj.optBoolean("isAudioFile", false),
                            iconEmoji = obj.optString("iconEmoji", ""),
                            category = obj.optString("category", "Quick Response"),
                            durationSeconds = obj.optInt("durationSeconds", 0)
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    val existingIds = list.map { it.id }.toSet()
                    val newDefaults = AudioPreset.DEFAULT_PRESETS.filter { it.id !in existingIds }
                    if (newDefaults.isNotEmpty()) {
                        list.addAll(newDefaults)
                        saveAllPresets(list)
                    }
                    _presets.value = list
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing saved presets order", e)
            }
        }

        // Migrate legacy custom presets if any, placing custom files at the top by default!
        val legacyCustomJson = prefs.getString(KEY_CUSTOM_PRESETS, null)
        val legacyCustom = mutableListOf<AudioPreset>()
        if (!legacyCustomJson.isNullOrBlank()) {
            try {
                val array = JSONArray(legacyCustomJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    legacyCustom.add(
                        AudioPreset(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            textOrPath = obj.getString("textOrPath"),
                            isAudioFile = obj.optBoolean("isAudioFile", false),
                            iconEmoji = obj.optString("iconEmoji", ""),
                            category = obj.optString("category", "Custom"),
                            durationSeconds = obj.optInt("durationSeconds", 0)
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        // Put custom presets FIRST so user can access them right away, followed by default presets!
        val initialList = legacyCustom + AudioPreset.DEFAULT_PRESETS
        _presets.value = initialList
        saveAllPresets(initialList)
    }

    private fun saveAllPresets(list: List<AudioPreset>) {
        try {
            val array = JSONArray()
            for (p in list) {
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("title", p.title)
                    put("textOrPath", p.textOrPath)
                    put("isAudioFile", p.isAudioFile)
                    put("iconEmoji", p.iconEmoji)
                    put("category", p.category)
                    put("durationSeconds", p.durationSeconds)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_ALL_PRESETS_ORDER, array.toString()).apply()
            _presets.value = list
        } catch (e: Exception) {
            Log.e(TAG, "Error saving presets order", e)
        }
    }

    fun movePresetUp(id: String) {
        val list = _presets.value.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index > 0) {
            val item = list.removeAt(index)
            list.add(index - 1, item)
            saveAllPresets(list)
        }
    }

    fun movePresetDown(id: String) {
        val list = _presets.value.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index >= 0 && index < list.size - 1) {
            val item = list.removeAt(index)
            list.add(index + 1, item)
            saveAllPresets(list)
        }
    }

    fun movePresetToTop(id: String) {
        val list = _presets.value.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index > 0) {
            val item = list.removeAt(index)
            list.add(0, item)
            saveAllPresets(list)
        }
    }

    fun reorderPresets(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val list = _presets.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            saveAllPresets(list)
        }
    }

    suspend fun importAudioFile(title: String, sourceUri: Uri, insertAtTop: Boolean = true): Result<AudioPreset> = withContext(Dispatchers.IO) {
        try {
            val destFileName = "preset_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.wav"
            val destFile = File(presetsDir, destFileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Cannot open audio source"))

            val preset = AudioPreset(
                id = "custom_${UUID.randomUUID().toString().take(8)}",
                title = title.ifBlank { "Custom Audio" },
                textOrPath = destFile.absolutePath,
                isAudioFile = true,
                iconEmoji = "",
                category = "Custom Audio"
            )

            val currentList = _presets.value.toMutableList()
            if (insertAtTop) {
                currentList.add(0, preset) // Insert at the very top!
            } else {
                currentList.add(preset)
            }
            saveAllPresets(currentList)

            Result.success(preset)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import audio preset", e)
            Result.failure(e)
        }
    }

    fun addCustomTextPreset(
        title: String,
        spokenText: String,
        category: String = "Custom",
        insertAtTop: Boolean = true
    ) {
        val cleanTitle = title.trim()
        val cleanText = spokenText.trim().ifBlank { cleanTitle }
        if (cleanTitle.isBlank()) return

        val preset = AudioPreset(
            id = "custom_txt_${UUID.randomUUID().toString().take(8)}",
            title = cleanTitle,
            textOrPath = cleanText,
            isAudioFile = false,
            iconEmoji = "",
            category = category
        )
        val currentList = _presets.value.toMutableList()
        if (insertAtTop) {
            currentList.add(0, preset)
        } else {
            currentList.add(preset)
        }
        saveAllPresets(currentList)
    }

    fun deletePreset(id: String) {
        val preset = _presets.value.firstOrNull { it.id == id }
        if (preset != null && preset.isAudioFile) {
            try {
                val f = File(preset.textOrPath)
                if (f.exists()) f.delete()
            } catch (_: Exception) {}
        }
        val currentList = _presets.value.filter { it.id != id }
        saveAllPresets(currentList)
    }

    fun renamePreset(id: String, newTitle: String) {
        val trimmed = newTitle.trim()
        if (trimmed.isBlank()) return
        val currentList = _presets.value.map {
            if (it.id == id) it.copy(title = trimmed) else it
        }
        saveAllPresets(currentList)
    }

    fun editPreset(id: String, newTitle: String, newText: String? = null) {
        val cleanTitle = newTitle.trim()
        if (cleanTitle.isBlank()) return
        val currentList = _presets.value.map { preset ->
            if (preset.id == id) {
                preset.copy(
                    title = cleanTitle,
                    textOrPath = if (preset.isAudioFile) preset.textOrPath else (newText?.trim()?.ifBlank { preset.textOrPath } ?: preset.textOrPath)
                )
            } else preset
        }
        saveAllPresets(currentList)
    }

    fun resetPresetsToDefault() {
        // Keep custom audio files at the top, restore defaults underneath
        val customFiles = _presets.value.filter { it.isAudioFile }
        val restored = customFiles + AudioPreset.DEFAULT_PRESETS
        saveAllPresets(restored)
    }

    /**
     * Preview preset locally through phone speaker.
     */
    fun previewPreset(preset: AudioPreset, onComplete: () -> Unit = {}) {
        stopPreview()
        _isPaused.value = false
        if (preset.isAudioFile) {
            val f = File(preset.textOrPath)
            if (f.exists()) {
                try {
                    val player = MediaPlayer().apply {
                        setDataSource(f.absolutePath)
                        setOnCompletionListener {
                            stopPreview()
                            onComplete()
                        }
                        setOnErrorListener { _, _, _ ->
                            stopPreview()
                            onComplete()
                            true
                        }
                        prepare()
                        start()
                    }
                    activePlayer = player
                    _currentlyPlayingId.value = preset.id
                    _isPaused.value = false
                } catch (e: Exception) {
                    Log.e(TAG, "Error playing audio file preset", e)
                    stopPreview()
                    onComplete()
                }
            }
        }
    }

    fun pausePreview() {
        try {
            if (activePlayer?.isPlaying == true) {
                activePlayer?.pause()
                _isPaused.value = true
                Log.i(TAG, "Preview paused: ${_currentlyPlayingId.value}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error pausing preview", e)
        }
    }

    fun resumePreview() {
        try {
            activePlayer?.start()
            _isPaused.value = false
            Log.i(TAG, "Preview resumed: ${_currentlyPlayingId.value}")
        } catch (e: Exception) {
            Log.w(TAG, "Error resuming preview", e)
        }
    }

    fun stopPreview() {
        try {
            activePlayer?.stop()
            activePlayer?.release()
        } catch (_: Exception) {}
        activePlayer = null
        _currentlyPlayingId.value = null
        _isPaused.value = false
    }

    companion object {
        private const val TAG = "AudioPresetManager"
        private const val PREFS_NAME = "audio_presets_prefs"
        private const val KEY_CUSTOM_PRESETS = "custom_audio_presets_json"
        private const val KEY_ALL_PRESETS_ORDER = "all_presets_order_json"
    }
}
