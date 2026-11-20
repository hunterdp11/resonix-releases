package com.callscreener.app.engine

import android.content.Context
import android.util.Log
import com.callscreener.app.model.QuickReply
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class QuickReplyManager(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _quickReplies = MutableStateFlow<List<QuickReply>>(emptyList())
    val quickReplies: StateFlow<List<QuickReply>> = _quickReplies.asStateFlow()

    init {
        loadQuickReplies()
    }

    fun loadQuickReplies() {
        val savedJson = prefs.getString(KEY_QUICK_REPLIES, null)
        if (!savedJson.isNullOrBlank()) {
            try {
                val array = JSONArray(savedJson)
                val list = mutableListOf<QuickReply>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        QuickReply(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            spokenText = obj.getString("spokenText"),
                            isHindi = obj.optBoolean("isHindi", false)
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    val existingIds = list.map { it.id }.toSet()
                    val newDefaults = QuickReply.DEFAULT_REPLIES.filter { it.id !in existingIds }
                    if (newDefaults.isNotEmpty()) {
                        list.addAll(newDefaults)
                        saveQuickReplies(list)
                    }
                    _quickReplies.value = list
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing saved quick replies", e)
            }
        }
        _quickReplies.value = QuickReply.DEFAULT_REPLIES
        saveQuickReplies(_quickReplies.value)
    }

    private fun saveQuickReplies(list: List<QuickReply>) {
        try {
            val array = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("spokenText", item.spokenText)
                    put("isHindi", item.isHindi)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_QUICK_REPLIES, array.toString()).apply()
            _quickReplies.value = list
        } catch (e: Exception) {
            Log.e(TAG, "Error saving quick replies", e)
        }
    }

    fun moveUp(id: String) {
        val list = _quickReplies.value.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index > 0) {
            val item = list.removeAt(index)
            list.add(index - 1, item)
            saveQuickReplies(list)
        }
    }

    fun moveDown(id: String) {
        val list = _quickReplies.value.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index >= 0 && index < list.size - 1) {
            val item = list.removeAt(index)
            list.add(index + 1, item)
            saveQuickReplies(list)
        }
    }

    fun moveToTop(id: String) {
        val list = _quickReplies.value.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index > 0) {
            val item = list.removeAt(index)
            list.add(0, item)
            saveQuickReplies(list)
        }
    }

    fun reorderQuickReplies(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val list = _quickReplies.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            saveQuickReplies(list)
        }
    }

    fun addReply(title: String, spokenText: String, isHindi: Boolean = false, insertAtTop: Boolean = true) {
        val cleanTitle = title.trim()
        val cleanSpoken = spokenText.trim().ifBlank { cleanTitle }
        if (cleanTitle.isBlank()) return

        val reply = QuickReply(
            id = "custom_qr_${UUID.randomUUID().toString().take(8)}",
            title = cleanTitle,
            spokenText = cleanSpoken,
            isHindi = isHindi
        )

        val list = _quickReplies.value.toMutableList()
        if (insertAtTop) {
            list.add(0, reply)
        } else {
            list.add(reply)
        }
        saveQuickReplies(list)
    }

    fun editReply(id: String, newTitle: String, newSpokenText: String, isHindi: Boolean) {
        val cleanTitle = newTitle.trim()
        val cleanSpoken = newSpokenText.trim().ifBlank { cleanTitle }
        if (cleanTitle.isBlank()) return

        val list = _quickReplies.value.map { reply ->
            if (reply.id == id) {
                reply.copy(
                    title = cleanTitle,
                    spokenText = cleanSpoken,
                    isHindi = isHindi
                )
            } else {
                reply
            }
        }
        saveQuickReplies(list)
    }

    fun deleteReply(id: String) {
        val list = _quickReplies.value.filter { it.id != id }
        saveQuickReplies(list)
    }

    fun resetToDefault() {
        val list = QuickReply.DEFAULT_REPLIES
        saveQuickReplies(list)
    }

    companion object {
        private const val TAG = "QuickReplyManager"
        private const val PREFS_NAME = "quick_replies_prefs"
        private const val KEY_QUICK_REPLIES = "saved_quick_replies_json"
    }
}
