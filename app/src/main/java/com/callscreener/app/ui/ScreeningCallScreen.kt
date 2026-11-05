package com.callscreener.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.callscreener.app.data.DialerRepository
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Tune
import com.callscreener.app.engine.AudioPresetManager
import com.callscreener.app.engine.QuickReplyManager
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.keyframes
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callscreener.app.model.AudioCallMode
import com.callscreener.app.model.CallSessionState
import com.callscreener.app.model.CallStatus
import com.callscreener.app.model.ChatMessage
import com.callscreener.app.model.MessageSender
import com.callscreener.app.model.QuickReply
import com.callscreener.app.model.VoicePersona
import com.callscreener.app.root.AppOperationMode
import com.callscreener.app.ui.theme.AppleAmber
import com.callscreener.app.ui.theme.AppleAmberGlassFill
import com.callscreener.app.ui.theme.AppleBlue
import com.callscreener.app.ui.theme.AppleBlueGlassFill
import com.callscreener.app.ui.theme.AppleGreen
import com.callscreener.app.ui.theme.AppleGreenGlassFill
import com.callscreener.app.ui.theme.AppleIndigo
import com.callscreener.app.ui.theme.ApplePurple
import com.callscreener.app.ui.theme.AppleRed
import com.callscreener.app.ui.theme.AppleRedGlassFill
import com.callscreener.app.ui.theme.AppleTeal
import com.callscreener.app.ui.theme.GlassBackgroundGradient
import com.callscreener.app.ui.theme.LiquidGlassBorder
import com.callscreener.app.ui.theme.LiquidGlassBorderSubtle
import com.callscreener.app.ui.theme.LiquidGlassFill
import com.callscreener.app.ui.theme.LiquidGlassFillElevated
import com.callscreener.app.ui.theme.LiquidGlassFillSubtle
import com.callscreener.app.model.AudioPreset
import com.callscreener.app.ui.theme.TextMuted
import com.callscreener.app.ui.theme.TextPrimary
import com.callscreener.app.ui.theme.TextSecondary
import com.callscreener.app.ui.theme.TextTertiary

enum class RibbonTab {
    QUICK_REPLIES,
    SOUNDBOARD,
    VOICE_PERSONAS
}

enum class CallDisplayMode {
    INCOMING_RINGING,
    STANDARD_IN_CALL,
    ASSISTANT_SCREENING
}

