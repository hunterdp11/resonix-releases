package com.callscreener.app.service

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log
import com.callscreener.app.notification.NotificationHelper
import com.callscreener.app.root.RootPrivilegeManager
import com.callscreener.app.session.CallSessionManager
import com.callscreener.app.ui.ScreeningCallActivity

class CallScreeningInCallService : InCallService() {

    private var wasCallAnswered = false
    private var isCurrentCallIncoming = false
    private var currentCallerNumber: String? = null
    private var currentCallerName: String? = null

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            try {
                super.onStateChanged(call, state)
                Log.d(TAG, "Call state changed: $state")
                when (state) {
                    Call.STATE_RINGING -> {
                        CallSessionManager.getInstance(applicationContext).onCallRinging()
                    }
                    Call.STATE_DIALING, Call.STATE_CONNECTING -> {
                        CallSessionManager.getInstance(applicationContext).onCallDialing()
                    }
                    Call.STATE_ACTIVE -> {
                        Log.i(TAG, "Call is now ACTIVE")
                        wasCallAnswered = true
                        currentCallerNumber?.let {
                            NotificationHelper.cancelMissedCallNotification(applicationContext, it)
                        }
                        CallSessionManager.getInstance(applicationContext).onCallConnected()
                    }
                    Call.STATE_DISCONNECTED -> {
                        Log.i(TAG, "Call DISCONNECTED")
                        if (isCurrentCallIncoming && !wasCallAnswered) {
                            val number = currentCallerNumber ?: extractPhoneNumber(call)
                            val name = currentCallerName ?: call.details.callerDisplayName
                            if (!number.isNullOrBlank()) {
                                NotificationHelper.postMissedCallNotification(applicationContext, number, name)
                            }
                        }
                        CallSessionManager.getInstance(applicationContext).onCallEnded()
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Exception inside Call.Callback onStateChanged", t)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        currentInstance = this
        Log.d(TAG, "CallScreeningInCallService created")
    }

    override fun onDestroy() {
        super.onDestroy()
        currentInstance = null
        Log.d(TAG, "CallScreeningInCallService destroyed")
    }

    override fun onCallAdded(call: Call) {
        try {
            super.onCallAdded(call)
            currentInstance = this
            Log.d(TAG, "onCallAdded: $call, state: ${call.state}")
            call.registerCallback(callback)

            val callerNumber = extractPhoneNumber(call)
            val callerName = call.details.callerDisplayName

            val isIncoming = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                call.details.callDirection == Call.Details.DIRECTION_INCOMING
            } else {
                call.state == Call.STATE_RINGING
            }

            isCurrentCallIncoming = isIncoming
            currentCallerNumber = callerNumber
            currentCallerName = callerName
            wasCallAnswered = (call.state == Call.STATE_ACTIVE)

            val sessionManager = CallSessionManager.getInstance(applicationContext)
            sessionManager.attachCall(call, callerNumber, callerName, isIncoming)

            // Launch the interactive live screening screen
            val intent = Intent(this, ScreeningCallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        } catch (t: Throwable) {
            Log.e(TAG, "Exception inside onCallAdded", t)
        }
    }

    override fun onCallRemoved(call: Call) {
        try {
            super.onCallRemoved(call)
            Log.d(TAG, "onCallRemoved: $call")
            call.unregisterCallback(callback)
            CallSessionManager.getInstance(applicationContext).onCallEnded()
        } catch (t: Throwable) {
            Log.e(TAG, "Exception inside onCallRemoved", t)
        }
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
        Log.d(TAG, "Telecom onCallAudioStateChanged: route = ${audioState?.route}, isMuted = ${audioState?.isMuted}")
        if (isTelecomMuted && audioState?.isMuted == false) {
            Log.i(TAG, "Telecom unmuted unexpectedly during screening; re-enforcing mute to protect user microphone privacy")
            try {
                setMuted(true)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to re-enforce setMuted", e)
            }
        }
    }

    fun setSpeakerRoute(on: Boolean) {
        try {
            if (on) {
                Log.i(TAG, "Executing Telecom setAudioRoute(ROUTE_SPEAKER)")
                setAudioRoute(CallAudioState.ROUTE_SPEAKER)
            } else {
                Log.i(TAG, "Executing Telecom setAudioRoute(ROUTE_EARPIECE)")
                setAudioRoute(CallAudioState.ROUTE_EARPIECE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error switching Telecom audio route", e)
        }
    }

    private fun extractPhoneNumber(call: Call): String {
        return try {
            val handle: Uri? = call.details.handle
            handle?.schemeSpecificPart ?: "Unknown Caller"
        } catch (e: Exception) {
            "Unknown Caller"
        }
    }

    companion object {
        private const val TAG = "CallScreenInCallService"
        var currentInstance: CallScreeningInCallService? = null
        var isTelecomMuted: Boolean = false
            private set

        fun setSpeaker(on: Boolean) {
            currentInstance?.setSpeakerRoute(on)
        }

        fun setCallMute(muted: Boolean) {
            isTelecomMuted = muted
            try {
                currentInstance?.setMuted(muted)
                Log.i(TAG, "Executed Telecom setMuted($muted)")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting Telecom mute state", e)
            }

            // Only mute modem voice uplink via ALSA and telecom cmd if root available
            // NEVER mute system recording mic (cmd audio) or zero ADC volume,
            // as that would prevent AudioRecord / SpeechRecognizer from capturing user voice!
            RootPrivilegeManager.executeAsync {
                val script = if (muted) {
                    """
                        cmd telecom set-call-muted true 2>/dev/null || true
                        tinymix 'Voice Tx Mute' 1 2>/dev/null || true
                        tinymix 'Voice Tx Device Mute' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer MultiMedia1' 1 2>/dev/null || true
                        tinymix 'Incall_Music Audio Mixer MultiMedia1' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer TX_Voice' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer SLIM_0_TX_Voice' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer PRI_TX_Voice' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer TERT_MI2S_TX_Voice' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer TX_CDC_DMA_TX_3' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer SLIM_0_TX' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer AFE_PCM_TX' 0 2>/dev/null || true
                    """.trimIndent()
                } else {
                    """
                        cmd telecom set-call-muted false 2>/dev/null || true
                        tinymix 'Voice Tx Mute' 0 2>/dev/null || true
                        tinymix 'Voice Tx Device Mute' 0 2>/dev/null || true
                        tinymix 'Voip Tx Mute' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer TX_Voice' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer SLIM_0_TX_Voice' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer PRI_TX_Voice' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer TERT_MI2S_TX_Voice' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer TX_CDC_DMA_TX_3' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer SLIM_0_TX' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer AFE_PCM_TX' 1 2>/dev/null || true
                    """.trimIndent()
                }
                RootPrivilegeManager.executeSuCommand(script)
            }
        }
    }
}
