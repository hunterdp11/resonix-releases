package com.callscreener.app.data

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.ContentValues
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
import androidx.compose.runtime.mutableIntStateOf

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

        val cleanPhone = contact.number.replace(Regex("[^0-9+]"), "")
        if (cleanPhone.contains(cleanQuery)) return true

        val nameT9 = nameToT9(contact.name)
        if (nameT9.contains(cleanQuery)) return true

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

    fun prewarmCache(context: Context) {
        val appContext = context.applicationContext ?: context
        if (inMemoryContactsCache == null) {
            getCachedContacts(appContext)
        }
        if (inMemoryCallLogsCache == null) {
            getCachedCallLogs(appContext)
        }
    }

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

    fun updateCachedContactPhoto(context: Context, number: String, photoPath: String?) {
        val clean = DIGITS_ONLY_REGEX.replace(number, "")
        val tail7 = if (clean.length >= 7) clean.takeLast(7) else clean
        val updated = inMemoryContactsCache?.map { contact ->
            val cClean = DIGITS_ONLY_REGEX.replace(contact.number, "")
            val match = (cClean.isNotEmpty() && cClean == clean) ||
                    (tail7.length >= 7 && cClean.length >= 7 && cClean.takeLast(7) == tail7)
            if (match) contact.copy(photoUri = photoPath) else contact
        }
        if (updated != null) {
            saveContactsToCache(context, updated)
        }
    }

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

        list.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        Log.i(TAG, "loadContacts: loaded ${list.size} contacts successfully")
        saveContactsToCache(appContext, list)
        list
    }

    fun lookupContactName(context: Context, rawNumber: String?): String? {
        if (rawNumber.isNullOrBlank()) return null
        val cleanNumber = rawNumber.trim()
        val appContext = context.applicationContext ?: context

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

    fun openSystemEditContact(context: Context, contact: ContactItem) {
        val appContext = context.applicationContext ?: context
        try {
            var contactUri: Uri? = null
            val cleanNumber = contact.number.trim()

            // 1. First, search by PhoneLookup to get the true canonical lookup URI
            if (cleanNumber.isNotBlank()) {
                try {
                    val lookupFilterUri = Uri.withAppendedPath(
                        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                        Uri.encode(cleanNumber)
                    )
                    val projection = arrayOf(
                        ContactsContract.PhoneLookup._ID,
                        ContactsContract.PhoneLookup.LOOKUP_KEY
                    )
                    appContext.contentResolver.query(lookupFilterUri, projection, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val cId = cursor.getLong(0)
                            val lookupKey = cursor.getString(1)
                            if (cId > 0 && !lookupKey.isNullOrBlank()) {
                                contactUri = ContactsContract.Contacts.getLookupUri(cId, lookupKey)
                            } else if (cId > 0) {
                                contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, cId)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "PhoneLookup failed in openSystemEditContact", e)
                }
            }

            // 2. If PhoneLookup didn't find it, try contact.id
            if (contactUri == null && contact.id > 0) {
                try {
                    val testUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.id)
                    appContext.contentResolver.query(testUri, arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY), null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val lookupKey = cursor.getString(1)
                            contactUri = if (!lookupKey.isNullOrBlank()) {
                                ContactsContract.Contacts.getLookupUri(contact.id, lookupKey)
                            } else {
                                testUri
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Contacts query failed in openSystemEditContact", e)
                }

                if (contactUri == null) {
                    try {
                        val dataUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, contact.id)
                        appContext.contentResolver.query(dataUri, arrayOf(ContactsContract.Data.CONTACT_ID), null, null, null)?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val realContactId = cursor.getLong(0)
                                if (realContactId > 0) {
                                    contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, realContactId)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Data query failed in openSystemEditContact", e)
                    }
                }
            }

            // 3. Launch Intent with fallback
            if (contactUri != null) {
                try {
                    val editIntent = Intent(Intent.ACTION_EDIT).apply {
                        setDataAndType(contactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(editIntent)
                    return
                } catch (e1: Exception) {
                    Log.w(TAG, "ACTION_EDIT with setDataAndType failed, trying with setData only", e1)
                }

                try {
                    val editIntent2 = Intent(Intent.ACTION_EDIT, contactUri).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(editIntent2)
                    return
                } catch (e2: Exception) {
                    Log.w(TAG, "ACTION_EDIT with setData failed, trying ACTION_VIEW", e2)
                }

                try {
                    val viewIntent = Intent(Intent.ACTION_VIEW, contactUri).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(viewIntent)
                    return
                } catch (e3: Exception) {
                    Log.w(TAG, "ACTION_VIEW failed, falling back to openSystemSaveContact", e3)
                }
            }

            // If contactUri is null or all edit intents failed, fall back to insert/edit
            openSystemSaveContact(context, contact.number, contact.name)
        } catch (e: Exception) {
            Log.e(TAG, "openSystemEditContact completely failed", e)
            openSystemSaveContact(context, contact.number, contact.name)
        }
    }

    suspend fun saveNewContact(context: Context, displayName: String, phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext ?: context
        val trimmedName = displayName.trim()
        val trimmedNumber = phoneNumber.trim()
        if (trimmedNumber.isBlank()) return@withContext false

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

    suspend fun setContactStarred(context: Context, contactId: Long, phoneNumber: String, starred: Boolean): Boolean = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext ?: context
        var providerSuccess = false

        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val values = ContentValues().apply {
                    put(ContactsContract.Contacts.STARRED, if (starred) 1 else 0)
                }

                if (contactId > 0) {
                    val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
                    val rows = appContext.contentResolver.update(contactUri, values, null, null)
                    if (rows > 0) {
                        providerSuccess = true
                        Log.i(TAG, "setContactStarred: Updated contact $contactId to starred=$starred via ID")
                    }
                }

                if (!providerSuccess && phoneNumber.isNotBlank()) {
                    val clean = phoneNumber.trim()
                    val lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(clean))
                    val lookupCursor = appContext.contentResolver.query(
                        lookupUri,
                        arrayOf(ContactsContract.PhoneLookup._ID),
                        null,
                        null,
                        null
                    )
                    var foundId: Long? = null
                    lookupCursor?.use { c ->
                        if (c.moveToFirst()) {
                            foundId = c.getLong(0)
                        }
                    }
                    if (foundId != null && foundId!! > 0) {
                        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, foundId!!)
                        val rows = appContext.contentResolver.update(contactUri, values, null, null)
                        if (rows > 0) {
                            providerSuccess = true
                            Log.i(TAG, "setContactStarred: Updated contact $foundId to starred=$starred via PhoneLookup")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "setContactStarred: Error updating contacts provider", e)
            }
        } else {
            Log.w(TAG, "setContactStarred: WRITE_CONTACTS permission not granted, updating local cache only")
        }

        try {
            val normTarget = PHONE_CLEAN_REGEX.replace(phoneNumber, "")
            val currentList = inMemoryContactsCache ?: getCachedContacts(appContext)
            val updatedList = currentList.map { item ->
                val normItem = PHONE_CLEAN_REGEX.replace(item.number, "")
                val isMatch = (contactId > 0 && item.id == contactId) ||
                        (normTarget.isNotEmpty() && (normItem == normTarget || (normTarget.length >= 7 && normItem.endsWith(normTarget.takeLast(7)))))
                if (isMatch) {
                    item.copy(isStarred = starred)
                } else {
                    item
                }
            }
            saveContactsToCache(appContext, updatedList)
        } catch (cacheEx: Exception) {
            Log.e(TAG, "setContactStarred: Error updating memory/disk cache", cacheEx)
        }

        true
    }

    // --- Call Log Deletion ---

    suspend fun deleteCallLogs(context: Context, logIds: Collection<Long>): Boolean = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext ?: context
        if (logIds.isEmpty()) return@withContext true
        val hasPermission = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.WRITE_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                val idList = logIds.toList()
                for (chunk in idList.chunked(50)) {
                    val inClause = chunk.joinToString(",")
                    val where = "${CallLog.Calls._ID} IN ($inClause)"
                    val deleted = appContext.contentResolver.delete(CallLog.Calls.CONTENT_URI, where, null)
                    Log.i(TAG, "deleteCallLogs: deleted $deleted rows from CallLog provider")
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteCallLogs: Error deleting from CallLog provider", e)
            }
        } else {
            Log.w(TAG, "deleteCallLogs: WRITE_CALL_LOG permission not granted, updating cache only")
        }

        try {
            val idSet = logIds.toSet()
            val currentList = inMemoryCallLogsCache ?: getCachedCallLogs(appContext)
            val updatedList = currentList.filter { it.id !in idSet }
            saveCallLogsToCache(appContext, updatedList)
        } catch (e: Exception) {
            Log.e(TAG, "deleteCallLogs: Error updating cache", e)
        }
        true
    }

    suspend fun deleteCallLogsForNumber(context: Context, number: String): Boolean = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext ?: context
        val clean = number.trim()
        if (clean.isBlank()) return@withContext false
        val hasPermission = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.WRITE_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                val digits = DIGITS_ONLY_REGEX.replace(clean, "")
                val where = "${CallLog.Calls.NUMBER} = ? OR ${CallLog.Calls.NUMBER} = ?"
                val deleted = appContext.contentResolver.delete(CallLog.Calls.CONTENT_URI, where, arrayOf(clean, digits))
                Log.i(TAG, "deleteCallLogsForNumber: deleted $deleted rows for $clean")
            } catch (e: Exception) {
                Log.e(TAG, "deleteCallLogsForNumber: Error", e)
            }
        }

        try {
            val normTarget = DIGITS_ONLY_REGEX.replace(clean, "")
            val currentList = inMemoryCallLogsCache ?: getCachedCallLogs(appContext)
            val updatedList = currentList.filter { log ->
                val logNorm = DIGITS_ONLY_REGEX.replace(log.number, "")
                if (normTarget.length >= 7 && logNorm.length >= 7) {
                    !logNorm.endsWith(normTarget.takeLast(7)) && !normTarget.endsWith(logNorm.takeLast(7))
                } else {
                    logNorm != normTarget && log.number != clean
                }
            }
            saveCallLogsToCache(appContext, updatedList)
        } catch (e: Exception) {
            Log.e(TAG, "deleteCallLogsForNumber: Error updating cache", e)
        }
        true
    }

    suspend fun clearAllCallLogs(context: Context): Boolean = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext ?: context
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            try {
                val deleted = appContext.contentResolver.delete(CallLog.Calls.CONTENT_URI, null, null)
                Log.i(TAG, "clearAllCallLogs: deleted $deleted rows")
            } catch (e: Exception) {
                Log.e(TAG, "clearAllCallLogs: Error", e)
            }
        }
        saveCallLogsToCache(appContext, emptyList())
        true
    }

    // --- Contact Deletion ---

    suspend fun deleteContacts(context: Context, contactsToDelete: Collection<ContactItem>): Boolean = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext ?: context
        if (contactsToDelete.isEmpty()) return@withContext true

        val hasWritePermission = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasWritePermission) {
            for (item in contactsToDelete) {
                try {
                    var deleted = 0
                    if (item.id > 0) {
                        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, item.id)
                        deleted = appContext.contentResolver.delete(contactUri, null, null)
                        appContext.contentResolver.delete(
                            ContactsContract.RawContacts.CONTENT_URI,
                            "${ContactsContract.RawContacts.CONTACT_ID} = ?",
                            arrayOf(item.id.toString())
                        )
                    }
                    if (item.number.isNotBlank()) {
                        val clean = item.number.trim()
                        val lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(clean))
                        appContext.contentResolver.query(lookupUri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)?.use { cursor ->
                            while (cursor.moveToNext()) {
                                val foundId = cursor.getLong(0)
                                if (foundId > 0) {
                                    val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, foundId)
                                    appContext.contentResolver.delete(uri, null, null)
                                    appContext.contentResolver.delete(
                                        ContactsContract.RawContacts.CONTENT_URI,
                                        "${ContactsContract.RawContacts.CONTACT_ID} = ?",
                                        arrayOf(foundId.toString())
                                    )
                                }
                            }
                        }
                    }
                    Log.i(TAG, "deleteContacts: deleted contact ${item.name} ($deleted rows)")
                } catch (e: Exception) {
                    Log.e(TAG, "deleteContacts: Error deleting contact ${item.name}", e)
                }
            }
        } else {
            Log.w(TAG, "deleteContacts: WRITE_CONTACTS permission not granted, updating cache only")
        }

        // Clean up caller profile data
        for (item in contactsToDelete) {
            CallerProfileManager.deleteProfile(appContext, item.number)
        }

        try {
            val idsToDelete = contactsToDelete.map { it.id }.toSet()
            val numbersToDelete = contactsToDelete.map { DIGITS_ONLY_REGEX.replace(it.number, "") }.filter { it.isNotEmpty() }.toSet()
            val currentList = inMemoryContactsCache ?: getCachedContacts(appContext)
            val updatedList = currentList.filter { item ->
                val itemNorm = DIGITS_ONLY_REGEX.replace(item.number, "")
                item.id !in idsToDelete && (itemNorm.isEmpty() || itemNorm !in numbersToDelete)
            }
            saveContactsToCache(appContext, updatedList)
        } catch (e: Exception) {
            Log.e(TAG, "deleteContacts: Error updating cache", e)
        }
        true
    }

    // --- Quick Access / Frequent Contacts Exclusion ---

    private const val PREFS_QUICK_ACCESS = "quick_access_prefs"
    private const val KEY_EXCLUDED_NUMBERS = "excluded_quick_access_numbers"
    val quickAccessVersionState = mutableIntStateOf(0)

    fun isQuickAccessExcluded(context: Context, number: String): Boolean {
        val clean = DIGITS_ONLY_REGEX.replace(number, "")
        if (clean.isBlank()) return false
        val prefs = context.getSharedPreferences(PREFS_QUICK_ACCESS, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_EXCLUDED_NUMBERS, null) ?: return false
        val tail10 = if (clean.length >= 10) clean.takeLast(10) else clean
        val tail7 = if (clean.length >= 7) clean.takeLast(7) else clean
        return set.contains(clean) || set.contains(tail10) || set.contains(tail7) ||
                (tail10.length >= 10 && set.any { it.endsWith(tail10) || tail10.endsWith(it) }) ||
                (tail7.length >= 7 && set.any { it.endsWith(tail7) || tail7.endsWith(it) })
    }

    fun setQuickAccessExcluded(context: Context, number: String, excluded: Boolean) {
        val clean = DIGITS_ONLY_REGEX.replace(number, "")
        if (clean.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_QUICK_ACCESS, Context.MODE_PRIVATE)
        val oldSet = prefs.getStringSet(KEY_EXCLUDED_NUMBERS, null) ?: emptySet()
        val set = HashSet(oldSet)
        val tail10 = if (clean.length >= 10) clean.takeLast(10) else clean
        val tail7 = if (clean.length >= 7) clean.takeLast(7) else clean
        if (excluded) {
            set.add(clean)
            if (tail10.length >= 10) set.add(tail10)
            if (tail7.length >= 7) set.add(tail7)
        } else {
            set.remove(clean)
            set.remove(tail10)
            set.remove(tail7)
            if (tail7.length >= 7) {
                set.removeAll { it.endsWith(tail7) || tail7.endsWith(it) }
            }
        }
        prefs.edit().putStringSet(KEY_EXCLUDED_NUMBERS, set).apply()
        quickAccessVersionState.intValue++
    }
}