@Composable
fun ScreeningCallScreen(
    state: CallSessionState,
    presets: List<AudioPreset> = AudioPreset.DEFAULT_PRESETS,
    onPlayPreset: (AudioPreset) -> Unit = {},
    onStartScreening: () -> Unit,
    onAnswerDirectly: () -> Unit = onStartScreening,
    onSendReply: (String) -> Unit,
    onTakeOver: () -> Unit,
    onHangUp: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onSelectPersona: (VoicePersona) -> Unit = {},
    onSwitchToAiTypeMode: () -> Unit = {},
    onSwitchToDirectMicMode: () -> Unit = {},
    onSwitchToVoiceChangerMode: () -> Unit = {},
    onSimulateCallerReply: (String) -> Unit = {},
    onToggleAiVoiceListening: () -> Unit = {},
    onToggleAutoRelay: (Boolean) -> Unit = {},
    onToggleRecording: () -> Unit = {},
    onToggleHold: () -> Unit = {},
    onToggleSpeechJammer: () -> Unit = {},
    onDtmfDigit: (Char) -> Unit = {},
    presetManager: AudioPresetManager? = null,
    quickReplyManager: QuickReplyManager? = null
) {
    var inputText by remember { mutableStateOf("") }
    var activeRibbonTab by remember { mutableStateOf(RibbonTab.QUICK_REPLIES) }
    var isPresetManagerOpen by remember { mutableStateOf(false) }
    var isFocusChatMode by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Smooth auto-scroll to the newest message
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    val isInCall = state.status == CallStatus.SCREENING_ACTIVE ||
            state.status == CallStatus.DIALING ||
            state.status == CallStatus.USER_TALKING

    val context = LocalContext.current

    // Prioritize contact name saved in phone's contacts; only show network owner name for unsaved contacts
    val effectiveCallerName = remember(state.callerNumber, state.callerName) {
        val savedName = DialerRepository.lookupContactName(context, state.callerNumber)
        if (!savedName.isNullOrBlank()) {
            savedName
        } else {
            state.callerName
        }
    }

    val currentDisplayMode = remember(state.status, state.isIncoming) {
        if (state.status == CallStatus.RINGING && state.isIncoming) {
            CallDisplayMode.INCOMING_RINGING
        } else if (state.status == CallStatus.SCREENING_ACTIVE) {
            CallDisplayMode.ASSISTANT_SCREENING
        } else {
            CallDisplayMode.STANDARD_IN_CALL
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassBackgroundGradient)
    ) {
        // Living Chromatic Liquid Aurora Backdrop
        AmbientLiquidBackdrop(isRinging = currentDisplayMode == CallDisplayMode.INCOMING_RINGING)

        AnimatedContent(
            targetState = currentDisplayMode,
            transitionSpec = {
                fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.94f, animationSpec = tween(400, easing = FastOutSlowInEasing)) togetherWith
                fadeOut(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                    scaleOut(targetScale = 1.05f, animationSpec = tween(280, easing = FastOutSlowInEasing))
            },
            label = "callScreenModeTransition",
            modifier = Modifier.fillMaxSize()
        ) { mode ->
            when (mode) {
                CallDisplayMode.INCOMING_RINGING -> {
                    // Full-Screen OEM Incoming Call Interface
                    OemIncomingCallScreen(
                        callerName = effectiveCallerName,
                        callerNumber = state.callerNumber,
                        isNumberHidden = state.isHideNumberOnCallerId,
                        onDecline = onHangUp,
                        onScreenWithAi = onStartScreening,
                        onAnswer = onAnswerDirectly
                    )
                }
                CallDisplayMode.STANDARD_IN_CALL -> {
                    // Standard In-Call Dialer (ColorOS / MIUI / iOS style)
                    StandardInCallScreen(
                        callerName = effectiveCallerName,
                        callerNumber = state.callerNumber,
                        isNumberHidden = state.isHideNumberOnCallerId,
                        durationSeconds = state.durationSeconds,
                        status = state.status,
                        isMuted = state.isMuted,
                        isSpeakerOn = state.isSpeakerOn,
                        isRecording = state.isRecording,
                        recordingDurationSeconds = state.recordingDurationSeconds,
                        onToggleMute = onToggleMute,
                        onToggleSpeaker = onToggleSpeaker,
                        onToggleRecording = onToggleRecording,
                        onToggleHold = onToggleHold,
                        isSpeechJammerActive = state.isSpeechJammerActive,
                        onToggleSpeechJammer = onToggleSpeechJammer,
                        onScreenWithAi = onStartScreening,
                        onDtmfDigit = onDtmfDigit,
                        onHangUp = onHangUp
                    )
                }
                CallDisplayMode.ASSISTANT_SCREENING -> {
                    // Screening Mode: Full Assistant Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .imePadding()
                    ) {
                // Super-Premium Redesigned Caller ID Card on Top (VisionOS / iOS 18 style)
                SuperPremiumCallerCard(
                    callerName = effectiveCallerName,
                    callerNumber = state.callerNumber,
                    isNumberHidden = state.isHideNumberOnCallerId,
                    durationSeconds = state.durationSeconds,
                    status = state.status,
                    audioMode = state.audioCallMode,
                    isSpeaking = state.isTtsSpeaking,
                    isRecording = state.isRecording,
                    recordingDurationSeconds = state.recordingDurationSeconds,
                    onToggleRecording = onToggleRecording,
                    isSpeechJammerActive = state.isSpeechJammerActive,
                    onToggleSpeechJammer = onToggleSpeechJammer
                )

            // Central Transcript & Message Timeline (Generous breathing room)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (state.messages.isEmpty() && state.status == CallStatus.DIALING) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            JumpingAudioWaveform(color = AppleTeal, isPlaying = true)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Connecting Call",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                text = "Direct audio uplink established. Assistant ready.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (state.messages.isEmpty() && state.status == CallStatus.SCREENING_ACTIVE) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            JumpingAudioWaveform(color = AppleBlue, isPlaying = state.isTtsSpeaking)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (state.isTtsSpeaking) "Synthesizing Vocal Reply" else "Listening to Conversation",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                text = if (state.isSimulated)
                                    "Connected to audio stream. Enter reply below to synthesize speech."
                                else
                                    "Live speech transcription will appear here in real time.",
                                color = TextTertiary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(state.messages) { msg ->
                            LiquidGlassBubble(msg)
                        }
                    }
                }
            }

            // Streamlined Liquid Hub (Debloated, Pure Elegance with Expand/Collapse Focus Toggle)
            if (isInCall) {
                // Fluid Apple Liquid Glass Mode Toggle Pill (Full Screen Chat Focus vs Expand Controls)
                LiquidGlassModeTogglePill(
                    isFocusChatMode = isFocusChatMode,
                    onToggle = { isFocusChatMode = !isFocusChatMode }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Unified Liquid Input & In-Call Command Island (Coordinated Single-Spring Fluid Animation)
                UnifiedLiquidCommandIsland(
                    text = inputText,
                    onTextChanged = { inputText = it },
                    onSend = {
                        if (inputText.isNotBlank()) {
                            onSendReply(inputText)
                            if (state.isSimulated) {
                                onSimulateCallerReply("Acknowledged, thank you.")
                            }
                            inputText = ""
                        }
                    },
                    state = state,
                    audioMode = state.audioCallMode,
                    isMuted = state.isMuted,
                    isSpeakerOn = state.isSpeakerOn,
                    onSwitchToAiType = onSwitchToAiTypeMode,
                    onSwitchToDirectMic = onSwitchToDirectMicMode,
                    onSwitchToVoiceChanger = onSwitchToVoiceChangerMode,
                    onTakeOver = onTakeOver,
                    onHangUp = onHangUp,
                    onToggleMute = onToggleMute,
                    onToggleSpeaker = onToggleSpeaker,
                    isRecording = state.isRecording,
                    onToggleRecording = onToggleRecording,
                    onToggleSpeechJammer = onToggleSpeechJammer,
                    onToggleAiVoiceListening = onToggleAiVoiceListening,
                    onToggleAutoRelay = onToggleAutoRelay,
                    isFocusChatMode = isFocusChatMode,
                    ribbonContent = {
                        LiquidWavyActionRibbon(
                            activeTab = activeRibbonTab,
                            onSelectTab = { activeRibbonTab = it },
                            quickReplies = state.quickReplies,
                            onReplySelected = { reply ->
                                onSendReply(reply.spokenText)
                                if (state.isSimulated) {
                                    val mockResponse = when (reply.id) {
                                        "who" -> "Hello, this is Dave from Courier Logistics."
                                        "reason" -> "I am calling regarding your express delivery."
                                        "busy" -> "Understood, I will call back at a later time."
                                        else -> "Thank you, noted."
                                    }
                                    onSimulateCallerReply(mockResponse)
                                }
                            },
                            presets = presets,
                            onPlayPreset = onPlayPreset,
                            currentPersona = state.currentPersona,
                            onSelectPersona = onSelectPersona,
                            onOpenPresetManager = { isPresetManagerOpen = true }
                        )
                    }
                )
            }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

    if (isPresetManagerOpen && presetManager != null && quickReplyManager != null) {
        PresetManagementScreen(
            presetManager = presetManager,
            quickReplyManager = quickReplyManager,
            onDismiss = { isPresetManagerOpen = false }
        )
    }
}

/**
 * Super-Premium VisionOS / iOS 18 Caller ID Bar
 * Redesigned with liquid moving aura, initials avatar, carrier verified tag, live audio equalizer and monospace timer
 */
@Composable
fun SuperPremiumCallerCard(
    callerName: String?,
    callerNumber: String,
    isNumberHidden: Boolean = false,
    durationSeconds: Long,
    status: CallStatus,
    audioMode: AudioCallMode,
    isSpeaking: Boolean,
    isRecording: Boolean = false,
    recordingDurationSeconds: Long = 0L,
    onToggleRecording: () -> Unit = {},
    isSpeechJammerActive: Boolean = false,
    onToggleSpeechJammer: () -> Unit = {}
) {
    val auraTransition = rememberInfiniteTransition(label = "callerAura")
    val rippleScale by auraTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.38f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "rippleScale"
    )
    val rippleAlpha by auraTransition.animateFloat(
        initialValue = 0.50f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "rippleAlpha"
    )

    val beaconPulse by auraTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "beaconPulse"
    )

    val initials = remember(callerName) {
        val words = (callerName ?: "Unknown").trim().split(" ")
        if (words.size >= 2) {
            "${words[0].take(1)}${words[1].take(1)}".uppercase()
        } else {
            words.firstOrNull()?.take(2)?.uppercase() ?: "CL"
        }
    }

    // VisionOS Floating Frosted Glass Dynamic Island Header Canopy
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x90000000),
                ambientColor = Color(0x50000000)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xF2181C26), // Frosted obsidian titanium
                        Color(0xE610131B), // AMOLED deep glass
                        Color(0xD9080A0E)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0x65FFFFFF), // Specular light-catching top bevel
                        Color(0x20FFFFFF),
                        Color(0x08FFFFFF)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Auxiliary Ribbon: Carrier Tag + Equalizer + Monospace Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Carrier & Screening Mode Micro-Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x18FFFFFF))
                        .border(0.6.dp, Color(0x25FFFFFF), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 3.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AppleGreen.copy(alpha = beaconPulse))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI SCREENING • ENCRYPTED",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                // Live Equalizer & Call Timer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Micro Audio Waveform
                    JumpingAudioWaveform(
                        color = AppleGreen,
                        isPlaying = isSpeaking || status == CallStatus.SCREENING_ACTIVE || status == CallStatus.USER_TALKING
                    )

                    // Call Duration Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x20FFFFFF))
                            .border(0.6.dp, Color(0x35FFFFFF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = if (status == CallStatus.DIALING || status == CallStatus.RINGING)
                                "--:--"
                            else
                                formatDuration(durationSeconds),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSpeechJammerActive) Color(0x359C27B0) else Color(0x18FFFFFF))
                            .border(
                                0.8.dp,
                                if (isSpeechJammerActive) Color(0xFFFF4081) else Color(0x30FFFFFF),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onToggleSpeechJammer() }
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isSpeechJammerActive) Color(0xFFFF4081) else Color(0xFFBA68C8))
                            )
                            Text(
                                text = if (isSpeechJammerActive) "JAMMING" else "JAMMER",
                                color = if (isSpeechJammerActive) Color(0xFFFF80AB) else Color.White,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Subtle Hairline Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.6.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0x28FFFFFF),
                                Color(0x35FFFFFF),
                                Color(0x28FFFFFF),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Main Caller Information Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High-End Specular Avatar with Dynamic Pulsing Aura
                Box(
                    modifier = Modifier.size(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSpeaking || status == CallStatus.SCREENING_ACTIVE || status == CallStatus.USER_TALKING) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .scale(rippleScale)
                                .clip(CircleShape)
                                .background(AppleBlue.copy(alpha = rippleAlpha * 0.4f))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(elevation = 8.dp, shape = CircleShape, spotColor = AppleBlue.copy(alpha = 0.5f))
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0x35007AFF),
                                        Color(0x25FFFFFF),
                                        Color(0x150F1422)
                                    )
                                )
                            )
                            .border(1.2.dp, Color(0x55FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(13.dp))

                // Caller Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = callerName ?: "Unknown Caller",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.5.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(AppleGreen)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isNumberHidden) "Private Number" else callerNumber,
                            color = Color(0xCCFFFFFF),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // If recording active: Red pulsing REC pill
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppleRed.copy(alpha = 0.22f))
                            .border(0.8.dp, AppleRed.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable { onToggleRecording() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AppleRed)
                            )
                            Text(
                                text = "REC ${formatDuration(recordingDurationSeconds)}",
                                color = AppleRed,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tier 1: Liquid Wavy Swipe Ribbon
 * Clean 2-segment switcher that smoothly transitions between Quick Replies and Voice Personas
 */
@Composable
fun LiquidWavyActionRibbon(
    activeTab: RibbonTab,
    onSelectTab: (RibbonTab) -> Unit,
    quickReplies: List<QuickReply>,
    onReplySelected: (QuickReply) -> Unit,
    presets: List<AudioPreset> = emptyList(),
    onPlayPreset: (AudioPreset) -> Unit = {},
    currentPersona: VoicePersona,
    onSelectPersona: (VoicePersona) -> Unit,
    onOpenPresetManager: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .animateContentSize(spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow))
    ) {
        // Tab Header: Ultra-slim liquid switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LiquidGlassFillSubtle)
                    .border(0.8.dp, LiquidGlassBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Quick Replies
                val isReplies = activeTab == RibbonTab.QUICK_REPLIES
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isReplies) AppleBlueGlassFill else SolidColor(Color.Transparent))
                        .clickable { onSelectTab(RibbonTab.QUICK_REPLIES) }
                        .padding(horizontal = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Quick Replies",
                        color = if (isReplies) TextPrimary else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isReplies) FontWeight.Bold else FontWeight.Medium
                    )
                }

                // Tab 2: Soundboard (Audio Presets)
                val isSoundboard = activeTab == RibbonTab.SOUNDBOARD
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSoundboard) AppleAmberGlassFill else SolidColor(Color.Transparent))
                        .clickable { onSelectTab(RibbonTab.SOUNDBOARD) }
                        .padding(horizontal = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Soundboard",
                        color = if (isSoundboard) TextPrimary else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSoundboard) FontWeight.Bold else FontWeight.Medium
                    )
                }

                // Tab 3: Voice Personas
                val isPersonas = activeTab == RibbonTab.VOICE_PERSONAS
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPersonas) AppleBlueGlassFill else SolidColor(Color.Transparent))
                        .clickable { onSelectTab(RibbonTab.VOICE_PERSONAS) }
                        .padding(horizontal = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Voice Personas",
                        color = if (isPersonas) TextPrimary else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isPersonas) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Compact Single-Row Horizontal Carousel (Never fills screen or crushes bottom buttons)
        if (activeTab == RibbonTab.QUICK_REPLIES) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(quickReplies, key = { it.id }) { reply ->
                    Box(
                        modifier = Modifier
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(18.dp), spotColor = Color(0x35000000))
                            .clip(RoundedCornerShape(18.dp))
                            .background(LiquidGlassFill)
                            .border(1.2.dp, LiquidGlassBorder, RoundedCornerShape(18.dp))
                            .clickable { onReplySelected(reply) }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = reply.title,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-0.2).sp
                        )
                    }
                }
            }
        } else if (activeTab == RibbonTab.SOUNDBOARD) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(presets, key = { it.id }) { preset ->
                    Box(
                        modifier = Modifier
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(18.dp), spotColor = AppleAmber.copy(alpha = 0.25f))
                            .clip(RoundedCornerShape(18.dp))
                            .background(LiquidGlassFill)
                            .border(
                                1.2.dp,
                                if (preset.isAudioFile) Brush.verticalGradient(listOf(AppleAmber, Color(0x30FFFFFF)))
                                else LiquidGlassBorder,
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { onPlayPreset(preset) }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (preset.isAudioFile) Icons.Rounded.GraphicEq else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = if (preset.isAudioFile) AppleAmber else Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = preset.title,
                                color = if (preset.isAudioFile) AppleAmber else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.2).sp
                            )
                        }
                    }
                }
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(VoicePersona.ALL_PERSONAS, key = { it.id }) { persona ->
                    val isSelected = persona.id == currentPersona.id

                    Box(
                        modifier = Modifier
                            .shadow(elevation = if (isSelected) 8.dp else 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x40000000))
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) LiquidGlassFillElevated else LiquidGlassFillSubtle)
                            .border(
                                1.2.dp,
                                if (isSelected) LiquidGlassBorder else LiquidGlassBorderSubtle,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectPersona(persona) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            isSelected -> AppleGreen.copy(alpha = 0.40f)
                                            persona.isHindi -> AppleGreen.copy(alpha = 0.22f)
                                            persona.isPureGirlVoice -> Color(0x25FFFFFF)
                                            else -> Color(0x18FFFFFF)
                                        }
                                    )
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = persona.code,
                                    color = if (isSelected || persona.isHindi) AppleGreen else TextTertiary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = persona.name,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * ColorOS / Flagship OEM-Style In-Call Command Console:
 * - Spacious 2x3 grid of 56dp circular frosted action buttons with crisp labels underneath
 * - Dedicated, large 70dp red Hang Up button centered at bottom with zero overlap
 * - Clean, full-width frosted Type-to-Speak input bar
 * - Minimalist monochromatic aesthetic without annoying rainbow saturation
 */
