package com.callscreener.app.service

import android.app.KeyguardManager
import android.app.Notification
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

    private var isForeground = false
    var activeCallBaseTimeMillis: Long? = null
        private set

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

    fun promoteToForeground(notification: android.app.Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NotificationHelper.NOTIFICATION_ID_CALL,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                )
            } else {
                startForeground(NotificationHelper.NOTIFICATION_ID_CALL, notification)
            }
            isForeground = true
            Log.i(TAG, "Service in foreground (phoneCall)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to startForeground", e)
        }
    }

    fun demoteFromForeground() {
        if (isForeground) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                isForeground = false
                activeCallBaseTimeMillis = null
                Log.i(TAG, "Service demoted from foreground")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stopForeground", e)
            }
        }
    }

    fun updateOngoingNotification(
        statusOverride: String? = null,
        baseTimeMillis: Long? = activeCallBaseTimeMillis
    ) {
        val sessionState = CallSessionManager.getInstance(applicationContext).sessionState.value
        val number = when {
            sessionState.isConference -> "Conference"
            sessionState.callerNumber.isNotBlank() -> sessionState.callerNumber
            else -> currentCallerNumber ?: "Unknown"
        }
        val name = when {
            sessionState.isConference -> "Conference Call"
            !sessionState.callerName.isNullOrBlank() -> sessionState.callerName
            else -> currentCallerName
        }
        val isScreening = sessionState.status == CallStatus.SCREENING_ACTIVE
        val notif = NotificationHelper.postOngoingCallNotification(
            context = applicationContext,
            callerNumber = number,
            callerName = name,
            isScreening = isScreening,
            statusOverride = statusOverride,
            baseTimeMillis = baseTimeMillis,
            isMuted = sessionState.isMuted,
            isSpeakerOn = sessionState.isSpeakerOn
        )
        promoteToForeground(notif)
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
            if (details.hasProperty(Call.Details.PROPERTY_CONFERENCE)) {
                Log.i(TAG, "onDetailsChanged: PROPERTY_CONFERENCE detected on call $call")
                CallSessionManager.getInstance(applicationContext).setConference(true, call)
            }
        }

        override fun onConferenceableCallsChanged(call: Call, conferenceableCalls: MutableList<Call>?) {
            super.onConferenceableCallsChanged(call, conferenceableCalls)
            val hasConferenceables = !conferenceableCalls.isNullOrEmpty()
            Log.d(TAG, "onConferenceableCallsChanged: hasConferenceables=$hasConferenceables")
            CallSessionManager.getInstance(applicationContext).setConferenceable(hasConferenceables)
        }

        override fun onChildrenChanged(call: Call, children: MutableList<Call>?) {
            super.onChildrenChanged(call, children)
            val hasChildren = !children.isNullOrEmpty()
            Log.d(TAG, "onChildrenChanged: hasChildren=$hasChildren, children=$children")
            if (hasChildren) {
                val sessionManager = CallSessionManager.getInstance(applicationContext)
                sessionManager.setConference(true, call)
            }
        }

        override fun onStateChanged(call: Call, state: Int) {
            try {
                super.onStateChanged(call, state)
                Log.d(TAG, "Call state changed: $call -> state: $state")
                val sessionManager = CallSessionManager.getInstance(applicationContext)
                val isCurrentActiveCall = (sessionManager.currentCall == null || sessionManager.currentCall == call)

                when (state) {
                    Call.STATE_RINGING -> {
                        val number = extractPhoneNumber(call)
                        if (isCurrentCallIncoming && handleBlockedCallIfApplicable(call, number)) {
                            return
                        }
                        if (!sessionManager.hasActiveCall()) {
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
                            sessionManager.onCallRinging()
                        } else {
                            // Call Waiting: secondary incoming call while already in call
                            Log.i(TAG, "Call Waiting incoming: $number")
                            val contactName = DialerRepository.lookupContactName(applicationContext, number) ?: call.details.callerDisplayName
                            sessionManager.attachCall(call, number, contactName, isIncoming = true)
                            NotificationHelper.postIncomingCallNotification(
                                context = applicationContext,
                                callerNumber = number,
                                callerName = contactName
                            )
                        }
                    }
                    Call.STATE_DIALING -> {
                        if (isCurrentActiveCall) {
                            sessionManager.onCallDialing()
                            updateOngoingNotification(statusOverride = "Calling...")
                        }
                    }
                    Call.STATE_CONNECTING -> {
                        if (isCurrentActiveCall) {
                            sessionManager.onCallDialing()
                            updateOngoingNotification(statusOverride = "Connecting...")
                        }
                    }
                    Call.STATE_ACTIVE -> {
                        Log.i(TAG, "Call is now ACTIVE: $call")
                        wasCallAnswered = true
                        RingtonePlayer.stop(applicationContext)
                        com.callscreener.app.telephony.CallVibrator.vibrateCallConnected(applicationContext)
                        DynamicCallCapsuleManager.dismissCapsule()
                        val num = extractPhoneNumber(call)
                        if (num.isNotBlank()) {
                            NotificationHelper.cancelMissedCallNotification(applicationContext, num)
                        }
                        if (activeCallBaseTimeMillis == null) {
                            activeCallBaseTimeMillis = System.currentTimeMillis()
                        }
                        if (isCurrentActiveCall) {
                            sessionManager.onCallConnected()
                            updateOngoingNotification(baseTimeMillis = activeCallBaseTimeMillis)
                        } else {
                            syncNotification()
                        }
                    }
                    Call.STATE_HOLDING -> {
                        Log.i(TAG, "Call on HOLD: $call")
                        val otherAliveCalls = calls.filter { it != call && it.state != Call.STATE_DISCONNECTED }
                        if (otherAliveCalls.isEmpty()) {
                            updateOngoingNotification(statusOverride = "Call on hold")
                        } else {
                            syncNotification()
                        }
                    }
                    Call.STATE_DISCONNECTED -> {
                        Log.i(TAG, "Call DISCONNECTED: $call")
                        sessionManager.onCallRemoved(call)
                        val otherAliveCalls = calls.filter { it != call && it.state != Call.STATE_DISCONNECTED }
                        if (otherAliveCalls.isNotEmpty() || sessionManager.hasActiveCall()) {
                            Log.i(TAG, "Remaining active calls present. Keeping notification active.")
                            syncNotification()
                        } else {
                            Log.i(TAG, "All calls disconnected. Ending session and dismissing notification.")
                            RingtonePlayer.stop(applicationContext)
                            com.callscreener.app.telephony.CallVibrator.vibrateCallEnded(applicationContext)
                            DynamicCallCapsuleManager.dismissCapsule()
                            demoteFromForeground()
                            NotificationHelper.cancelCallNotification(applicationContext)
                            if (isCurrentCallIncoming && !wasCallAnswered && !isCurrentCallBlocked) {
                                val number = currentCallerNumber ?: extractPhoneNumber(call)
                                val name = currentCallerName ?: DialerRepository.lookupContactName(applicationContext, number) ?: call.details?.callerDisplayName
                                if (!number.isNullOrBlank()) {
                                    NotificationHelper.postMissedCallNotification(applicationContext, number, name)
                                }
                            }
                        }
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
        demoteFromForeground()
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

            val isConference = call.details?.hasProperty(Call.Details.PROPERTY_CONFERENCE) == true ||
                    !call.children.isNullOrEmpty()
            val isConferenceChild = call.parent != null
            if (isConference) {
                Log.i(TAG, "onCallAdded: Conference call detected: $call")
                sessionManager.setConference(true, call)
                syncNotification()
                return
            }
            if (isConferenceChild) {
                Log.i(TAG, "onCallAdded: Child call of conference detected: $call")
                return
            }

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
                val isAlreadyInCall = sessionManager.hasActiveCall()
                if (!isAlreadyInCall) {
                    RingtonePlayer.start(applicationContext)
                }
                NotificationHelper.postIncomingCallNotification(
                    context = applicationContext,
                    callerNumber = callerNumber,
                    callerName = resolvedCallerName
                )
                if (isUserUsingPhone || isAlreadyInCall) {
                    Log.i(TAG, "User actively in call or using phone: presenting minimal incoming call heads-up banner")
                } else {
                    Log.i(TAG, "Device locked or screen off: presenting full-screen incoming call view")
                    val intent = Intent(this, ScreeningCallActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                }
            } else {
                val initialStatus = if (call.state == Call.STATE_ACTIVE) null else "Calling..."
                updateOngoingNotification(statusOverride = initialStatus)
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
            call.unregisterCallback(callback)
            val sessionManager = CallSessionManager.getInstance(applicationContext)
            sessionManager.onCallRemoved(call)
            val otherAliveCalls = calls.filter { it != call && it.state != Call.STATE_DISCONNECTED }
            if (otherAliveCalls.isNotEmpty() || sessionManager.hasActiveCall()) {
                Log.i(TAG, "onCallRemoved: Active calls remaining: ${otherAliveCalls.size}. Keeping session and notification active.")
                syncNotification()
            } else {
                Log.i(TAG, "onCallRemoved: All calls ended.")
                RingtonePlayer.stop(applicationContext)
                DynamicCallCapsuleManager.dismissCapsule()
                demoteFromForeground()
                NotificationHelper.cancelCallNotification(applicationContext)
            }
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

    fun extractPhoneNumber(call: Call): String {
        return Companion.extractPhoneNumber(call)
    }

    companion object {
        private const val TAG = "CallScreenInCallService"
        var currentInstance: CallScreeningInCallService? = null
        var isTelecomMuted: Boolean = false
            private set

        fun extractPhoneNumber(call: Call): String {
            return try {
                val handle: Uri? = call.details?.handle
                handle?.schemeSpecificPart ?: "Unknown Caller"
            } catch (e: Exception) {
                "Unknown Caller"
            }
        }

        fun markCallBlocked(number: String?) {
            currentInstance?.markCurrentCallBlocked(number)
        }

        fun setSpeaker(on: Boolean) {
            currentInstance?.setSpeakerRoute(on)
        }

        fun syncNotification() {
            currentInstance?.let { service ->
                val sessionState = CallSessionManager.getInstance(service.applicationContext).sessionState.value
                val statusText = when {
                    sessionState.isConference -> "Conference call"
                    sessionState.hasSecondaryCall -> "Active (1 on hold)"
                    sessionState.status == CallStatus.DIALING -> "Calling..."
                    sessionState.status == CallStatus.RINGING -> "Ringing..."
                    sessionState.status == CallStatus.ON_HOLD -> "Call on hold"
                    sessionState.status == CallStatus.SCREENING_ACTIVE -> "AI Screening in progress..."
                    sessionState.status == CallStatus.USER_TALKING -> null
                    else -> null
                }
                service.updateOngoingNotification(
                    statusOverride = statusText,
                    baseTimeMillis = service.activeCallBaseTimeMillis
                )
            }
        }

        fun applyCrystalClearAudioTuning(context: Context? = null) {
            val ctx = context ?: currentInstance?.applicationContext
            try {
                val am = ctx?.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
                am?.setParameters("voice_tx_mute=0")
                am?.setParameters("voice_tx_mute=false")
                am?.setParameters("voice_rx_mute=0")
                am?.setParameters("voice_rx_mute=false")
                am?.setParameters("incall_music_enable=false")
                am?.setParameters("incall_music_enable=0")
                am?.setParameters("fluence=dualmic")
                am?.setParameters("fluence=true")
                am?.setParameters("echo_cancellation=1")
                am?.setParameters("noise_suppression=1")
                am?.setParameters("ec_ns=1")
            } catch (_: Exception) {}

            RootPrivilegeManager.executeAsync {
                val script = """
                    # 1. Unmute Telecom and cellular modem TX uplink across 4G/5G/VoLTE/VoWiFi
                    cmd telecom set-call-muted false 2>/dev/null || true
                    tinymix 'Voice Tx Mute' 0 2>/dev/null || true
                    tinymix 'Voice Tx Device Mute' 0 2>/dev/null || true
                    tinymix 'Voip Tx Mute' 0 2>/dev/null || true
                    tinymix 'VoiceMMode1 Tx Mute' 0 2>/dev/null || true
                    tinymix 'VoiceMMode2 Tx Mute' 0 2>/dev/null || true
                    tinymix 'VoLTE Tx Mute' 0 2>/dev/null || true
                    tinymix 'VoWIFI Tx Mute' 0 2>/dev/null || true
                    tinymix 'Uplink Mute' 0 2>/dev/null || true

                    # 2. Terminate AI relay and audio player playback routing into modem
                    tinymix 'Voice_Tx Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer MultiMedia2' 0 2>/dev/null || true
                    tinymix 'VoiceMMode1_Tx Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'VoiceMMode1_Tx Mixer MultiMedia2' 0 2>/dev/null || true
                    tinymix 'VoiceMMode2_Tx Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'VoiceMMode2_Tx Mixer MultiMedia2' 0 2>/dev/null || true
                    tinymix 'VoLTE_Tx Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'VoLTE_Tx Mixer MultiMedia2' 0 2>/dev/null || true
                    tinymix 'VoWIFI_Tx Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'VoWIFI_Tx Mixer MultiMedia2' 0 2>/dev/null || true
                    tinymix 'Incall_Music Audio Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'Incall_Music Audio Mixer MultiMedia2' 0 2>/dev/null || true
                    tinymix 'Incall_Music_2 Audio Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'Incall_Music_2 Audio Mixer MultiMedia2' 0 2>/dev/null || true
                    tinymix 'Audio_Incall_Playback' 0 2>/dev/null || true
                    tinymix 'Incall_Playback' 0 2>/dev/null || true
                    tinymix 'Audio_Incall_Playback_DL' 0 2>/dev/null || true
                    tinymix 'Voice Call Uplink' 0 2>/dev/null || true
                    tinymix 'Playback Incall Music' 0 2>/dev/null || true
                    tinymix 'Incall_Music' 0 2>/dev/null || true
                    tinymix 'Media_Tx' 0 2>/dev/null || true

                    # 3. CRITICAL: Block all downlink loopback and unconnected floating bus noise from Voice_Tx (eliminates echo and hiss)
                    tinymix 'Voice_Tx Mixer PRI_TX_Voice' 0 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer TERT_MI2S_TX_Voice' 0 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer TX_CDC_DMA_TX_3' 0 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer SLIM_0_TX' 0 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer AFE_PCM_TX' 0 2>/dev/null || true

                    # 4. Connect microphone lines cleanly to Voice_Tx
                    tinymix 'Voice_Tx Mixer TX_Voice' 1 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer SLIM_0_TX_Voice' 1 2>/dev/null || true

                    # 5. Enable hardware dual-mic Fluence noise cancellation and echo suppression
                    setprop persist.vendor.audio.fluence.voicecall true 2>/dev/null || true
                    setprop vendor.audio.fluence.voicecall true 2>/dev/null || true
                    setprop persist.vendor.audio.fluence.speaker true 2>/dev/null || true
                    setprop persist.vendor.audio.fluence.voicerec true 2>/dev/null || true
                    tinymix 'Voice Noise Suppression' 1 2>/dev/null || true
                    tinymix 'Voice Echo Cancellation' 1 2>/dev/null || true
                    tinymix 'Voice EC NS' 1 2>/dev/null || true
                    tinymix 'Voice EC_NS' 1 2>/dev/null || true
                    tinymix 'EC_NS' 1 2>/dev/null || true
                    tinymix 'Fluence Mode' 'Dual_Mic' 2>/dev/null || true
                    tinymix 'Fluence Type' 'Dual_Mic' 2>/dev/null || true
                    tinymix 'Fluence Enable' 1 2>/dev/null || true
                """.trimIndent()
                RootPrivilegeManager.executeSuCommand(script)
            }
        }

        fun isolateMicForAiRelay(isolate: Boolean) {
            isTelecomMuted = false
            try {
                currentInstance?.setMuted(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error ensuring unmuted Telecom state", e)
            }

            if (!isolate) {
                applyCrystalClearAudioTuning()
            } else {
                RootPrivilegeManager.executeAsync {
                    val script = """
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
                    RootPrivilegeManager.executeSuCommand(script)
                }
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
                        tinymix 'Voice_Tx Mixer TX_Voice' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer SLIM_0_TX_Voice' 1 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer PRI_TX_Voice' 0 2>/dev/null || true
                        tinymix 'Voice_Tx Mixer AFE_PCM_TX' 0 2>/dev/null || true
                    """.trimIndent()
                }
                RootPrivilegeManager.executeSuCommand(script)
            }
        }
    }
}
