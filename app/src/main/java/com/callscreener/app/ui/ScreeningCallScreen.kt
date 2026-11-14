package com.callscreener.app.ui

import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.PI
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import com.callscreener.app.data.CallerProfileManager
import com.callscreener.app.data.rememberContactPhotoBitmap

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.unit.Dp
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import com.callscreener.app.data.DialerRepository
import com.callscreener.app.engine.JammerMode
import com.callscreener.app.session.CallSessionManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.delay
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.SwapCalls
import androidx.compose.material.icons.automirrored.rounded.CallMerge
import com.callscreener.app.engine.AudioPresetManager
import com.callscreener.app.engine.QuickReplyManager
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.callscreener.app.root.RootPrivilegeManager
import com.callscreener.app.ui.theme.VibrantAmber
import com.callscreener.app.ui.theme.VibrantAmberGlassFill
import com.callscreener.app.ui.theme.VibrantBlue
import com.callscreener.app.ui.theme.VibrantBlueGlassFill
import com.callscreener.app.ui.theme.VibrantGreen
import com.callscreener.app.ui.theme.VibrantGreenGlassFill
import com.callscreener.app.ui.theme.VibrantIndigo
import com.callscreener.app.ui.theme.VibrantPurple
import com.callscreener.app.ui.theme.VibrantRed
import com.callscreener.app.ui.theme.VibrantRedGlassFill
import com.callscreener.app.ui.theme.VibrantTeal
import com.callscreener.app.ui.theme.GlassBackgroundGradient
import com.callscreener.app.ui.theme.AppFont
import com.callscreener.app.ui.theme.FrostedGlassBorder
import com.callscreener.app.ui.theme.FrostedGlassBorderSubtle
import com.callscreener.app.ui.theme.FrostedGlassFill
import com.callscreener.app.ui.theme.FrostedGlassFillElevated
import com.callscreener.app.ui.theme.FrostedGlassFillSubtle
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
    onPauseCustomAudio: () -> Unit = {},
    onResumeCustomAudio: () -> Unit = {},
    onStopCustomAudio: () -> Unit = {},
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
    onSimulateCallerReply: (String) -> Unit = {},
    onToggleAiVoiceListening: () -> Unit = {},
    onToggleAutoRelay: (Boolean) -> Unit = {},
    onToggleRecording: () -> Unit = {},
    onToggleHold: () -> Unit = {},
    onToggleSpeechJammer: () -> Unit = {},
    onToggleEchoUserVoice: () -> Unit = {},
    onDtmfDigit: (Char) -> Unit = {},
    onAddCall: (String) -> Unit = {},
    onMergeCalls: () -> Unit = {},
    onSwapCalls: () -> Unit = {},
    presetManager: AudioPresetManager? = null,
    quickReplyManager: QuickReplyManager? = null
) {
    var inputText by remember { mutableStateOf("") }
    var isPresetManagerOpen by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    val context = LocalContext.current

    val monetColors = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                dynamicDarkColorScheme(context)
            } catch (_: Throwable) {
                getPixelMonetDarkColorScheme()
            }
        } else {
            getPixelMonetDarkColorScheme()
        }
    }

    val effectiveCallerName = remember(state.callerNumber, state.callerName) {
        val savedName = DialerRepository.lookupContactName(context, state.callerNumber)
        if (!savedName.isNullOrBlank()) {
            savedName
        } else {
            state.callerName
        }
    }

    val effectiveSecondaryCallerName = remember(state.secondaryCallerNumber, state.secondaryCallerName) {
        val num = state.secondaryCallerNumber
        if (!num.isNullOrBlank()) {
            val saved = DialerRepository.lookupContactName(context, num)
            if (!saved.isNullOrBlank()) saved else state.secondaryCallerName ?: num
        } else {
            state.secondaryCallerName
        }
    }

    val isRootActiveForMode = state.operationMode == AppOperationMode.ROOT_ENHANCED &&
        RootPrivilegeManager.isRootModeActive(context)

    val currentDisplayMode = remember(state.status, state.isIncoming, isRootActiveForMode) {
        if (state.status == CallStatus.RINGING && state.isIncoming) {
            CallDisplayMode.INCOMING_RINGING
        } else if (state.status == CallStatus.SCREENING_ACTIVE && isRootActiveForMode) {
            CallDisplayMode.ASSISTANT_SCREENING
        } else {
            CallDisplayMode.STANDARD_IN_CALL
        }
    }

    var isQuickReplyOpen by remember { mutableStateOf(false) }
    var isToolsMenuOpen by remember { mutableStateOf(false) }
    var isAddCallOpen by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassBackgroundGradient)
    ) {
        AmbientLiquidBackdrop(isRinging = currentDisplayMode == CallDisplayMode.INCOMING_RINGING)

        AnimatedContent(
            targetState = currentDisplayMode,
            transitionSpec = {
                if (initialState == CallDisplayMode.INCOMING_RINGING && targetState == CallDisplayMode.ASSISTANT_SCREENING) {
                    (fadeIn(animationSpec = tween(420, easing = FastOutSlowInEasing)) +
                     scaleIn(initialScale = 0.92f, animationSpec = spring(dampingRatio = 0.80f, stiffness = Spring.StiffnessMediumLow)) +
                     slideInVertically(initialOffsetY = { (it * 0.08f).toInt() }, animationSpec = spring(dampingRatio = 0.80f, stiffness = Spring.StiffnessMediumLow))
                    ).togetherWith(
                        fadeOut(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 1.06f, animationSpec = tween(280, easing = FastOutSlowInEasing))
                    )
                } else if (initialState == CallDisplayMode.ASSISTANT_SCREENING && targetState == CallDisplayMode.STANDARD_IN_CALL) {
                    (fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                     scaleIn(initialScale = 0.94f, animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow))
                    ).togetherWith(
                        fadeOut(animationSpec = tween(260, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 1.04f, animationSpec = tween(260, easing = FastOutSlowInEasing))
                    )
                } else if (initialState == CallDisplayMode.INCOMING_RINGING && targetState == CallDisplayMode.STANDARD_IN_CALL) {
                    (fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                     scaleIn(initialScale = 0.93f, animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow))
                    ).togetherWith(
                        fadeOut(animationSpec = tween(260, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 1.05f, animationSpec = tween(260, easing = FastOutSlowInEasing))
                    )
                } else {
                    (fadeIn(animationSpec = tween(360, easing = FastOutSlowInEasing)) +
                     scaleIn(initialScale = 0.95f, animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow))
                    ).togetherWith(
                        fadeOut(animationSpec = tween(240, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 1.04f, animationSpec = tween(240, easing = FastOutSlowInEasing))
                    )
                }
            },
            label = "callScreenModeTransition",
            modifier = Modifier.fillMaxSize()
        ) { mode ->
            when (mode) {
                CallDisplayMode.INCOMING_RINGING -> {
                    val isScreenCallAvailable = state.operationMode == AppOperationMode.ROOT_ENHANCED &&
                        RootPrivilegeManager.isRootModeActive(context)
                    OemIncomingCallScreen(
                        callerName = effectiveCallerName,
                        callerNumber = state.callerNumber,
                        isNumberHidden = state.isHideNumberOnCallerId,
                        onDecline = onHangUp,
                        onScreenWithAi = onStartScreening,
                        onAnswer = onAnswerDirectly,
                        isDeclineHandledExternally = true,
                        isQuickReplyOpen = isQuickReplyOpen,
                        onToggleQuickReply = { isQuickReplyOpen = it },
                        isScreenCallAvailable = isScreenCallAvailable
                    )
                }
                CallDisplayMode.STANDARD_IN_CALL -> {
                    val isScreenCallAvailable = state.operationMode == AppOperationMode.ROOT_ENHANCED &&
                        RootPrivilegeManager.isRootModeActive(context)
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
                        isEchoUserVoice = state.isEchoUserVoiceEnabled,
                        onToggleEchoUserVoice = onToggleEchoUserVoice,
                        onScreenWithAi = onStartScreening,
                        onDtmfDigit = onDtmfDigit,
                        onHangUp = onHangUp,
                        hasSecondaryCall = state.hasSecondaryCall,
                        secondaryCallerNumber = state.secondaryCallerNumber,
                        secondaryCallerName = effectiveSecondaryCallerName,
                        isConference = state.isConference,
                        canMergeCalls = state.canMergeCalls,
                        canSwapCalls = state.canSwapCalls,
                        onAddCall = onAddCall,
                        onMergeCalls = onMergeCalls,
                        onSwapCalls = onSwapCalls,
                        isAddCallOpen = isAddCallOpen,
                        onToggleAddCall = { isAddCallOpen = it },
                        monetColors = monetColors,
                        isEndCallHandledExternally = true,
                        isScreenCallAvailable = isScreenCallAvailable
                    )
                }
                CallDisplayMode.ASSISTANT_SCREENING -> {
                    MaterialYouAssistantScreeningScreen(
                        state = state,
                        callerName = effectiveCallerName,
                        callerNumber = state.callerNumber,
                        isNumberHidden = state.isHideNumberOnCallerId,
                        inputText = inputText,
                        onInputTextChanged = { inputText = it },
                        onSendReply = { text ->
                            if (text.isNotBlank()) {
                                onSendReply(text)
                                if (state.isSimulated) {
                                    onSimulateCallerReply("Acknowledged, thank you.")
                                }
                                inputText = ""
                            }
                        },
                        onQuickReplySelected = { reply ->
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
                        onTakeOver = onTakeOver,
                        onHangUp = onHangUp,
                        onToggleMute = onToggleMute,
                        onToggleSpeaker = onToggleSpeaker,
                        onToggleRecording = onToggleRecording,
                        onToggleSpeechJammer = onToggleSpeechJammer,
                        isEchoUserVoice = state.isEchoUserVoiceEnabled,
                        onToggleEchoUserVoice = onToggleEchoUserVoice,
                        onToggleAiVoiceListening = onToggleAiVoiceListening,
                        onSwitchToAiTypeMode = onSwitchToAiTypeMode,
                        onSwitchToDirectMicMode = onSwitchToDirectMicMode,
                        presets = presets,
                        onPlayPreset = onPlayPreset,
                        onPauseCustomAudio = onPauseCustomAudio,
                        onResumeCustomAudio = onResumeCustomAudio,
                        onStopCustomAudio = onStopCustomAudio,
                        currentPersona = state.currentPersona,
                        onSelectPersona = onSelectPersona,
                        onOpenPresetManager = { isPresetManagerOpen = true },
                        listState = listState,
                        monetColors = monetColors,
                        isHangUpHandledExternally = true,
                        isToolsMenuOpen = isToolsMenuOpen,
                        onToggleToolsMenu = { isToolsMenuOpen = it }
                    )
                }
            }
        }

        SharedMorphingDeclineButton(
            mode = currentDisplayMode,
            isQuickReplyOpen = isQuickReplyOpen,
            isToolsMenuOpen = isToolsMenuOpen,
            isAddCallOpen = isAddCallOpen,
            onHangUp = onHangUp,
            monetColors = monetColors,
            isCallEnded = state.status == CallStatus.ENDED
        )

        ScreeningToolsBottomSheet(
            visible = isToolsMenuOpen,
            onDismiss = { isToolsMenuOpen = false },
            audioMode = state.audioCallMode,
            onSwitchToAiType = onSwitchToAiTypeMode,
            onSwitchToDirectMic = onSwitchToDirectMicMode,
            currentPersona = state.currentPersona,
            onSelectPersona = onSelectPersona,
            presets = presets,
            onPlayPreset = onPlayPreset,
            playingAudioPresetId = state.playingAudioPresetId,
            isAudioPresetPlaying = state.isAudioPresetPlaying,
            isAudioPresetPaused = state.isAudioPresetPaused,
            onOpenPresetManager = { isPresetManagerOpen = true },
            isSpeechJammerActive = state.isSpeechJammerActive,
            onToggleSpeechJammer = onToggleSpeechJammer,
            isEchoUserVoice = state.isEchoUserVoiceEnabled,
            onToggleEchoUserVoice = onToggleEchoUserVoice,
            isRecording = state.isRecording,
            recordingDurationSeconds = state.recordingDurationSeconds,
            onToggleRecording = onToggleRecording,
            monetColors = monetColors
        )
    }

    if (isPresetManagerOpen && presetManager != null && quickReplyManager != null) {
        PresetManagementScreen(
            presetManager = presetManager,
            quickReplyManager = quickReplyManager,
            monetColors = monetColors,
            onDismiss = { isPresetManagerOpen = false }
        )
    }
}

