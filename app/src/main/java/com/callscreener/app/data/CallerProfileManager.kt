package com.callscreener.app.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * Caller Profile Model:
 * Holds custom caller metadata, screening persona, screening notes, bypass rules,
 * and custom gallery contact photo path.
 */
data class CallerProfile(
    val category: String = "None",
    val note: String = "",                  // Custom caller context or instructions (e.g. "Ask if project related")
    val assignedPersona: String = "",       // Voice persona name (e.g. "Samantha", "Jarvis")
    val autoScreen: Boolean = false,        // Always auto-screen before ringing
    val isVipBypass: Boolean = false,       // VIP: Skip screening and ring directly
    val customPhotoPath: String? = null,    // Persistent local photo file path from gallery
    val customName: String? = null          // Saved custom contact name or display nickname
)

/**
 * Fast LRU bitmap cache for zero-jank 120Hz contact avatar rendering.
 */
object ContactPhotoCache {
    private val memoryCache = object : LruCache<String, Bitmap>(80) {}

    fun get(path: String): Bitmap? = memoryCache.get(path)

    fun put(path: String, bitmap: Bitmap) {
        memoryCache.put(path, bitmap)
    }

    fun remove(path: String) {
        memoryCache.remove(path)
    }

    fun clear() {
        memoryCache.evictAll()
    }
}

/**
 * Composable helper with zero-jank in-memory caching to asynchronously load and cache contact bitmaps.
 * Supports both local cached file paths and Android content:// provider URIs.
 */
@Composable
fun rememberContactPhotoBitmap(photoPath: String?): ImageBitmap? {
    if (photoPath.isNullOrBlank()) return null
    val context = androidx.compose.ui.platform.LocalContext.current
    var bitmap by remember(photoPath) {
        val cached = ContactPhotoCache.get(photoPath)
        mutableStateOf(cached?.asImageBitmap())
    }
    LaunchedEffect(photoPath) {
        if (bitmap == null) {
            val loaded = withContext(Dispatchers.IO) {
                try {
                    if (photoPath.startsWith("content://")) {
                        context.contentResolver.openInputStream(Uri.parse(photoPath))?.use { input ->
                            val decoded = BitmapFactory.decodeStream(input)
                            if (decoded != null) {
                                ContactPhotoCache.put(photoPath, decoded)
                                decoded.asImageBitmap()
                            } else null
                        }
                    } else {
                        val file = File(photoPath)
                        if (file.exists() && file.length() > 0) {
                            val decoded = BitmapFactory.decodeFile(file.absolutePath)
                            if (decoded != null) {
                                ContactPhotoCache.put(photoPath, decoded)
                                decoded.asImageBitmap()
                            } else null
                        } else null
                    }
                } catch (_: Exception) {
                    null
                }
            }
            if (loaded != null) {
                bitmap = loaded
            }
        }
    }
    return bitmap
}

/**
 * Thread-safe persistent manager for manual caller profiles and custom contact photos.
 */
object CallerProfileManager {
    private const val TAG = "CallerProfileManager"
    private const val PREFS_NAME = "call_screener_caller_profiles"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun normalizeNumber(number: String): String {
        val clean = number.filter { it.isDigit() }
        return if (clean.length >= 10) clean.takeLast(10) else clean
    }

    fun getProfile(context: Context, number: String): CallerProfile? {
        val key = normalizeNumber(number)
        if (key.isBlank()) return null
        val raw = getPrefs(context).getString(key, null) ?: return null
        return try {
            val json = JSONObject(raw)
            CallerProfile(
                category = json.optString("category", "None"),
                note = json.optString("note", ""),
                assignedPersona = json.optString("assignedPersona", ""),
                autoScreen = json.optBoolean("autoScreen", false),
                isVipBypass = json.optBoolean("isVipBypass", false),
                customPhotoPath = json.optString("customPhotoPath", "").ifEmpty { null },
                customName = json.optString("customName", "").ifEmpty { null }
            )
        } catch (_: Exception) {
            null
        }
    }

