package com.callscreener.app.data

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentProviderOperation
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ContactItem(
    val id: Long,
    val name: String,
    val number: String,
    val photoUri: String? = null,
    val isStarred: Boolean = false
)

data class CallLogItem(
    val id: Long,
    val name: String?,
    val number: String,
    val type: Int,
    val timestamp: Long,
    val durationSeconds: Long
)

object T9Helper {
    private val charToDigitMap = mapOf(
        'a' to '2', 'b' to '2', 'c' to '2',
        'd' to '3', 'e' to '3', 'f' to '3',
        'g' to '4', 'h' to '4', 'i' to '4',
        'j' to '5', 'k' to '5', 'l' to '5',
        'm' to '6', 'n' to '6', 'o' to '6',
        'p' to '7', 'q' to '7', 'r' to '7', 's' to '7',
        't' to '8', 'u' to '8', 'v' to '8',
        'w' to '9', 'x' to '9', 'y' to '9', 'z' to '9'
    )

    fun nameToT9(name: String): String {
        val sb = StringBuilder()
        for (ch in name.lowercase()) {
            val digit = charToDigitMap[ch]
            if (digit != null) {
                sb.append(digit)
            }
        }
        return sb.toString()
    }

    fun matches(contact: ContactItem, query: String): Boolean {
        val cleanQuery = query.trim().replace(Regex("[^0-9*#+]"), "")
        if (cleanQuery.isBlank()) return false

        // 1. Phone number match
        val cleanPhone = contact.number.replace(Regex("[^0-9+]"), "")
        if (cleanPhone.contains(cleanQuery)) return true

        // 2. Full Name T9 match
        val nameT9 = nameToT9(contact.name)
        if (nameT9.contains(cleanQuery)) return true

        // 3. Match beginning of any word (e.g. "D" in "John Doe")
        val words = contact.name.lowercase().split(Regex("[\\s-]+")).filter { it.isNotBlank() }
        for (word in words) {
            val wordT9 = nameToT9(word)
            if (wordT9.startsWith(cleanQuery)) return true
        }

        return false
    }
}

object DialerRepository {
    private const val TAG = "DialerRepository"
    private val DIGITS_ONLY_REGEX = Regex("[^0-9]")
    private val PHONE_CLEAN_REGEX = Regex("[^0-9+]")

    @Volatile
    private var inMemoryContactsCache: List<ContactItem>? = null
    @Volatile
    private var inMemoryCallLogsCache: List<CallLogItem>? = null

    /**
     * Prewarms contacts and call logs into memory synchronously on application start (taking 2-5ms)
     * so UI components can access them immediately on the first frame of cold start.
     */
    fun prewarmCache(context: Context) {
        val appContext = context.applicationContext ?: context
        if (inMemoryContactsCache == null) {
            getCachedContacts(appContext)
        }
        if (inMemoryCallLogsCache == null) {
            getCachedCallLogs(appContext)
        }
    }

    /**
     * Instant synchronous read of cached contacts (RAM first, fallback to disk JSON cache).
     */
    fun getCachedContacts(context: Context): List<ContactItem> {
        inMemoryContactsCache?.let { return it }
        val appContext = context.applicationContext ?: context
        try {
            val file = File(appContext.filesDir, "cached_contacts.json")
            if (file.exists()) {
                val text = file.readText()
                if (text.isNotBlank()) {
                    val jsonArray = JSONArray(text)
                    val list = ArrayList<ContactItem>(jsonArray.length())
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            ContactItem(
                                id = obj.optLong("id", 0L),
                                name = obj.optString("name", "Unknown"),
                                number = obj.optString("number", ""),
                                photoUri = if (obj.has("photo")) obj.getString("photo") else null,
                                isStarred = obj.optBoolean("starred", false)
                            )
                        )
                    }
                    inMemoryContactsCache = list
                    return list
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading contacts from cache", e)
        }
        return emptyList()
    }

    /**
     * Persists fresh contacts to RAM and disk cache.
     */
    fun saveContactsToCache(context: Context, contacts: List<ContactItem>) {
        inMemoryContactsCache = contacts
        val appContext = context.applicationContext ?: context
        try {
            val file = File(appContext.filesDir, "cached_contacts.json")
            val jsonArray = JSONArray()
            for (item in contacts) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("name", item.name)
                obj.put("number", item.number)
                if (item.photoUri != null) obj.put("photo", item.photoUri)
                obj.put("starred", item.isStarred)
                jsonArray.put(obj)
            }
            file.writeText(jsonArray.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error saving contacts to cache", e)
        }
    }

