package com.callscreener.app.session

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.telecom.Call
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
    private var durationJob: Job? = null
    private var pendingGreeting: String? = null

    private val speechManager = CallSpeechManager(appContext)
    val aiVoiceRelayManager = AiVoiceRelayManager(appContext)
    val recordingEngine = CallRecordingEngine(appContext)
    val presetManager = AudioPresetManager(appContext)
    val quickReplyManager = QuickReplyManager(appContext)
    val voiceChangerEngine = com.callscreener.app.engine.RealtimeVoiceChangerEngine()
    private val privacyPrefs = appContext.getSharedPreferences("call_screener_privacy_prefs", Context.MODE_PRIVATE)
    private var recordingTimerJob: Job? = null
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _sessionState = MutableStateFlow(CallSessionState())
    val sessionState: StateFlow<CallSessionState> = _sessionState.asStateFlow()

    init {
        // Observe customized quick replies
        scope.launch {
            quickReplyManager.quickReplies.collect { replies ->
                _sessionState.update { it.copy(quickReplies = replies) }
            }
        }

        // Initialize operating mode (Standard vs Root-Enhanced)
        val initialMode = RootPrivilegeManager.getOperationMode(appContext)
        val savedClir = privacyPrefs.getBoolean("clir_masking_enabled", false)
        val savedClirSavedContacts = privacyPrefs.getBoolean("clir_mask_saved_contacts_enabled", false)
        _sessionState.update {
            it.copy(
                operationMode = initialMode,
                isClirMaskingEnabled = savedClir,
                isClirMaskSavedContactsEnabled = savedClirSavedContacts
            )
        }

        // Observe TTS speaking state
        scope.launch {
            speechManager.isSpeaking.collect { speaking ->
                _sessionState.update { it.copy(isTtsSpeaking = speaking) }
                aiVoiceRelayManager.isTtsCurrentlySpeaking = speaking
                if (speaking) {
                    aiVoiceRelayManager.stopListening()
                } else {
                    // TTS finished speaking: resume listening to user if Auto-Relay is active
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

        // Local preview monitor for in-call speech synthesis (soft ~22% volume so user can hear what is playing)
        speechManager.onAudioFileReadyForMonitor = { file ->
            playLocalAudioMonitor(file, volume = 0.22f)
        }
        speechManager.onAudioFileMonitorStop = {
            stopLocalAudioMonitor()
        }

        // When TTS finishes speaking
        speechManager.onSpeechFinished = {
            scope.launch {
                stopLocalAudioMonitor()
                aiVoiceRelayManager.isTtsCurrentlySpeaking = false
                delay(300L)
                val inCall = _sessionState.value.status == CallStatus.SCREENING_ACTIVE ||
                             _sessionState.value.status == CallStatus.USER_TALKING ||
                             _sessionState.value.status == CallStatus.DIALING
                if (inCall && _sessionState.value.audioCallMode == AudioCallMode.AI_TYPE_SPEAK) {
                    if (_sessionState.value.isSimulated) {
                        // In simulator, automatically simulate caller's response if messages only has greeting
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

        // When user speaks into mic, AI Voice Relay captures it and immediately synthesizes it in AI female voice!
        scope.launch {
            aiVoiceRelayManager.speechEvents.collect { userSpokenText ->
                if (userSpokenText.isNotBlank()) {
                    Log.i(TAG, "AI Voice Relay captured speech: '$userSpokenText'. Relaying to caller in female AI voice!")
                    sendUserReply(userSpokenText)
                }
            }
        }

        // Observe live partial transcription of user's voice
        scope.launch {
            aiVoiceRelayManager.livePartialTranscript.collect { partial ->
                _sessionState.update { it.copy(userLiveTranscript = partial) }
            }
        }

        // Observe listening state
        scope.launch {
            aiVoiceRelayManager.isListening.collect { listening ->
                _sessionState.update { it.copy(isListeningToUserSpeech = listening) }
            }
        }
    }

    /**
     * Called when a Telecom call is added via InCallService.
     */
    fun attachCall(call: Call, callerNumber: String, callerName: String?, isIncoming: Boolean) {
        activeTelecomCall = call
        speechManager.isSimulatorMode = false
        val initialStatus = if (isIncoming) CallStatus.RINGING else CallStatus.DIALING
        val prev = _sessionState.value
        val initialAudioMode = prev.audioCallMode
        aiVoiceRelayManager.isAutoRelayActive = prev.isAutoRelayEnabled

        // Ensure phonebook saved contact name always takes precedence over network owner name
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
                isClirMaskingEnabled = prev.isClirMaskingEnabled,
                isClirMaskSavedContactsEnabled = prev.isClirMaskSavedContactsEnabled,
                isIncognitoDialActive = prev.isIncognitoDialActive,
                isAutoRecordEnabled = prev.isAutoRecordEnabled,
                operationMode = prev.operationMode,
                currentPersona = prev.currentPersona
            )
        }
    }

    fun onCallRinging() {
        _sessionState.update { it.copy(status = CallStatus.RINGING) }
    }

    fun onCallDialing() {
        _sessionState.update { it.copy(status = CallStatus.DIALING) }
    }

    /**
     * Answer the call and initiate the AI voice screening loop.
     */
    fun startScreening(greeting: String? = "Hello. The person you have reached is using an automated assistant. Please state your name and reason for calling.") {
        com.callscreener.app.telephony.RingtonePlayer.stop(appContext)
        pendingGreeting = if (_sessionState.value.isGreetingEnabled) greeting else null

        val isSim = _sessionState.value.isSimulated
        speechManager.isSimulatorMode = isSim

        val isPrivileged = (RootPrivilegeManager.isPrivilegedApp(appContext) || RootPrivilegeManager.isRootAvailable()) && !isSim
        speechManager.useDirectRootInjection = isPrivileged

        // CRITICAL PRIVACY FIX: Mute Telecom in-call uplink so user's natural microphone voice NEVER leaks to the caller!
        CallScreeningInCallService.setCallMute(true)
        audioManager.isMicrophoneMute = false // Unmuted ONLY for local SpeechRecognizer capture

        _sessionState.update {
            it.copy(
                status = CallStatus.SCREENING_ACTIVE,
                audioCallMode = AudioCallMode.AI_TYPE_SPEAK,
                isSpeakerOn = !isPrivileged && !isSim
            )
        }

        if (!isSim) {
            if (isPrivileged) {
                Log.i(TAG, "Root/Privileged mode active: Silent digital call audio injection enabled!")
                setSpeakerphone(false) // Stay silent locally, caller hears audio in-system!
            } else {
                setSpeakerphone(true)
            }
        }
        startDurationTimer()

        val call = activeTelecomCall
        if (call != null) {
            try {
                // Strictly only answer if call is actually ringing to prevent crash on outgoing/active calls
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
            // Simulator mode: trigger speech immediately without carrier call dependencies
            onCallConnected()
        }
    }

    /**
     * Answer incoming call directly without automated screening (standard everyday call).
     */
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

        // Switch immediately to standard natural microphone mode
        switchToDirectMicMode()

        if (!isSim) {
            setSpeakerphone(false) // Standard handset earpiece for normal direct calling
        }
        startDurationTimer()

        if (_sessionState.value.isAutoRecordEnabled && !_sessionState.value.isRecording) {
            startRecording()
        }
    }

    /**
     * Play DTMF tone during active direct call for IVR keypad navigation.
     */
    fun playDtmf(digit: Char) {
        try {
            activeTelecomCall?.playDtmfTone(digit)
            activeTelecomCall?.stopDtmfTone()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending DTMF $digit", e)
        }
    }

    /**
     * Toggle call hold during active call.
     */
    fun toggleHold() {
        val call = activeTelecomCall
        if (call != null) {
            try {
                if (_sessionState.value.status == CallStatus.ON_HOLD) {
                    call.unhold()
                    _sessionState.update { it.copy(status = CallStatus.USER_TALKING) }
                } else {
                    call.hold()
                    _sessionState.update { it.copy(status = CallStatus.ON_HOLD) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling call hold", e)
            }
        } else {
            _sessionState.update {
                it.copy(status = if (it.status == CallStatus.ON_HOLD) CallStatus.USER_TALKING else CallStatus.ON_HOLD)
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

    fun setClirMaskingEnabled(enabled: Boolean) {
        privacyPrefs.edit().putBoolean("clir_masking_enabled", enabled).apply()
        _sessionState.update { it.copy(isClirMaskingEnabled = enabled) }
        Log.i(TAG, "CLIR (#31#) caller ID masking set to: $enabled")
    }

    fun setClirMaskSavedContactsEnabled(enabled: Boolean) {
        privacyPrefs.edit().putBoolean("clir_mask_saved_contacts_enabled", enabled).apply()
        _sessionState.update { it.copy(isClirMaskSavedContactsEnabled = enabled) }
        Log.i(TAG, "CLIR mask on saved contacts set to: $enabled")
    }

    fun setIncognitoDialActive(active: Boolean) {
        _sessionState.update { it.copy(isIncognitoDialActive = active) }
        Log.i(TAG, "Transient Incognito Dial active: $active")
    }

    fun toggleIncognitoDial(): Boolean {
        val newState = !_sessionState.value.isIncognitoDialActive
        setIncognitoDialActive(newState)
        return newState
    }

    fun setAutoRecordEnabled(enabled: Boolean) {
        _sessionState.update { it.copy(isAutoRecordEnabled = enabled) }
        Log.i(TAG, "Auto-record calls set to: $enabled")
    }

    fun setOperationMode(mode: AppOperationMode) {
        RootPrivilegeManager.setOperationMode(appContext, mode)
        _sessionState.update { it.copy(operationMode = mode) }
        Log.i(TAG, "Switched app operation mode to: $mode")
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
            val msg = if (_sessionState.value.operationMode == AppOperationMode.ROOT_ENHANCED) {
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

    /**
     * Triggered when the carrier phone call becomes ACTIVE or Simulator initiates audio.
     */
    fun onCallConnected() {
        scope.launch {
            val isSim = _sessionState.value.isSimulated
            val num = _sessionState.value.callerNumber
            if (num.isNotBlank()) {
                com.callscreener.app.notification.NotificationHelper.cancelMissedCallNotification(appContext, num)
            }
            speechManager.isSimulatorMode = isSim

            val isPrivileged = (RootPrivilegeManager.isPrivilegedApp(appContext) || RootPrivilegeManager.isRootAvailable()) && !isSim
            speechManager.useDirectRootInjection = isPrivileged

            if (!isSim) {
                delay(500L) // Wait for hardware audio channel to settle
                if (isPrivileged) {
                    setSpeakerphone(false) // Direct silent injection
                } else {
                    setSpeakerphone(true)
                }

                // If in AI Relay or Screening mode, isolate physical mic from carrier uplink immediately
                if (_sessionState.value.audioCallMode != AudioCallMode.DIRECT_MIC) {
                    CallScreeningInCallService.setCallMute(true)
                    RootPrivilegeManager.executeAsync {
                        val script = """
                            cmd telecom set-call-muted true 2>/dev/null || true
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
                            tinymix 'Voice Tx Mute' 1 2>/dev/null || true
                            tinymix 'Voice Tx Device Mute' 0 2>/dev/null || true
                        """.trimIndent()
                        RootPrivilegeManager.executeSuCommand(script)
                    }
                } else {
                    CallScreeningInCallService.setCallMute(false)
                }
            } else {
                delay(200L)
            }

            _sessionState.update { current ->
                if (current.status == CallStatus.RINGING || current.status == CallStatus.DIALING) {
                    current.copy(status = CallStatus.SCREENING_ACTIVE)
                } else {
                    current
                }
            }
            startDurationTimer()

            val greetingToSpeak = if (_sessionState.value.isGreetingEnabled) pendingGreeting else null
            pendingGreeting = null
            if (!greetingToSpeak.isNullOrBlank()) {
                Log.i(TAG, "onCallConnected: Speaking greeting: $greetingToSpeak (isSim=$isSim)")
                sendUserReply(greetingToSpeak)
            } else if (_sessionState.value.isAutoRelayEnabled && _sessionState.value.audioCallMode == AudioCallMode.AI_TYPE_SPEAK) {
                // If greeting disabled, immediately arm AI Voice Relay
                delay(200L)
                aiVoiceRelayManager.startListening()
            }

            if (_sessionState.value.isAutoRecordEnabled && !_sessionState.value.isRecording) {
                delay(300L)
                startRecording()
            }
        }
    }

    /**
     * MODE 1: Switch to AI Type-to-Speak Mode.
     * What you type is spoken by the selected female voice directly to the caller.
     * Physical mic is unrouted from carrier uplink via ALSA so speech to AI doesn't leak to caller.
     */
    fun switchToAiTypeMode() {
        speechManager.stop()

        val isSim = _sessionState.value.isSimulated
        speechManager.isSimulatorMode = isSim

        val isPrivileged = (RootPrivilegeManager.isPrivilegedApp(appContext) || RootPrivilegeManager.isRootAvailable()) && !isSim
        speechManager.useDirectRootInjection = isPrivileged

        // Ensure physical mic is unrouted from caller uplink so natural voice never leaks to caller
        if (!isSim) {
            scope.launch {
                try {
                    val script = """
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
                    RootPrivilegeManager.executeSuCommand(script)
                } catch (_: Exception) {}
            }
        }

        if (voiceChangerEngine.isVoiceChangerActive.value) {
            voiceChangerEngine.stop()
        }

        audioManager.isMicrophoneMute = false
        CallScreeningInCallService.setCallMute(true) // Carrier uplink MUST be muted so caller cannot hear user's real voice!

        _sessionState.update {
            it.copy(
                audioCallMode = AudioCallMode.AI_TYPE_SPEAK,
                isRealtimeVoiceChangerActive = false,
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

    /**
     * MODE 2: Switch to Direct Real Mic Mode.
     * Raw mic → Telecom uplink → caller (no processing).
     */
    fun switchToDirectMicMode() {
        speechManager.stop()
        aiVoiceRelayManager.release()
        if (voiceChangerEngine.isVoiceChangerActive.value) {
            voiceChangerEngine.stop()
        }

        audioManager.mode = AudioManager.MODE_IN_CALL

        // Restore hardware audio parameters
        try {
            audioManager.setParameters("voice_tx_mute=0")
            audioManager.setParameters("voice_tx_mute=false")
        } catch (_: Exception) {}

        // Restore ALSA mixer controls via root
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

        // Unmute telephony uplink so your natural voice transmits directly
        audioManager.isMicrophoneMute = false
        CallScreeningInCallService.setCallMute(false)

        _sessionState.update {
            it.copy(
                audioCallMode = AudioCallMode.DIRECT_MIC,
                isRealtimeVoiceChangerActive = false,
                status = CallStatus.USER_TALKING,
                isMuted = false
            )
        }
        addMessage(MessageSender.SYSTEM, "Audio Mode: Direct Microphone. Transmitting unprocessed natural voice.")
    }

    /**
     * MODE 3: Switch to Real-Time Voice Changer Mode.
     * Mic -> In-memory ultra-low-latency DSP (Pitch Shift, Formant Smoothing, Robot, Radio, Deep Voice)
     * -> Telecom voice uplink. Raw physical microphone is isolated so caller NEVER hears your real voice!
     */
    fun switchToVoiceChangerMode(persona: VoicePersona? = null) {
        speechManager.stop()
        aiVoiceRelayManager.release()

        val targetPersona = persona ?: _sessionState.value.currentPersona
        voiceChangerEngine.setPersona(targetPersona)

        if (voiceChangerEngine.isVoiceChangerActive.value) {
            voiceChangerEngine.stop()
        }

        audioManager.mode = AudioManager.MODE_IN_CALL
        audioManager.isMicrophoneMute = false
        CallScreeningInCallService.setCallMute(true) // Carrier uplink muted from raw mic

        // Disconnect raw physical mic from hardware voice uplink via root ALSA mixer
        scope.launch {
            try {
                val script = """
                    tinymix 'Voice_Tx Mixer MultiMedia1' 1 2>/dev/null || true
                    tinymix 'Incall_Music Audio Mixer MultiMedia1' 1 2>/dev/null || true
                    tinymix 'Incall_Music Audio Mixer MultiMedia2' 1 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer TX_CDC_DMA_TX_3' 0 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer SLIM_0_TX' 0 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer AFE_PCM_TX' 0 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer PRI_TX_Voice' 0 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer TERT_MI2S_TX_Voice' 0 2>/dev/null || true
                    tinymix 'Voice_Tx Mixer TX_Voice' 0 2>/dev/null || true
                """.trimIndent()
                RootPrivilegeManager.executeSuCommand(script)
            } catch (_: Exception) {}
        }

        val isSim = _sessionState.value.isSimulated
        voiceChangerEngine.start(
            streamType = if (isSim) AudioManager.STREAM_MUSIC else AudioManager.STREAM_VOICE_CALL,
            context = appContext,
            isTestMode = isSim
        )

        _sessionState.update {
            it.copy(
                audioCallMode = AudioCallMode.VOICE_CHANGER,
                isRealtimeVoiceChangerActive = true,
                status = CallStatus.USER_TALKING,
                currentPersona = targetPersona,
                isMuted = false
            )
        }
        addMessage(MessageSender.SYSTEM, "Audio Mode: Live Voice Changer (${targetPersona.name}). Your voice is modified in real-time. Caller cannot hear your real voice.")
    }

    /**
     * Change AI Voice Persona in real time.
     */
    fun setVoicePersona(persona: VoicePersona) {
        speechManager.setVoicePersona(persona)
        aiVoiceRelayManager.targetLanguageTag = persona.localeTag
        voiceChangerEngine.setPersona(persona)
        _sessionState.update { it.copy(currentPersona = persona) }
        addMessage(MessageSender.SYSTEM, "Voice profile updated: ${persona.name} (${persona.tag})")
    }

    /**
     * Send user typed text or quick reply via TextToSpeech to the caller.
     */
    fun sendUserReply(text: String) {
        if (text.isBlank()) return

        val isSim = _sessionState.value.isSimulated
        speechManager.isSimulatorMode = isSim

        val isPrivileged = (RootPrivilegeManager.isPrivilegedApp(appContext) || RootPrivilegeManager.isRootAvailable()) && !isSim
        speechManager.useDirectRootInjection = isPrivileged

        if (!isPrivileged && !isSim) {
            setSpeakerphone(true)
        } else if (!isSim) {
            setSpeakerphone(false)
        }

        addMessage(MessageSender.USER_TTS, text)
        scope.launch {
            // Flag TTS speaking so mic doesn't capture TTS output
            aiVoiceRelayManager.isTtsCurrentlySpeaking = true
            aiVoiceRelayManager.stopListening()
            delay(100L)
            speechManager.speak(text, isSim)
        }
    }

    private var localMonitorPlayer: MediaPlayer? = null

    /**
     * Plays a gentle, comfortable sidetone preview of custom audio on the local device
     * (earpiece/headset/speaker at ~20% volume) so the user knows exactly what is playing
     * to the caller, without causing acoustic feedback.
     */
    fun playLocalAudioMonitor(audioFile: File, volume: Float = 0.20f) {
        stopLocalAudioMonitor()
        try {
            localMonitorPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
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

    /**
     * Injects an Audio Preset directly into active call or simulator.
     * Uses Root Call Audio Injector for 100% digital playback with ZERO physical mic noise!
     */
    fun playAudioPreset(preset: AudioPreset) {
        val isSim = _sessionState.value.isSimulated
        addMessage(MessageSender.USER_TTS, "[Preset] ${preset.title}")

        if (preset.isAudioFile) {
            val audioFile = File(preset.textOrPath)
            if (audioFile.exists()) {
                scope.launch {
                    aiVoiceRelayManager.stopListening()
                    if (isSim) {
                        presetManager.previewPreset(preset)
                    } else {
                        // Play gentle local sidetone monitor so user can hear what is playing at ~20% volume
                        playLocalAudioMonitor(audioFile, volume = 0.20f)

                        speechManager.injector.injectAudioFile(audioFile) { success ->
                            stopLocalAudioMonitor()
                            Log.i(TAG, "Audio preset injected into call: $success")
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
            // Text-based preset: synthesize via neural AI voice directly into call stream
            sendUserReply(preset.textOrPath)
        }
    }

    /**
     * Push-to-Talk or Toggle for AI Voice Relay.
     * When toggled ON, listens to user's mic speech.
     * As soon as speech finishes, synthesizes in AI female voice and sends to caller.
     */
    fun toggleAiVoiceListening() {
        if (aiVoiceRelayManager.isListening.value) {
            aiVoiceRelayManager.stopListening()
        } else {
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

    /**
     * User decides to take over the call normally (switches to direct mic).
     */
    fun takeOverCall() {
        switchToDirectMicMode()
    }

    fun answer() = answerCallDirectly()

    /**
     * End / Reject the call.
     */
    fun hangUp() {
        com.callscreener.app.telephony.RingtonePlayer.stop(appContext)
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
        speechManager.isSimulatorMode = false
        aiVoiceRelayManager.release()
        stopLocalAudioMonitor()
        durationJob?.cancel()

        try {
            activeTelecomCall?.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting call", e)
        }
        activeTelecomCall = null

        _sessionState.update { it.copy(status = CallStatus.ENDED) }
        resetAudioMode()
    }

    fun onCallEnded() {
        com.callscreener.app.telephony.RingtonePlayer.stop(appContext)
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
        speechManager.isSimulatorMode = false
        aiVoiceRelayManager.release()
        stopLocalAudioMonitor()
        durationJob?.cancel()
        activeTelecomCall = null
        _sessionState.update { it.copy(status = CallStatus.ENDED) }
        resetAudioMode()
    }

    /**
     * Toggle Mute.
     */
    fun toggleMute() {
        val newMute = !_sessionState.value.isMuted
        audioManager.isMicrophoneMute = newMute
        if (_sessionState.value.audioCallMode == AudioCallMode.DIRECT_MIC) {
            CallScreeningInCallService.setCallMute(newMute)
        } else {
            // In Screening / AI Voice Relay mode, call uplink remains muted to protect user privacy
            CallScreeningInCallService.setCallMute(true)
            if (newMute) {
                aiVoiceRelayManager.stopListening()
            } else if (_sessionState.value.isAutoRelayEnabled && !_sessionState.value.isTtsSpeaking) {
                aiVoiceRelayManager.startListening()
            }
        }
        _sessionState.update { it.copy(isMuted = newMute) }
    }

    /**
     * Toggle Speakerphone
     */
    fun toggleSpeaker() {
        val newSpeaker = !_sessionState.value.isSpeakerOn
        setSpeakerphone(newSpeaker)
        _sessionState.update { it.copy(isSpeakerOn = newSpeaker) }
    }

    private fun setSpeakerphone(on: Boolean) {
        try {
            CallScreeningInCallService.setSpeaker(on)

            audioManager.mode = AudioManager.MODE_IN_CALL
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (on) {
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
                        Log.i(TAG, "Routing audio to connected Bluetooth/Headset device: ${btDevice.productName}")
                        audioManager.setCommunicationDevice(btDevice)
                    } else {
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
                if (!on) {
                    @Suppress("DEPRECATION")
                    if (audioManager.isBluetoothScoAvailableOffCall) {
                        audioManager.startBluetoothSco()
                        audioManager.isBluetoothScoOn = true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle speakerphone", e)
        }
    }

    private fun resetAudioMode() {
        try {
            if (voiceChangerEngine.isVoiceChangerActive.value) {
                voiceChangerEngine.stop()
            }
            CallScreeningInCallService.setSpeaker(false)
            CallScreeningInCallService.setCallMute(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager.clearCommunicationDevice()
            } else {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = false
            }
            audioManager.isMicrophoneMute = false
            audioManager.mode = AudioManager.MODE_NORMAL
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

    /**
     * Simulator mode for testing without requiring a real incoming SIM call.
     * Routes audio through media output so Bluetooth earbuds, headphones, and speakers work perfectly!
     */
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
                isClirMaskingEnabled = prev.isClirMaskingEnabled,
                isClirMaskSavedContactsEnabled = prev.isClirMaskSavedContactsEnabled,
                isIncognitoDialActive = prev.isIncognitoDialActive,
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