@Composable
fun SharedMorphingDeclineButton(
    mode: CallDisplayMode,
    isQuickReplyOpen: Boolean,
    isToolsMenuOpen: Boolean = false,
    isAddCallOpen: Boolean = false,
    onHangUp: () -> Unit,
    monetColors: ColorScheme,
    isCallEnded: Boolean = false
) {
    val haptic = LocalHapticFeedback.current

    val isHidden = isQuickReplyOpen || isCallEnded || isToolsMenuOpen || isAddCallOpen

    val quickReplyAlpha by animateFloatAsState(
        targetValue = if (isHidden) 0f else 1f,
        animationSpec = tween(180, easing = LinearEasing),
        label = "morphQuickReplyAlpha"
    )

    if (quickReplyAlpha <= 0.01f && isHidden) {
        return
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .graphicsLayer {
                alpha = quickReplyAlpha
            }
    ) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight

        val halfWidth = (totalWidth - 46.dp) / 2
        val inCallButtonWidth = 84.dp
        val inCallButtonHeight = 76.dp
        val inCallCornerRadius = 38.dp
        val inCallCenterX = (totalWidth - inCallButtonWidth) / 2

        val targetWidth = when (mode) {
            CallDisplayMode.INCOMING_RINGING -> 72.dp
            CallDisplayMode.ASSISTANT_SCREENING -> halfWidth
            CallDisplayMode.STANDARD_IN_CALL -> inCallButtonWidth
        }
        val targetHeight = when (mode) {
            CallDisplayMode.INCOMING_RINGING -> 72.dp
            CallDisplayMode.ASSISTANT_SCREENING -> 54.dp
            CallDisplayMode.STANDARD_IN_CALL -> inCallButtonHeight
        }
        val targetCornerRadius = when (mode) {
            CallDisplayMode.INCOMING_RINGING -> 36.dp
            CallDisplayMode.ASSISTANT_SCREENING -> 27.dp
            CallDisplayMode.STANDARD_IN_CALL -> inCallCornerRadius
        }
        val targetLeft = when (mode) {
            CallDisplayMode.INCOMING_RINGING -> 62.dp
            CallDisplayMode.ASSISTANT_SCREENING -> 16.dp
            CallDisplayMode.STANDARD_IN_CALL -> inCallCenterX
        }
        val targetBottom = when (mode) {
            CallDisplayMode.INCOMING_RINGING -> 62.dp
            CallDisplayMode.ASSISTANT_SCREENING -> 12.dp
            CallDisplayMode.STANDARD_IN_CALL -> 24.dp
        }
        val targetHasLabel = mode == CallDisplayMode.ASSISTANT_SCREENING

        val morphSpringSpec = spring<Dp>(
            dampingRatio = 0.88f,
            stiffness = Spring.StiffnessMediumLow
        )

        val buttonWidth by animateDpAsState(targetValue = targetWidth, animationSpec = morphSpringSpec, label = "morphWidth")
        val buttonHeight by animateDpAsState(targetValue = targetHeight, animationSpec = morphSpringSpec, label = "morphHeight")
        val buttonCornerRadius by animateDpAsState(targetValue = targetCornerRadius, animationSpec = morphSpringSpec, label = "morphRadius")
        val buttonLeft by animateDpAsState(targetValue = targetLeft, animationSpec = morphSpringSpec, label = "morphLeft")
        val buttonBottom by animateDpAsState(targetValue = targetBottom, animationSpec = morphSpringSpec, label = "morphBottom")

        val labelAlpha by animateFloatAsState(
            targetValue = if (targetHasLabel) 1f else 0f,
            animationSpec = tween(
                durationMillis = if (targetHasLabel) 220 else 70,
                delayMillis = if (targetHasLabel) 80 else 0,
                easing = if (targetHasLabel) FastOutSlowInEasing else LinearEasing
            ),
            label = "morphLabelAlpha"
        )
        val iconSize by animateDpAsState(
            targetValue = when (mode) {
                CallDisplayMode.ASSISTANT_SCREENING -> 22.dp
                CallDisplayMode.STANDARD_IN_CALL -> 36.dp
                CallDisplayMode.INCOMING_RINGING -> 32.dp
            },
            animationSpec = morphSpringSpec,
            label = "morphIconSize"
        )

        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        val scaleX by animateFloatAsState(
            targetValue = if (isPressed) 1.08f else 1.0f,
            animationSpec = spring(dampingRatio = 0.54f, stiffness = Spring.StiffnessMediumLow),
            label = "morphScaleX"
        )
        val scaleY by animateFloatAsState(
            targetValue = if (isPressed) 0.88f else 1.0f,
            animationSpec = spring(dampingRatio = 0.54f, stiffness = Spring.StiffnessMediumLow),
            label = "morphScaleY"
        )
        val elevation by animateDpAsState(
            targetValue = if (isPressed) 4.dp else 14.dp,
            animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
            label = "morphElevation"
        )
        val iconRotation by animateFloatAsState(
            targetValue = if (isPressed) -12f else 0f,
            animationSpec = spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMediumLow),
            label = "morphIconRotation"
        )

        val infiniteTransition = rememberInfiniteTransition(label = "morphInfinite")
        val auraPulse by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.20f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "morphAuraPulse"
        )
        val auraAlpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "morphAuraAlpha"
        )
        val glintPhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "morphGlintPhase"
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset {
                    IntOffset(
                        x = buttonLeft.roundToPx(),
                        y = -buttonBottom.roundToPx()
                    )
                }
                .size(buttonWidth, buttonHeight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val auraMult = if (isPressed) 1.25f else auraPulse
                        this.scaleX = auraMult
                        this.scaleY = auraMult
                        this.alpha = if (isPressed) 0.50f else auraAlpha
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF2A4D).copy(alpha = 0.45f),
                                Color(0xFFD00826).copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(buttonCornerRadius)
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.scaleX = scaleX
                        this.scaleY = scaleY
                    }
                    .shadow(
                        elevation = elevation,
                        shape = RoundedCornerShape(buttonCornerRadius),
                        spotColor = Color(0x95FF2A4D)
                    )
                    .clip(RoundedCornerShape(buttonCornerRadius))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFF334B),
                                Color(0xFFF01438),
                                Color(0xFFD00826)
                            )
                        )
                    )
                    .border(
                        width = 1.3.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isPressed) 0.65f else 0.45f),
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = if (isPressed) 0.50f else 0.22f)
                            )
                        ),
                        shape = RoundedCornerShape(buttonCornerRadius)
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = !isHidden
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onHangUp()
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(topStart = buttonCornerRadius, topEnd = buttonCornerRadius))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.30f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val glintW = size.width * 0.45f
                    val currentX = (size.width + glintW * 2) * glintPhase - glintW
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.20f),
                                Color.Transparent
                            ),
                            startX = currentX - glintW * 0.5f,
                            endX = currentX + glintW * 0.5f
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = if (targetHasLabel) "Hang up" else "Decline call",
                        tint = Color.White,
                        modifier = Modifier
                            .size(iconSize)
                            .graphicsLayer {
                                rotationZ = iconRotation
                            }
                    )
                    if (labelAlpha > 0.05f) {
                        Spacer(modifier = Modifier.width((8 * labelAlpha).dp))
                        Text(
                            text = "Hang up",
                            color = Color.White.copy(alpha = labelAlpha),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.2.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier.graphicsLayer {
                                alpha = labelAlpha
                                translationX = (1f - labelAlpha) * -12f
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialYouAssistantScreeningScreen(
    state: CallSessionState,
    callerName: String?,
    callerNumber: String,
    isNumberHidden: Boolean,
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    onSendReply: (String) -> Unit,
    onQuickReplySelected: (QuickReply) -> Unit,
    onTakeOver: () -> Unit,
    onHangUp: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleRecording: () -> Unit,
    onToggleSpeechJammer: () -> Unit,
    isEchoUserVoice: Boolean = false,
    onToggleEchoUserVoice: () -> Unit = {},
    onToggleAiVoiceListening: () -> Unit,
    onSwitchToAiTypeMode: () -> Unit,
    onSwitchToDirectMicMode: () -> Unit,
    presets: List<AudioPreset>,
    onPlayPreset: (AudioPreset) -> Unit,
    onPauseCustomAudio: () -> Unit = {},
    onResumeCustomAudio: () -> Unit = {},
    onStopCustomAudio: () -> Unit = {},
    currentPersona: VoicePersona,
    onSelectPersona: (VoicePersona) -> Unit,
    onOpenPresetManager: () -> Unit,
    listState: LazyListState,
    monetColors: ColorScheme,
    isHangUpHandledExternally: Boolean = false,
    isToolsMenuOpen: Boolean = false,
    onToggleToolsMenu: (Boolean) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to monetColors.primary.copy(alpha = 0.22f),
                        0.35f to monetColors.secondaryContainer.copy(alpha = 0.14f),
                        0.65f to monetColors.surface,
                        1.0f to monetColors.surfaceContainerLowest
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            ScreeningTopAppBar(
                callerName = callerName,
                callerNumber = callerNumber,
                isNumberHidden = isNumberHidden,
                durationSeconds = state.durationSeconds,
                isSpeaking = state.isTtsSpeaking,
                isRecording = state.isRecording,
                recordingDurationSeconds = state.recordingDurationSeconds,
                isMuted = state.isMuted,
                isSpeakerOn = state.isSpeakerOn,
                isSpeechJammerActive = state.isSpeechJammerActive,
                onToggleSpeaker = onToggleSpeaker,
                onToggleMute = onToggleMute,
                onOpenTools = { onToggleToolsMenu(true) },
                monetColors = monetColors
            )

            ScreeningTranscriptView(
                messages = state.messages,
                isTtsSpeaking = state.isTtsSpeaking,
                isListeningToUser = state.isListeningToUserSpeech,
                userLiveTranscript = state.userLiveTranscript,
                isSimulated = state.isSimulated,
                listState = listState,
                monetColors = monetColors,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            ScreeningResponsesAndAudioSection(
                quickReplies = state.quickReplies,
                onReplySelected = onQuickReplySelected,
                presets = presets,
                onPlayPreset = onPlayPreset,
                playingAudioPresetId = state.playingAudioPresetId,
                playingAudioPresetTitle = state.playingAudioPresetTitle,
                isAudioPresetPlaying = state.isAudioPresetPlaying,
                isAudioPresetPaused = state.isAudioPresetPaused,
                onPauseCustomAudio = onPauseCustomAudio,
                onResumeCustomAudio = onResumeCustomAudio,
                onStopCustomAudio = onStopCustomAudio,
                onOpenPresetManager = onOpenPresetManager,
                monetColors = monetColors
            )

            Spacer(modifier = Modifier.height(2.dp))

            ScreeningTypeToSpeakBar(
                text = inputText,
                onTextChanged = onInputTextChanged,
                onSend = { onSendReply(inputText) },
                isListeningToMic = state.isListeningToUserSpeech,
                onToggleMic = onToggleAiVoiceListening,
                monetColors = monetColors
            )

            Spacer(modifier = Modifier.height(4.dp))

            SmartJammerControl(
                isActive = state.isSpeechJammerActive,
                onToggle = onToggleSpeechJammer,
                isEchoUserVoice = isEchoUserVoice,
                onToggleEchoUserVoice = onToggleEchoUserVoice,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                monetColors = monetColors
            )

            ScreeningBottomActionBar(
                onHangUp = onHangUp,
                onTakeOver = onTakeOver,
                monetColors = monetColors,
                isHangUpHandledExternally = isHangUpHandledExternally
            )
        }
    }
}

@Composable
private fun ScreeningTopAppBar(
    callerName: String?,
    callerNumber: String,
    isNumberHidden: Boolean,
    durationSeconds: Long,
    isSpeaking: Boolean,
    isRecording: Boolean,
    recordingDurationSeconds: Long,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    isSpeechJammerActive: Boolean,
    onToggleSpeaker: () -> Unit,
    onToggleMute: () -> Unit,
    onOpenTools: () -> Unit,
    monetColors: ColorScheme
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val displayName = if (!callerName.isNullOrBlank() && callerName != "Unknown Caller") {
        callerName
    } else if (isNumberHidden) {
        "Private Caller"
    } else if (callerNumber.isNotBlank()) {
        callerNumber
    } else {
        "Unknown Caller"
    }

    val initials = remember(displayName) {
        val parts = displayName.trim().split(" ").filter { it.isNotBlank() }
        if (parts.size >= 2) {
            "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        } else {
            parts.firstOrNull()?.take(2)?.uppercase() ?: "#"
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "topBarAura")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSpeaking) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(monetColors.primary.copy(alpha = 0.25f))
                )
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(monetColors.surfaceContainerHigh)
                    .border(1.dp, monetColors.outlineVariant.copy(alpha = 0.40f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = monetColors.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = displayName,
                color = monetColors.onSurface,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(monetColors.primary)
                )
                Text(
                    text = "Screening • ${formatDuration(durationSeconds)}",
                    color = monetColors.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )

                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(monetColors.errorContainer.copy(alpha = 0.7f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "REC ${formatDuration(recordingDurationSeconds)}",
                            color = monetColors.onErrorContainer,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isSpeakerOn) monetColors.primaryContainer else monetColors.surfaceContainerHigh)
                    .border(
                        0.8.dp,
                        if (isSpeakerOn) monetColors.primary.copy(alpha = 0.6f) else monetColors.outlineVariant.copy(alpha = 0.3f),
                        CircleShape
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleSpeaker()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Speaker",
                    tint = if (isSpeakerOn) monetColors.onPrimaryContainer else monetColors.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isMuted) monetColors.errorContainer else monetColors.surfaceContainerHigh)
                    .border(
                        0.8.dp,
                        if (isMuted) monetColors.error.copy(alpha = 0.6f) else monetColors.outlineVariant.copy(alpha = 0.3f),
                        CircleShape
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleMute()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    contentDescription = "Mute",
                    tint = if (isMuted) monetColors.onErrorContainer else monetColors.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(monetColors.surfaceContainerHigh)
                    .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.3f), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenTools()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Screening tools menu",
                    tint = monetColors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                if (isSpeechJammerActive) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-3).dp, y = 3.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1))
                    )
                }
            }
        }
    }
}

