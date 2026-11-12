package com.callscreener.app.service

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log
import com.callscreener.app.data.DialerRepository
import com.callscreener.app.model.CallStatus
import com.callscreener.app.notification.NotificationHelper
import com.callscreener.app.root.RootPrivilegeManager
import com.callscreener.app.session.CallSessionManager
import com.callscreener.app.telephony.RingtonePlayer
import com.callscreener.app.ui.ScreeningCallActivity
import com.callscreener.app.ui.overlay.DynamicCallCapsuleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallScreeningInCallService : InCallService() {

    private var wasCallAnswered = false
    private var isCurrentCallIncoming = false
    private var isCurrentCallBlocked = false
    private var currentCallerNumber: String? = null
    private var currentCallerName: String? = null

    fun markCurrentCallBlocked(number: String?) {
        isCurrentCallBlocked = true
        wasCallAnswered = true
        RingtonePlayer.stop(applicationContext)
        NotificationHelper.cancelCallNotification(applicationContext)
        DynamicCallCapsuleManager.dismissCapsule()
        number?.let { NotificationHelper.cancelMissedCallNotification(applicationContext, it) }
        currentCallerNumber?.let { NotificationHelper.cancelMissedCallNotification(applicationContext, it) }
        Log.i(TAG, "markCurrentCallBlocked: Suppressed missed call notifications for '$number'")
    }

    private fun handleBlockedCallIfApplicable(call: Call, number: String?): Boolean {
        if (number.isNullOrBlank() || number == "Unknown Caller") return false
        val blockManager = com.callscreener.app.engine.CallBlockManager.getInstance(applicationContext)
        val blockedContact = blockManager.getBlockedContact(number) ?: return false

        val forwardTarget = blockedContact.forwardToNumber
            ?: if (blockManager.blockMode == com.callscreener.app.engine.BlockRejectMode.FORWARD_TO_TARGET) blockManager.defaultForwardNumber else null

        if (!forwardTarget.isNullOrBlank()) {
            Log.i(TAG, "BLOCKED CALL from $number triggering Silent Forward / Deflect to: $forwardTarget")
            markCurrentCallBlocked(number)
            blockManager.deflectOrForwardCall(call, forwardTarget, number)
            return true
        }

        Log.i(TAG, "BLOCKED CALL intercepted: $number (Stealth Action: ${blockManager.blockMode.name})")
        markCurrentCallBlocked(number)
        blockManager.handleBlockedInCall(call, number)
        return true
    }

    private val callback = object : Call.Callback() {
        override fun onDetailsChanged(call: Call, details: Call.Details) {
            super.onDetailsChanged(call, details)
            val number = details.handle?.schemeSpecificPart ?: currentCallerNumber
            if (isCurrentCallIncoming && !number.isNullOrBlank() && !wasCallAnswered) {
                if (handleBlockedCallIfApplicable(call, number)) {
                    Log.i(TAG, "onDetailsChanged: Intercepted blocked call from '$number'")
                }
            }
        }

        override fun onStateChanged(call: Call, state: Int) {
            try {
                super.onStateChanged(call, state)
                Log.d(TAG, "Call state changed: $state")
                when (state) {
                    Call.STATE_RINGING -> {
                        val number = currentCallerNumber ?: extractPhoneNumber(call)
                        if (isCurrentCallIncoming && handleBlockedCallIfApplicable(call, number)) {
                            return
                        }
                        RingtonePlayer.start(applicationContext)

                        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                        val isUserUsingPhone = (powerManager?.isInteractive == true) && (keyguardManager?.isKeyguardLocked == false)

                        NotificationHelper.postIncomingCallNotification(
                            context = applicationContext,
                            callerNumber = number,
                            callerName = currentCallerName
                        )
                        if (!isUserUsingPhone) {
                            val intent = Intent(this@CallScreeningInCallService, ScreeningCallActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            startActivity(intent)
                        }
                        CallSessionManager.getInstance(applicationContext).onCallRinging()
                    }
                    Call.STATE_DIALING, Call.STATE_CONNECTING -> {
                        CallSessionManager.getInstance(applicationContext).onCallDialing()
                    }
                    Call.STATE_ACTIVE -> {
                        Log.i(TAG, "Call is now ACTIVE")
                        wasCallAnswered = true
                        RingtonePlayer.stop(applicationContext)
                        com.callscreener.app.telephony.CallVibrator.vibrateCallConnected(applicationContext)
                        DynamicCallCapsuleManager.dismissCapsule()
                        currentCallerNumber?.let {
                            NotificationHelper.cancelMissedCallNotification(applicationContext, it)
                        }
                        NotificationHelper.postOngoingCallNotification(
                            applicationContext,
                            currentCallerNumber ?: extractPhoneNumber(call),
                            currentCallerName,
                            CallSessionManager.getInstance(applicationContext).sessionState.value.status == CallStatus.SCREENING_ACTIVE
                        )
                        CallSessionManager.getInstance(applicationContext).onCallConnected()
                    }
                    Call.STATE_DISCONNECTED -> {
                        Log.i(TAG, "Call DISCONNECTED")
                        RingtonePlayer.stop(applicationContext)
                        com.callscreener.app.telephony.CallVibrator.vibrateCallEnded(applicationContext)
                        DynamicCallCapsuleManager.dismissCapsule()
                        NotificationHelper.cancelCallNotification(applicationContext)
                        if (isCurrentCallIncoming && !wasCallAnswered && !isCurrentCallBlocked) {
                            val number = currentCallerNumber ?: extractPhoneNumber(call)
                            val name = currentCallerName ?: DialerRepository.lookupContactName(applicationContext, number) ?: call.details.callerDisplayName
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
        RingtonePlayer.stop(applicationContext)
        NotificationHelper.cancelCallNotification(applicationContext)
        DynamicCallCapsuleManager.dismissCapsule()
        currentInstance = null
        Log.d(TAG, "CallScreeningInCallService destroyed")
    }

    override fun onCallAdded(call: Call) {
        try {
            super.onCallAdded(call)
            currentInstance = this
            Log.d(TAG, "onCallAdded: $call, state: ${call.state}")
            call.registerCallback(callback)

            val sessionManager = CallSessionManager.getInstance(applicationContext)

            val callerNumber = extractPhoneNumber(call)
            val networkCallerName = call.details.callerDisplayName

            val savedContactName = DialerRepository.lookupContactName(applicationContext, callerNumber)
            val resolvedCallerName = if (!savedContactName.isNullOrBlank()) {
                savedContactName
            } else if (!networkCallerName.isNullOrBlank()) {
                networkCallerName
            } else {
                null
            }

            val isIncoming = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                call.details.callDirection == Call.Details.DIRECTION_INCOMING
            } else {
                call.state == Call.STATE_RINGING
            }

            isCurrentCallIncoming = isIncoming
            currentCallerNumber = callerNumber
            currentCallerName = resolvedCallerName
            wasCallAnswered = (call.state == Call.STATE_ACTIVE)
            isCurrentCallBlocked = false

            if (isIncoming && handleBlockedCallIfApplicable(call, callerNumber)) {
                return
            }

            sessionManager.attachCall(call, callerNumber, resolvedCallerName, isIncoming)

            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            val isUserUsingPhone = (powerManager?.isInteractive == true) && (keyguardManager?.isKeyguardLocked == false)

            if (isIncoming && call.state == Call.STATE_RINGING) {
                RingtonePlayer.start(applicationContext)
                NotificationHelper.postIncomingCallNotification(
                    context = applicationContext,
                    callerNumber = callerNumber,
                    callerName = resolvedCallerName
                )
                if (isUserUsingPhone) {
                    Log.i(TAG, "User actively using phone: presenting minimal incoming call heads-up banner like Google Dialer")
                } else {
                    Log.i(TAG, "Device locked or screen off: presenting full-screen incoming call view")
                    val intent = Intent(this, ScreeningCallActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                }
            } else {
                val intent = Intent(this, ScreeningCallActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Exception inside onCallAdded", t)
        }
    }

    override fun onCallRemoved(call: Call) {
        try {
            super.onCallRemoved(call)
            Log.d(TAG, "onCallRemoved: $call")
            RingtonePlayer.stop(applicationContext)
            DynamicCallCapsuleManager.dismissCapsule()
            NotificationHelper.cancelCallNotification(applicationContext)
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

        if (audioState != null && (audioState.supportedRouteMask and CallAudioState.ROUTE_BLUETOOTH) != 0) {
            if (audioState.route == CallAudioState.ROUTE_EARPIECE) {
                Log.i(TAG, "Connected Bluetooth headset detected; auto-routing Telecom audio to ROUTE_BLUETOOTH")
                try {
                    setAudioRoute(CallAudioState.ROUTE_BLUETOOTH)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to route Telecom to Bluetooth", e)
                }
            }
        }
    }

    fun setSpeakerRoute(on: Boolean) {
        try {
            if (on) {
                Log.i(TAG, "Executing Telecom setAudioRoute(ROUTE_SPEAKER)")
                setAudioRoute(CallAudioState.ROUTE_SPEAKER)
            } else {
                val supportedMask = callAudioState?.supportedRouteMask ?: 0
                if ((supportedMask and CallAudioState.ROUTE_BLUETOOTH) != 0) {
                    Log.i(TAG, "Executing Telecom setAudioRoute(ROUTE_BLUETOOTH)")
                    setAudioRoute(CallAudioState.ROUTE_BLUETOOTH)
                } else if ((supportedMask and CallAudioState.ROUTE_WIRED_HEADSET) != 0) {
                    Log.i(TAG, "Executing Telecom setAudioRoute(ROUTE_WIRED_HEADSET)")
                    setAudioRoute(CallAudioState.ROUTE_WIRED_HEADSET)
                } else {
                    Log.i(TAG, "Executing Telecom setAudioRoute(ROUTE_EARPIECE)")
                    setAudioRoute(CallAudioState.ROUTE_EARPIECE)
                }
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

        fun markCallBlocked(number: String?) {
            currentInstance?.markCurrentCallBlocked(number)
        }

        fun setSpeaker(on: Boolean) {
            currentInstance?.setSpeakerRoute(on)
        }

        fun isolateMicForAiRelay(isolate: Boolean) {
            isTelecomMuted = false
            try {
                currentInstance?.setMuted(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error ensuring unmuted Telecom state", e)
            }

            RootPrivilegeManager.executeAsync {
                val script = if (isolate) {
                    """
                        cmd telecom set-call-muted false 2>/dev/null || true
                        tinymix 'Voice Tx Mute' 0 2>/dev/null || true
                        tinymix 'Voice Tx Device Mute' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer MultiMedia1' 1 2>/dev/null || true
                        tinymix 'Incall_Music Audio Mixer MultiMedia1' 1 2>/dev/null || true
                        tinymix 'Incall_Music Audio Mixer MultiMedia2' 1 2>/dev/null || true
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
                        tinymix 'Voice_Tx Mixer MultiMedia1' 0 2>/dev/null || true
                        tinymix 'Incall_Music Audio Mixer MultiMedia1' 0 2>/dev/null || true
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

        fun setCallMute(muted: Boolean) {
            isTelecomMuted = muted
            try {
                currentInstance?.setMuted(muted)
                Log.i(TAG, "Executed Telecom setMuted($muted)")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting Telecom mute state", e)
            }

            RootPrivilegeManager.executeAsync {
                val script = if (muted) {
                    """
                        cmd telecom set-call-muted true 2>/dev/null || true
                        tinymix 'Voice Tx Mute' 1 2>/dev/null || true
                        tinymix 'Voice Tx Device Mute' 0 2>/dev/null || true
                    """.trimIndent()
                } else {
                    """
                        cmd telecom set-call-muted false 2>/dev/null || true
                        tinymix 'Voice Tx Mute' 0 2>/dev/null || true
                        tinymix 'Voice Tx Device Mute' 0 2>/dev/null || true
                        tinymix 'Voip Tx Mute' 0 2>/dev/null || true
                    """.trimIndent()
                }
                RootPrivilegeManager.executeSuCommand(script)
            }
        }
    }
}