@Composable
fun UnifiedLiquidCommandIsland(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    state: CallSessionState,
    audioMode: AudioCallMode,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    onSwitchToAiType: () -> Unit,
    onSwitchToDirectMic: () -> Unit,
    onSwitchToVoiceChanger: () -> Unit = {},
    onTakeOver: () -> Unit,
    onHangUp: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    isRecording: Boolean = false,
    onToggleRecording: () -> Unit = {},
    onToggleSpeechJammer: () -> Unit = {},
    onToggleAiVoiceListening: () -> Unit = {},
    onToggleAutoRelay: (Boolean) -> Unit = {},
    isFocusChatMode: Boolean = false,
    ribbonContent: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val sendScale by animateFloatAsState(
        targetValue = if (text.isNotBlank()) 1.0f else 0.88f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "sendScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status & Live Relay Transcription Banner
        if (audioMode == AudioCallMode.AI_TYPE_SPEAK) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (state.isListeningToUserSpeech) Color(0x2230D158) else Color(0x14FFFFFF))
                    .border(
                        0.8.dp,
                        if (state.isListeningToUserSpeech) Color(0x6030D158) else Color(0x24FFFFFF),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (state.isListeningToUserSpeech) AppleGreen else Color.White.copy(alpha = 0.6f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.userLiveTranscript.isNotBlank())
                            "\"${state.userLiveTranscript}\""
                        else if (state.isListeningToUserSpeech)
                            "Listening to mic • Relaying in AI Voice"
                        else
                            "AI Voice Relay: Speak in mic or type",
                        color = if (state.isListeningToUserSpeech) AppleGreen else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (state.isAutoRelayEnabled) AppleGreen.copy(alpha = 0.25f) else Color(0x18FFFFFF))
                        .clickable { onToggleAutoRelay(!state.isAutoRelayEnabled) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (state.isAutoRelayEnabled) "AUTO ON" else "AUTO OFF",
                        color = if (state.isAutoRelayEnabled) AppleGreen else TextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else if (audioMode == AudioCallMode.VOICE_CHANGER) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x288E2DE2))
                    .border(
                        0.8.dp,
                        Color(0x608E2DE2),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(ApplePurple)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voice Morph Active: ${state.currentPersona.name} • Raw Mic Muted",
                        color = Color(0xFFE4C4FB),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ApplePurple.copy(alpha = 0.35f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "15ms DSP",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Type-to-Speak Input Bar (Fluid Apple Liquid Glass with Coordinated Horizontal Expansion)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = when (audioMode) {
                            AudioCallMode.AI_TYPE_SPEAK -> if (state.isListeningToUserSpeech) "Listening... speak into mic or type" else "Type a message to speak in AI Voice..."
                            AudioCallMode.VOICE_CHANGER -> "Voice morph active: speaking as ${state.currentPersona.name}..."
                            else -> "Direct microphone active..."
                        },
                        color = TextTertiary,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0x16FFFFFF))
                    .border(
                        0.8.dp,
                        if (text.isNotBlank()) Color(0x60FFFFFF) else Color(0x22FFFFFF),
                        RoundedCornerShape(22.dp)
                    ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (text.isNotBlank()) {
                // Send Typed Text Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .scale(sendScale)
                        .clip(CircleShape)
                        .background(AppleGreen)
                        .clickable(onClick = onSend),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Speak",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else if (audioMode == AudioCallMode.AI_TYPE_SPEAK) {
                // Mic PTT / Toggle Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (state.isListeningToUserSpeech) AppleGreen else Color(0x22FFFFFF))
                        .border(1.dp, if (state.isListeningToUserSpeech) AppleGreen else Color(0x35FFFFFF), CircleShape)
                        .clickable(onClick = onToggleAiVoiceListening),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.isListeningToUserSpeech) Icons.Filled.Mic else Icons.Filled.MicOff,
                        contentDescription = "Toggle Mic",
                        tint = if (state.isListeningToUserSpeech) Color.Black else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Compact Liquid Glass Decline Button in Chat Focus Mode (Glides in/out horizontally with Apple physics)
            AnimatedVisibility(
                visible = isFocusChatMode,
                enter = fadeIn(animationSpec = tween(220)) + expandHorizontally(
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                    expandFrom = Alignment.End
                ),
                exit = fadeOut(animationSpec = tween(160)) + shrinkHorizontally(
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                    shrinkTowards = Alignment.End
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(elevation = 14.dp, shape = CircleShape, spotColor = Color(0xAAFF3B30))
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFFF453A),
                                        Color(0xFFD70015)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x90FFFFFF),
                                        Color(0x30FFFFFF)
                                    )
                                ),
                                CircleShape
                            )
                            .clickable(onClick = onHangUp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CallEnd,
                            contentDescription = "Decline Call",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ColorOS 2x3 Grid of 56dp Circular In-Call Control Buttons & Massive Decline Button + Action Ribbon
        // Smoothly collapses downwards/upwards in ONE synchronized fluid motion in Chat Focus Mode (Zero Bottom Jitter)
        AnimatedVisibility(
            visible = !isFocusChatMode,
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(
                animationSpec = tween(260, easing = FastOutSlowInEasing),
                expandFrom = Alignment.Bottom
            ),
            exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(
                animationSpec = tween(220, easing = FastOutSlowInEasing),
                shrinkTowards = Alignment.Bottom
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tier 1: Action Ribbon (Quick replies, soundboard, voice personas)
                ribbonContent()
                // Row 1: Mute | Speaker | Record
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Button 1: Mute
                    OemInCallControlButton(
                        icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                        label = if (isMuted) "Unmute" else "Mute",
                        isActive = isMuted,
                        activeColor = Color.White,
                        activeContentColor = Color.Black,
                        onClick = onToggleMute
                    )

                    // Button 2: Speaker
                    OemInCallControlButton(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        label = "Speaker",
                        isActive = isSpeakerOn,
                        activeColor = Color.White,
                        activeContentColor = Color.Black,
                        onClick = onToggleSpeaker
                    )

                    // Button 3: Record
                    OemInCallControlButton(
                        icon = Icons.Rounded.GraphicEq,
                        label = if (isRecording) "${state.recordingDurationSeconds}s" else "Record",
                        isActive = isRecording,
                        activeColor = AppleRed,
                        activeContentColor = Color.White,
                        onClick = onToggleRecording
                    )
                }

                // Row 2: AI Voice Relay | Voice Morph (DSP) | Direct Mic
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Button 4: AI Voice Relay Mode
                    val isAi = audioMode == AudioCallMode.AI_TYPE_SPEAK
                    OemInCallControlButton(
                        icon = Icons.Rounded.RecordVoiceOver,
                        label = "AI Voice",
                        isActive = isAi,
                        activeColor = AppleGreen,
                        activeContentColor = Color.Black,
                        onClick = onSwitchToAiType
                    )

                    // Button 5: Live Voice Morph (DSP Realtime Pitch Shifter)
                    val isVoiceMorph = audioMode == AudioCallMode.VOICE_CHANGER
                    OemInCallControlButton(
                        icon = Icons.Rounded.GraphicEq,
                        label = "Voice Morph",
                        isActive = isVoiceMorph,
                        activeColor = ApplePurple,
                        activeContentColor = Color.White,
                        onClick = onSwitchToVoiceChanger
                    )

                    // Button 6: Direct Mic Mode
                    val isDirect = audioMode == AudioCallMode.DIRECT_MIC
                    OemInCallControlButton(
                        icon = Icons.Filled.Call,
                        label = "Direct Mic",
                        isActive = isDirect,
                        activeColor = Color.White,
                        activeContentColor = Color.Black,
                        onClick = onSwitchToDirectMic
                    )
                }

                // Speech Jammer (DAF Disruption) Live Trigger Pill in Assistant Screening
                val jammerEngine = remember { com.callscreener.app.engine.SpeechJammerEngine.getInstance(context) }
                if (jammerEngine.isEnabled) {
                    val isJammerActive = state.isSpeechJammerActive
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isJammerActive) Color(0x359C27B0) else Color(0x14FFFFFF))
                            .border(
                                width = if (isJammerActive) 1.5.dp else 0.8.dp,
                                color = if (isJammerActive) Color(0xFFBA68C8) else Color(0x25FFFFFF),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onToggleSpeechJammer() }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(if (isJammerActive) Color(0xFFFF4081) else Color(0x28BA68C8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Psychology,
                                        contentDescription = null,
                                        tint = if (isJammerActive) Color.White else Color(0xFFE1BEE7),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (isJammerActive) "JAMMING CALLER ACTIVE" else "Acoustic Speech Jammer (DAF)",
                                        color = if (isJammerActive) Color(0xFFFF80AB) else Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isJammerActive)
                                            "${jammerEngine.delayMs}ms delayed feedback • caller speech motor paralyzed"
                                        else
                                            "Tap to disrupt caller with 200ms delayed auditory feedback",
                                        color = TextSecondary,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isJammerActive) Color(0xFFFF4081) else Color(0x25BA68C8))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (isJammerActive) "STOP" else "ENGAGE",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Dedicated, Massive, Centered iOS Specular Liquid Glass Red End Call Button
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 4.dp)
                        .size(72.dp)
                        .shadow(elevation = 22.dp, shape = CircleShape, spotColor = Color(0xAAFF3B30))
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFF453A),
                                    Color(0xFFD70015)
                                )
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x90FFFFFF),
                                    Color(0x28FFFFFF)
                                )
                            ),
                            CircleShape
                        )
                        .clickable(onClick = onHangUp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

/**
 * iOS 26 Inspired Liquid Glass Circular In-Call Control Button
 */
@Composable
fun OemInCallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean = false,
    activeColor: Color = Color.White,
    activeContentColor: Color = Color.Black,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) {
                        Brush.verticalGradient(
                            listOf(
                                activeColor.copy(alpha = 0.95f),
                                activeColor.copy(alpha = 0.70f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color(0x35FFFFFF),
                                Color(0x12FFFFFF),
                                Color(0x1EFFFFFF)
                            )
                        )
                    }
                )
                .border(
                    1.2.dp,
                    if (isActive) {
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.8f),
                                activeColor.copy(alpha = 0.5f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color(0x6AFFFFFF),
                                Color(0x18FFFFFF)
                            )
                        )
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeContentColor else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = if (isActive) activeColor else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * Animated Apple-Style 5-Bar Jumping Waveform with Luminous Gradient Ends
 */