    /**
     * Instant synchronous read of cached call logs (RAM first, fallback to disk JSON cache).
     */
    fun getCachedCallLogs(context: Context): List<CallLogItem> {
        inMemoryCallLogsCache?.let { return it }
        val appContext = context.applicationContext ?: context
        try {
            val file = File(appContext.filesDir, "cached_call_logs.json")
            if (file.exists()) {
                val text = file.readText()
                if (text.isNotBlank()) {
                    val jsonArray = JSONArray(text)
                    val list = ArrayList<CallLogItem>(jsonArray.length())
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            CallLogItem(
                                id = obj.optLong("id", 0L),
                                name = if (obj.has("name")) obj.getString("name") else null,
                                number = obj.optString("number", ""),
                                type = obj.optInt("type", 1),
                                timestamp = obj.optLong("date", 0L),
                                durationSeconds = obj.optLong("duration", 0L)
                            )
                        )
                    }
                    inMemoryCallLogsCache = list
                    return list
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading call logs from cache", e)
        }
        return emptyList()
    }

    /**
     * Persists fresh call logs to RAM and disk cache.
     */
    fun saveCallLogsToCache(context: Context, logs: List<CallLogItem>) {
        inMemoryCallLogsCache = logs
        val appContext = context.applicationContext ?: context
        try {
            val file = File(appContext.filesDir, "cached_call_logs.json")
            val jsonArray = JSONArray()
            for (item in logs.take(150)) {
                val obj = JSONObject()
                obj.put("id", item.id)
                if (item.name != null) obj.put("name", item.name)
                obj.put("number", item.number)
                obj.put("type", item.type)
                obj.put("date", item.timestamp)
                obj.put("duration", item.durationSeconds)
                jsonArray.put(obj)
            }
            file.writeText(jsonArray.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error saving call logs to cache", e)
        }
    }

    suspend fun loadContacts(context: Context): List<ContactItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<ContactItem>()
        val appContext = context.applicationContext ?: context
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "loadContacts: READ_CONTACTS permission NOT granted")
            return@withContext list
        }

        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                ContactsContract.CommonDataKinds.Phone.STARRED
            )

            // Try first with safe DISPLAY_NAME ASC; if query throws (some OEM ROMs restrict sort order), fall back to null
            var cursor: android.database.Cursor? = null
            try {
                cursor = appContext.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    null,
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
                )
            } catch (sortEx: Exception) {
                Log.w(TAG, "Sort order query failed, retrying without sort order", sortEx)
                cursor = appContext.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
                )
            }

            cursor?.use {
                val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
                val contactIdIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                val starredIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)

                val seenKeys = HashSet<String>()

                while (it.moveToNext()) {
                    val id = if (idIdx >= 0) it.getLong(idIdx) else 0L
                    val contactId = if (contactIdIdx >= 0) it.getLong(contactIdIdx) else id
                    val rawName = if (nameIdx >= 0) it.getString(nameIdx) else null
                    val rawNum = if (numIdx >= 0) it.getString(numIdx) else null
                    val photo = if (photoIdx >= 0) it.getString(photoIdx) else null
                    val isStarred = if (starredIdx >= 0) it.getInt(starredIdx) == 1 else false

                    val name = if (!rawName.isNullOrBlank()) rawName.trim() else "Unknown"
                    val number = rawNum?.trim() ?: ""

                    if (number.isBlank() && (name.isBlank() || name == "Unknown")) {
                        continue
                    }

                    val normalizedNum = PHONE_CLEAN_REGEX.replace(number, "")
                    val primaryId = if (contactId > 0) contactId else id
                    val uniqueRowId = if (id > 0) id else primaryId
                    val dedupKey = "$primaryId:$normalizedNum"
                    if (!seenKeys.contains(dedupKey)) {
                        seenKeys.add(dedupKey)
                        val customPhoto = CallerProfileManager.getCustomPhotoPath(appContext, number)
                        list.add(ContactItem(id = uniqueRowId, name = name, number = number, photoUri = customPhoto ?: photo, isStarred = isStarred))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading contacts from CommonDataKinds.Phone", e)
        }

        // Secondary fallback: if Phone.CONTENT_URI yielded 0 items, query ContactsContract.Contacts directly
        if (list.isEmpty()) {
            try {
                Log.i(TAG, "Phone table empty or failed, trying fallback to ContactsContract.Contacts")
                val contactsProjection = arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.PHOTO_URI
                )
                val contactsCursor = appContext.contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    contactsProjection,
                    null,
                    null,
                    null
                )
                contactsCursor?.use { c ->
                    val idIdx = c.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIdx = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val photoIdx = c.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)

                    while (c.moveToNext()) {
                        val id = if (idIdx >= 0) c.getLong(idIdx) else 0L
                        val name = if (nameIdx >= 0) c.getString(nameIdx) ?: "Unknown" else "Unknown"
                        val photo = if (photoIdx >= 0) c.getString(photoIdx) else null
                        if (name.isNotBlank() && name != "Unknown") {
                            list.add(ContactItem(id = id, name = name.trim(), number = "", photoUri = photo))
                        }
                    }
                }
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Error in fallback contacts query", fallbackEx)
            }
        }

        // Sort in memory with localized case-insensitive collation
        list.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        Log.i(TAG, "loadContacts: loaded ${list.size} contacts successfully")
        saveContactsToCache(appContext, list)
        list
    }

    /**
     * Fast, indexed contact lookup by phone number using Android's native ContactsContract.PhoneLookup index.
     * Returns the user's saved contact name from the device phonebook, or null if unsaved.
     */
    fun lookupContactName(context: Context, rawNumber: String?): String? {
        if (rawNumber.isNullOrBlank()) return null
        val cleanNumber = rawNumber.trim()
        val appContext = context.applicationContext ?: context

        // Check custom name in CallerProfile first (user explicit override)
        val customName = CallerProfileManager.getCustomName(appContext, cleanNumber)
        if (!customName.isNullOrBlank()) {
            return customName
        }

        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(cleanNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            appContext.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx) else null
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up contact for $rawNumber", e)
            null
        }
    }

    suspend fun loadCallLogs(context: Context, contacts: List<ContactItem> = emptyList()): List<CallLogItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CallLogItem>()
        val appContext = context.applicationContext ?: context
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return@withContext list
        }

        // Fast O(1) indexed maps for contact resolution
        val exactContactsMap = HashMap<String, String>(contacts.size)
        val tail7ContactsMap = HashMap<String, String>(contacts.size)

        for (contact in contacts) {
            val clean = DIGITS_ONLY_REGEX.replace(contact.number, "")
            if (clean.isNotEmpty()) {
                exactContactsMap[clean] = contact.name
                if (clean.length >= 7) {
                    val tail7 = clean.takeLast(7)
                    tail7ContactsMap.putIfAbsent(tail7, contact.name)
                }
            }
        }

        try {
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            )
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC"
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(CallLog.Calls._ID)
                val nameIdx = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val numIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
                val durIdx = it.getColumnIndex(CallLog.Calls.DURATION)

                var count = 0
                while (it.moveToNext() && count < 150) {
                    val id = if (idIdx >= 0) it.getLong(idIdx) else 0L
                    val rawName = if (nameIdx >= 0) it.getString(nameIdx) else null
                    val number = if (numIdx >= 0) it.getString(numIdx) ?: "" else ""
                    val type = if (typeIdx >= 0) it.getInt(typeIdx) else CallLog.Calls.INCOMING_TYPE
                    val date = if (dateIdx >= 0) it.getLong(dateIdx) else 0L
                    val duration = if (durIdx >= 0) it.getLong(durIdx) else 0L

                    // Fast O(1) contact name lookup: exact norm match or last 7 digits match
                    val norm = DIGITS_ONLY_REGEX.replace(number, "")
                    val savedContactName = exactContactsMap[norm] ?: if (norm.length >= 7) {
                        tail7ContactsMap[norm.takeLast(7)]
                    } else null

                    val resolvedName = if (!savedContactName.isNullOrBlank()) {
                        savedContactName
                    } else if (!rawName.isNullOrBlank()) {
                        rawName
                    } else {
                        CallerProfileManager.getCustomName(appContext, number)
                    }

                    list.add(CallLogItem(id, resolvedName, number, type, date, duration))
                    count++
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading call logs", e)
        }
        saveCallLogsToCache(appContext, list)
        list
    }

    fun placeCall(context: Context, rawNumber: String) {
        val cleanNumber = rawNumber.filter { it.isDigit() || it == '+' || it == '*' || it == '#' || it == ',' || it == ';' }
        if (cleanNumber.isBlank()) return
        val uri = Uri.fromParts("tel", cleanNumber, null)

        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
        val hasCallPermission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        // 1. If this app is Default Dialer (ROLE_DIALER), place call directly via TelecomManager
        if (hasCallPermission && telecomManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val isDefault = telecomManager.defaultDialerPackage == context.packageName
            if (isDefault) {
                try {
                    telecomManager.placeCall(uri, Bundle())
                    return
                } catch (e: Exception) {
                    Log.e(TAG, "TelecomManager.placeCall failed, falling through to intent", e)
                }
            }
        }

        // 2. Direct cellular call via Intent.ACTION_CALL
        if (hasCallPermission) {
            try {
                val callIntent = Intent(Intent.ACTION_CALL, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(callIntent)
                return
            } catch (e: Exception) {
                Log.e(TAG, "ACTION_CALL failed, falling through to ACTION_DIAL", e)
            }
        }

        // 3. Fallback to ACTION_DIAL
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating call to $cleanNumber", e)
            Toast.makeText(context, "Unable to place call", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendSms(context: Context, rawNumber: String) {
        val cleanNumber = rawNumber.trim()
        if (cleanNumber.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${Uri.encode(cleanNumber)}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching SMS for $cleanNumber", e)
            Toast.makeText(context, "Cannot open messaging app", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied $text", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error copying to clipboard", e)
        }
    }

    fun formatPhoneNumber(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            raw.startsWith("+") && digits.length >= 11 -> {
                "+${digits.take(digits.length - 10)} (${digits.takeLast(10).take(3)}) ${digits.takeLast(7).take(3)}-${digits.takeLast(4)}"
            }
            digits.length == 10 -> {
                "(${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6)}"
            }
            digits.length == 11 && digits.startsWith("1") -> {
                "+1 (${digits.substring(1, 4)}) ${digits.substring(4, 7)}-${digits.substring(7)}"
            }
            else -> raw
        }
    }

    fun maskPhoneNumber(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return if (digits.length >= 4) {
            "•••• ••• ••${digits.takeLast(2)}"
        } else {
            "••••••••••"
        }
    }

    /**
     * Check if a phone number exists in system contacts.
     */
    fun isNumberInContacts(context: Context, phoneNumber: String): Boolean {
        val cleanNumber = phoneNumber.trim()
        if (cleanNumber.isBlank()) return false
        val appContext = context.applicationContext ?: context
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return try {
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(cleanNumber))
            val projection = arrayOf(ContactsContract.PhoneLookup._ID)
            appContext.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
            } ?: false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Launch the system Contacts app to create a new contact with pre-filled phone number and optional name.
     */
    fun openSystemSaveContact(context: Context, phoneNumber: String, initialName: String? = null) {
        val cleanNumber = phoneNumber.trim()
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                type = ContactsContract.RawContacts.CONTENT_TYPE
                putExtra(ContactsContract.Intents.Insert.PHONE, cleanNumber)
                putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                if (!initialName.isNullOrBlank()) {
                    putExtra(ContactsContract.Intents.Insert.NAME, initialName.trim())
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "ACTION_INSERT failed, falling back to ACTION_INSERT_OR_EDIT", e)
            try {
                val fallback = Intent(Intent.ACTION_INSERT_OR_EDIT).apply {
                    type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
                    putExtra(ContactsContract.Intents.Insert.PHONE, cleanNumber)
                    if (!initialName.isNullOrBlank()) {
                        putExtra(ContactsContract.Intents.Insert.NAME, initialName.trim())
                    }
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
            } catch (ex2: Exception) {
                Log.e(TAG, "Failed to launch system contact save intent", ex2)
                Toast.makeText(context, "Could not open Contacts app", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Launch the system Contacts app to add this phone number to an existing contact.
     */
    fun openSystemAddToExistingContact(context: Context, phoneNumber: String) {
        val cleanNumber = phoneNumber.trim()
        try {
            val intent = Intent(Intent.ACTION_INSERT_OR_EDIT).apply {
                type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
                putExtra(ContactsContract.Intents.Insert.PHONE, cleanNumber)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open add to existing contact intent", e)
            Toast.makeText(context, "Could not open Contacts app", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Programmatically insert a new contact into Android's Contacts Provider.
     * Requires WRITE_CONTACTS permission. Always saves in CallerProfile for instant local reflection.
     */
    suspend fun saveNewContact(context: Context, displayName: String, phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext ?: context
        val trimmedName = displayName.trim()
        val trimmedNumber = phoneNumber.trim()
        if (trimmedNumber.isBlank()) return@withContext false

        // Always save to in-app CallerProfile immediately for instant zero-lag UI update
        CallerProfileManager.saveCustomName(appContext, trimmedNumber, trimmedName.ifBlank { trimmedNumber })

        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "saveNewContact: WRITE_CONTACTS permission not granted, saved to CallerProfile only")
            return@withContext true
        }

        try {
            val ops = ArrayList<ContentProviderOperation>()
            val rawContactInsertIndex = ops.size

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            if (trimmedName.isNotBlank()) {
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, trimmedName)
                        .build()
                )
            }

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, trimmedNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
            )

            appContext.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            Log.i(TAG, "saveNewContact: Successfully saved $trimmedName ($trimmedNumber) to contacts provider")
            true
        } catch (e: Exception) {
            Log.e(TAG, "saveNewContact: Error writing to contacts provider", e)
            false
        }
    }
}

