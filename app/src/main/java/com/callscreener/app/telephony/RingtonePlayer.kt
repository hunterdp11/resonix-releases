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

/**
 * Handles incoming call ringtone and vibration playback when CallScreeningInCallService
 * is operating as the active InCallService (IN_CALL_SERVICE_RINGING).
 */
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

            // 1. Play Ringtone if ringer mode is NORMAL
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

            // 2. Play Vibration if not SILENT
            if (ringerMode != AudioManager.RINGER_MODE_SILENT) {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vm?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }

                if (vibrator != null && vibrator.hasVibrator()) {
                    val pattern = longArrayOf(0, 1000, 1000)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(pattern, 0)
                    }
                    activeVibrator = vibrator
                    Log.i(TAG, "Incoming call vibration pattern started")
                }
            }

            isPlaying = true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting incoming call ringtone or vibration", e)
        }
    }

    @Synchronized
    fun stop(context: Context) {
        if (!isPlaying && activeRingtone == null && activeVibrator == null) return

        try {
            Log.i(TAG, "Stopping ringtone and cancelling vibration")
            activeRingtone?.stop()
            activeRingtone = null

            activeVibrator?.cancel()
            activeVibrator = null

            isPlaying = false
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping ringtone or vibration", e)
        }
    }
}