    fun saveProfile(context: Context, number: String, profile: CallerProfile) {
        val key = normalizeNumber(number)
        if (key.isBlank()) return
        val json = JSONObject().apply {
            put("category", profile.category)
            put("note", profile.note)
            put("assignedPersona", profile.assignedPersona)
            put("autoScreen", profile.autoScreen)
            put("isVipBypass", profile.isVipBypass)
            if (!profile.customPhotoPath.isNullOrBlank()) {
                put("customPhotoPath", profile.customPhotoPath)
            }
            if (!profile.customName.isNullOrBlank()) {
                put("customName", profile.customName)
            }
        }
        getPrefs(context).edit().putString(key, json.toString()).apply()
    }

    fun getCustomName(context: Context, number: String): String? {
        return getProfile(context, number)?.customName
    }

    fun saveCustomName(context: Context, number: String, name: String) {
        val existing = getProfile(context, number) ?: CallerProfile()
        saveProfile(context, number, existing.copy(customName = name.trim()))
    }

    fun deleteProfile(context: Context, number: String) {
        val key = normalizeNumber(number)
        if (key.isBlank()) return
        removeCustomPhoto(context, number)
        getPrefs(context).edit().remove(key).apply()
    }

    fun getContactPhotoFile(context: Context, number: String): File {
        val key = normalizeNumber(number)
        val dir = File(context.filesDir, "contact_photos").apply { if (!exists()) mkdirs() }
        return File(dir, "avatar_${key}.jpg")
    }

    fun getCustomPhotoPath(context: Context, number: String): String? {
        val key = normalizeNumber(number)
        if (key.isBlank()) return null
        val profile = getProfile(context, number)
        if (!profile?.customPhotoPath.isNullOrBlank()) {
            val file = File(profile!!.customPhotoPath!!)
            if (file.exists() && file.length() > 0) return file.absolutePath
        }
        val defaultFile = getContactPhotoFile(context, number)
        return if (defaultFile.exists() && defaultFile.length() > 0) defaultFile.absolutePath else null
    }

    suspend fun saveCustomPhotoFromUri(context: Context, number: String, imageUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val key = normalizeNumber(number)
            if (key.isBlank()) return@withContext null
            val targetFile = getContactPhotoFile(context, number)

            context.contentResolver.openInputStream(imageUri)?.use { input ->
                val bytes = input.readBytes()
                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOpts)

                val targetDim = 512
                var sampleSize = 1
                while (boundsOpts.outWidth / (sampleSize * 2) >= targetDim &&
                    boundsOpts.outHeight / (sampleSize * 2) >= targetDim
                ) {
                    sampleSize *= 2
                }

                val decodeOpts = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
                if (original != null) {
                    val dim = minOf(original.width, original.height)
                    val x = (original.width - dim) / 2
                    val y = (original.height - dim) / 2
                    val square = Bitmap.createBitmap(original, x, y, dim, dim)
                    val scaled = if (dim > targetDim) {
                        Bitmap.createScaledBitmap(square, targetDim, targetDim, true)
                    } else {
                        square
                    }

                    FileOutputStream(targetFile).use { out ->
                        scaled.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    }

                    // Update memory cache immediately
                    ContactPhotoCache.put(targetFile.absolutePath, scaled)

                    if (square != original && square != scaled) square.recycle()
                    if (original != scaled) original.recycle()

                    val path = targetFile.absolutePath
                    val current = getProfile(context, number) ?: CallerProfile()
                    saveProfile(context, number, current.copy(customPhotoPath = path))
                    Log.i(TAG, "Saved custom photo for $number to $path")
                    return@withContext path
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving custom contact photo for $number", e)
        }
        null
    }

    fun removeCustomPhoto(context: Context, number: String) {
        try {
            val file = getContactPhotoFile(context, number)
            if (file.exists()) {
                ContactPhotoCache.remove(file.absolutePath)
                file.delete()
            }
            val current = getProfile(context, number)
            if (current != null) {
                saveProfile(context, number, current.copy(customPhotoPath = null))
            }
            Log.i(TAG, "Removed custom photo for $number")
        } catch (e: Exception) {
            Log.e(TAG, "Failed removing custom photo for $number", e)
        }
    }
}