@Composable
fun JumpingAudioWaveform(color: Color, isPlaying: Boolean) {
    val transition = rememberInfiniteTransition(label = "waveform")

    val h1 by transition.animateFloat(
        initialValue = 5f, targetValue = if (isPlaying) 16f else 5f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse), label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 10f, targetValue = if (isPlaying) 22f else 7f,
        animationSpec = infiniteRepeatable(tween(310, easing = LinearEasing), RepeatMode.Reverse), label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 7f, targetValue = if (isPlaying) 18f else 5f,
        animationSpec = infiniteRepeatable(tween(510, easing = LinearEasing), RepeatMode.Reverse), label = "h3"
    )
    val h4 by transition.animateFloat(
        initialValue = 12f, targetValue = if (isPlaying) 20f else 7f,
        animationSpec = infiniteRepeatable(tween(360, easing = LinearEasing), RepeatMode.Reverse), label = "h4"
    )
    val h5 by transition.animateFloat(
        initialValue = 5f, targetValue = if (isPlaying) 14f else 5f,
        animationSpec = infiniteRepeatable(tween(470, easing = LinearEasing), RepeatMode.Reverse), label = "h5"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.height(24.dp)
    ) {
        WaveBar(heightDp = h1, color = color)
        WaveBar(heightDp = h2, color = color)
        WaveBar(heightDp = h3, color = color)
        WaveBar(heightDp = h4, color = color)
        WaveBar(heightDp = h5, color = color)
    }
}

