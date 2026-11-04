package com.callscreener.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.callscreener.app.notification.NotificationHelper

class CallScreenerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        NotificationHelper.initNotificationChannels(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_CALL_SCREENING,
                "Call Screening Active",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows live call screening notification and quick controls"
                setSound(null, null)
                enableVibration(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID_CALL_SCREENING = "call_screening_channel"
    }
}

