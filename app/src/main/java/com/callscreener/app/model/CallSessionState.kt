package com.callscreener.app.model

enum class CallStatus {
    IDLE,
    RINGING,           // Incoming call ringing
    DIALING,           // Outgoing call dialing
    SCREENING_ACTIVE,  // Type-to-Speak & Screening active
    USER_TALKING,      // Direct voice call
    ON_HOLD,
    ENDED
}

enum class AudioCallMode {
    AI_TYPE_SPEAK,     // AI Voice speaks typed words & greetings
    DIRECT_MIC         // Direct real mic (natural voice)
}

data class CallSessionState(
    val status: CallStatus = CallStatus.IDLE,
    val audioCallMode: AudioCallMode = AudioCallMode.AI_TYPE_SPEAK,
    val callerNumber: String = "",
    val callerName: String? = null,
    val isIncoming: Boolean = true,
    val durationSeconds: Long = 0L,
    val isSpeakerOn: Boolean = false,
    val isMuted: Boolean = false,
    val isTtsSpeaking: Boolean = false,
    val currentPersona: VoicePersona = VoicePersona.DEFAULT,
    val isListeningToUserSpeech: Boolean = false,
    val userLiveTranscript: String = "",
    val isAutoRelayEnabled: Boolean = false,
    val isGreetingEnabled: Boolean = false,
    val isHideNumberOnCallerId: Boolean = false,
    val isRecording: Boolean = false,
    val recordingDurationSeconds: Long = 0L,
    val isAutoRecordEnabled: Boolean = false,
    val lastRecordedFilePath: String? = null,
    val operationMode: com.callscreener.app.root.AppOperationMode = com.callscreener.app.root.AppOperationMode.STANDARD,
    val messages: List<ChatMessage> = emptyList(),
    val quickReplies: List<QuickReply> = QuickReply.DEFAULT_REPLIES,
    val isSpeechJammerActive: Boolean = false,
    val isSpeechJammerEnabled: Boolean = true,
    val isSimulated: Boolean = false
)