@Composable
fun PixelAssistantWaveDots(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotsWave")
    val dotColors = listOf(
        Color(0xFF4285F4),
        Color(0xFFEA4335),
        Color(0xFFFBBC05),
        Color(0xFF34A853)
    )
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dotColors.forEachIndexed { index, color ->
            val sinVal = sin(wavePhase + index * 0.78f)
            val scale = 0.82f + ((sinVal + 1f) * 0.22f)
            val yOffset = sinVal * 4.5f
            Box(
                modifier = Modifier
                    .offset(y = yOffset.dp)
                    .size(10.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun ScreeningTranscriptView(
    messages: List<ChatMessage>,
    isTtsSpeaking: Boolean,
    isListeningToUser: Boolean,
    userLiveTranscript: String,
    isSimulated: Boolean,
    listState: LazyListState,
    monetColors: ColorScheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (messages.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                PixelAssistantWaveDots()

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Call Assistant is screening this call",
                    color = monetColors.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.2).sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isSimulated)
                        "Select a suggested reply below or type a message to synthesize speech."
                    else
                        "Live speech transcription from the caller will appear here in real time.",
                    color = monetColors.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(monetColors.surfaceContainerHigh)
                        .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.35f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isTtsSpeaking) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = monetColors.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Speaking vocal reply to caller...",
                                color = monetColors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            JumpingAudioWaveform(color = monetColors.primary, isPlaying = true)
                            Text(
                                text = "Listening for caller's voice...",
                                color = monetColors.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp)
            ) {
                items(messages) { msg ->
                    MaterialYouCallScreenBubble(msg, monetColors)
                }

                if (isListeningToUser && userLiveTranscript.isNotBlank()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(0.85f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(end = 4.dp, bottom = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Mic,
                                        contentDescription = null,
                                        tint = Color(0xFF00E676),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "SPEAKING LIVE",
                                        color = Color(0xFF00E676),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomEnd = 18.dp, bottomStart = 18.dp))
                                        .background(monetColors.secondaryContainer.copy(alpha = 0.90f))
                                        .border(1.dp, Color(0xFF00E676).copy(alpha = 0.40f), RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomEnd = 18.dp, bottomStart = 18.dp))
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "\"$userLiveTranscript\"",
                                        color = monetColors.onSecondaryContainer,
                                        fontSize = 14.5.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }

                if (isTtsSpeaking) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(monetColors.primaryContainer.copy(alpha = 0.7f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                JumpingAudioWaveform(color = monetColors.primary, isPlaying = true)
                                Text(
                                    text = "Assistant speaking...",
                                    color = monetColors.onPrimaryContainer,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialYouCallScreenBubble(
    message: ChatMessage,
    monetColors: ColorScheme
) {
    val isCaller = message.sender == MessageSender.CALLER
    val isSystem = message.sender == MessageSender.SYSTEM

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val bubbleAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "bubbleAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = bubbleAlpha },
        contentAlignment = when {
            isSystem -> Alignment.Center
            isCaller -> Alignment.CenterStart
            else -> Alignment.CenterEnd
        }
    ) {
        if (isSystem) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(monetColors.surfaceContainerHighest.copy(alpha = 0.55f))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = message.text,
                    color = monetColors.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else if (isCaller) {
            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "CALLER",
                    color = monetColors.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp))
                        .background(monetColors.surfaceContainerHigh)
                        .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.30f), RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = message.text,
                        color = monetColors.onSurface,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = monetColors.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "ASSISTANT",
                        color = monetColors.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomEnd = 20.dp, bottomStart = 20.dp))
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomEnd = 20.dp, bottomStart = 20.dp))
                        .background(monetColors.primaryContainer)
                        .border(0.8.dp, monetColors.primary.copy(alpha = 0.35f), RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomEnd = 20.dp, bottomStart = 20.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = message.text,
                        color = monetColors.onPrimaryContainer,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

private enum class ScreeningActionTab {
    QUICK_REPLIES,
    CUSTOM_AUDIO
}

@Composable
private fun ScreeningResponsesAndAudioSection(
    quickReplies: List<QuickReply>,
    onReplySelected: (QuickReply) -> Unit,
    presets: List<AudioPreset>,
    onPlayPreset: (AudioPreset) -> Unit,
    playingAudioPresetId: String? = null,
    playingAudioPresetTitle: String? = null,
    isAudioPresetPlaying: Boolean = false,
    isAudioPresetPaused: Boolean = false,
    onPauseCustomAudio: () -> Unit = {},
    onResumeCustomAudio: () -> Unit = {},
    onStopCustomAudio: () -> Unit = {},
    onOpenPresetManager: () -> Unit,
    monetColors: ColorScheme
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var activeTab by remember { mutableStateOf(ScreeningActionTab.QUICK_REPLIES) }

    val defaultSuggestions = remember {
        listOf(
            QuickReply(id = "who", title = "Who is this?", spokenText = "May I ask who is calling, please?"),
            QuickReply(id = "reason", title = "What is this regarding?", spokenText = "Could you tell me what this call is regarding?"),
            QuickReply(id = "busy", title = "I'll call back later", spokenText = "I am unavailable to talk right now. I will call you back later."),
            QuickReply(id = "urgent", title = "Is it urgent?", spokenText = "Is this an urgent matter, or can it wait?"),
            QuickReply(id = "text", title = "Please text me", spokenText = "Please send the details via text message instead.")
        )
    }

    val allReplies = remember(quickReplies) {
        val list = mutableListOf<QuickReply>()
        val seen = mutableSetOf<String>()
        quickReplies.forEach {
            if (seen.add(it.id)) list.add(it)
        }
        defaultSuggestions.forEach {
            if (seen.add(it.id)) list.add(it)
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (activeTab == ScreeningActionTab.QUICK_REPLIES) monetColors.primaryContainer
                        else monetColors.surfaceContainerHigh.copy(alpha = 0.65f)
                    )
                    .border(
                        0.8.dp,
                        if (activeTab == ScreeningActionTab.QUICK_REPLIES) monetColors.primary.copy(alpha = 0.50f)
                        else monetColors.outlineVariant.copy(alpha = 0.25f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        activeTab = ScreeningActionTab.QUICK_REPLIES
                    }
                    .padding(horizontal = 11.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Message,
                        contentDescription = null,
                        tint = if (activeTab == ScreeningActionTab.QUICK_REPLIES) monetColors.onPrimaryContainer else monetColors.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Quick Replies",
                        color = if (activeTab == ScreeningActionTab.QUICK_REPLIES) monetColors.onPrimaryContainer else monetColors.onSurfaceVariant,
                        fontSize = 11.5.sp,
                        fontWeight = if (activeTab == ScreeningActionTab.QUICK_REPLIES) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (activeTab == ScreeningActionTab.CUSTOM_AUDIO) monetColors.primaryContainer
                        else monetColors.surfaceContainerHigh.copy(alpha = 0.65f)
                    )
                    .border(
                        0.8.dp,
                        if (activeTab == ScreeningActionTab.CUSTOM_AUDIO) monetColors.primary.copy(alpha = 0.50f)
                        else monetColors.outlineVariant.copy(alpha = 0.25f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        activeTab = ScreeningActionTab.CUSTOM_AUDIO
                    }
                    .padding(horizontal = 11.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = if (activeTab == ScreeningActionTab.CUSTOM_AUDIO) monetColors.onPrimaryContainer else monetColors.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Custom Audio (${presets.size})",
                        color = if (activeTab == ScreeningActionTab.CUSTOM_AUDIO) monetColors.onPrimaryContainer else monetColors.onSurfaceVariant,
                        fontSize = 11.5.sp,
                        fontWeight = if (activeTab == ScreeningActionTab.CUSTOM_AUDIO) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isAudioPresetPlaying || isAudioPresetPaused,
            enter = fadeIn(tween(180)) + expandVertically(tween(200)),
            exit = fadeOut(tween(140)) + shrinkVertically(tween(160)),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isAudioPresetPaused) VibrantAmber.copy(alpha = 0.18f)
                        else monetColors.primaryContainer.copy(alpha = 0.40f)
                    )
                    .border(
                        0.8.dp,
                        if (isAudioPresetPaused) VibrantAmber.copy(alpha = 0.55f)
                        else monetColors.primary.copy(alpha = 0.45f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (!isAudioPresetPaused) {
                            JumpingAudioWaveform(color = monetColors.primary, isPlaying = true)
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(VibrantAmber.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Pause,
                                    contentDescription = null,
                                    tint = VibrantAmber,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isAudioPresetPaused) "PAUSED" else "PLAYING CUSTOM AUDIO",
                                color = if (isAudioPresetPaused) VibrantAmber else monetColors.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = playingAudioPresetTitle ?: "Audio Preset",
                                color = monetColors.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isAudioPresetPaused) VibrantAmber else monetColors.primary)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (isAudioPresetPaused) onResumeCustomAudio() else onPauseCustomAudio()
                                }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAudioPresetPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                    contentDescription = if (isAudioPresetPaused) "Resume" else "Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isAudioPresetPaused) "Resume" else "Pause",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(monetColors.surfaceContainerHighest)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onStopCustomAudio()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Stop",
                                tint = monetColors.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            if (activeTab == ScreeningActionTab.QUICK_REPLIES) {
                itemsIndexed(allReplies, key = { index, reply -> "reply_${reply.id}_$index" }) { _, reply ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(monetColors.surfaceContainerHigh)
                            .border(1.dp, monetColors.outlineVariant.copy(alpha = 0.40f), CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onReplySelected(reply)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Message,
                                contentDescription = null,
                                tint = monetColors.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = reply.title,
                                color = monetColors.onSurface,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = (-0.1).sp
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(presets, key = { index, preset -> "preset_${preset.id}_$index" }) { _, preset ->
                    val isCurrent = playingAudioPresetId == preset.id
                    val isPlayingThis = isCurrent && isAudioPresetPlaying && !isAudioPresetPaused
                    val isPausedThis = isCurrent && isAudioPresetPaused

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isPlayingThis) monetColors.primaryContainer
                                else if (isPausedThis) VibrantAmber.copy(alpha = 0.20f)
                                else monetColors.surfaceContainerHigh
                            )
                            .border(
                                width = if (isPlayingThis || isPausedThis) 1.2.dp else 1.dp,
                                color = if (isPlayingThis) monetColors.primary
                                else if (isPausedThis) VibrantAmber
                                else monetColors.primary.copy(alpha = 0.35f),
                                shape = CircleShape
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPlayPreset(preset)
                            }
                            .padding(horizontal = 13.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isPlayingThis) monetColors.primary
                                        else if (isPausedThis) VibrantAmber
                                        else monetColors.primaryContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlayingThis) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (isPlayingThis) "Pause preset" else "Play preset",
                                    tint = if (isPlayingThis || isPausedThis) Color.White else monetColors.onPrimaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = if (isPausedThis) "${preset.title} (Paused)" else preset.title,
                                color = if (isPausedThis) VibrantAmber else monetColors.onSurface,
                                fontSize = 12.5.sp,
                                fontWeight = if (isPlayingThis || isPausedThis) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = (-0.1).sp
                            )
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(monetColors.surfaceContainerHighest.copy(alpha = 0.5f))
                            .border(1.dp, monetColors.outlineVariant.copy(alpha = 0.35f), CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onOpenPresetManager()
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = monetColors.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Manage Presets",
                                color = monetColors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreeningTypeToSpeakBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    isListeningToMic: Boolean,
    onToggleMic: () -> Unit,
    monetColors: ColorScheme
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val sendSource = remember { MutableInteractionSource() }
    val isSendPressed by sendSource.collectIsPressedAsState()
    val sendScale by animateFloatAsState(
        targetValue = if (isSendPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "typeSendScale"
    )

    val isHasText = text.isNotBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(52.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                spotColor = if (isHasText) monetColors.primary.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.25f)
            )
            .clip(CircleShape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        monetColors.surfaceContainerHigh,
                        monetColors.surfaceContainerHighest,
                        monetColors.surfaceContainerHigh
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    if (isHasText) {
                        listOf(
                            Color.White.copy(alpha = 0.50f),
                            monetColors.primary,
                            monetColors.outlineVariant.copy(alpha = 0.35f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.35f),
                            monetColors.outlineVariant.copy(alpha = 0.35f)
                        )
                    }
                ),
                shape = CircleShape
            )
            .padding(start = 12.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isHasText) monetColors.primary.copy(alpha = 0.18f)
                    else monetColors.surfaceContainerLowest.copy(alpha = 0.40f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Keyboard,
                contentDescription = "Type response",
                tint = if (isHasText) monetColors.primary else monetColors.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        BasicTextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = AppFont,
                color = monetColors.onSurface,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp
            ),
            cursorBrush = SolidColor(monetColors.primary),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = if (isListeningToMic) "Listening... speak into mic" else "Type message to speak aloud...",
                            color = if (isListeningToMic) Color(0xFF00E676) else monetColors.onSurfaceVariant.copy(alpha = 0.55f),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (isHasText) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTextChanged("")
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Clear text",
                    tint = monetColors.onSurfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        if (isHasText) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(sendScale)
                    .shadow(elevation = 6.dp, shape = CircleShape, spotColor = monetColors.primary.copy(alpha = 0.45f))
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                monetColors.primary,
                                monetColors.primaryContainer
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.40f), CircleShape)
                    .clickable(
                        interactionSource = sendSource,
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSend()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send and speak aloud",
                    tint = monetColors.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isListeningToMic) Color(0xFF00C853)
                        else monetColors.surfaceContainerLowest.copy(alpha = 0.50f)
                    )
                    .border(
                        0.8.dp,
                        if (isListeningToMic) Color.White.copy(alpha = 0.60f)
                        else monetColors.outlineVariant.copy(alpha = 0.35f),
                        CircleShape
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleMic()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListeningToMic) Icons.Filled.Mic else Icons.Filled.MicOff,
                    contentDescription = "Toggle mic voice",
                    tint = if (isListeningToMic) Color.White else monetColors.onSurfaceVariant.copy(alpha = 0.80f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ScreeningBottomActionBar(
    onHangUp: () -> Unit,
    onTakeOver: () -> Unit,
    monetColors: ColorScheme,
    isHangUpHandledExternally: Boolean = false
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "bottomBarInfinite")
    val glintPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "actionBarGlint"
    )

    val hangUpSource = remember { MutableInteractionSource() }
    val isHangUpPressed by hangUpSource.collectIsPressedAsState()
    val hangUpScaleX by animateFloatAsState(
        targetValue = if (isHangUpPressed) 1.04f else 1.0f,
        animationSpec = spring(dampingRatio = 0.54f, stiffness = Spring.StiffnessMediumLow),
        label = "hangUpScaleX"
    )
    val hangUpScaleY by animateFloatAsState(
        targetValue = if (isHangUpPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.54f, stiffness = Spring.StiffnessMediumLow),
        label = "hangUpScaleY"
    )
    val hangUpCornerRadius by animateDpAsState(
        targetValue = if (isHangUpPressed) 18.dp else 27.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "hangUpCornerRadius"
    )
    val hangUpElevation by animateDpAsState(
        targetValue = if (isHangUpPressed) 3.dp else 8.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "hangUpElevation"
    )
    val hangUpIconRotation by animateFloatAsState(
        targetValue = if (isHangUpPressed) -12f else 0f,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMediumLow),
        label = "hangUpIconRotation"
    )

    val takeOverSource = remember { MutableInteractionSource() }
    val isTakeOverPressed by takeOverSource.collectIsPressedAsState()
    val takeOverScaleX by animateFloatAsState(
        targetValue = if (isTakeOverPressed) 1.04f else 1.0f,
        animationSpec = spring(dampingRatio = 0.54f, stiffness = Spring.StiffnessMediumLow),
        label = "takeOverScaleX"
    )
    val takeOverScaleY by animateFloatAsState(
        targetValue = if (isTakeOverPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.54f, stiffness = Spring.StiffnessMediumLow),
        label = "takeOverScaleY"
    )
    val takeOverCornerRadius by animateDpAsState(
        targetValue = if (isTakeOverPressed) 18.dp else 27.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "takeOverCornerRadius"
    )
    val takeOverElevation by animateDpAsState(
        targetValue = if (isTakeOverPressed) 3.dp else 8.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "takeOverElevation"
    )
    val takeOverIconRotation by animateFloatAsState(
        targetValue = if (isTakeOverPressed) 10f else 0f,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMediumLow),
        label = "takeOverIconRotation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isHangUpHandledExternally) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .graphicsLayer {
                        scaleX = hangUpScaleX
                        scaleY = hangUpScaleY
                    }
                    .shadow(
                        elevation = hangUpElevation,
                        shape = RoundedCornerShape(hangUpCornerRadius),
                        spotColor = Color(0x95FF334B)
                    )
                    .clip(RoundedCornerShape(hangUpCornerRadius))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFF334B),
                                Color(0xFFD00826)
                            )
                        )
                    )
                    .border(
                        1.2.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isHangUpPressed) 0.65f else 0.45f),
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = if (isHangUpPressed) 0.50f else 0.22f)
                            )
                        ),
                        RoundedCornerShape(hangUpCornerRadius)
                    )
                    .clickable(
                        interactionSource = hangUpSource,
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onHangUp()
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(topStart = hangUpCornerRadius, topEnd = hangUpCornerRadius))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.28f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val glintW = size.width * 0.45f
                    val currentX = (size.width + glintW * 2) * glintPhase - glintW
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.20f),
                                Color.Transparent
                            ),
                            startX = currentX - glintW * 0.5f,
                            endX = currentX + glintW * 0.5f
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = "Hang up",
                        tint = Color.White,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer {
                                rotationZ = hangUpIconRotation
                            }
                    )
                    Text(
                        text = "Hang up",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        } else {
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .graphicsLayer {
                    scaleX = takeOverScaleX
                    scaleY = takeOverScaleY
                }
                .shadow(
                    elevation = takeOverElevation,
                    shape = RoundedCornerShape(takeOverCornerRadius),
                    spotColor = Color(0x9000E676)
                )
                .clip(RoundedCornerShape(takeOverCornerRadius))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF00E676),
                            Color(0xFF009624)
                        )
                    )
                )
                .border(
                    1.2.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isTakeOverPressed) 0.65f else 0.45f),
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = if (isTakeOverPressed) 0.50f else 0.22f)
                        )
                    ),
                    RoundedCornerShape(takeOverCornerRadius)
                )
                .clickable(
                    interactionSource = takeOverSource,
                    indication = null
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTakeOver()
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(topStart = takeOverCornerRadius, topEnd = takeOverCornerRadius))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.28f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val glintW = size.width * 0.45f
                val currentX = (size.width + glintW * 2) * glintPhase - glintW
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        startX = currentX - glintW * 0.5f,
                        endX = currentX + glintW * 0.5f
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Take over call",
                    tint = Color.White,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            rotationZ = takeOverIconRotation
                        }
                )
                Text(
                    text = "Take over",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

@Composable
private fun ScreeningToolsBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    audioMode: AudioCallMode,
    onSwitchToAiType: () -> Unit,
    onSwitchToDirectMic: () -> Unit,
    currentPersona: VoicePersona,
    onSelectPersona: (VoicePersona) -> Unit,
    presets: List<AudioPreset>,
    onPlayPreset: (AudioPreset) -> Unit,
    playingAudioPresetId: String? = null,
    isAudioPresetPlaying: Boolean = false,
    isAudioPresetPaused: Boolean = false,
    onOpenPresetManager: () -> Unit,
    isSpeechJammerActive: Boolean,
    onToggleSpeechJammer: () -> Unit,
    isEchoUserVoice: Boolean = false,
    onToggleEchoUserVoice: () -> Unit = {},
    isRecording: Boolean,
    recordingDurationSeconds: Long,
    onToggleRecording: () -> Unit,
    monetColors: ColorScheme
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    BackHandler(enabled = visible) {
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(280)) { it },
        exit = fadeOut(tween(160)) + slideOutVertically(tween(240)) { it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80000000))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(monetColors.surfaceContainerHigh)
                    .border(1.dp, monetColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .navigationBarsPadding()
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp, 4.dp)
                            .clip(CircleShape)
                            .background(monetColors.outlineVariant)
                            .align(Alignment.CenterHorizontally)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = null,
                                tint = monetColors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Screening Tools & Audio",
                                color = monetColors.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(monetColors.surfaceContainerHighest)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = monetColors.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "VOICE RELAY MODE",
                            color = monetColors.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(monetColors.surfaceContainer)
                                .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val isAi = audioMode == AudioCallMode.AI_TYPE_SPEAK
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isAi) monetColors.primaryContainer else Color.Transparent)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSwitchToAiType()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = if (isAi) monetColors.onPrimaryContainer else monetColors.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "AI Voice (TTS)",
                                        color = if (isAi) monetColors.onPrimaryContainer else monetColors.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        fontWeight = if (isAi) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }

                            val isDirect = audioMode == AudioCallMode.DIRECT_MIC
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDirect) monetColors.primaryContainer else Color.Transparent)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSwitchToDirectMic()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Call,
                                        contentDescription = null,
                                        tint = if (isDirect) monetColors.onPrimaryContainer else monetColors.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Direct Mic",
                                        color = if (isDirect) monetColors.onPrimaryContainer else monetColors.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        fontWeight = if (isDirect) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "VOICE PERSONA",
                            color = monetColors.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(VoicePersona.ALL_PERSONAS, key = { it.id }) { persona ->
                                val isSelected = persona.id == currentPersona.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) monetColors.primaryContainer else monetColors.surfaceContainer)
                                        .border(
                                            0.8.dp,
                                            if (isSelected) monetColors.primary else monetColors.outlineVariant.copy(alpha = 0.3f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onSelectPersona(persona)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = persona.code,
                                            color = if (isSelected) monetColors.primary else monetColors.onSurfaceVariant,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = persona.name,
                                            color = if (isSelected) monetColors.onPrimaryContainer else monetColors.onSurface,
                                            fontSize = 12.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SOUNDBOARD AUDIO",
                                color = monetColors.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Manage Presets",
                                color = monetColors.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable {
                                    onDismiss()
                                    onOpenPresetManager()
                                }
                            )
                        }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(presets, key = { index, preset -> "preset_${preset.id}_$index" }) { _, preset ->
                                val isCurrent = playingAudioPresetId == preset.id
                                val isPlayingThis = isCurrent && isAudioPresetPlaying && !isAudioPresetPaused
                                val isPausedThis = isCurrent && isAudioPresetPaused

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isPlayingThis) monetColors.primaryContainer
                                            else if (isPausedThis) VibrantAmber.copy(alpha = 0.20f)
                                            else monetColors.surfaceContainer
                                        )
                                        .border(
                                            width = if (isPlayingThis || isPausedThis) 1.2.dp else 0.8.dp,
                                            color = if (isPlayingThis) monetColors.primary
                                            else if (isPausedThis) VibrantAmber
                                            else monetColors.outlineVariant.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onPlayPreset(preset)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingThis) Icons.Rounded.Pause
                                            else if (isPausedThis) Icons.Rounded.PlayArrow
                                            else if (preset.isAudioFile) Icons.Rounded.GraphicEq
                                            else Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = null,
                                            tint = if (isPlayingThis) monetColors.primary
                                            else if (isPausedThis) VibrantAmber
                                            else if (preset.isAudioFile) VibrantAmber
                                            else monetColors.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (isPausedThis) "${preset.title} (Paused)" else preset.title,
                                            color = if (isPausedThis) VibrantAmber else monetColors.onSurface,
                                            fontSize = 12.sp,
                                            fontWeight = if (isPlayingThis || isPausedThis) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            SmartJammerControl(
                                isActive = isSpeechJammerActive,
                                onToggle = onToggleSpeechJammer,
                                isEchoUserVoice = isEchoUserVoice,
                                onToggleEchoUserVoice = onToggleEchoUserVoice,
                                modifier = Modifier.fillMaxWidth(),
                                monetColors = monetColors
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isRecording) VibrantRed.copy(alpha = 0.22f) else monetColors.surfaceContainer)
                                .border(
                                    0.8.dp,
                                    if (isRecording) VibrantRed else monetColors.outlineVariant.copy(alpha = 0.35f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onToggleRecording()
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isRecording) VibrantRed else monetColors.onSurfaceVariant)
                                )
                                Text(
                                    text = if (isRecording) "REC ${formatDuration(recordingDurationSeconds)}" else "Record Call",
                                    color = if (isRecording) VibrantRed else monetColors.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmartJammerControl(
    isActive: Boolean,
    onToggle: () -> Unit,
    isEchoUserVoice: Boolean = false,
    onToggleEchoUserVoice: () -> Unit = {},
    modifier: Modifier = Modifier,
    monetColors: ColorScheme = getPixelMonetDarkColorScheme()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val jammerEngine = remember { com.callscreener.app.engine.SpeechJammerEngine.getInstance(context) }
    if (!jammerEngine.isEnabled) return

    val activeMode by jammerEngine.activeModeFlow.collectAsState()
    val isEcho = activeMode == JammerMode.INFINITE_ECHO
    val engineEchoUserVoice by jammerEngine.isEchoUserVoiceFlow.collectAsState()
    val userVoiceEchoActive = isEchoUserVoice || engineEchoUserVoice

    val accentColor = if (isEcho) Color(0xFF6366F1) else Color(0xFFFF2D55)
    val activeBorder = if (isEcho) Color(0xFFA5B4FC) else Color(0xFFFDA4AF)
    val activeBg = if (isEcho) Color(0x356366F1) else Color(0x35FF2D55)

    Row(
        modifier = modifier
            .height(46.dp)
            .clip(CircleShape)
            .background(if (isActive) activeBg else monetColors.surfaceContainerHigh.copy(alpha = 0.90f))
            .border(
                width = if (isActive) 1.2.dp else 0.8.dp,
                color = if (isActive) activeBorder else monetColors.outlineVariant.copy(alpha = 0.35f),
                shape = CircleShape
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isActive) accentColor else monetColors.surfaceContainerHighest)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val nextMode = if (activeMode == JammerMode.DAF_JAMMER) JammerMode.INFINITE_ECHO else JammerMode.DAF_JAMMER
                    jammerEngine.mode = nextMode
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isEcho) Icons.Rounded.GraphicEq else Icons.Rounded.Psychology,
                contentDescription = null,
                tint = if (isActive) Color.White else monetColors.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val nextMode = if (activeMode == JammerMode.DAF_JAMMER) JammerMode.INFINITE_ECHO else JammerMode.DAF_JAMMER
                    jammerEngine.mode = nextMode
                },
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = if (isEcho) "Echo Chamber" else "Speech Jammer",
                    color = if (isActive) Color.White else monetColors.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = AppFont,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
            }
            Text(
                text = if (isEcho) {
                    if (userVoiceEchoActive) "Echo • Both voices" else "Echo • Caller voice"
                } else {
                    "DAF • Delayed playback"
                },
                color = if (isActive) Color.White.copy(alpha = 0.80f) else monetColors.onSurfaceVariant,
                fontSize = 10.5.sp,
                fontFamily = AppFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (userVoiceEchoActive) Color(0xFF2EAA55).copy(alpha = 0.20f)
                    else monetColors.surfaceContainerHighest
                )
                .border(
                    width = 0.8.dp,
                    color = if (userVoiceEchoActive) Color(0xFF2EAA55).copy(alpha = 0.65f)
                    else monetColors.outlineVariant.copy(alpha = 0.35f),
                    shape = CircleShape
                )
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleEchoUserVoice()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (userVoiceEchoActive) Icons.Filled.Mic else Icons.Filled.MicOff,
                contentDescription = "My Voice Echo",
                tint = if (userVoiceEchoActive) Color(0xFF2EAA55) else monetColors.onSurfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.size(15.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .height(32.dp)
                .clip(CircleShape)
                .background(if (isActive) accentColor else monetColors.primary.copy(alpha = 0.16f))
                .border(
                    width = 0.8.dp,
                    color = if (isActive) Color.Transparent else monetColors.primary.copy(alpha = 0.35f),
                    shape = CircleShape
                )
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isActive) "ACTIVE" else "ENGAGE",
                color = if (isActive) Color.White else monetColors.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.4.sp
            )
        }
    }
}

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

