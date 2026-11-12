package com.callscreener.app.telephony

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object RingtonePlayer {

    private const val TAG = "RingtonePlayer"
    private var activeRingtone: Ringtone? = null
    private var activeVibrator: Vibrator? = null
    private var isPlaying = false

    @Synchronized
    fun start(context: Context) {
        if (isPlaying) {
            Log.d(TAG, "RingtonePlayer already playing; ignoring start call")
            return
        }

        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val ringerMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL

            Log.i(TAG, "Starting ringtone & vibration; ringerMode = $ringerMode")

            if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                var ringtoneUri: Uri? = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
                if (ringtoneUri == null) {
                    ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                }

                if (ringtoneUri != null) {
                    val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
                    if (ringtone != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ringtone.isLooping = true
                        }
                        ringtone.audioAttributes = AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .build()
                        ringtone.play()
                        activeRingtone = ringtone
                        Log.i(TAG, "Default ringtone playback started successfully")
                    }
                }
            }

            CallVibrator.startRingingVibration(context)

            isPlaying = true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting incoming call ringtone or vibration", e)
        }
    }

    @Synchronized
    fun stop(context: Context) {
        try {
            CallVibrator.stopRingingVibration()
        } catch (_: Exception) {}

        if (!isPlaying && activeRingtone == null) return

        try {
            Log.i(TAG, "Stopping ringtone and cancelling vibration")
            activeRingtone?.stop()
            activeRingtone = null

            isPlaying = false
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping ringtone or vibration", e)
        }
    }
}