@Composable
fun WaveBar(heightDp: Float, color: Color) {
    Box(
        modifier = Modifier
            .width(2.8.dp)
            .height(heightDp.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.9f),
                        color
                    )
                )
            )
    )
}

/**
 * Living Chromatic Liquid Aurora Backdrop:
 * Deep Obsidian canvas with dynamic drifting chromatic orbs, fluid ambient light dispersion,
 * and rhythmic acoustic breathing when ringing.
 */
@Composable
fun AmbientLiquidBackdrop(isRinging: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "auroraShift")

    // Slow organic chromatic drift coordinates
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isRinging) 9500 else 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isRinging) 12000 else 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    // Breathing pulse for orbs (faster & more pronounced when ringing)
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isRinging) 1600 else 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingPulse"
    )

    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF04060A))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val rad1 = Math.toRadians(phase1.toDouble())
            val rad2 = Math.toRadians(phase2.toDouble())

            // Orb 1: Upper-Mid Drift (Electric Azure & Cyan)
            val orb1X = (width * 0.50f) + (width * 0.30f * Math.sin(rad1).toFloat())
            val orb1Y = (height * 0.28f) + (height * 0.12f * Math.cos(rad1).toFloat())
            val orb1Radius = width * 0.72f * breathingPulse

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x38007AFF), // Electric Blue
                        Color(0x2200D2FF), // Cyan Sheen
                        Color(0x0C0044BB),
                        Color.Transparent
                    ),
                    center = Offset(orb1X, orb1Y),
                    radius = orb1Radius
                ),
                radius = orb1Radius,
                center = Offset(orb1X, orb1Y)
            )

            // Orb 2: Lower-Mid Drift (Deep Violet & Royal Purple)
            val orb2X = (width * 0.48f) + (width * 0.32f * Math.cos(rad2).toFloat())
            val orb2Y = (height * 0.58f) + (height * 0.14f * Math.sin(rad2).toFloat())
            val orb2Radius = width * 0.76f * (2.05f - breathingPulse)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x325856D6), // Deep Indigo / Violet
                        Color(0x1EAF52DE), // Royal Purple
                        Color(0x08331166),
                        Color.Transparent
                    ),
                    center = Offset(orb2X, orb2Y),
                    radius = orb2Radius
                ),
                radius = orb2Radius,
                center = Offset(orb2X, orb2Y)
            )

            // Orb 3: Bottom Controls Glow (Emerald & Seafoam Mint when incoming ringing, or subtle oceanic teal)
            val orb3Y = height * 0.86f
            val orb3Radius = width * 0.62f * breathingPulse
            val orb3Colors = if (isRinging) {
                listOf(
                    Color(0x3830D158).copy(alpha = ringPulse * 0.55f), // Emerald radiance
                    Color(0x2200C7BE).copy(alpha = ringPulse * 0.38f),
                    Color(0x0A004D40),
                    Color.Transparent
                )
            } else {
                listOf(
                    Color(0x2000B4D8), // Oceanic Teal
                    Color(0x0E0077B6),
                    Color.Transparent
                )
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = orb3Colors,
                    center = Offset(width * 0.5f, orb3Y),
                    radius = orb3Radius
                ),
                radius = orb3Radius,
                center = Offset(width * 0.5f, orb3Y)
            )
        }

        // Pristine subtle top sheen (Apple glass dispersion)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x18FFFFFF),
                            Color(0x07FFFFFF),
                            Color.Transparent
                        )
                    )
                )
        )

        // Soft ambient vignette for deep focus
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0x1A000000),
                            Color(0x45000000)
                        )
                    )
                )
        )
    }
}

/**
 * Glass Transcript Chat Bubble with Spring Entrance Animation & Liquid Glow Pulse
 */
