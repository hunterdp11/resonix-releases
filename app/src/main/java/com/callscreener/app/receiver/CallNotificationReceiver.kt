package com.callscreener.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.callscreener.app.notification.NotificationHelper
import com.callscreener.app.session.CallSessionManager
import com.callscreener.app.telephony.RingtonePlayer
import com.callscreener.app.ui.ScreeningCallActivity

class CallNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i(TAG, "onReceive action: $action")

        val sessionManager = CallSessionManager.getInstance(context.applicationContext)

        RingtonePlayer.stop(context.applicationContext)
        com.callscreener.app.ui.overlay.DynamicCallCapsuleManager.dismissCapsule()

        when (action) {
            ACTION_DECLINE -> {
                Log.i(TAG, "Notification Decline action clicked")
                sessionManager.hangUp()
                NotificationHelper.cancelCallNotification(context.applicationContext)
            }
            ACTION_ANSWER -> {
                Log.i(TAG, "Notification Answer action clicked")
                sessionManager.answerCallDirectly()
                NotificationHelper.cancelCallNotification(context.applicationContext)
                val callIntent = Intent(context, ScreeningCallActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                context.startActivity(callIntent)
            }
            ACTION_SCREEN -> {
                Log.i(TAG, "Notification Screen action clicked")
                sessionManager.startScreening()
                NotificationHelper.cancelCallNotification(context.applicationContext)
                val callIntent = Intent(context, ScreeningCallActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                context.startActivity(callIntent)
            }
        }
    }

    companion object {
        private const val TAG = "CallNotifReceiver"
        const val ACTION_DECLINE = "com.callscreener.app.ACTION_DECLINE_CALL"
        const val ACTION_ANSWER = "com.callscreener.app.ACTION_ANSWER_CALL"
        const val ACTION_SCREEN = "com.callscreener.app.ACTION_SCREEN_CALL"
    }
}
