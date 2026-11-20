package com.callscreener.app.telephony

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object CallVibrator {

    private const val TAG = "CallVibrator"
    private var activeRingingVibrator: Vibrator? = null
    @Volatile private var isRingingVibrating = false
    @Volatile private var lastConnectedVibrateMs = 0L
    @Volatile private var lastEndedVibrateMs = 0L

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator ?: context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to obtain Vibrator service", e)
            null
        }
    }

    @Synchronized
    fun startRingingVibration(context: Context) {
        if (isRingingVibrating) {
            Log.d(TAG, "Ringing vibration already running")
            return
        }

        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val ringerMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
            if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
                Log.i(TAG, "Device is in Silent mode; skipping ringing vibration")
                return
            }

            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) {
                Log.w(TAG, "Device does not have a vibrator")
                return
            }

            val pattern = longArrayOf(0, 1000, 1000)
            val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, amplitudes, 0)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    try {
                        val vibrationAttributes = VibrationAttributes.createForUsage(VibrationAttributes.USAGE_RINGTONE)
                        vibrator.vibrate(effect, vibrationAttributes)
                    } catch (_: Throwable) {
                        vibrator.vibrate(effect, audioAttributes)
                    }
                } else {
                    vibrator.vibrate(effect, audioAttributes)
                }
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, 0)
            }

            activeRingingVibrator = vibrator
            isRingingVibrating = true
            Log.i(TAG, "Incoming call ringing vibration started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting ringing vibration", e)
        }
    }

    @Synchronized
    fun stopRingingVibration() {
        try {
            activeRingingVibrator?.cancel()
        } catch (e: Exception) {
            Log.w(TAG, "Error cancelling ringing vibrator", e)
        }
        activeRingingVibrator = null
        isRingingVibrating = false
        Log.d(TAG, "Ringing vibration stopped")
    }

    @Synchronized
    fun vibrateCallConnected(context: Context) {
        val now = System.currentTimeMillis()
        if (now - lastConnectedVibrateMs < 800L) {
            return
        }
        lastConnectedVibrateMs = now

        try {
            stopRingingVibration()
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                vibrator.vibrate(effect, audioAttributes)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(80L, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect, audioAttributes)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(80L)
            }
            Log.i(TAG, "Vibrated for call connected")
        } catch (e: Exception) {
            Log.w(TAG, "Error vibrating for call connected", e)
        }
    }

    @Synchronized
    fun vibrateCallEnded(context: Context) {
        val now = System.currentTimeMillis()
        if (now - lastEndedVibrateMs < 800L) {
            return
        }
        lastEndedVibrateMs = now

        try {
            stopRingingVibration()
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .build()

            val pattern = longArrayOf(0, 60, 100, 70)
            val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
                vibrator.vibrate(effect, audioAttributes)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
            Log.i(TAG, "Vibrated for call ended")
        } catch (e: Exception) {
            Log.w(TAG, "Error vibrating for call ended", e)
        }
    }

    fun vibrateClick(context: Context) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                vibrator.vibrate(effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(25L, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(25L)
            }
        } catch (_: Exception) {}
    }
}