@Composable
fun LiquidGlassBubble(message: ChatMessage) {
    val isCaller = message.sender == MessageSender.CALLER
    val isSystem = message.sender == MessageSender.SYSTEM
    val isAssistant = message.sender == MessageSender.USER_TTS

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val bubbleAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "bubbleAlpha"
    )
    val bubbleScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.90f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
        label = "bubbleScale"
    )

    val pulseTransition = rememberInfiniteTransition(label = "bubblePulse")
    val pulseGlow by pulseTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseGlow"
    )

    val bubbleBrush = when {
        isSystem -> LiquidGlassFillSubtle
        isCaller -> LiquidGlassFill
        else -> AppleBlueGlassFill
    }

    val borderBrush = when {
        isSystem -> LiquidGlassBorderSubtle
        isCaller -> LiquidGlassBorder
        isAssistant -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = pulseGlow),
                Color(0x35FFFFFF)
            )
        )
        else -> LiquidGlassBorder
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = bubbleAlpha
                scaleX = bubbleScale
                scaleY = bubbleScale
            },
        contentAlignment = when {
            isSystem -> Alignment.Center
            isCaller -> Alignment.CenterStart
            else -> Alignment.CenterEnd
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(if (isSystem) 0.92f else 0.82f)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isCaller) 4.dp else 18.dp,
                    bottomEnd = if (!isCaller && !isSystem) 4.dp else 18.dp
                ), spotColor = Color(0x40000000))
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isCaller) 4.dp else 18.dp,
                        bottomEnd = if (!isCaller && !isSystem) 4.dp else 18.dp
                    )
                )
                .background(bubbleBrush)
                .border(
                    1.2.dp,
                    borderBrush,
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isCaller) 4.dp else 18.dp,
                        bottomEnd = if (!isCaller && !isSystem) 4.dp else 18.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (!isSystem) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCaller) "CALLER" else "AI ASSISTANT",
                        color = if (isCaller) AppleAmber else Color.White.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
            }
            Text(
                text = message.text,
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

/**
 * Ultra-Premium OEM-Grade Flagship Incoming Call Screen
 * (Apple iOS 18 / Samsung S24 Ultra / VisionOS Calling Architecture)
 */
@Composable
fun OemIncomingCallScreen(
    callerName: String?,
    callerNumber: String,
    isNumberHidden: Boolean = false,
    onDecline: () -> Unit,
    onScreenWithAi: () -> Unit,
    onAnswer: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "callingRipples")

    // Multi-layered pulsing radar rings around caller avatar
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1Scale"
    )
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.48f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1Alpha"
    )

    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2Scale"
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2Alpha"
    )

    val ring3Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring3Scale"
    )
    val ring3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring3Alpha"
    )

    // Gentle organic avatar breathing
    val avatarBreathing by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarBreathing"
    )

    // Carrier badge status dot pulse
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    // Answer button gentle organic pulse
    val answerPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "answerPulse"
    )

    // Answer ambient aura scale
    val answerAuraScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "answerAuraScale"
    )

    val declineGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "declineGlowAlpha"
    )

    val declineGlowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "declineGlowScale"
    )

    // Subtle incoming call ringing handset vibration wiggle (every 2.4s)
    val phoneWiggleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2400
                0f at 0
                -12f at 160 using FastOutSlowInEasing
                12f at 320 using FastOutSlowInEasing
                -8f at 480 using FastOutSlowInEasing
                8f at 640 using FastOutSlowInEasing
                -4f at 800 using FastOutSlowInEasing
                0f at 960 using FastOutSlowInEasing
                0f at 2400
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "phoneWiggleAngle"
    )

    // AI Screen Pill shimmer sweep
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    // Tactile Springs for buttons
    val declineSource = remember { MutableInteractionSource() }
    val isDeclinePressed by declineSource.collectIsPressedAsState()
    val declineScale by animateFloatAsState(
        targetValue = if (isDeclinePressed) 0.85f else 1.0f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow),
        label = "declineScale"
    )

    val answerSource = remember { MutableInteractionSource() }
    val isAnswerPressed by answerSource.collectIsPressedAsState()
    val answerScale by animateFloatAsState(
        targetValue = if (isAnswerPressed) 0.85f else answerPulse,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow),
        label = "answerScale"
    )

    val screenAiSource = remember { MutableInteractionSource() }
    val isScreenAiPressed by screenAiSource.collectIsPressedAsState()
    val screenAiScale by animateFloatAsState(
        targetValue = if (isScreenAiPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = Spring.StiffnessMediumLow),
        label = "screenAiScale"
    )

    val initials = remember(callerName, callerNumber) {
        val name = callerName?.trim()
        if (!name.isNullOrBlank() && name != "Unknown Caller") {
            val parts = name.split(" ").filter { it.isNotBlank() }
            if (parts.size >= 2) {
                "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            } else {
                name.take(2).uppercase()
            }
        } else {
            "ID"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Network & Carrier Tag
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1AFFFFFF))
                    .border(0.8.dp, LiquidGlassBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AppleGreen.copy(alpha = dotAlpha))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CARRIER VERIFIED • INCOMING CALL",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.6.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Large Glowing Avatar with Triple Pulsing Radar Rings
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Ripple Ring 3
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(ring3Scale)
                        .clip(CircleShape)
                        .border(1.2.dp, Color(0x7030D158).copy(alpha = ring3Alpha), CircleShape)
                )

                // Outer Ripple Ring 2
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(ring2Scale)
                        .clip(CircleShape)
                        .border(1.5.dp, AppleTeal.copy(alpha = ring2Alpha), CircleShape)
                )

                // Outer Ripple Ring 1
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(ring1Scale)
                        .clip(CircleShape)
                        .border(1.5.dp, AppleBlue.copy(alpha = ring1Alpha), CircleShape)
                )

                // Central Specular Glass Avatar
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(avatarBreathing)
                        .shadow(elevation = 20.dp, shape = CircleShape, spotColor = AppleBlue.copy(alpha = 0.5f))
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    AppleBlue.copy(alpha = 0.35f),
                                    Color(0x28FFFFFF),
                                    Color(0x100D1426)
                                )
                            )
                        )
                        .border(1.5.dp, LiquidGlassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val displayCallerTitle = if (!callerName.isNullOrBlank() && callerName != "Unknown Caller") {
                callerName
            } else if (isNumberHidden) {
                "Private Caller"
            } else {
                "Unknown Caller"
            }

            val displayCallerNumber = if (isNumberHidden) {
                "Private / Hidden Number"
            } else {
                callerNumber
            }

            // Caller Name with High-Contrast Typography
            Text(
                text = displayCallerTitle,
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.6).sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Phone Number
            Text(
                text = displayCallerNumber,
                color = TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Mobile • High Definition Audio",
                color = TextTertiary,
                fontSize = 12.sp
            )
        }

        // Bottom OEM ColorOS-Style Incoming Call Controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Screen with AI Assistant - Shimmering Border & Spring Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(54.dp)
                    .scale(screenAiScale)
                    .shadow(elevation = 14.dp, shape = RoundedCornerShape(27.dp), spotColor = Color(0x60000000))
                    .clip(RoundedCornerShape(27.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x38FFFFFF),
                                Color(0x18FFFFFF),
                                Color(0x22FFFFFF)
                            )
                        )
                    )
                    .border(
                        1.2.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0x40FFFFFF),
                                Color(0xC0FFFFFF),
                                Color(0x8030D158),
                                Color(0x40FFFFFF)
                            ),
                            start = Offset(shimmerOffset * 500f, 0f),
                            end = Offset((shimmerOffset + 1f) * 500f, 150f)
                        ),
                        RoundedCornerShape(27.dp)
                    )
                    .clickable(
                        interactionSource = screenAiSource,
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onScreenWithAi()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.RecordVoiceOver,
                        contentDescription = "Screen with AI",
                        tint = AppleGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Screen Call with AI Assistant",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Bottom Decline (Left) and Answer (Right) - Modern Minimalist Frosted Glass Discs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 42.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ultra-Saturated Liquid Ruby Glass Decline Button (Left)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(88.dp)
                ) {
                    // Deep Luminous Saturated Ruby Glow Aura
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .scale(declineGlowScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFFFF3B30).copy(alpha = declineGlowAlpha * 0.90f),
                                        Color(0xFFFF2D55).copy(alpha = declineGlowAlpha * 0.45f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Pure Saturated Liquid Ruby Glass Disc
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .scale(declineScale)
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                spotColor = Color(0x90FF3B30),
                                ambientColor = Color(0x50FF2D55)
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFFF453A), // Brilliant luminous ruby crown
                                        Color(0xFFFF3B30), // Saturated vivid Apple red core
                                        Color(0xFFD70015), // Deep jewel-toned crimson
                                        Color(0xFFA50011)  // Rich optical base
                                    )
                                )
                            )
                            .border(
                                1.6.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xF5FFFFFF), // Razor-sharp crystalline specular top
                                        Color(0x90FFB4AE), // Luminous rose refraction
                                        Color(0x40FFFFFF), // Mid transmission
                                        Color(0x80FF3B30)  // Bottom caustic return rim
                                    )
                                ),
                                CircleShape
                            )
                            .clickable(
                                interactionSource = declineSource,
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDecline()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner Caustic Lens Contour Bevel
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(1.5.dp)
                                .border(
                                    0.8.dp,
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0x95FFFFFF),
                                            Color(0x20FFFFFF),
                                            Color(0x50FFA39E)
                                        )
                                    ),
                                    CircleShape
                                )
                        )

                        // Top Optical Glass Specular Dome Sheen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .align(Alignment.TopCenter)
                                .clip(RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0x75FFFFFF),
                                            Color(0x24FFFFFF),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Bottom Caustic Reflection Bounce
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color(0x35FFFFFF)
                                        )
                                    )
                                )
                        )

                        Icon(
                            imageVector = Icons.Filled.CallEnd,
                            contentDescription = "Decline",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Ultra-Saturated Liquid Emerald Glass Answer Button (Right)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(88.dp)
                ) {
                    // Deep Luminous Saturated Emerald Glow Aura
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .scale(answerAuraScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF34C759).copy(alpha = answerPulse * 0.85f),
                                        Color(0xFF30D158).copy(alpha = answerPulse * 0.40f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Pure Saturated Liquid Emerald Glass Disc
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .scale(answerScale)
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                spotColor = Color(0x9034C759),
                                ambientColor = Color(0x5030D158)
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF38E568), // Brilliant luminous emerald crown
                                        Color(0xFF34C759), // Saturated vivid Apple green core
                                        Color(0xFF28A745), // Deep jewel-toned emerald
                                        Color(0xFF13732B)  // Rich optical base
                                    )
                                )
                            )
                            .border(
                                1.6.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xF5FFFFFF), // Razor-sharp crystalline specular top
                                        Color(0x95A8FFC2), // Luminous mint refraction
                                        Color(0x40FFFFFF), // Mid transmission
                                        Color(0x8034C759)  // Bottom caustic return rim
                                    )
                                ),
                                CircleShape
                            )
                            .clickable(
                                interactionSource = answerSource,
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAnswer()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner Caustic Lens Contour Bevel
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(1.5.dp)
                                .border(
                                    0.8.dp,
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0x95FFFFFF),
                                            Color(0x20FFFFFF),
                                            Color(0x509EFFA8)
                                        )
                                    ),
                                    CircleShape
                                )
                        )

                        // Top Optical Glass Specular Dome Sheen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .align(Alignment.TopCenter)
                                .clip(RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0x75FFFFFF),
                                            Color(0x24FFFFFF),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Bottom Caustic Reflection Bounce
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color(0x35FFFFFF)
                                        )
                                    )
                                )
                        )

                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Answer",
                            tint = Color.White,
                            modifier = Modifier
                                .size(34.dp)
                                .rotate(phoneWiggleAngle)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Standard In-Call Dialer Screen (ColorOS / MIUI / iOS style)
 * Displayed when user answers a call directly without assistant screening.
 * Features:
 * - Minimalist OLED dark theme with ambient liquid backdrop
 * - Large contact avatar & initials with soft glowing rim
 * - Clean caller name, number & monospace duration timer
 * - Symmetrical 6-button in-call control grid:
 *   1. Mute (Toggle mic mute)
 *   2. Keypad (In-call DTMF dialpad for automated phone menus)
 *   3. Speaker (Hands-free speakerphone / earpiece)
 *   4. Record (REC) (One-tap dual-side call recording with active pulsing indicator)
 *   5. Hold (Call hold toggle)
 *   6. Screen with AI (Hand over to assistant screening anytime)
 * - Large Red End Call Button
 */
