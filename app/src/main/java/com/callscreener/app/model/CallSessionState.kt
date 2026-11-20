package com.callscreener.app.model

enum class CallStatus {
    IDLE,
    RINGING,
    DIALING,
    SCREENING_ACTIVE,
    USER_TALKING,
    ON_HOLD,
    ENDED
}

enum class AudioCallMode {
    AI_TYPE_SPEAK,
    DIRECT_MIC
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
    val isEchoUserVoiceEnabled: Boolean = false,
    val playingAudioPresetId: String? = null,
    val playingAudioPresetTitle: String? = null,
    val isAudioPresetPlaying: Boolean = false,
    val isAudioPresetPaused: Boolean = false,
    val isSimulated: Boolean = false,
    val hasSecondaryCall: Boolean = false,
    val secondaryCallerNumber: String? = null,
    val secondaryCallerName: String? = null,
    val isConference: Boolean = false,
    val canMergeCalls: Boolean = false,
    val canSwapCalls: Boolean = false
)
