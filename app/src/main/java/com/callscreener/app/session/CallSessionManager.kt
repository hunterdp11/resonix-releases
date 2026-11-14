package com.callscreener.app.session

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.telecom.TelecomManager
import android.telecom.VideoProfile
import android.util.Log
import com.callscreener.app.data.DialerRepository
import com.callscreener.app.engine.AiVoiceRelayManager
import com.callscreener.app.engine.AudioPresetManager
import com.callscreener.app.engine.QuickReplyManager
import com.callscreener.app.engine.CallSpeechManager
import com.callscreener.app.model.AudioCallMode
import com.callscreener.app.model.AudioPreset
import com.callscreener.app.model.CallSessionState
import com.callscreener.app.model.CallStatus
import com.callscreener.app.model.ChatMessage
import com.callscreener.app.model.MessageSender
import com.callscreener.app.model.VoicePersona
import com.callscreener.app.recorder.CallRecordingEngine
import com.callscreener.app.root.AppOperationMode
import com.callscreener.app.root.RootPrivilegeManager
import com.callscreener.app.service.CallScreeningInCallService
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CallSessionManager private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var activeTelecomCall: Call? = null
    private var secondaryTelecomCall: Call? = null

    fun hasActiveCall(): Boolean = activeTelecomCall != null &&
            _sessionState.value.status != CallStatus.ENDED &&
            _sessionState.value.status != CallStatus.IDLE

    fun hasSecondaryCall(): Boolean = secondaryTelecomCall != null || _sessionState.value.hasSecondaryCall

    val currentCall: Call?
        get() = activeTelecomCall
    private var durationJob: Job? = null
    private var pendingGreeting: String? = null

    private val speechManager = CallSpeechManager(appContext)
    val aiVoiceRelayManager = AiVoiceRelayManager(appContext)
    val recordingEngine = CallRecordingEngine(appContext)
    val presetManager = AudioPresetManager(appContext)
    val quickReplyManager = QuickReplyManager(appContext)
    val speechJammerEngine = com.callscreener.app.engine.SpeechJammerEngine.getInstance(appContext)
    val proximityManager = com.callscreener.app.telephony.CallProximityManager(appContext)
    private var recordingTimerJob: Job? = null
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _sessionState = MutableStateFlow(CallSessionState())
    val sessionState: StateFlow<CallSessionState> = _sessionState.asStateFlow()

    init {
        scope.launch {
            speechJammerEngine.isJammerActive.collect { active ->
                _sessionState.update { it.copy(isSpeechJammerActive = active) }
            }
        }

        scope.launch {
            speechJammerEngine.isEchoUserVoiceFlow.collect { echoUser ->
                _sessionState.update { it.copy(isEchoUserVoiceEnabled = echoUser) }
            }
        }

        scope.launch {
            quickReplyManager.quickReplies.collect { replies ->
                _sessionState.update { it.copy(quickReplies = replies) }
            }
        }

        val initialMode = RootPrivilegeManager.getOperationMode(appContext)
        _sessionState.update { it.copy(operationMode = initialMode) }

        scope.launch {
            speechManager.isSpeaking.collect { speaking ->
                _sessionState.update { it.copy(isTtsSpeaking = speaking) }
                aiVoiceRelayManager.isTtsCurrentlySpeaking = speaking
                if (speaking) {
                    aiVoiceRelayManager.stopListening()
                } else {
                    if (_sessionState.value.status == CallStatus.SCREENING_ACTIVE &&
                        _sessionState.value.audioCallMode == AudioCallMode.AI_TYPE_SPEAK &&
                        _sessionState.value.isAutoRelayEnabled
                    ) {
                        delay(350L)
                        aiVoiceRelayManager.startListening()
                    }
                }
            }
        }

        speechManager.onAudioFileReadyForMonitor = { file ->
            playLocalAudioMonitor(file, volume = 0.22f)
        }
        speechManager.onAudioFileMonitorStop = {
            stopLocalAudioMonitor()
        }

        speechManager.onSpeechFinished = {
            scope.launch {
                stopLocalAudioMonitor()
                aiVoiceRelayManager.isTtsCurrentlySpeaking = false
                delay(300L)
                if (!_sessionState.value.isSimulated) {
                    setSpeakerphone(_sessionState.value.isSpeakerOn)
                }
                val inCall = _sessionState.value.status == CallStatus.SCREENING_ACTIVE ||
                             _sessionState.value.status == CallStatus.USER_TALKING ||
                             _sessionState.value.status == CallStatus.DIALING
                if (inCall && _sessionState.value.audioCallMode == AudioCallMode.AI_TYPE_SPEAK) {
                    if (_sessionState.value.isSimulated) {
                        if (_sessionState.value.messages.count { it.sender == MessageSender.CALLER } == 0) {
                            delay(1200L)
                            addMessage(MessageSender.CALLER, "Hello, this is Courier Logistics with your parcel. Can you receive it?")
                        }
                    }
                    if (_sessionState.value.isAutoRelayEnabled) {
                        aiVoiceRelayManager.startListening()
                    }
                }
            }
        }

        scope.launch {
            aiVoiceRelayManager.speechEvents.collect { spokenText ->
                if (spokenText.isNotBlank()) {
                    Log.i(TAG, "Manual user dictation captured: '$spokenText'. Relaying to caller!")
                    sendUserReply(spokenText)
                }
            }
        }

        scope.launch {
            aiVoiceRelayManager.livePartialTranscript.collect { partial ->
                _sessionState.update { it.copy(userLiveTranscript = partial) }
            }
        }

        scope.launch {
            aiVoiceRelayManager.isListening.collect { listening ->
                _sessionState.update { it.copy(isListeningToUserSpeech = listening) }
            }
        }
    }

    fun attachCall(call: Call, callerNumber: String, callerName: String?, isIncoming: Boolean) {
        if (hasActiveCall() && activeTelecomCall != call) {
            if (isIncoming && call.state == Call.STATE_RINGING) {
                attachCallWaiting(call, callerNumber, callerName)
            } else {
                attachSecondaryCall(call, callerNumber, callerName)
            }
            return
        }
        activeTelecomCall = call
        secondaryTelecomCall = null
        speechManager.isSimulatorMode = false
        val initialStatus = if (isIncoming) CallStatus.RINGING else CallStatus.DIALING
        val prev = _sessionState.value
        val initialAudioMode = prev.audioCallMode
        aiVoiceRelayManager.isAutoRelayActive = prev.isAutoRelayEnabled

        val savedContactName = DialerRepository.lookupContactName(appContext, callerNumber)
        val resolvedCallerName = if (!savedContactName.isNullOrBlank()) {
            savedContactName
        } else if (!callerName.isNullOrBlank()) {
            callerName
        } else {
            null
        }

        _sessionState.update {
            CallSessionState(
                status = initialStatus,
                audioCallMode = initialAudioMode,
                callerNumber = callerNumber,
                callerName = resolvedCallerName,
                isIncoming = isIncoming,
                isSimulated = false,
                isGreetingEnabled = prev.isGreetingEnabled,
                isAutoRelayEnabled = prev.isAutoRelayEnabled,
                isHideNumberOnCallerId = prev.isHideNumberOnCallerId,
                isAutoRecordEnabled = prev.isAutoRecordEnabled,
                operationMode = prev.operationMode,
                currentPersona = prev.currentPersona
            )
        }
        CallScreeningInCallService.syncNotification()
    }

    private fun attachCallWaiting(call: Call, callerNumber: String, callerName: String?) {
        secondaryTelecomCall = call
        val savedContactName = DialerRepository.lookupContactName(appContext, callerNumber)
        val resolvedCallerName = if (!savedContactName.isNullOrBlank()) {
            savedContactName
        } else if (!callerName.isNullOrBlank()) {
            callerName
        } else {
            null
        }

        _sessionState.update {
            it.copy(
                hasSecondaryCall = true,
                secondaryCallerNumber = callerNumber,
                secondaryCallerName = resolvedCallerName,
                canMergeCalls = false,
                canSwapCalls = true
            )
        }
        Log.i(TAG, "Attached incoming call waiting: $callerNumber ($resolvedCallerName)")
        CallScreeningInCallService.syncNotification()
    }

    private fun attachSecondaryCall(call: Call, callerNumber: String, callerName: String?) {
        secondaryTelecomCall = activeTelecomCall
        try {
            secondaryTelecomCall?.hold()
        } catch (e: Exception) {
            Log.w(TAG, "Failed putting primary call on hold", e)
        }

        val secNumber = _sessionState.value.callerNumber
        val secName = _sessionState.value.callerName

        activeTelecomCall = call
        val savedContactName = DialerRepository.lookupContactName(appContext, callerNumber)
        val resolvedCallerName = if (!savedContactName.isNullOrBlank()) {
            savedContactName
        } else if (!callerName.isNullOrBlank()) {
            callerName
        } else {
            null
        }

        _sessionState.update {
            it.copy(
                callerNumber = callerNumber,
                callerName = resolvedCallerName,
                status = CallStatus.DIALING,
                hasSecondaryCall = true,
                secondaryCallerNumber = secNumber,
                secondaryCallerName = secName,
                canMergeCalls = true,
                canSwapCalls = true
            )
        }
        Log.i(TAG, "Attached secondary call ($callerNumber), previous call placed on hold ($secNumber)")
        CallScreeningInCallService.syncNotification()
    }

    fun addCall(number: String) {
        val cleanNumber = number.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
        if (cleanNumber.isBlank()) return
        try {
            activeTelecomCall?.hold()
        } catch (e: Exception) {
            Log.w(TAG, "Error holding active call before addCall", e)
        }
        DialerRepository.placeCall(appContext, cleanNumber)
    }

    fun setConferenceable(canMerge: Boolean) {
        _sessionState.update { it.copy(canMergeCalls = canMerge) }
    }

    fun setConference(isConf: Boolean) {
        _sessionState.update {
            it.copy(
                isConference = isConf,
                canMergeCalls = false,
                canSwapCalls = false,
                hasSecondaryCall = false
            )
        }
        CallScreeningInCallService.syncNotification()
    }

    fun mergeConferenceCalls() {
        try {
            val call1 = activeTelecomCall
            val call2 = secondaryTelecomCall
            Log.i(TAG, "mergeConferenceCalls: call1=$call1, call2=$call2")
            var merged = false
            if (call1 != null && call2 != null) {
                try {
                    call1.conference(call2)
                    merged = true
                    Log.i(TAG, "call1.conference(call2) succeeded")
                } catch (e: Exception) {
                    Log.w(TAG, "call1.conference(call2) failed, trying call2.conference(call1)", e)
                    try {
                        call2.conference(call1)
                        merged = true
                        Log.i(TAG, "call2.conference(call1) succeeded")
                    } catch (e2: Exception) {
                        Log.e(TAG, "call2.conference(call1) also failed", e2)
                    }
                }
            }
            if (!merged) {
                val confList1 = call1?.conferenceableCalls
                if (!confList1.isNullOrEmpty()) {
                    call1?.conference(confList1[0])
                    merged = true
                    Log.i(TAG, "call1 merged via conferenceableCalls")
                } else {
                    val confList2 = call2?.conferenceableCalls
                    if (!confList2.isNullOrEmpty()) {
                        call2?.conference(confList2[0])
                        merged = true
                        Log.i(TAG, "call2 merged via conferenceableCalls")
                    }
                }
            }
            if (merged) {
                _sessionState.update {
                    it.copy(
                        isConference = true,
                        canMergeCalls = false,
                        canSwapCalls = false,
                        hasSecondaryCall = false
                    )
                }
                CallScreeningInCallService.syncNotification()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error merging conference calls", e)
        }
    }

    fun swapCalls() {
        val primary = activeTelecomCall ?: return
        val secondary = secondaryTelecomCall ?: return
        try {
            try {
                primary.hold()
            } catch (e: Exception) {
                Log.w(TAG, "Error holding primary call", e)
            }
            try {
                secondary.unhold()
            } catch (e: Exception) {
                Log.w(TAG, "Error unholding secondary call", e)
            }

            activeTelecomCall = secondary
            secondaryTelecomCall = primary

            val curNumber = _sessionState.value.callerNumber
            val curName = _sessionState.value.callerName
            val secNumber = _sessionState.value.secondaryCallerNumber ?: ""
            val secName = _sessionState.value.secondaryCallerName

            _sessionState.update {
                it.copy(
                    callerNumber = secNumber,
                    callerName = secName,
                    secondaryCallerNumber = curNumber,
                    secondaryCallerName = curName,
                    status = CallStatus.USER_TALKING,
                    hasSecondaryCall = true,
                    canSwapCalls = true,
                    canMergeCalls = true
                )
            }
            Log.i(TAG, "Swapped calls successfully: active=$secNumber ($secName), on-hold=$curNumber ($curName)")
            CallScreeningInCallService.syncNotification()
        } catch (e: Exception) {
            Log.e(TAG, "Error swapping calls", e)
        }
    }

    fun onCallRinging() {
        _sessionState.update { it.copy(status = CallStatus.RINGING) }
    }

    fun onCallDialing() {
        _sessionState.update { it.copy(status = CallStatus.DIALING) }
    }

    fun startScreening(greeting: String? = "Hello! How can I help you today?") {
        com.callscreener.app.telephony.RingtonePlayer.stop(appContext)
        pendingGreeting = if (_sessionState.value.isGreetingEnabled) greeting else null

        val isSim = _sessionState.value.isSimulated
        speechManager.isSimulatorMode = isSim

        val isPrivileged = RootPrivilegeManager.isRootModeActive(appContext) && !isSim
        speechManager.useDirectRootInjection = isPrivileged

        CallScreeningInCallService.isolateMicForAiRelay(true)
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isMicrophoneMute = false

        _sessionState.update {
            it.copy(
                status = CallStatus.SCREENING_ACTIVE,
                audioCallMode = AudioCallMode.AI_TYPE_SPEAK,
                isSpeakerOn = false
            )
        }

        aiVoiceRelayManager.isAutoRelayActive = _sessionState.value.isAutoRelayEnabled

        if (!isSim) {
            setSpeakerphone(false)
        }
        startDurationTimer()

        val call = activeTelecomCall
        if (call != null) {
            try {
                if (call.state == Call.STATE_RINGING) {
                    Log.i(TAG, "Answering incoming ringing call...")
                    call.answer(VideoProfile.STATE_AUDIO_ONLY)
                } else {
                    Log.i(TAG, "Call is not in ringing state (state=${call.state}), skipping answer()")
                    onCallConnected()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error answering call", e)
            }
        } else {
            onCallConnected()
        }
    }

    fun answerCallDirectly() {
        com.callscreener.app.telephony.RingtonePlayer.stop(appContext)
        pendingGreeting = null
        val isSim = _sessionState.value.isSimulated

        val call = activeTelecomCall
        if (call != null) {
            try {
                if (call.state == Call.STATE_RINGING) {
                    Log.i(TAG, "Answering incoming call directly...")
                    call.answer(VideoProfile.STATE_AUDIO_ONLY)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error answering call directly", e)
            }
        }

        switchToDirectMicMode()

        if (!isSim) {
            setSpeakerphone(false)
        }
        startDurationTimer()

        if (_sessionState.value.isAutoRecordEnabled && !_sessionState.value.isRecording) {
            startRecording()
        }
    }

    fun playDtmf(digit: Char) {
        try {
            com.callscreener.app.telephony.CallVibrator.vibrateClick(appContext)
            activeTelecomCall?.playDtmfTone(digit)
            activeTelecomCall?.stopDtmfTone()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending DTMF $digit", e)
        }
    }

    fun toggleHold() {
        val call = activeTelecomCall
        val newHold = _sessionState.value.status != CallStatus.ON_HOLD
        _sessionState.update {
            it.copy(status = if (newHold) CallStatus.ON_HOLD else CallStatus.USER_TALKING)
        }
        if (call != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    if (newHold) {
                        call.hold()
                    } else {
                        call.unhold()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error toggling call hold", e)
                }
            }
        }
    }

    fun setGreetingEnabled(enabled: Boolean) {
        _sessionState.update { it.copy(isGreetingEnabled = enabled) }
        Log.i(TAG, "Automated screening greeting enabled: $enabled")
    }

    fun setHideNumberEnabled(enabled: Boolean) {
        _sessionState.update { it.copy(isHideNumberOnCallerId = enabled) }
        Log.i(TAG, "Hide caller phone number on caller ID: $enabled")
    }

    fun setAutoRecordEnabled(enabled: Boolean) {
        _sessionState.update { it.copy(isAutoRecordEnabled = enabled) }
        Log.i(TAG, "Auto-record calls set to: $enabled")
    }

    fun setOperationMode(mode: AppOperationMode) {
        RootPrivilegeManager.setOperationMode(appContext, mode)
        _sessionState.update { it.copy(operationMode = mode) }
        val isRoot = mode == AppOperationMode.ROOT_ENHANCED && RootPrivilegeManager.isRootModeActive(appContext)
        speechManager.useDirectRootInjection = isRoot
        Log.i(TAG, "Switched app operation mode to: $mode (isRootActive=$isRoot)")
    }

    fun startRecording(): File? {
        val callerNum = _sessionState.value.callerNumber
        val isSim = _sessionState.value.isSimulated
        val file = recordingEngine.startRecording(callerNum, isSim)
        if (file != null) {
            _sessionState.update {
                it.copy(
                    isRecording = true,
                    recordingDurationSeconds = 0L,
                    lastRecordedFilePath = file.absolutePath
                )
            }
            startRecordingTimer()
            val msg = if (RootPrivilegeManager.isRootModeActive(appContext)) {
                "Dual-side call recording active (Root HAL audio capture)"
            } else {
                "Call recording active (Standard Microphone capture)"
            }
            addMessage(MessageSender.SYSTEM, msg)
        }
        return file
    }

    fun stopRecording(): File? {
        val file = recordingEngine.stopRecording()
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        _sessionState.update { it.copy(isRecording = false) }
        if (file != null) {
            addMessage(MessageSender.SYSTEM, "Call recording saved: ${file.name}")
        }
        return file
    }

    fun toggleRecording() {
        if (_sessionState.value.isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecordingTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = scope.launch {
            var secs = 0L
            while (isActive && recordingEngine.isRecording) {
                delay(1000L)
                secs++
                _sessionState.update { it.copy(recordingDurationSeconds = secs) }
            }
        }
    }

    fun onCallConnected() {
        com.callscreener.app.telephony.CallVibrator.vibrateCallConnected(appContext)
        scope.launch {
            val isSim = _sessionState.value.isSimulated
            val num = _sessionState.value.callerNumber
            if (num.isNotBlank()) {
                com.callscreener.app.notification.NotificationHelper.cancelMissedCallNotification(appContext, num)
            }
            speechManager.isSimulatorMode = isSim

            val isPrivileged = (RootPrivilegeManager.isPrivilegedApp(appContext) || RootPrivilegeManager.isRootAvailable()) && !isSim
            speechManager.useDirectRootInjection = isPrivileged

            _sessionState.update { current ->
                if (current.status == CallStatus.RINGING || current.status == CallStatus.DIALING) {
                    val targetStatus = if (current.isIncoming &&
                        current.audioCallMode == AudioCallMode.AI_TYPE_SPEAK &&
                        current.operationMode == AppOperationMode.ROOT_ENHANCED &&
                        !current.hasSecondaryCall
                    ) {
                        CallStatus.SCREENING_ACTIVE
                    } else {
                        CallStatus.USER_TALKING
                    }
                    current.copy(status = targetStatus)
                } else {
                    current
                }
            }

            if (!isSim) {
                delay(300L)
                setSpeakerphone(_sessionState.value.isSpeakerOn)

                if (_sessionState.value.status == CallStatus.SCREENING_ACTIVE &&
                    _sessionState.value.audioCallMode != AudioCallMode.DIRECT_MIC
                ) {
                    CallScreeningInCallService.isolateMicForAiRelay(true)
                    audioManager.mode = AudioManager.MODE_NORMAL
                    audioManager.isMicrophoneMute = false
                } else {
                    CallScreeningInCallService.isolateMicForAiRelay(false)
                }
            } else {
                delay(200L)
            }

            startDurationTimer()

            val greetingToSpeak = if (_sessionState.value.isGreetingEnabled && _sessionState.value.status == CallStatus.SCREENING_ACTIVE) pendingGreeting else null
            pendingGreeting = null
            if (!greetingToSpeak.isNullOrBlank()) {
                Log.i(TAG, "onCallConnected: Speaking greeting: $greetingToSpeak (isSim=$isSim)")
                sendUserReply(greetingToSpeak)
            } else if (_sessionState.value.isAutoRelayEnabled && _sessionState.value.status == CallStatus.SCREENING_ACTIVE) {
                delay(200L)
                aiVoiceRelayManager.startListening()
            }

            if (isSim) {
                scope.launch {
                    delay(2200L)
                    if (_sessionState.value.status == CallStatus.SCREENING_ACTIVE &&
                        _sessionState.value.messages.none { it.sender == MessageSender.CALLER }) {
                        addMessage(MessageSender.CALLER, "Hello, this is Dave from Express Logistics, calling about package #4829.")
                    }
                }
            }

            if (_sessionState.value.isAutoRecordEnabled && !_sessionState.value.isRecording) {
                delay(300L)
                startRecording()
            }
            CallScreeningInCallService.syncNotification()
        }
    }

    fun switchToAiTypeMode() {
        speechManager.stop()

        val isSim = _sessionState.value.isSimulated
        speechManager.isSimulatorMode = isSim

        val isPrivileged = RootPrivilegeManager.isRootModeActive(appContext) && !isSim
        speechManager.useDirectRootInjection = isPrivileged

        if (!isSim) {
            CallScreeningInCallService.isolateMicForAiRelay(true)
        }

        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isMicrophoneMute = false
        CallScreeningInCallService.setCallMute(false)

        _sessionState.update {
            it.copy(
                audioCallMode = AudioCallMode.AI_TYPE_SPEAK,
                status = CallStatus.SCREENING_ACTIVE,
                isMuted = false
            )
        }
        addMessage(MessageSender.SYSTEM, "Audio Mode: AI Voice Relay. Speak into your mic — your words will be converted to AI female voice automatically.")

        aiVoiceRelayManager.isAutoRelayActive = _sessionState.value.isAutoRelayEnabled
        if (_sessionState.value.isAutoRelayEnabled) {
            aiVoiceRelayManager.startListening()
        }
    }

    fun switchToDirectMicMode() {
        speechManager.stop()
        aiVoiceRelayManager.stopListening()

        audioManager.mode = AudioManager.MODE_IN_CALL

        try {
            audioManager.setParameters("voice_tx_mute=0")
            audioManager.setParameters("voice_tx_mute=false")
        } catch (_: Exception) {}

        scope.launch {
            try {
                val script = """
                    tinymix 'Voice_Tx Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'Incall_Music Audio Mixer MultiMedia1' 0 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer TX_Voice' 1 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer SLIM_0_TX_Voice' 1 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer PRI_TX_Voice' 1 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer TERT_MI2S_TX_Voice' 1 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer TX_CDC_DMA_TX_3' 1 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer SLIM_0_TX' 1 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer AFE_PCM_TX' 1 2>/dev/null || true
                    tinymix 'Voice Tx Mute' 0 2>/dev/null || true
                    tinymix 'Voice Tx Device Mute' 0 2>/dev/null || true
                """.trimIndent()
                RootPrivilegeManager.executeSuCommand(script)
            } catch (_: Exception) {}
        }

        audioManager.isMicrophoneMute = false
        CallScreeningInCallService.isolateMicForAiRelay(false)
        CallScreeningInCallService.setCallMute(false)

        _sessionState.update {
            it.copy(
                audioCallMode = AudioCallMode.DIRECT_MIC,
                status = CallStatus.USER_TALKING,
                isMuted = false
            )
        }
        addMessage(MessageSender.SYSTEM, "Audio Mode: Direct Microphone. Transmitting unprocessed natural voice.")
    }

    fun setVoicePersona(persona: VoicePersona) {
        speechManager.setVoicePersona(persona)
        aiVoiceRelayManager.targetLanguageTag = persona.localeTag
        _sessionState.update { it.copy(currentPersona = persona) }
        addMessage(MessageSender.SYSTEM, "Voice profile updated: ${persona.name} (${persona.tag})")
    }

    fun sendUserReply(text: String) {
        if (text.isBlank()) return

        val isSim = _sessionState.value.isSimulated
        speechManager.isSimulatorMode = isSim

        val isPrivileged = RootPrivilegeManager.isRootModeActive(appContext) && !isSim
        speechManager.useDirectRootInjection = isPrivileged

        if (!isSim) {
            setSpeakerphone(_sessionState.value.isSpeakerOn)
        }

        addMessage(MessageSender.USER_TTS, text)
        scope.launch {
            aiVoiceRelayManager.isTtsCurrentlySpeaking = true
            aiVoiceRelayManager.stopListening()
            delay(100L)
            speechManager.speak(text, isSim)
        }
    }

    private var localMonitorPlayer: MediaPlayer? = null

    fun playLocalAudioMonitor(audioFile: File, volume: Float = 0.20f) {
        stopLocalAudioMonitor()
        try {
            localMonitorPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(audioFile.absolutePath)
                setVolume(volume, volume)
                setOnCompletionListener {
                    stopLocalAudioMonitor()
                }
                setOnErrorListener { _, _, _ ->
                    stopLocalAudioMonitor()
                    true
                }
                prepare()
                start()
            }
            Log.d(TAG, "Started local audio monitor for ${audioFile.name} at volume $volume")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start local audio monitor", e)
        }
    }

    fun stopLocalAudioMonitor() {
        try {
            localMonitorPlayer?.stop()
            localMonitorPlayer?.release()
        } catch (_: Exception) {}
        localMonitorPlayer = null
    }

    fun pauseLocalAudioMonitor() {
        try {
            if (localMonitorPlayer?.isPlaying == true) {
                localMonitorPlayer?.pause()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to pause local audio monitor", e)
        }
    }

    fun resumeLocalAudioMonitor() {
        try {
            localMonitorPlayer?.start()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resume local audio monitor", e)
        }
    }

    private var customAudioJob: Job? = null

    fun playAudioPreset(preset: AudioPreset) {
        val isSim = _sessionState.value.isSimulated

        if (preset.isAudioFile && _sessionState.value.playingAudioPresetId == preset.id) {
            if (_sessionState.value.isAudioPresetPlaying && !_sessionState.value.isAudioPresetPaused) {
                pauseCustomAudio()
                return
            } else if (_sessionState.value.isAudioPresetPaused) {
                resumeCustomAudio()
                return
            }
        }

        stopCustomAudio()

        addMessage(MessageSender.USER_TTS, "[Preset] ${preset.title}")

        if (preset.isAudioFile) {
            val audioFile = File(preset.textOrPath)
            if (audioFile.exists()) {
                _sessionState.update {
                    it.copy(
                        playingAudioPresetId = preset.id,
                        playingAudioPresetTitle = preset.title,
                        isAudioPresetPlaying = true,
                        isAudioPresetPaused = false
                    )
                }

                customAudioJob = scope.launch {
                    aiVoiceRelayManager.stopListening()
                    if (isSim) {
                        presetManager.previewPreset(preset) {
                            onCustomAudioFinished(preset.id)
                        }
                    } else {
                        playLocalAudioMonitor(audioFile, volume = 0.20f)

                        speechManager.injector.injectAudioFile(audioFile) { success ->
                            stopLocalAudioMonitor()
                            Log.i(TAG, "Audio preset injected into call: $success")
                            onCustomAudioFinished(preset.id)
                            if (!_sessionState.value.isSimulated) {
                                setSpeakerphone(_sessionState.value.isSpeakerOn)
                            }
                            if (_sessionState.value.status == CallStatus.SCREENING_ACTIVE &&
                                _sessionState.value.audioCallMode == AudioCallMode.AI_TYPE_SPEAK &&
                                _sessionState.value.isAutoRelayEnabled
                            ) {
                                aiVoiceRelayManager.startListening()
                            }
                        }
                    }
                }
            } else {
                Log.w(TAG, "Preset audio file does not exist: ${preset.textOrPath}")
            }
        } else {
            sendUserReply(preset.textOrPath)
        }
    }

    fun pauseCustomAudio() {
        val isSim = _sessionState.value.isSimulated
        if (isSim) {
            presetManager.pausePreview()
        } else {
            speechManager.injector.pauseInjection()
            pauseLocalAudioMonitor()
        }
        _sessionState.update { it.copy(isAudioPresetPaused = true) }
        Log.i(TAG, "Custom audio paused: ${_sessionState.value.playingAudioPresetId}")
    }

    fun resumeCustomAudio() {
        val isSim = _sessionState.value.isSimulated
        if (isSim) {
            presetManager.resumePreview()
        } else {
            speechManager.injector.resumeInjection()
            resumeLocalAudioMonitor()
        }
        _sessionState.update { it.copy(isAudioPresetPaused = false, isAudioPresetPlaying = true) }
        Log.i(TAG, "Custom audio resumed: ${_sessionState.value.playingAudioPresetId}")
    }

    fun stopCustomAudio() {
        val isSim = _sessionState.value.isSimulated
        if (isSim) {
            presetManager.stopPreview()
        } else {
            speechManager.injector.stopInjection()
            stopLocalAudioMonitor()
        }
        customAudioJob?.cancel()
        customAudioJob = null
        _sessionState.update {
            it.copy(
                playingAudioPresetId = null,
                playingAudioPresetTitle = null,
                isAudioPresetPlaying = false,
                isAudioPresetPaused = false
            )
        }
        Log.i(TAG, "Custom audio stopped")
    }

    private fun onCustomAudioFinished(presetId: String) {
        if (_sessionState.value.playingAudioPresetId == presetId) {
            _sessionState.update {
                it.copy(
                    playingAudioPresetId = null,
                    playingAudioPresetTitle = null,
                    isAudioPresetPlaying = false,
                    isAudioPresetPaused = false
                )
            }
            customAudioJob = null
            if (!_sessionState.value.isSimulated) {
                setSpeakerphone(_sessionState.value.isSpeakerOn)
            }
        }
    }

    fun toggleAiVoiceListening() {
        if (aiVoiceRelayManager.isListening.value) {
            aiVoiceRelayManager.stopListening()
        } else {
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isMicrophoneMute = false
            aiVoiceRelayManager.startListening()
        }
    }

    fun setAutoRelayEnabled(enabled: Boolean) {
        _sessionState.update { it.copy(isAutoRelayEnabled = enabled) }
        aiVoiceRelayManager.isAutoRelayActive = enabled
        if (enabled && !_sessionState.value.isTtsSpeaking && _sessionState.value.audioCallMode == AudioCallMode.AI_TYPE_SPEAK) {
            aiVoiceRelayManager.startListening()
        } else if (!enabled) {
            aiVoiceRelayManager.stopListening()
        }
    }

    fun takeOverCall() {
        switchToDirectMicMode()
    }

    fun answer() = answerCallDirectly()

    fun hangUpCurrentCall() {
        val active = activeTelecomCall
        val secondary = secondaryTelecomCall
        if (active != null && secondary != null) {
            try {
                Log.i(TAG, "hangUpCurrentCall: disconnecting active call, will promote held secondary call")
                active.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting active call", e)
            }
        } else {
            hangUp()
        }
    }

    fun hangUp() {
        com.callscreener.app.telephony.RingtonePlayer.stop(appContext)
        com.callscreener.app.telephony.CallVibrator.vibrateCallEnded(appContext)
        com.callscreener.app.notification.NotificationHelper.cancelCallNotification(appContext)
        if (_sessionState.value.isIncoming && _sessionState.value.status == CallStatus.RINGING) {
            val num = _sessionState.value.callerNumber
            val name = _sessionState.value.callerName
            if (num.isNotBlank()) {
                com.callscreener.app.notification.NotificationHelper.postMissedCallNotification(appContext, num, name)
            }
        }
        if (_sessionState.value.isRecording) {
            stopRecording()
        }
        speechManager.stop()
        stopCustomAudio()
        speechManager.isSimulatorMode = false
        aiVoiceRelayManager.release()
        stopLocalAudioMonitor()
        durationJob?.cancel()

        try {
            activeTelecomCall?.disconnect()
            secondaryTelecomCall?.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting calls", e)
        }
        activeTelecomCall = null
        secondaryTelecomCall = null

        _sessionState.update { it.copy(status = CallStatus.ENDED) }
        resetAudioMode()
    }

    fun onCallRemoved(call: Call) {
        if (call == secondaryTelecomCall) {
            secondaryTelecomCall = null
            _sessionState.update {
                it.copy(
                    hasSecondaryCall = false,
                    secondaryCallerNumber = null,
                    secondaryCallerName = null,
                    canMergeCalls = false,
                    canSwapCalls = false
                )
            }
            Log.i(TAG, "Secondary call disconnected; maintaining active primary call")
            CallScreeningInCallService.syncNotification()
        } else if (call == activeTelecomCall && secondaryTelecomCall != null) {
            activeTelecomCall = secondaryTelecomCall
            secondaryTelecomCall = null
            val promotedNum = _sessionState.value.secondaryCallerNumber ?: ""
            val promotedName = _sessionState.value.secondaryCallerName
            try {
                activeTelecomCall?.unhold()
            } catch (_: Exception) {}
            _sessionState.update {
                it.copy(
                    callerNumber = promotedNum,
                    callerName = promotedName,
                    hasSecondaryCall = false,
                    secondaryCallerNumber = null,
                    secondaryCallerName = null,
                    canMergeCalls = false,
                    canSwapCalls = false,
                    status = CallStatus.USER_TALKING
                )
            }
            Log.i(TAG, "Primary call disconnected; unheld and promoted secondary call to active: $promotedNum ($promotedName)")
            CallScreeningInCallService.syncNotification()
        } else {
            onCallEnded()
        }
    }

    fun onCallEnded() {
        com.callscreener.app.telephony.RingtonePlayer.stop(appContext)
        com.callscreener.app.telephony.CallVibrator.vibrateCallEnded(appContext)
        com.callscreener.app.notification.NotificationHelper.cancelCallNotification(appContext)
        if (_sessionState.value.isIncoming && _sessionState.value.status == CallStatus.RINGING) {
            val num = _sessionState.value.callerNumber
            val name = _sessionState.value.callerName
            if (num.isNotBlank()) {
                com.callscreener.app.notification.NotificationHelper.postMissedCallNotification(appContext, num, name)
            }
        }
        if (_sessionState.value.isRecording) {
            stopRecording()
        }
        speechManager.stop()
        stopCustomAudio()
        speechManager.isSimulatorMode = false
        aiVoiceRelayManager.release()
        stopLocalAudioMonitor()
        durationJob?.cancel()
        activeTelecomCall = null
        secondaryTelecomCall = null
        _sessionState.update { it.copy(status = CallStatus.ENDED) }
        resetAudioMode()
    }

    fun toggleMute() {
        val newMute = !_sessionState.value.isMuted
        _sessionState.update { it.copy(isMuted = newMute) }
        scope.launch(Dispatchers.IO) {
            try {
                audioManager.isMicrophoneMute = newMute
                if (newMute) {
                    CallScreeningInCallService.setCallMute(true)
                    aiVoiceRelayManager.stopListening()
                } else {
                    if (_sessionState.value.audioCallMode == AudioCallMode.DIRECT_MIC) {
                        CallScreeningInCallService.setCallMute(false)
                    } else {
                        CallScreeningInCallService.isolateMicForAiRelay(true)
                        if (_sessionState.value.isAutoRelayEnabled && !_sessionState.value.isTtsSpeaking) {
                            aiVoiceRelayManager.startListening()
                        }
                    }
                }
                CallScreeningInCallService.syncNotification()
            } catch (e: Exception) {
                Log.e(TAG, "Error applying mute state", e)
            }
        }
    }

    fun toggleSpeaker() {
        val newSpeaker = !_sessionState.value.isSpeakerOn
        _sessionState.update { it.copy(isSpeakerOn = newSpeaker) }
        scope.launch(Dispatchers.IO) {
            setSpeakerphone(newSpeaker)
            CallScreeningInCallService.syncNotification()
        }
    }

    private fun setSpeakerphone(on: Boolean) {
        try {
            CallScreeningInCallService.setSpeaker(on)
            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = on

            if (_sessionState.value.audioCallMode != AudioCallMode.AI_TYPE_SPEAK) {
                audioManager.mode = AudioManager.MODE_IN_CALL
            } else {
                audioManager.mode = AudioManager.MODE_NORMAL
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (on) {
                    proximityManager.release()
                    val speaker = audioManager.availableCommunicationDevices.firstOrNull {
                        it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                    }
                    if (speaker != null) {
                        audioManager.setCommunicationDevice(speaker)
                    }
                } else {
                    val btDevice = audioManager.availableCommunicationDevices.firstOrNull {
                        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                        it.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                    }
                    if (btDevice != null) {
                        proximityManager.release()
                        Log.i(TAG, "Routing audio to connected Bluetooth/Headset device: ${btDevice.productName}")
                        audioManager.setCommunicationDevice(btDevice)
                    } else {
                        proximityManager.acquire()
                        val earpiece = audioManager.availableCommunicationDevices.firstOrNull {
                            it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                        }
                        if (earpiece != null) {
                            audioManager.setCommunicationDevice(earpiece)
                        } else {
                            audioManager.clearCommunicationDevice()
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = on
                if (on) {
                    proximityManager.release()
                } else {
                    @Suppress("DEPRECATION")
                    if (audioManager.isBluetoothScoAvailableOffCall) {
                        proximityManager.release()
                        audioManager.startBluetoothSco()
                        audioManager.isBluetoothScoOn = true
                    } else {
                        proximityManager.acquire()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle speakerphone", e)
        }
    }

    fun toggleSpeechJammer() {
        if (speechJammerEngine.isJammerActive.value) {
            speechJammerEngine.stopJammer()
            if (_sessionState.value.audioCallMode == AudioCallMode.AI_TYPE_SPEAK && !speechJammerEngine.isEchoUserVoiceEnabled) {
                CallScreeningInCallService.isolateMicForAiRelay(true)
            }
        } else {
            audioManager.mode = AudioManager.MODE_IN_CALL
            audioManager.isMicrophoneMute = false
            CallScreeningInCallService.setCallMute(false)
            CallScreeningInCallService.isolateMicForAiRelay(false)
            if (speechJammerEngine.isEchoUserVoiceEnabled) {
                _sessionState.update { it.copy(isMuted = false) }
            }
            speechJammerEngine.startJammer(forTestLab = false)
        }
    }

    fun toggleEchoUserVoice() {
        val newState = !speechJammerEngine.isEchoUserVoiceEnabled
        setEchoUserVoice(newState)
    }

    fun setEchoUserVoice(enabled: Boolean) {
        speechJammerEngine.setEchoUserVoice(enabled)
        _sessionState.update { it.copy(isEchoUserVoiceEnabled = enabled) }
        if (enabled) {
            audioManager.isMicrophoneMute = false
            CallScreeningInCallService.setCallMute(false)
            CallScreeningInCallService.isolateMicForAiRelay(false)
            _sessionState.update { it.copy(isMuted = false) }

            if (!speechJammerEngine.isJammerActive.value) {
                audioManager.mode = AudioManager.MODE_IN_CALL
                speechJammerEngine.startJammer(forTestLab = false)
            }
        } else {
            if (_sessionState.value.audioCallMode == AudioCallMode.AI_TYPE_SPEAK) {
                CallScreeningInCallService.isolateMicForAiRelay(true)
            }
        }
    }

    private fun resetAudioMode() {
        try {
            proximityManager.release()
            if (speechJammerEngine.isJammerActive.value) {
                speechJammerEngine.stopJammer()
            }
            CallScreeningInCallService.setSpeaker(false)
            CallScreeningInCallService.setCallMute(false)
            CallScreeningInCallService.isolateMicForAiRelay(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager.clearCommunicationDevice()
            } else {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = false
            }
            audioManager.isMicrophoneMute = false
            audioManager.mode = AudioManager.MODE_NORMAL

            if (RootPrivilegeManager.isRootAvailable()) {
                scope.launch {
                    try {
                        RootPrivilegeManager.executeSuCommand("""
                            tinymix 'Voice_Tx Mixer MultiMedia1' 0 2>/dev/null || true
                            tinymix 'Voice_Tx Mixer MultiMedia2' 0 2>/dev/null || true
                            tinymix 'Incall_Music Audio Mixer MultiMedia1' 0 2>/dev/null || true
                            tinymix 'Incall_Music Audio Mixer MultiMedia2' 0 2>/dev/null || true
                            tinymix 'Incall_Music_2 Audio Mixer MultiMedia1' 0 2>/dev/null || true
                            tinymix 'Voice_Rx Mixer MultiMedia1' 0 2>/dev/null || true
                            tinymix 'AFE_PCM_RX Audio Mixer MultiMedia1' 0 2>/dev/null || true
                            tinymix 'PRI_MI2S_RX Audio Mixer MultiMedia1' 0 2>/dev/null || true
                            tinymix 'TERT_MI2S_RX Audio Mixer MultiMedia1' 0 2>/dev/null || true
                            tinymix 'SLIM_0_RX Audio Mixer MultiMedia1' 0 2>/dev/null || true
                            tinymix 'Voice_Tx Mixer TX_Voice' 1 2>/dev/null || true
                            tinymix 'Voice Tx Mute' 0 2>/dev/null || true
                            tinymix 'Voice Tx Device Mute' 0 2>/dev/null || true
                        """.trimIndent())
                    } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reset audio mode", e)
        }
    }

    private fun addMessage(sender: MessageSender, text: String) {
        val message = ChatMessage(sender = sender, text = text)
        _sessionState.update { current ->
            current.copy(messages = current.messages + message)
        }
    }

    private fun startDurationTimer() {
        durationJob?.cancel()
        durationJob = scope.launch {
            var seconds = 0L
            while (isActive) {
                delay(1000L)
                seconds++
                _sessionState.update { it.copy(durationSeconds = seconds) }
            }
        }
    }

    fun startSimulatedCall(simulatedNumber: String = "+1 (555) 382-9910", simulatedName: String = "Delivery Courier") {
        speechManager.isSimulatorMode = true
        speechManager.useDirectRootInjection = false
        audioManager.mode = AudioManager.MODE_NORMAL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.clearCommunicationDevice()
        }
        val prev = _sessionState.value
        val savedName = DialerRepository.lookupContactName(appContext, simulatedNumber)
        val resolvedName = if (!savedName.isNullOrBlank()) savedName else simulatedName
        _sessionState.update {
            CallSessionState(
                status = CallStatus.RINGING,
                audioCallMode = prev.audioCallMode,
                callerNumber = simulatedNumber,
                callerName = resolvedName,
                isIncoming = true,
                isSimulated = true,
                isGreetingEnabled = prev.isGreetingEnabled,
                isAutoRelayEnabled = prev.isAutoRelayEnabled,
                isHideNumberOnCallerId = prev.isHideNumberOnCallerId,
                isAutoRecordEnabled = prev.isAutoRecordEnabled,
                currentPersona = prev.currentPersona,
                operationMode = prev.operationMode
            )
        }
    }

    fun testSpeech(text: String) {
        speechManager.isSimulatorMode = true
        speechManager.speak(text, isSimulated = true)
    }

    fun simulateCallerResponse(response: String) {
        scope.launch {
            delay(1000L)
            addMessage(MessageSender.CALLER, response)
        }
    }

    companion object {
        private const val TAG = "CallSessionManager"

        @Volatile
        private var INSTANCE: CallSessionManager? = null

        fun getInstance(context: Context): CallSessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CallSessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