@Composable
fun StandardInCallScreen(
    callerName: String?,
    callerNumber: String,
    isNumberHidden: Boolean = false,
    durationSeconds: Long,
    status: CallStatus,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    isRecording: Boolean,
    recordingDurationSeconds: Long,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleRecording: () -> Unit,
    onToggleHold: () -> Unit,
    isSpeechJammerActive: Boolean = false,
    onToggleSpeechJammer: () -> Unit = {},
    onScreenWithAi: () -> Unit,
    onDtmfDigit: (Char) -> Unit,
    onHangUp: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showKeypad by remember { mutableStateOf(false) }
    var dtmfDigits by remember { mutableStateOf("") }

    val displayCallerTitle = if (!callerName.isNullOrBlank() && callerName != "Unknown Caller") {
        callerName
    } else if (isNumberHidden) {
        "Private Caller"
    } else if (callerNumber.isNotBlank()) {
        callerNumber
    } else {
        "Unknown Caller"
    }

    val initials = if (!callerName.isNullOrBlank() && callerName != "Unknown Caller") {
        callerName.split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercase() }.joinToString("")
    } else {
        "#"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Carrier / HD Tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x18FFFFFF))
                    .border(0.8.dp, Color(0x28FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                when (status) {
                                    CallStatus.ON_HOLD -> AppleAmber
                                    CallStatus.DIALING -> AppleBlue
                                    else -> AppleGreen
                                }
                            )
                    )
                    Text(
                        text = when (status) {
                            CallStatus.ON_HOLD -> "ON HOLD"
                            CallStatus.DIALING -> "DIALING • CELLULAR"
                            else -> "HD CALL • CELLULAR"
                        },
                        color = when (status) {
                            CallStatus.ON_HOLD -> AppleAmber
                            CallStatus.DIALING -> AppleBlue
                            else -> TextSecondary
                        },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Large Contact Avatar with Ambient Aura
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .shadow(elevation = 18.dp, shape = CircleShape, spotColor = AppleBlue.copy(alpha = 0.4f))
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                AppleBlue.copy(alpha = 0.35f),
                                Color(0x28FFFFFF),
                                Color(0x150D1426)
                            )
                        )
                    )
                    .border(1.5.dp, LiquidGlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Caller Name
            Text(
                text = displayCallerTitle,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Caller Subtitle / Phone Number
            if (callerNumber.isNotBlank() && callerName != callerNumber && !isNumberHidden) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = callerNumber,
                    color = TextSecondary,
                    fontSize = 13.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Duration & Recording Status Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Call Duration
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x18FFFFFF))
                        .border(0.8.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (status == CallStatus.DIALING) "Connecting..." else formatDuration(durationSeconds),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // If recording active: Red pulsing REC pill
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppleRed.copy(alpha = 0.2f))
                            .border(0.8.dp, AppleRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AppleRed)
                            )
                            Text(
                                text = "REC " + formatDuration(recordingDurationSeconds),
                                color = AppleRed,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Speech Jammer (DAF Disruption) Live Interactive Pill
            if (status != CallStatus.ENDED) {
                val jammerEngine = remember { com.callscreener.app.engine.SpeechJammerEngine.getInstance(context) }
                if (jammerEngine.isEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSpeechJammerActive) Color(0x359C27B0) else Color(0x14FFFFFF))
                            .border(
                                width = if (isSpeechJammerActive) 1.5.dp else 0.8.dp,
                                color = if (isSpeechJammerActive) Color(0xFFBA68C8) else Color(0x22FFFFFF),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleSpeechJammer()
                            }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(if (isSpeechJammerActive) Color(0xFFFF4081) else Color(0x28BA68C8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Psychology,
                                        contentDescription = null,
                                        tint = if (isSpeechJammerActive) Color.White else Color(0xFFE1BEE7),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (isSpeechJammerActive) "JAMMING CALLER ACTIVE" else "Acoustic Speech Jammer (DAF)",
                                        color = if (isSpeechJammerActive) Color(0xFFFF80AB) else Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isSpeechJammerActive)
                                            "${jammerEngine.delayMs}ms delayed feedback • caller speech motor paralyzed"
                                        else
                                            "Tap to disrupt caller with 200ms delayed auditory feedback",
                                        color = TextSecondary,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSpeechJammerActive) Color(0xFFFF4081) else Color(0x25BA68C8))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (isSpeechJammerActive) "STOP" else "ENGAGE",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Middle Dynamic Section: DTMF Dialpad OR Clean Spacer
            if (showKeypad) {
                Spacer(modifier = Modifier.height(12.dp))

                // DTMF Display & Keypad
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Digits Display
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x15FFFFFF))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dtmfDigits.ifBlank { "Touch dialpad to send tones" },
                            color = if (dtmfDigits.isBlank()) TextTertiary else Color.White,
                            fontSize = if (dtmfDigits.isBlank()) 12.sp else 16.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (dtmfDigits.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Filled.Backspace,
                                contentDescription = "Clear",
                                tint = TextSecondary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        dtmfDigits = dtmfDigits.dropLast(1)
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3x4 DTMF Keypad Grid
                    val keypadRows = listOf(
                        listOf('1' to "", '2' to "ABC", '3' to "DEF"),
                        listOf('4' to "GHI", '5' to "JKL", '6' to "MNO"),
                        listOf('7' to "PQRS", '8' to "TUV", '9' to "WXYZ"),
                        listOf('*' to "", '0' to "+", '#' to "")
                    )

                    keypadRows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            row.forEach { (digit, letters) ->
                                InCallDtmfKey(
                                    digit = digit,
                                    letters = letters,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        dtmfDigits += digit
                                        onDtmfDigit(digit)
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // 6-Button In-Call Actions Grid (Prominent, Spacious ColorOS / MIUI / iOS Flagship Style)
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)
            ) {
                // Row 1: Mute | Keypad | Speaker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InCallActionButton(
                        icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                        label = if (isMuted) "Unmute" else "Mute",
                        isActive = isMuted,
                        activeColor = AppleRed,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleMute()
                        }
                    )

                    InCallActionButton(
                        icon = Icons.Rounded.Dialpad,
                        label = if (showKeypad) "Hide" else "Keypad",
                        isActive = showKeypad,
                        activeColor = AppleBlue,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showKeypad = !showKeypad
                        }
                    )

                    InCallActionButton(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        label = if (isSpeakerOn) "Speaker On" else "Speaker",
                        isActive = isSpeakerOn,
                        activeColor = AppleTeal,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleSpeaker()
                        }
                    )
                }

                // Row 2: Record | Hold | Screen with AI
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InCallActionButton(
                        icon = Icons.Rounded.GraphicEq,
                        label = if (isRecording) "Recording" else "Record",
                        isActive = isRecording,
                        activeColor = AppleRed,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleRecording()
                        }
                    )

                    InCallActionButton(
                        icon = if (status == CallStatus.ON_HOLD) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        label = if (status == CallStatus.ON_HOLD) "Unhold" else "Hold",
                        isActive = status == CallStatus.ON_HOLD,
                        activeColor = AppleAmber,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleHold()
                        }
                    )

                    InCallActionButton(
                        icon = Icons.Rounded.RecordVoiceOver,
                        label = "Screen Call",
                        isActive = false,
                        activeColor = AppleIndigo,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onScreenWithAi()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Large Red End Call Button with Tactile Spring Physics & Specular Bevel
            val endCallInteractionSource = remember { MutableInteractionSource() }
            val isEndCallPressed by endCallInteractionSource.collectIsPressedAsState()
            val endCallScale by animateFloatAsState(
                targetValue = if (isEndCallPressed) 0.85f else 1.0f,
                animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow),
                label = "endCallScale"
            )

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .scale(endCallScale)
                    .shadow(
                        elevation = 22.dp,
                        shape = CircleShape,
                        spotColor = Color(0xDDFF3B30),
                        ambientColor = Color(0x50FF3B30)
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFF453A),
                                Color(0xFFD70015),
                                Color(0xFF9E0B07)
                            )
                        )
                    )
                    .border(
                        1.6.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color(0xAAFFFFFF),
                                Color(0x30FFFFFF),
                                Color(0x50FF453A)
                            )
                        ),
                        CircleShape
                    )
                    .clickable(
                        interactionSource = endCallInteractionSource,
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onHangUp()
                    },
                contentAlignment = Alignment.Center
            ) {
                // Top crescent sheen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x40FFFFFF),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))
        }
    }
}

