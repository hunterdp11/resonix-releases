package com.callscreener.app.telephony

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.PowerManager
import android.util.Log

/**
 * Manages proximity sensor and PROXIMITY_SCREEN_OFF_WAKE_LOCK during in-call earpiece usage.
 * Automatically turns off the screen when the phone is held to the ear, and turns it back on
 * when pulled away, preventing accidental face touches during calls.
 */
class CallProximityManager(private val context: Context) : SensorEventListener {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val proximitySensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private var wakeLock: PowerManager.WakeLock? = null
    private var isListeningSensor = false
    private var isNear = false

    init {
        initWakeLock()
    }

    private fun initWakeLock() {
        if (wakeLock == null && powerManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
                        wakeLock = powerManager.newWakeLock(
                            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                            "CallScreener:InCallProximityWakeLock"
                        ).apply {
                            setReferenceCounted(false)
                        }
                        Log.i(TAG, "Initialized PROXIMITY_SCREEN_OFF_WAKE_LOCK successfully")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to create PROXIMITY_SCREEN_OFF_WAKE_LOCK", e)
            }
        }
    }

    /**
     * Call when in earpiece mode during an active call (or ringing call where handset is used).
     */
    fun acquire() {
        try {
            // 1. Acquire PowerManager Proximity WakeLock if supported
            if (wakeLock == null) {
                initWakeLock()
            }
            wakeLock?.let {
                if (!it.isHeld) {
                    it.acquire(3 * 60 * 60 * 1000L) // 3-hour safety timeout
                    Log.i(TAG, "Acquired in-call proximity wake lock")
                }
            }

            // 2. Also register SensorEventListener to monitor sensor transitions
            if (!isListeningSensor && proximitySensor != null && sensorManager != null) {
                sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
                isListeningSensor = true
                Log.i(TAG, "Registered Proximity Sensor listener")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring proximity sensor", e)
        }
    }

    /**
     * Call when switching to speakerphone, bluetooth headset, wired headphones, or call ended.
     */
    fun release() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
                    Log.i(TAG, "Released in-call proximity wake lock")
                }
            }
        } catch (e: Exception) {
            try {
                wakeLock?.release()
            } catch (_: Exception) {}
            Log.w(TAG, "Error releasing wake lock", e)
        }

        try {
            if (isListeningSensor && sensorManager != null) {
                sensorManager.unregisterListener(this)
                isListeningSensor = false
                Log.i(TAG, "Unregistered Proximity Sensor listener")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering proximity sensor listener", e)
        }
        isNear = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_PROXIMITY) return
        val distance = event.values[0]
        val maxRange = proximitySensor?.maximumRange ?: 5f
        val near = distance < maxRange && distance < 5f
        if (near != isNear) {
            isNear = near
            Log.d(TAG, "Proximity changed: distance=$distance, isNear=$isNear")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        private const val TAG = "CallProximityManager"
    }
}

