package com.callscreener.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.telecom.TelecomManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.callscreener.app.R
import com.callscreener.app.data.CallerProfileManager
import com.callscreener.app.data.DialerRepository
import com.callscreener.app.receiver.CallNotificationReceiver
import com.callscreener.app.ui.MainActivity
import com.callscreener.app.ui.ScreeningCallActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NotificationHelper {

    const val CHANNEL_MISSED_CALLS = "missed_calls_channel"
    private const val CHANNEL_MISSED_NAME = "Missed Calls"

    const val CHANNEL_INCOMING_CALLS = "incoming_calls_channel"
    private const val CHANNEL_INCOMING_NAME = "Incoming Calls"

    const val CHANNEL_ONGOING_CALLS = "ongoing_calls_channel"
    private const val CHANNEL_ONGOING_NAME = "Active Call"

    const val NOTIFICATION_ID_CALL = 20001

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val missedCallChannel = NotificationChannel(
                CHANNEL_MISSED_CALLS,
                CHANNEL_MISSED_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for unanswered or missed incoming calls"
                enableLights(true)
                lightColor = 0xFFFF3B30.toInt()
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val incomingCallChannel = NotificationChannel(
                CHANNEL_INCOMING_CALLS,
                CHANNEL_INCOMING_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming calls"
                setSound(null, null) // InCallService / RingtonePlayer manages ringtone & vibration directly
                enableVibration(false)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val ongoingCallChannel = NotificationChannel(
                CHANNEL_ONGOING_CALLS,
                CHANNEL_ONGOING_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Status of active in-progress phone call"
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannels(listOf(missedCallChannel, incomingCallChannel, ongoingCallChannel))
        }
    }

    /**
     * Post a rich, interactive Missed Call Notification with 1-tap Call Back, Message & Screen with AI.
     */
    fun postMissedCallNotification(
        context: Context,
        callerNumber: String,
        callerName: String? = null
    ) {
        initNotificationChannels(context)

        val cleanNumber = callerNumber.trim()
        val savedName = DialerRepository.lookupContactName(context, cleanNumber)
        val displayName = if (!savedName.isNullOrBlank()) {
            savedName
        } else if (!callerName.isNullOrBlank() && callerName != "Unknown Caller") {
            callerName
        } else {
            DialerRepository.formatPhoneNumber(cleanNumber)
        }

        val timeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val notificationId = ("missed_" + cleanNumber).hashCode()

        // Content Intent: Open MainActivity directly to Recents Tab
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_TAB", "RECENTS")
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: Call Back Intent
        val callBackIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.fromParts("tel", cleanNumber, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val callBackPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 1,
            callBackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: Send SMS / Message Intent
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$cleanNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val smsPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 2,
            smsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 3: Screen With AI (Direct launch)
        val screenIntent = Intent(context, ScreeningCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_CALLER_NUMBER", cleanNumber)
            putExtra("EXTRA_CALLER_NAME", displayName)
            putExtra("EXTRA_SIMULATED", true)
        }
        val screenPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 3,
            screenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeAvatar = createAvatarBitmap(context, cleanNumber, displayName, isMissed = true)

        val builder = NotificationCompat.Builder(context, CHANNEL_MISSED_CALLS)
            .setSmallIcon(R.drawable.ic_stat_call_missed)
            .setLargeIcon(largeAvatar)
            .setContentTitle("Missed Call")
            .setContentText(displayName)
            .setSubText(timeString)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MISSED_CALL)
            .setColor(0xFFE22B45.toInt()) // Pixel Expressive Vibrant Ruby Red
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_notification_call_back,
                "Call Back",
                callBackPendingIntent
            )
            .addAction(
                R.drawable.ic_notification_message,
                "Message",
                smsPendingIntent
            )
            .addAction(
                R.drawable.ic_notification_screen_ai,
                "Screen with AI",
                screenPendingIntent
            )

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Notification permission missing or revoked on Android 13+
        } catch (_: Exception) {}
    }

    /**
     * Clear missed call notification for a given number.
     */
    fun cancelMissedCallNotification(context: Context, callerNumber: String) {
        val notificationId = ("missed_" + callerNumber.trim()).hashCode()
        try {
            NotificationManagerCompat.from(context).cancel(notificationId)
        } catch (_: Exception) {}
    }

    /**
     * Post an authentic Google Dialer / Android 12+ CallStyle Heads-Up Notification for Incoming Calls.
     * When the user is actively using the phone (interactive & keyguard unlocked), Android OS automatically
     * displays the minimal heads-up call banner at the top of the screen with Decline and Answer buttons.
     * When the phone is locked or screen is off, fullScreenIntent presents the full-screen caller screen.
     * ZERO special overlay permissions (SYSTEM_ALERT_WINDOW) required.
     */
    fun postIncomingCallNotification(
        context: Context,
        callerNumber: String,
        callerName: String? = null,
        hasCapsule: Boolean = false,
        isUserUsingPhone: Boolean = false
    ) {
        initNotificationChannels(context)

        val cleanNumber = callerNumber.trim()
        val savedName = DialerRepository.lookupContactName(context, cleanNumber)
        val displayName = if (!savedName.isNullOrBlank()) {
            savedName
        } else if (!callerName.isNullOrBlank() && callerName != "Unknown Caller") {
            callerName
        } else {
            DialerRepository.formatPhoneNumber(cleanNumber)
        }

        // Tap opens live screening activity
        val contentIntent = Intent(context, ScreeningCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_CALL,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Decline Call
        val declineIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_DECLINE
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID_CALL + 1,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Answer Call Directly
        val answerIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_ANSWER
        }
        val answerPendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID_CALL + 2,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Screen Call with AI
        val screenIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_SCREEN
        }
        val screenPendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID_CALL + 3,
            screenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeAvatar = createAvatarBitmap(context, cleanNumber, displayName, isMissed = false)

        val person = Person.Builder()
            .setName(displayName)
            .setIcon(IconCompat.createWithBitmap(largeAvatar))
            .setUri("tel:$cleanNumber")
            .setImportant(true)
            .build()

        // Authentic Google Dialer & OEM CallStyle banner
        val callStyle = NotificationCompat.CallStyle.forIncomingCall(
            person,
            declinePendingIntent,
            answerPendingIntent
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_INCOMING_CALLS)
            .setSmallIcon(R.drawable.ic_stat_call_incoming)
            .setContentTitle(displayName)
            .setContentText("Incoming call • $cleanNumber")
            .setStyle(callStyle)
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(contentPendingIntent, true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFF1A73E8.toInt()) // Google Blue
            .addAction(
                R.drawable.ic_notification_screen_ai,
                "Screen with AI",
                screenPendingIntent
            )

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID_CALL, builder.build())
        } catch (_: Exception) {}
    }

    /**
     * Post ongoing notification while call is connected/active so user can easily return to the call or end it.
     */
    fun postOngoingCallNotification(
        context: Context,
        callerNumber: String,
        callerName: String? = null,
        isScreening: Boolean = false
    ) {
        initNotificationChannels(context)

        val cleanNumber = callerNumber.trim()
        val savedName = DialerRepository.lookupContactName(context, cleanNumber)
        val displayName = if (!savedName.isNullOrBlank()) {
            savedName
        } else if (!callerName.isNullOrBlank() && callerName != "Unknown Caller") {
            callerName
        } else {
            DialerRepository.formatPhoneNumber(cleanNumber)
        }

        val contentIntent = Intent(context, ScreeningCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_CALL,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val hangupIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_DECLINE
        }
        val hangupPendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID_CALL + 1,
            hangupIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = if (isScreening) "AI Screening in progress..." else "Active phone call"
        val largeAvatar = createAvatarBitmap(context, cleanNumber, displayName, isMissed = false)

        val person = Person.Builder()
            .setName(displayName)
            .setIcon(IconCompat.createWithBitmap(largeAvatar))
            .setUri("tel:$cleanNumber")
            .setImportant(true)
            .build()

        val callStyle = NotificationCompat.CallStyle.forOngoingCall(
            person,
            hangupPendingIntent
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ONGOING_CALLS)
            .setSmallIcon(R.drawable.ic_stat_call_ongoing)
            .setContentTitle(displayName)
            .setContentText(statusText)
            .setStyle(callStyle)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFF1A73E8.toInt())

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID_CALL, builder.build())
        } catch (_: Exception) {}
    }

    /**
     * Cancel the active or incoming call notification.
     */
    fun cancelCallNotification(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_CALL)
        } catch (_: Exception) {}
    }

    /**
     * Resolves the contact profile photo for a phone number if available.
     */
    private fun getContactPhotoBitmap(context: Context, number: String): Bitmap? {
        if (number.isBlank()) return null
        // 1. User-customized avatar profile photo
        try {
            val customPath = CallerProfileManager.getCustomPhotoPath(context, number)
            if (!customPath.isNullOrBlank()) {
                val file = java.io.File(customPath)
                if (file.exists()) {
                    val bmp = android.graphics.BitmapFactory.decodeFile(customPath)
                    if (bmp != null) return bmp
                }
            }
        } catch (_: Throwable) {}

        // 2. Android system contacts provider photo
        try {
            val uri = Uri.withAppendedPath(
                android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
            context.contentResolver.query(
                uri,
                arrayOf(android.provider.ContactsContract.PhoneLookup.PHOTO_URI),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val photoUriString = cursor.getString(0)
                    if (!photoUriString.isNullOrBlank()) {
                        context.contentResolver.openInputStream(Uri.parse(photoUriString))?.use { stream ->
                            return android.graphics.BitmapFactory.decodeStream(stream)
                        }
                    }
                }
            }
        } catch (_: Throwable) {}
        return null
    }

    /**
     * Creates a high-DPI (192x192) Pixel Expressive circular notification avatar bitmap.
     * Incorporates custom contact photo if available, or renders a dynamic Material You gradient disc.
     */
    private fun createAvatarBitmap(
        context: Context,
        callerNumber: String,
        name: String,
        isMissed: Boolean
    ): Bitmap {
        val size = 192
        val sourceBitmap = getContactPhotoBitmap(context, callerNumber)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        if (sourceBitmap != null) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
            paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)

            val srcSize = Math.min(sourceBitmap.width, sourceBitmap.height)
            val srcX = (sourceBitmap.width - srcSize) / 2
            val srcY = (sourceBitmap.height - srcSize) / 2
            val srcRect = android.graphics.Rect(srcX, srcY, srcX + srcSize, srcY + srcSize)
            val dstRect = android.graphics.Rect(0, 0, size, size)
            canvas.drawBitmap(sourceBitmap, srcRect, dstRect, paint)

            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f
                color = 0x30FFFFFF.toInt()
            }
            canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 1.5f, borderPaint)
        } else {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = if (isMissed) {
                    android.graphics.LinearGradient(
                        0f, 0f, 0f, size.toFloat(),
                        0xFFE22B45.toInt(), // Saturated Pixel Ruby Red
                        0xFFBA1A30.toInt(), // Deep Carmine
                        android.graphics.Shader.TileMode.CLAMP
                    )
                } else {
                    android.graphics.LinearGradient(
                        0f, 0f, 0f, size.toFloat(),
                        0xFF1A73E8.toInt(), // Google Blue
                        0xFF1557B0.toInt(), // Deep Monet Blue
                        android.graphics.Shader.TileMode.CLAMP
                    )
                }
            }
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint)

            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f
                color = 0x35FFFFFF.toInt()
            }
            canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 1.5f, borderPaint)

            val initial = if (name.isNotBlank()) name.first().uppercase() else "#"
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 76f
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }

            val yPos = (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText(initial, size / 2f, yPos, textPaint)
        }

        return output
    }
}