@Composable
fun InCallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val btnScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "btnScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .width(84.dp)
            .scale(btnScale)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) {
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFEFF3F8)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color(0x2AFFFFFF),
                                Color(0x14FFFFFF)
                            )
                        )
                    }
                )
                .border(
                    1.4.dp,
                    if (isActive) {
                        Brush.verticalGradient(
                            listOf(
                                Color.White,
                                Color(0xE0FFFFFF)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color(0x4DFFFFFF),
                                Color(0x18FFFFFF)
                            )
                        )
                    },
                    CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color(0xFF0F141C) else Color.White,
                modifier = Modifier.size(29.dp)
            )
        }

        Text(
            text = label,
            color = if (isActive) Color.White else Color(0xDDFFFFFF),
            fontSize = 12.5.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            letterSpacing = (-0.1).sp,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun InCallDtmfKey(
    digit: Char,
    letters: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x1AFFFFFF))
            .border(0.8.dp, Color(0x25FFFFFF), RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = digit.toString(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (letters.isNotBlank()) {
                Text(
                    text = letters,
                    color = TextTertiary,
                    fontSize = 8.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Apple iOS Liquid Glass Mode Toggle Pill:
 * Fluid pill that allows the user to expand or collapse the quick replies, soundboard presets,
 * and command tiles into a full-screen chat transcript view with voice relay mic & inline decline button.
 */
@Composable
fun LiquidGlassModeTogglePill(
    isFocusChatMode: Boolean,
    onToggle: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val chevronRotation by animateFloatAsState(
        targetValue = if (isFocusChatMode) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.76f, stiffness = Spring.StiffnessMediumLow),
        label = "chevronRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = Color(0x40000000),
                    ambientColor = Color(0x20000000)
                )
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x28FFFFFF),
                            Color(0x12FFFFFF)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0x60FFFFFF),
                            Color(0x18FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggle()
                }
                .padding(horizontal = 16.dp, vertical = 7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Subtle Mode Indicator Icon
                Icon(
                    imageVector = if (isFocusChatMode) Icons.Rounded.GridView else Icons.Rounded.ChatBubbleOutline,
                    contentDescription = null,
                    tint = if (isFocusChatMode) AppleGreen else Color(0xDDFFFFFF),
                    modifier = Modifier.size(14.dp)
                )

                Text(
                    text = if (isFocusChatMode) "Expand Controls & Sounds" else "Focus Chat Screen",
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                )

                // Smoothly Rotating Animated Chevron
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Toggle Expand/Collapse",
                    tint = Color(0xCCFFFFFF),
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(chevronRotation)
                )
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
