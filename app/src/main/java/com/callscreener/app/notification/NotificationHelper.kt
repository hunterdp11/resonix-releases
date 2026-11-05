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
import com.callscreener.app.ui.MainActivity
import com.callscreener.app.ui.ScreeningCallActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NotificationHelper {

    const val CHANNEL_MISSED_CALLS = "missed_calls_channel"
    private const val CHANNEL_MISSED_NAME = "Missed Calls"

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

            notificationManager.createNotificationChannel(missedCallChannel)
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