@Composable
fun AmbientLiquidBackdrop(isRinging: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "auroraShift")

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

            val orb1X = (width * 0.50f) + (width * 0.30f * Math.sin(rad1).toFloat())
            val orb1Y = (height * 0.28f) + (height * 0.12f * Math.cos(rad1).toFloat())
            val orb1Radius = width * 0.72f * breathingPulse

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x38007AFF),
                        Color(0x2200D2FF),
                        Color(0x0C0044BB),
                        Color.Transparent
                    ),
                    center = Offset(orb1X, orb1Y),
                    radius = orb1Radius
                ),
                radius = orb1Radius,
                center = Offset(orb1X, orb1Y)
            )

            val orb2X = (width * 0.48f) + (width * 0.32f * Math.cos(rad2).toFloat())
            val orb2Y = (height * 0.58f) + (height * 0.14f * Math.sin(rad2).toFloat())
            val orb2Radius = width * 0.76f * (2.05f - breathingPulse)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x325856D6),
                        Color(0x1EAF52DE),
                        Color(0x08331166),
                        Color.Transparent
                    ),
                    center = Offset(orb2X, orb2Y),
                    radius = orb2Radius
                ),
                radius = orb2Radius,
                center = Offset(orb2X, orb2Y)
            )

            val orb3Y = height * 0.86f
            val orb3Radius = width * 0.62f * breathingPulse
            val orb3Colors = if (isRinging) {
                listOf(
                    Color(0x3830D158).copy(alpha = ringPulse * 0.55f),
                    Color(0x2200C7BE).copy(alpha = ringPulse * 0.38f),
                    Color(0x0A004D40),
                    Color.Transparent
                )
            } else {
                listOf(
                    Color(0x2000B4D8),
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

internal fun getPixelMonetDarkColorScheme(): ColorScheme {
    return darkColorScheme(
        primary = Color(0xFFA8C7FA),
        onPrimary = Color(0xFF042E69),
        primaryContainer = Color(0xFF234479),
        onPrimaryContainer = Color(0xFFD3E3FD),
        secondary = Color(0xFFC2E7FF),
        secondaryContainer = Color(0xFF2A4257),
        onSecondaryContainer = Color(0xFFD4E4F7),
        tertiary = Color(0xFFD0BCFF),
        tertiaryContainer = Color(0xFF4A4458),
        onTertiaryContainer = Color(0xFFE8DEF8),
        surface = Color(0xFF111318),
        surfaceContainer = Color(0xFF1E2025),
        surfaceContainerHigh = Color(0xFF282A2F),
        surfaceContainerHighest = Color(0xFF33353A),
        onSurface = Color(0xFFE2E2E6),
        onSurfaceVariant = Color(0xFFC4C6D0),
        outline = Color(0xFF8E9099),
        outlineVariant = Color(0xFF44474F),
        error = Color(0xFFFFB4AB),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6)
    )
}

@Composable
fun OemIncomingCallScreen(
    callerName: String?,
    callerNumber: String,
    isNumberHidden: Boolean = false,
    onDecline: () -> Unit,
    onScreenWithAi: () -> Unit,
    onAnswer: () -> Unit,
    isDeclineHandledExternally: Boolean = false,
    isQuickReplyOpen: Boolean = false,
    onToggleQuickReply: ((Boolean) -> Unit)? = null,
    isScreenCallAvailable: Boolean = true
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val monetColors = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                dynamicDarkColorScheme(context)
            } catch (_: Throwable) {
                getPixelMonetDarkColorScheme()
            }
        } else {
            getPixelMonetDarkColorScheme()
        }
    }

    var localQuickReplySheet by remember { mutableStateOf(false) }
    val effectiveQuickReplyOpen = if (onToggleQuickReply != null) isQuickReplyOpen else localQuickReplySheet

    val infiniteTransition = rememberInfiniteTransition(label = "monetIncomingAnimations")

    val orbBreath by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbBreath"
    )

    val avatarPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarPulse"
    )

    val answerPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "answerPulse"
    )

    val phoneWiggleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2200
                0f at 0
                -14f at 140 using FastOutSlowInEasing
                14f at 280 using FastOutSlowInEasing
                -10f at 420 using FastOutSlowInEasing
                10f at 560 using FastOutSlowInEasing
                -4f at 700 using FastOutSlowInEasing
                0f at 840 using FastOutSlowInEasing
                0f at 2200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "phoneWiggleAngle"
    )

    val screenShimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "screenShimmerPhase"
    )

    val screenAuraPulse by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "screenAuraPulse"
    )

    val declineSource = remember { MutableInteractionSource() }
    val isDeclinePressed by declineSource.collectIsPressedAsState()
    val declineScale by animateFloatAsState(
        targetValue = if (isDeclinePressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "declineScale"
    )

    var isAnswerPressed by remember { mutableStateOf(false) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "animatedAnswerOffsetY"
    )
    val dragProgress = (abs(dragOffsetY) / 100f).coerceIn(0f, 1f)
    val answerScale by animateFloatAsState(
        targetValue = if (isAnswerPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "answerScale"
    )

    val screenSource = remember { MutableInteractionSource() }
    val isScreenPressed by screenSource.collectIsPressedAsState()
    val screenScale by animateFloatAsState(
        targetValue = if (isScreenPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "screenScale"
    )

    val replySource = remember { MutableInteractionSource() }
    val isReplyPressed by replySource.collectIsPressedAsState()
    val replyScale by animateFloatAsState(
        targetValue = if (isReplyPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "replyScale"
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
            ""
        }
    }

    val photoPath = remember(callerNumber) {
        CallerProfileManager.getCustomPhotoPath(context, callerNumber)
    }
    val contactBitmap = rememberContactPhotoBitmap(photoPath)

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to monetColors.primary.copy(alpha = 0.28f),
                        0.38f to monetColors.primaryContainer.copy(alpha = 0.18f),
                        0.70f to monetColors.surface,
                        1.0f to monetColors.surfaceContainerLowest
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier.size(124.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(avatarPulse)
                            .background(monetColors.primary.copy(alpha = 0.12f), CircleShape)
                    )

                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = CircleShape,
                                spotColor = monetColors.primary.copy(alpha = 0.25f)
                            )
                            .clip(CircleShape)
                            .background(monetColors.primaryContainer)
                            .border(
                                width = 1.dp,
                                color = monetColors.outlineVariant.copy(alpha = 0.40f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (contactBitmap != null) {
                            Image(
                                bitmap = contactBitmap,
                                contentDescription = displayCallerTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else if (initials.isNotBlank()) {
                            Text(
                                text = initials,
                                color = monetColors.onPrimaryContainer,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = monetColors.onPrimaryContainer,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = displayCallerTitle,
                    color = monetColors.onSurface,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = displayCallerNumber,
                    color = monetColors.onSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.4.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(monetColors.surfaceContainerHigh.copy(alpha = 0.85f))
                            .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.40f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = "HD Voice",
                                tint = monetColors.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "HD Voice",
                                color = monetColors.onSurface,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(monetColors.surfaceContainerHigh.copy(alpha = 0.85f))
                            .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.40f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = Color(0xFF34A853),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Spam Protected",
                                color = monetColors.onSurfaceVariant,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                if (isScreenCallAvailable) {
                    BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    val density = LocalDensity.current
                    val buttonWidthPx = with(density) { maxWidth.toPx() }
                    val shimmerSpan = buttonWidthPx * 0.85f
                    val totalSweepDistance = buttonWidthPx + shimmerSpan * 2f
                    val sweepStartX = -shimmerSpan + (totalSweepDistance * screenShimmerPhase)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .graphicsLayer {
                                scaleX = screenScale
                                scaleY = screenScale
                            }
                            .shadow(
                                elevation = if (isScreenPressed) 2.dp else 6.dp,
                                shape = RoundedCornerShape(27.dp),
                                spotColor = monetColors.primary.copy(alpha = 0.25f + screenAuraPulse * 0.15f)
                            )
                            .clip(RoundedCornerShape(27.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        monetColors.surfaceContainerHigh,
                                        monetColors.primaryContainer.copy(alpha = 0.25f + screenAuraPulse * 0.15f),
                                        monetColors.surfaceContainerHigh
                                    )
                                )
                            )
                            .border(
                                width = 1.2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        monetColors.outlineVariant.copy(alpha = 0.35f),
                                        monetColors.primary.copy(alpha = 0.40f),
                                        Color(0xFF4285F4).copy(alpha = 0.85f),
                                        Color(0xFF34A853).copy(alpha = 0.80f),
                                        monetColors.primary.copy(alpha = 0.85f),
                                        monetColors.outlineVariant.copy(alpha = 0.35f)
                                    ),
                                    start = Offset(sweepStartX, 0f),
                                    end = Offset(sweepStartX + shimmerSpan, 140f)
                                ),
                                shape = RoundedCornerShape(27.dp)
                            )
                            .clickable(
                                interactionSource = screenSource,
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onScreenWithAi()
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(monetColors.primary.copy(alpha = 0.14f))
                                    .border(0.8.dp, monetColors.primary.copy(alpha = 0.30f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RecordVoiceOver,
                                    contentDescription = "Screen call",
                                    tint = monetColors.primary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = "Screen call",
                                color = monetColors.onSurface,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(monetColors.primaryContainer.copy(alpha = 0.85f))
                                    .border(
                                        0.6.dp,
                                        monetColors.primary.copy(alpha = 0.35f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Assistant",
                                    color = monetColors.onPrimaryContainer,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.3.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = replyScale
                            scaleY = replyScale
                        }
                        .shadow(
                            elevation = if (isReplyPressed) 2.dp else 4.dp,
                            shape = RoundedCornerShape(21.dp),
                            spotColor = monetColors.primary.copy(alpha = 0.20f)
                        )
                        .clip(RoundedCornerShape(21.dp))
                        .background(monetColors.surfaceContainerHigh)
                        .border(
                            width = 1.dp,
                            color = monetColors.outlineVariant.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(21.dp)
                        )
                        .clickable(
                            interactionSource = replySource,
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (onToggleQuickReply != null) onToggleQuickReply(true) else localQuickReplySheet = true
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Message,
                            contentDescription = "Quick reply",
                            tint = monetColors.primary,
                            modifier = Modifier.size(16.dp)
                        )

                        Text(
                            text = "Quick reply",
                            color = monetColors.onSurface,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.2.sp
                        )

                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = null,
                            tint = monetColors.onSurfaceVariant.copy(alpha = 0.70f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.width(84.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isDeclineHandledExternally) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .graphicsLayer {
                                            scaleX = declineScale
                                            scaleY = declineScale
                                        }
                                        .shadow(
                                            elevation = if (isDeclinePressed) 2.dp else 6.dp,
                                            shape = CircleShape,
                                            spotColor = Color(0xFFD32F2F).copy(alpha = 0.45f)
                                        )
                                        .clip(CircleShape)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFFE53935),
                                                    Color(0xFFD32F2F),
                                                    Color(0xFFC62828)
                                                )
                                            )
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                        .clickable(
                                            interactionSource = declineSource,
                                            indication = null
                                        ) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onDecline()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CallEnd,
                                        contentDescription = "Decline call",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(72.dp))
                            }
                        }

                        Text(
                            text = "Decline",
                            color = monetColors.onSurfaceVariant,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.2.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.width(84.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .scale(answerPulse)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                Color(0xFF00E676).copy(alpha = 0.40f),
                                                Color(0xFF00C853).copy(alpha = 0.15f),
                                                Color.Transparent
                                            )
                                        ),
                                        CircleShape
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(0, animatedOffsetY.roundToInt()) }
                                    .size(72.dp)
                                    .graphicsLayer {
                                        scaleX = answerScale
                                        scaleY = answerScale
                                    }
                                    .shadow(
                                        elevation = (if (isAnswerPressed) 2f else 6f + dragProgress * 6f).dp,
                                        shape = CircleShape,
                                        spotColor = Color(0xFF00E676).copy(alpha = 0.65f)
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF00E676),
                                                Color(0xFF00C853),
                                                Color(0xFF00963E)
                                            )
                                        )
                                    )
                                    .border(
                                        1.3.dp,
                                        Brush.linearGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.65f),
                                                Color.White.copy(alpha = 0.20f),
                                                Color.White.copy(alpha = 0.35f)
                                            )
                                        ),
                                        CircleShape
                                    )
                                    .pointerInput(Unit) {
                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)
                                            isAnswerPressed = true
                                            var isDragged = false
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                                if (!change.pressed) {
                                                    isAnswerPressed = false
                                                    if (dragOffsetY <= -50f || !isDragged) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        onAnswer()
                                                    }
                                                    dragOffsetY = 0f
                                                    break
                                                }
                                                val dy = change.position.y - down.position.y
                                                if (dy < -10f) {
                                                    isDragged = true
                                                    dragOffsetY = dy.coerceIn(-140f, 0f)
                                                    change.consume()
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp)
                                        .align(Alignment.TopCenter)
                                        .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color.White.copy(alpha = 0.35f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )

                                Icon(
                                    imageVector = Icons.Filled.Call,
                                    contentDescription = "Answer call",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .rotate(if (dragOffsetY == 0f) phoneWiggleAngle else 0f)
                                )
                            }
                        }

                        Text(
                            text = "Answer",
                            color = monetColors.onSurfaceVariant,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.2.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = effectiveQuickReplyOpen,
            enter = fadeIn(tween(200)) + slideInVertically(tween(300)) { it },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(250)) { it }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000))
                    .clickable {
                        if (onToggleQuickReply != null) onToggleQuickReply(false) else localQuickReplySheet = false
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false) {}
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(monetColors.surfaceContainerHigh)
                        .border(1.dp, monetColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .navigationBarsPadding()
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp, 4.dp)
                                .clip(CircleShape)
                                .background(monetColors.outlineVariant)
                                .align(Alignment.CenterHorizontally)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Quick response",
                                color = monetColors.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(monetColors.surfaceContainerHighest)
                                    .clickable {
                                        if (onToggleQuickReply != null) onToggleQuickReply(false) else localQuickReplySheet = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close",
                                    tint = monetColors.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        val quickMessages = listOf(
                            "Can't talk now. What's up?",
                            "I'll call you right back.",
                            "Please text me instead.",
                            "On my way, will call you soon."
                        )

                        quickMessages.forEach { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(monetColors.surfaceContainer)
                                    .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        if (onToggleQuickReply != null) onToggleQuickReply(false) else localQuickReplySheet = false
                                        onDecline()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = msg,
                                        color = monetColors.onSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Message,
                                        contentDescription = null,
                                        tint = monetColors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

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
    isEchoUserVoice: Boolean = false,
    onToggleEchoUserVoice: () -> Unit = {},
    onScreenWithAi: () -> Unit,
    onDtmfDigit: (Char) -> Unit,
    onHangUp: () -> Unit,
    hasSecondaryCall: Boolean = false,
    secondaryCallerNumber: String? = null,
    secondaryCallerName: String? = null,
    isConference: Boolean = false,
    canMergeCalls: Boolean = false,
    canSwapCalls: Boolean = false,
    onAddCall: (String) -> Unit = {},
    onMergeCalls: () -> Unit = {},
    onSwapCalls: () -> Unit = {},
    isAddCallOpen: Boolean = false,
    onToggleAddCall: ((Boolean) -> Unit)? = null,
    monetColors: ColorScheme = getPixelMonetDarkColorScheme(),
    isEndCallHandledExternally: Boolean = false,
    isScreenCallAvailable: Boolean = true
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showKeypad by remember { mutableStateOf(false) }
    var dtmfDigits by remember { mutableStateOf("") }
    var localAddCallSheet by remember { mutableStateOf(false) }
    val showAddCallSheet = if (onToggleAddCall != null) isAddCallOpen else localAddCallSheet
    fun setAddCallSheet(open: Boolean) {
        if (onToggleAddCall != null) {
            onToggleAddCall(open)
        } else {
            localAddCallSheet = open
        }
    }
    var addCallNumber by remember { mutableStateOf("") }

    val displayCallerTitle = if (isConference) {
        "Conference Call"
    } else if (!callerName.isNullOrBlank() && callerName != "Unknown Caller") {
        callerName
    } else if (isNumberHidden) {
        "Private Caller"
    } else if (callerNumber.isNotBlank()) {
        callerNumber
    } else {
        "Unknown Caller"
    }

    val displayCallerNumber = if (isConference) {
        "Active multi-party conference"
    } else if (isNumberHidden) {
        "Private / Hidden Number"
    } else {
        callerNumber
    }

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
            ""
        }
    }

    val photoPath = remember(callerNumber) {
        CallerProfileManager.getCustomPhotoPath(context, callerNumber)
    }
    val contactBitmap = rememberContactPhotoBitmap(photoPath)

    val bgInfiniteTransition = rememberInfiniteTransition(label = "inCallBgPulse")
    val pulseScale by bgInfiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgPulseScale"
    )
    val pulseAlpha by bgInfiniteTransition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.40f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgPulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to monetColors.primary.copy(alpha = 0.22f),
                        0.30f to monetColors.secondaryContainer.copy(alpha = 0.14f),
                        0.60f to monetColors.surface,
                        1.0f to monetColors.surfaceContainerLowest
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        spotColor = monetColors.primary.copy(alpha = 0.18f)
                    )
                    .clip(CircleShape)
                    .background(monetColors.surfaceContainerHigh.copy(alpha = 0.82f))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.25f),
                                monetColors.outlineVariant.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val dotColor = when (status) {
                        CallStatus.ON_HOLD -> VibrantAmber
                        CallStatus.DIALING -> monetColors.primary
                        else -> Color(0xFF34A853)
                    }
                    val dotAlpha by rememberInfiniteTransition(label = "liveCallDot").animateFloat(
                        initialValue = 0.50f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(900, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dotAlpha"
                    )

                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                dotColor.copy(alpha = if (status == CallStatus.ON_HOLD) 1f else dotAlpha)
                            )
                    )

                    Text(
                        text = when (status) {
                            CallStatus.ON_HOLD -> "ON HOLD"
                            CallStatus.DIALING -> "DIALING…"
                            else -> formatDuration(durationSeconds)
                        },
                        color = when (status) {
                            CallStatus.ON_HOLD -> VibrantAmber
                            CallStatus.DIALING -> monetColors.primary
                            else -> monetColors.onSurface
                        },
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.4.sp
                    )

                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(monetColors.onSurfaceVariant.copy(alpha = 0.40f))
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "HD Voice",
                            tint = monetColors.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "HD Voice",
                            color = monetColors.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.2.sp
                        )
                    }

                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(monetColors.onSurfaceVariant.copy(alpha = 0.40f))
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(VibrantRed)
                            )
                            Text(
                                text = "REC " + formatDuration(recordingDurationSeconds),
                                color = VibrantRed,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = hasSecondaryCall && !isConference,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = monetColors.surfaceContainerHigh.copy(alpha = 0.90f),
                    border = BorderStroke(1.dp, monetColors.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Pause,
                                contentDescription = "On Hold",
                                tint = VibrantAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = "On Hold",
                                    color = VibrantAmber,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = secondaryCallerName ?: secondaryCallerNumber ?: "Call 2",
                                    color = monetColors.onSurface,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(monetColors.primary.copy(alpha = 0.15f))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSwapCalls()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapCalls,
                                    contentDescription = "Swap",
                                    tint = monetColors.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Swap",
                                    color = monetColors.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            val inCallTransitionSpec = tween<Dp>(durationMillis = 260, easing = FastOutSlowInEasing)
            val animatedAvatarSize by animateDpAsState(
                targetValue = if (showKeypad) 48.dp else 96.dp,
                animationSpec = inCallTransitionSpec,
                label = "avatarMorphSize"
            )
            val animatedAvatarCorner by animateDpAsState(
                targetValue = if (showKeypad) 24.dp else 32.dp,
                animationSpec = inCallTransitionSpec,
                label = "avatarMorphCorner"
            )
            val animatedAvatarSpacer by animateDpAsState(
                targetValue = if (showKeypad) 6.dp else 16.dp,
                animationSpec = inCallTransitionSpec,
                label = "avatarMorphSpacer"
            )
            val animatedTitleSpacer by animateDpAsState(
                targetValue = if (showKeypad) 4.dp else 10.dp,
                animationSpec = inCallTransitionSpec,
                label = "titleMorphSpacer"
            )
            val animatedTitleFontSize by animateFloatAsState(
                targetValue = if (showKeypad) 19f else 25f,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                label = "titleFontSize"
            )

            Spacer(modifier = Modifier.height(animatedAvatarSpacer))

            Box(
                modifier = Modifier.size(animatedAvatarSize),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(animatedAvatarSize * 1.34f)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    monetColors.primary.copy(alpha = if (showKeypad) 0.16f else 0.36f),
                                    monetColors.tertiary.copy(alpha = if (showKeypad) 0.08f else 0.18f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(animatedAvatarSize)
                        .shadow(
                            elevation = if (showKeypad) 4.dp else 14.dp,
                            shape = RoundedCornerShape(animatedAvatarCorner),
                            spotColor = monetColors.primary.copy(alpha = 0.35f)
                        )
                        .clip(RoundedCornerShape(animatedAvatarCorner))
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    monetColors.primaryContainer.copy(alpha = 0.95f),
                                    monetColors.surfaceContainerHigh
                                )
                            )
                        )
                        .border(
                            1.2.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.70f),
                                    monetColors.primary.copy(alpha = 0.85f),
                                    monetColors.outlineVariant.copy(alpha = 0.30f)
                                )
                            ),
                            RoundedCornerShape(animatedAvatarCorner)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (contactBitmap != null) {
                        Image(
                            bitmap = contactBitmap,
                            contentDescription = displayCallerTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(animatedAvatarCorner))
                        )
                    } else if (initials.isNotBlank()) {
                        Text(
                            text = initials,
                            color = monetColors.onPrimaryContainer,
                            fontSize = if (showKeypad) 18.sp else 34.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = monetColors.onPrimaryContainer,
                            modifier = Modifier.size(if (showKeypad) 22.dp else 48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(animatedTitleSpacer))

            Text(
                text = displayCallerTitle,
                color = monetColors.onSurface,
                fontSize = animatedTitleFontSize.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = AppFont,
                letterSpacing = (-0.3).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            AnimatedVisibility(
                visible = !showKeypad,
                enter = fadeIn(tween(180, delayMillis = 40)) + expandVertically(tween(240, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(120)) + shrinkVertically(tween(200, easing = FastOutLinearInEasing))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (callerNumber.isNotBlank() && callerName != callerNumber && !isNumberHidden) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = displayCallerNumber,
                            color = monetColors.onSurfaceVariant,
                            fontSize = 14.5.sp,
                            fontFamily = AppFont,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.25.sp
                        )
                    }

                    if (!isNumberHidden && callerNumber.isNotBlank() &&
                        (callerName.isNullOrBlank() || callerName == callerNumber || callerName.equals("Unknown Caller", ignoreCase = true))
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(monetColors.surfaceContainerHigh.copy(alpha = 0.85f))
                                .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.40f), CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    com.callscreener.app.data.DialerRepository.openSystemSaveContact(context, callerNumber)
                                }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PersonAdd,
                                    contentDescription = "Save Contact",
                                    tint = monetColors.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Save Contact",
                                    color = monetColors.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showKeypad,
                    enter = slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(260, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(220, delayMillis = 30, easing = LinearOutSlowInEasing)),
                    exit = slideOutVertically(
                        targetOffsetY = { it / 4 },
                        animationSpec = tween(180, easing = FastOutLinearInEasing)
                    ) + fadeOut(animationSpec = tween(150, easing = FastOutLinearInEasing))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .height(46.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = CircleShape,
                                    spotColor = monetColors.primary.copy(alpha = 0.20f)
                                )
                                .clip(CircleShape)
                                .background(monetColors.surfaceContainerHigh.copy(alpha = 0.90f))
                                .border(1.dp, monetColors.outlineVariant.copy(alpha = 0.40f), CircleShape)
                                .padding(start = 18.dp, end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = dtmfDigits.ifBlank { "Touch dialpad to send tones" },
                                color = if (dtmfDigits.isBlank()) monetColors.onSurfaceVariant.copy(alpha = 0.60f) else monetColors.onSurface,
                                fontSize = if (dtmfDigits.isBlank()) 12.5.sp else 18.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (dtmfDigits.isBlank()) FontWeight.Normal else FontWeight.Bold,
                                letterSpacing = if (dtmfDigits.isBlank()) 0.sp else 2.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (dtmfDigits.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(monetColors.surfaceContainerHighest)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            dtmfDigits = dtmfDigits.dropLast(1)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Clear",
                                        tint = monetColors.primary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val keypadRows = remember {
                            listOf(
                                listOf('1' to "", '2' to "ABC", '3' to "DEF"),
                                listOf('4' to "GHI", '5' to "JKL", '6' to "MNO"),
                                listOf('7' to "PQRS", '8' to "TUV", '9' to "WXYZ"),
                                listOf('*' to "", '0' to "+", '#' to "")
                            )
                        }

                        keypadRows.forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(22.dp),
                                modifier = Modifier.padding(vertical = 3.5.dp)
                            ) {
                                row.forEach { (digit, letters) ->
                                    InCallDtmfKey(
                                        digit = digit,
                                        letters = letters,
                                        monetColors = monetColors,
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
                }
            }

            AnimatedVisibility(
                visible = isScreenCallAvailable && !showKeypad && status != CallStatus.ENDED,
                enter = fadeIn(tween(200, delayMillis = 40)) + expandVertically(tween(240, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(130)) + shrinkVertically(tween(180, easing = FastOutLinearInEasing))
            ) {
                Column {
                    SmartJammerControl(
                        isActive = isSpeechJammerActive,
                        onToggle = onToggleSpeechJammer,
                        isEchoUserVoice = isEchoUserVoice,
                        onToggleEchoUserVoice = onToggleEchoUserVoice,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        monetColors = monetColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        InCallActionButton(
                            icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                            label = if (isMuted) "Unmute" else "Mute",
                            isActive = isMuted,
                            activeColor = VibrantRed,
                            monetColors = monetColors,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleMute()
                            }
                        )
                    }

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        InCallActionButton(
                            icon = if (showKeypad) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.Dialpad,
                            label = if (showKeypad) "Hide" else "Keypad",
                            isActive = showKeypad,
                            activeColor = monetColors.primary,
                            monetColors = monetColors,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showKeypad = !showKeypad
                            }
                        )
                    }

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        InCallActionButton(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            label = if (isSpeakerOn) "Speaker" else "Earpiece",
                            isActive = isSpeakerOn,
                            activeColor = monetColors.primary,
                            monetColors = monetColors,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleSpeaker()
                            }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !showKeypad,
                    enter = fadeIn(tween(200, delayMillis = 40)) + expandVertically(tween(240, easing = FastOutSlowInEasing)),
                    exit = fadeOut(tween(130)) + shrinkVertically(tween(180, easing = FastOutLinearInEasing))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isScreenCallAvailable) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                InCallActionButton(
                                    icon = Icons.Rounded.GraphicEq,
                                    label = if (isRecording) "Recording" else "Record",
                                    isActive = isRecording,
                                    activeColor = VibrantRed,
                                    monetColors = monetColors,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onToggleRecording()
                                    }
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            if (hasSecondaryCall && !isConference) {
                                InCallActionButton(
                                    icon = Icons.Rounded.SwapCalls,
                                    label = "Swap",
                                    isActive = true,
                                    activeColor = monetColors.primary,
                                    monetColors = monetColors,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSwapCalls()
                                    }
                                )
                            } else {
                                InCallActionButton(
                                    icon = if (status == CallStatus.ON_HOLD) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                    label = if (status == CallStatus.ON_HOLD) "Unhold" else "Hold",
                                    isActive = status == CallStatus.ON_HOLD,
                                    activeColor = VibrantAmber,
                                    monetColors = monetColors,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onToggleHold()
                                    }
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            val shouldShowMerge = (canMergeCalls || hasSecondaryCall) && !isConference
                            if (shouldShowMerge) {
                                InCallActionButton(
                                    icon = Icons.AutoMirrored.Rounded.CallMerge,
                                    label = "Merge",
                                    isActive = true,
                                    activeColor = monetColors.primary,
                                    monetColors = monetColors,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onMergeCalls()
                                    }
                                )
                            } else {
                                InCallActionButton(
                                    icon = Icons.Rounded.PersonAdd,
                                    label = "Add call",
                                    isActive = false,
                                    activeColor = monetColors.primary,
                                    monetColors = monetColors,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        setAddCallSheet(true)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3rd row: Screen Call (visible only when root/screen call is available and not in keypad mode)
            AnimatedVisibility(
                visible = isScreenCallAvailable && !showKeypad && status != CallStatus.ENDED,
                enter = fadeIn(tween(200, delayMillis = 60)) + expandVertically(tween(260, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(130)) + shrinkVertically(tween(180, easing = FastOutLinearInEasing))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        // Empty spacer slot for alignment
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        InCallActionButton(
                            icon = Icons.Rounded.RecordVoiceOver,
                            label = "Screen call",
                            isActive = false,
                            activeColor = monetColors.primary,
                            monetColors = monetColors,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onScreenWithAi()
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        // Empty spacer slot for alignment
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isScreenCallAvailable) 24.dp else 12.dp))

            val endCallInfiniteTransition = rememberInfiniteTransition(label = "endCallInfinite")
            val endCallAuraPulse by endCallInfiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.18f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "endCallAuraPulse"
            )
            val endCallAuraAlpha by endCallInfiniteTransition.animateFloat(
                initialValue = 0.32f,
                targetValue = 0.12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "endCallAuraAlpha"
            )
            val endCallGlintPhase by endCallInfiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2600, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "endCallGlintPhase"
            )

            val endCallInteractionSource = remember { MutableInteractionSource() }
            val isEndCallPressed by endCallInteractionSource.collectIsPressedAsState()

            val endCallScaleX by animateFloatAsState(
                targetValue = if (isEndCallPressed) 1.07f else 1.0f,
                animationSpec = spring(dampingRatio = 0.54f, stiffness = Spring.StiffnessMediumLow),
                label = "endCallScaleX"
            )
            val endCallScaleY by animateFloatAsState(
                targetValue = if (isEndCallPressed) 0.85f else 1.0f,
                animationSpec = spring(dampingRatio = 0.54f, stiffness = Spring.StiffnessMediumLow),
                label = "endCallScaleY"
            )
            val endCallCornerRadius by animateDpAsState(
                targetValue = if (isEndCallPressed) 24.dp else 38.dp,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
                label = "endCallCornerRadius"
            )
            val endCallElevation by animateDpAsState(
                targetValue = if (isEndCallPressed) 4.dp else 16.dp,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
                label = "endCallElevation"
            )
            val endCallIconRotation by animateFloatAsState(
                targetValue = if (isEndCallPressed) -14f else 0f,
                animationSpec = spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMediumLow),
                label = "endCallIconRotation"
            )

            if (!isEndCallHandledExternally) {
                Box(
                    modifier = Modifier.size(width = 84.dp, height = 76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val auraMult = if (isEndCallPressed) 1.25f else endCallAuraPulse
                                scaleX = auraMult
                                scaleY = auraMult
                                alpha = if (isEndCallPressed) 0.50f else endCallAuraAlpha
                            }
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF2A4D).copy(alpha = 0.45f),
                                        Color(0xFFD00826).copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(endCallCornerRadius)
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = endCallScaleX
                                scaleY = endCallScaleY
                            }
                            .shadow(
                                elevation = endCallElevation,
                                shape = RoundedCornerShape(endCallCornerRadius),
                                spotColor = Color(0x95FF2A4D)
                            )
                            .clip(RoundedCornerShape(endCallCornerRadius))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFFF334B),
                                        Color(0xFFF01438),
                                        Color(0xFFD00826)
                                    )
                                )
                            )
                            .border(
                                1.5.dp,
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = if (isEndCallPressed) 0.65f else 0.50f),
                                        Color.White.copy(alpha = 0.18f),
                                        Color.White.copy(alpha = if (isEndCallPressed) 0.50f else 0.22f)
                                    )
                                ),
                                RoundedCornerShape(endCallCornerRadius)
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                                .align(Alignment.TopCenter)
                                .clip(RoundedCornerShape(topStart = endCallCornerRadius, topEnd = endCallCornerRadius))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.32f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val glintW = size.width * 0.45f
                            val currentX = (size.width + glintW * 2) * endCallGlintPhase - glintW
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.22f),
                                        Color.Transparent
                                    ),
                                    startX = currentX - glintW * 0.5f,
                                    endX = currentX + glintW * 0.5f
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.Filled.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer {
                                    rotationZ = endCallIconRotation
                                }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(width = 84.dp, height = 76.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        AnimatedVisibility(
            visible = showAddCallSheet,
            enter = fadeIn(tween(200)) + slideInVertically(tween(300)) { it },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(250)) { it }
        ) {
            var showDialpad by remember { mutableStateOf(false) }
            val focusRequester = remember { FocusRequester() }
            val keyboardController = LocalSoftwareKeyboardController.current

            LaunchedEffect(showAddCallSheet) {
                if (showAddCallSheet && !showDialpad) {
                    delay(300)
                    try {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    } catch (_: Exception) {}
                }
            }

            val allContacts = remember { DialerRepository.getCachedContacts(context) }
            val filteredContacts = remember(addCallNumber, allContacts) {
                if (addCallNumber.isBlank()) {
                    allContacts
                } else {
                    val clean = addCallNumber.filter { it.isDigit() }
                    allContacts.filter { c ->
                        c.name.contains(addCallNumber, ignoreCase = true) ||
                        (clean.isNotEmpty() && c.number.replace(Regex("[^0-9]"), "").contains(clean))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000))
                    .clickable { setAddCallSheet(false) },
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {}
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(monetColors.surfaceContainerHigh)
                        .border(
                            1.dp,
                            monetColors.outlineVariant.copy(alpha = 0.35f),
                            RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                        )
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 580.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp, 4.dp)
                                .clip(CircleShape)
                                .background(monetColors.outlineVariant)
                                .align(Alignment.CenterHorizontally)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PersonAdd,
                                    contentDescription = null,
                                    tint = monetColors.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Add Call",
                                    color = monetColors.onSurface,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        showDialpad = !showDialpad
                                        if (showDialpad) {
                                            keyboardController?.hide()
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Dialpad,
                                        contentDescription = "Toggle Keypad",
                                        tint = if (showDialpad) monetColors.primary else monetColors.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { setAddCallSheet(false) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Close",
                                        tint = monetColors.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Search & Number Input Box (Material 3 OutlinedTextField)
                        OutlinedTextField(
                            value = addCallNumber,
                            onValueChange = { addCallNumber = it },
                            placeholder = {
                                Text(
                                    text = "Search contacts or type number…",
                                    color = monetColors.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search",
                                    tint = if (addCallNumber.isNotBlank()) monetColors.primary else monetColors.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (addCallNumber.isNotBlank()) {
                                    IconButton(
                                        onClick = { addCallNumber = "" },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Clear",
                                            tint = monetColors.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = monetColors.surfaceContainerHighest,
                                unfocusedContainerColor = monetColors.surfaceContainerHighest,
                                focusedBorderColor = monetColors.primary,
                                unfocusedBorderColor = monetColors.outlineVariant.copy(alpha = 0.35f),
                                focusedTextColor = monetColors.onSurface,
                                unfocusedTextColor = monetColors.onSurface,
                                cursorColor = monetColors.primary
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    val cleanDigits = addCallNumber.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
                                    if (cleanDigits.isNotBlank()) {
                                        onAddCall(cleanDigits)
                                        setAddCallSheet(false)
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )

                        // Contacts Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (addCallNumber.isBlank()) "Contacts (${filteredContacts.size})" else "Matching Contacts (${filteredContacts.size})",
                                color = monetColors.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (!showDialpad) {
                                Text(
                                    text = "Show Dialpad",
                                    color = monetColors.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { showDialpad = true }
                                )
                            }
                        }

                        // Contacts List (All contacts, smooth scrolling, no limit)
                        if (filteredContacts.isNotEmpty()) {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .heightIn(min = 100.dp, max = if (showDialpad) 140.dp else 280.dp)
                            ) {
                                items(filteredContacts) { contact ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = monetColors.surfaceContainerHighest.copy(alpha = 0.60f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onAddCall(contact.number)
                                                setAddCallSheet(false)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(monetColors.primary.copy(alpha = 0.20f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = contact.name.take(1).uppercase(),
                                                    color = monetColors.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = contact.name,
                                                    color = monetColors.onSurface,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = contact.number,
                                                    color = monetColors.onSurfaceVariant,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Filled.Call,
                                                contentDescription = "Call",
                                                tint = Color(0xFF34A853),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (addCallNumber.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No contacts match \"$addCallNumber\"",
                                    color = monetColors.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Collapsible In-Sheet Dialpad
                        AnimatedVisibility(
                            visible = showDialpad,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            val padRows = remember {
                                listOf(
                                    listOf("1", "2", "3"),
                                    listOf("4", "5", "6"),
                                    listOf("7", "8", "9"),
                                    listOf("*", "0", "#")
                                )
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                padRows.forEach { row ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        row.forEach { digit ->
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = monetColors.surfaceContainerHighest,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                                    .clickable {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        addCallNumber += digit
                                                    }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = digit,
                                                        color = monetColors.onSurface,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Call Action Button
                        Button(
                            onClick = {
                                if (addCallNumber.isNotBlank()) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val cleanDigits = addCallNumber.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
                                    onAddCall(if (cleanDigits.isNotBlank()) cleanDigits else addCallNumber)
                                    setAddCallSheet(false)
                                }
                            },
                            enabled = addCallNumber.isNotBlank(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF34A853),
                                contentColor = Color.White,
                                disabledContainerColor = monetColors.surfaceContainerHighest,
                                disabledContentColor = monetColors.onSurfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Call,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (addCallNumber.isNotBlank()) "Call $addCallNumber & Hold Active" else "Select a contact or enter number",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InCallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = Color.Unspecified,
    monetColors: ColorScheme = getPixelMonetDarkColorScheme(),
    isAssistantSpecial: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val btnScale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1.0f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMediumLow),
        label = "btnScale"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 20.dp else 28.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "btnCorner"
    )

    val effectiveActiveColor = if (activeColor != Color.Unspecified) activeColor else monetColors.primary

    val animatedBg by animateColorAsState(
        targetValue = when {
            isActive && activeColor == VibrantRed -> Color(0xFF4A1018)
            isActive && activeColor == VibrantAmber -> Color(0xFF422800)
            isActive -> monetColors.primaryContainer
            isAssistantSpecial -> monetColors.surfaceContainerHighest
            else -> monetColors.surfaceContainerHigh
        },
        animationSpec = tween(durationMillis = 140),
        label = "btnBg"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = when {
            isActive && activeColor == VibrantRed -> Color(0xFFFF5466).copy(alpha = 0.85f)
            isActive && activeColor == VibrantAmber -> VibrantAmber.copy(alpha = 0.85f)
            isActive -> monetColors.primary.copy(alpha = 0.85f)
            isAssistantSpecial -> monetColors.primary.copy(alpha = 0.50f)
            else -> monetColors.outlineVariant.copy(alpha = 0.28f)
        },
        animationSpec = tween(durationMillis = 140),
        label = "btnBorder"
    )

    val animatedTint by animateColorAsState(
        targetValue = when {
            isActive && activeColor == VibrantRed -> Color(0xFFFF5466)
            isActive && activeColor == VibrantAmber -> VibrantAmber
            isActive -> monetColors.onPrimaryContainer
            isAssistantSpecial -> monetColors.primary
            else -> monetColors.onSurface
        },
        animationSpec = tween(durationMillis = 140),
        label = "btnTint"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.scale(btnScale)
    ) {
        Box(
            modifier = Modifier
                .size(width = 76.dp, height = 70.dp)
                .shadow(
                    elevation = if (isPressed) 2.dp else if (isActive) 6.dp else 2.dp,
                    shape = RoundedCornerShape(cornerRadius),
                    spotColor = if (isActive) effectiveActiveColor.copy(alpha = 0.40f) else Color.Transparent,
                    ambientColor = Color.Transparent
                )
                .clip(RoundedCornerShape(cornerRadius))
                .background(animatedBg)
                .border(
                    width = if (isActive) 1.5.dp else 1.dp,
                    color = animatedBorderColor,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = if (isPressed) 0.08f else 0.16f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = animatedTint,
                modifier = Modifier.size(30.dp)
            )
        }

        Text(
            text = label,
            color = if (isActive) monetColors.onSurface else monetColors.onSurfaceVariant,
            fontSize = 12.5.sp,
            fontFamily = AppFont,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            letterSpacing = 0.15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun InCallDtmfKey(
    digit: Char,
    letters: String,
    monetColors: ColorScheme = getPixelMonetDarkColorScheme(),
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val keyScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.70f, stiffness = Spring.StiffnessMediumLow),
        label = "dtmfKeyScale"
    )
    val keyBg by animateColorAsState(
        targetValue = if (isPressed) monetColors.primaryContainer.copy(alpha = 0.90f) else monetColors.surfaceContainerHigh.copy(alpha = 0.85f),
        animationSpec = tween(durationMillis = 100),
        label = "dtmfKeyBg"
    )
    val keyBorder by animateColorAsState(
        targetValue = if (isPressed) monetColors.primary.copy(alpha = 0.65f) else monetColors.outlineVariant.copy(alpha = 0.35f),
        animationSpec = tween(durationMillis = 100),
        label = "dtmfKeyBorder"
    )

    Box(
        modifier = Modifier
            .size(66.dp)
            .scale(keyScale)
            .shadow(
                elevation = if (isPressed) 2.dp else 4.dp,
                shape = CircleShape,
                spotColor = monetColors.primary.copy(alpha = 0.18f)
            )
            .clip(CircleShape)
            .background(keyBg)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.28f),
                        keyBorder
                    )
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit.toString(),
                color = if (isPressed) monetColors.onPrimaryContainer else monetColors.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = AppFont,
                lineHeight = 26.sp
            )
            if (letters.isNotBlank()) {
                Text(
                    text = letters,
                    color = if (isPressed) monetColors.onPrimaryContainer.copy(alpha = 0.85f) else monetColors.onSurfaceVariant.copy(alpha = 0.75f),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFont,
                    letterSpacing = 0.8.sp,
                    lineHeight = 11.sp
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
