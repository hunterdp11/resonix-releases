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
import com.callscreener.app.R
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

        val largeAvatar = createAvatarBitmap(displayName)

        val builder = NotificationCompat.Builder(context, CHANNEL_MISSED_CALLS)
            .setSmallIcon(android.R.drawable.sym_call_missed)
            .setLargeIcon(largeAvatar)
            .setContentTitle("Missed Call")
            .setContentText(displayName)
            .setSubText(timeString)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MISSED_CALL)
            .setColor(0xFFD70015.toInt()) // Carmine red
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                android.R.drawable.ic_menu_call,
                "Call Back",
                callBackPendingIntent
            )
            .addAction(
                android.R.drawable.ic_dialog_email,
                "Message",
                smsPendingIntent
            )
            .addAction(
                android.R.drawable.ic_btn_speak_now,
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
     * Post a high-priority Heads-Up Notification for Incoming Calls with Decline, Answer, and Screen actions.
     */
    fun postIncomingCallNotification(
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

        val largeAvatar = createAvatarBitmap(displayName)

        val builder = NotificationCompat.Builder(context, CHANNEL_INCOMING_CALLS)
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setLargeIcon(largeAvatar)
            .setContentTitle(displayName)
            .setContentText("Incoming phone call...")
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(contentPendingIntent, true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFF34C759.toInt()) // Apple Green
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Decline",
                declinePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_call,
                "Answer",
                answerPendingIntent
            )
            .addAction(
                android.R.drawable.ic_btn_speak_now,
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
        val largeAvatar = createAvatarBitmap(displayName)

        val builder = NotificationCompat.Builder(context, CHANNEL_ONGOING_CALLS)
            .setSmallIcon(android.R.drawable.sym_action_call)
            .setLargeIcon(largeAvatar)
            .setContentTitle(displayName)
            .setContentText(statusText)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFF007AFF.toInt())
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "End Call",
                hangupPendingIntent
            )

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

    private fun createAvatarBitmap(name: String): Bitmap {
        val size = 120
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD70015.toInt() // Carmine accent
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, circlePaint)

        val initial = if (name.isNotBlank()) name.first().uppercase() else "#"
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 50f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val yPos = (size / 2f - (textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText(initial, size / 2f, yPos, textPaint)

        return bitmap
    }
}
