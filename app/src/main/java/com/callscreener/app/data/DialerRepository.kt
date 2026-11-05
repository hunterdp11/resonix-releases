package com.callscreener.app.data

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ContactItem(
    val id: Long,
    val name: String,
    val number: String,
    val photoUri: String? = null
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

    suspend fun loadContacts(context: Context): List<ContactItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<ContactItem>()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return@withContext list
        }

        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            )
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} COLLATE NOCASE ASC"
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                val seenNumbers = HashSet<String>()

                while (it.moveToNext()) {
                    val id = if (idIdx >= 0) it.getLong(idIdx) else 0L
                    val name = if (nameIdx >= 0) it.getString(nameIdx) ?: "Unknown" else "Unknown"
                    val number = if (numIdx >= 0) it.getString(numIdx) ?: "" else ""
                    val photo = if (photoIdx >= 0) it.getString(photoIdx) else null

                    val normalizedNum = number.replace(Regex("[^0-9+]"), "")
                    if (normalizedNum.isNotBlank() && !seenNumbers.contains(normalizedNum)) {
                        seenNumbers.add(normalizedNum)
                        list.add(ContactItem(id, name, number, photo))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading contacts", e)
        }
        list
    }

    /**
     * Fast, indexed contact lookup by phone number using Android's native ContactsContract.PhoneLookup index.
     * Returns the user's saved contact name from the device phonebook, or null if unsaved.
     */
    fun lookupContactName(context: Context, rawNumber: String?): String? {
        if (rawNumber.isNullOrBlank()) return null
        val cleanNumber = rawNumber.trim()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(cleanNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
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
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return@withContext list
        }

        // Fast lookup map for reverse phone -> contact name
        val contactsMap = contacts.associateBy(
            keySelector = { it.number.replace(Regex("[^0-9]"), "") },
            valueTransform = { it.name }
        )

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

                    // Prioritize phonebook saved contact name; fall back to carrier CACHED_NAME ONLY for unsaved contacts
                    val norm = number.replace(Regex("[^0-9]"), "")
                    val savedContactName = contactsMap[norm] ?: if (norm.length >= 7) {
                        val tail7 = norm.takeLast(7)
                        contacts.firstOrNull { c ->
                            val cNorm = c.number.replace(Regex("[^0-9]"), "")
                            cNorm.length >= 7 && (cNorm.endsWith(tail7) || norm.endsWith(cNorm.takeLast(7)))
                        }?.name
                    } else null

                    val resolvedName = if (!savedContactName.isNullOrBlank()) {
                        savedContactName
                    } else if (!rawName.isNullOrBlank()) {
                        rawName
                    } else {
                        null
                    }

                    list.add(CallLogItem(id, resolvedName, number, type, date, duration))
                    count++
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading call logs", e)
        }
        list
    }

    fun placeCall(
        context: Context,
        rawNumber: String,
        isSavedContact: Boolean = false,
        forceIncognito: Boolean? = null
    ) {
        var cleanNumber = rawNumber.filter { it.isDigit() || it == '+' || it == '*' || it == '#' || it == ',' || it == ';' }
        if (cleanNumber.isBlank()) return

        // Resolve CLIR (#31#) Caller ID Masking
        val sessionManager = com.callscreener.app.session.CallSessionManager.getInstance(context)
        val sessionState = sessionManager.sessionState.value

        val bareDigits = cleanNumber.removePrefix("+").removePrefix("#31#")
        val isEmergency = setOf("100", "101", "102", "108", "112", "911", "999", "000").contains(bareDigits)

        val shouldInjectClir = !isEmergency && (
            forceIncognito == true ||
            (forceIncognito == null && sessionState.isIncognitoDialActive) ||
            (sessionState.isClirMaskingEnabled && (!isSavedContact || sessionState.isClirMaskSavedContactsEnabled))
        )

        if (shouldInjectClir) {
            if (!cleanNumber.startsWith("#31#")) {
                cleanNumber = "#31#$cleanNumber"
            }
            if (sessionState.isIncognitoDialActive) {
                sessionManager.setIncognitoDialActive(false)
            }
            Toast.makeText(context, "🕵️ Private Call: Caller ID masked (#31#)", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "Placed call with CLIR #31# mask: $cleanNumber (isSavedContact: $isSavedContact)")
        }

        // 1. If CLIR #31# is present and Root is available, execute via CALL_PRIVILEGED
        // Root (UID 0) has CALL_PRIVILEGED authority which prevents Android framework from stripping #31#
        if (cleanNumber.contains("#")) {
            try {
                val encodedForShell = Uri.encode(cleanNumber)
                val cmd = "am start -a android.intent.action.CALL_PRIVILEGED -d 'tel:$encodedForShell' 2>/dev/null || am start -a android.intent.action.CALL -d 'tel:$encodedForShell' 2>/dev/null"
                val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
                val exitCode = p.waitFor()
                if (exitCode == 0) {
                    Log.i(TAG, "Privileged root call command executed successfully with code: $cleanNumber")
                    return
                }
            } catch (e: Exception) {
                Log.w(TAG, "Root privileged call invocation failed, falling back to standard intent", e)
            }
        }

        val uri = Uri.fromParts("tel", cleanNumber, null)

        // 2. Try privileged intent directly (works when mounted in /system/priv-app)
        try {
            val privIntent = Intent("android.intent.action.CALL_PRIVILEGED", uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(privIntent)
            return
        } catch (_: Exception) {}

        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
        val hasCallPermission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        // 3. If this app is Default Dialer (ROLE_DIALER), place call directly via TelecomManager
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

        // 4. Direct cellular call via Intent.ACTION_CALL
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

        // 5. Fallback to ACTION_DIAL
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
}

