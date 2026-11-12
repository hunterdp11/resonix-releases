package com.callscreener.app.ui

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callscreener.app.engine.JammerMode
import com.callscreener.app.engine.SpeechJammerEngine
import com.callscreener.app.ui.theme.GoogleSans
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SpeechJammerDialog(
    engine: SpeechJammerEngine,
    monetColors: ColorScheme? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val colors: ColorScheme = monetColors ?: remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                dynamicDarkColorScheme(context)
            } catch (_: Exception) {
                getPixelMonetDarkColorScheme()
            }
        } else {
            getPixelMonetDarkColorScheme()
        }
    }

    var isEnabled by remember { mutableStateOf(engine.isEnabled) }
    var selectedMode by remember { mutableStateOf(engine.mode) }
    var selectedDelayMs by remember { mutableIntStateOf(engine.delayMs) }
    var selectedGain by remember { mutableFloatStateOf(engine.intensityGain) }
    var selectedEchoDecay by remember { mutableFloatStateOf(engine.echoDecay) }
    var autoJamUnknown by remember { mutableStateOf(engine.autoJamUnknown) }
    var echoUserVoice by remember { mutableStateOf(engine.isEchoUserVoiceEnabled) }

    var isTestRunning by remember { mutableStateOf(false) }
    val liveDisruptionLevel by engine.liveDisruptionLevel.collectAsState()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    fun animateDismiss() {
        if (!isVisible) return
        coroutineScope.launch {
            if (isTestRunning) engine.stopJammer()
            isVisible = false
            delay(180)
            onDismiss()
        }
    }

    BackHandler(enabled = isVisible) {
        animateDismiss()
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isTestRunning) {
                engine.stopJammer()
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = spring(
                dampingRatio = 0.85f,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(animationSpec = tween(200)),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(180, easing = FastOutLinearInEasing)
        ) + fadeOut(animationSpec = tween(140))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.background)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    animateDismiss()
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceVariant.copy(alpha = 0.60f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = colors.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Echo Chamber & Jammer",
                                    color = colors.onSurface,
                                    fontSize = 19.sp,
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.3).sp
                                )
                                Text(
                                    text = "Delayed Auditory Feedback (DAF)",
                                    color = colors.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (isEnabled) Color(0xFF2EAA55).copy(alpha = 0.18f)
                                    else colors.surfaceContainerHighest.copy(alpha = 0.70f)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isEnabled) Color(0xFF2EAA55) else colors.onSurfaceVariant.copy(alpha = 0.5f))
                                )
                                Text(
                                    text = if (isEnabled) "ACTIVE" else "OFF",
                                    color = if (isEnabled) Color(0xFF2EAA55) else colors.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.surfaceContainerHigh.copy(alpha = 0.50f))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(colors.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Bolt,
                                            contentDescription = null,
                                            tint = colors.onPrimaryContainer,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Text(
                                        text = "HOW ACOUSTIC DISRUPTION WORKS",
                                        color = colors.primary,
                                        fontSize = 11.sp,
                                        fontFamily = GoogleSans,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                                Text(
                                    text = "Human speech requires auditory feedback to monitor cadence. By reflecting the caller's voice back with a ~200ms delay, their brain experiences a timing collision—inducing immediate stuttering, slurred speech, and total verbal confusion.",
                                    color = colors.onSurfaceVariant,
                                    fontSize = 12.5.sp,
                                    fontFamily = GoogleSans,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(colors.surfaceContainerHigh.copy(alpha = 0.55f))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(if (isEnabled) colors.primaryContainer else colors.surfaceContainerHighest),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Sensors,
                                            contentDescription = null,
                                            tint = if (isEnabled) colors.onPrimaryContainer else colors.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Acoustic Disruption Engine",
                                            color = colors.onSurface,
                                            fontSize = 15.sp,
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isEnabled) "Active in calls • Quick toggle on call screen" else "Engine disabled • Controls hidden",
                                            color = colors.onSurfaceVariant,
                                            fontSize = 12.sp,
                                            fontFamily = GoogleSans
                                        )
                                    }
                                }

                                Switch(
                                    checked = isEnabled,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isEnabled = it
                                        engine.isEnabled = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2EAA55),
                                        uncheckedThumbColor = colors.onSurfaceVariant,
                                        uncheckedTrackColor = colors.surfaceContainerHighest
                                    )
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "ACOUSTIC DISRUPTION MODE",
                                    color = colors.primary,
                                    fontSize = 11.sp,
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }

                            val modes = listOf(
                                JammerMode.DAF_JAMMER to (
                                    "Speech Jammer (DAF)" to
                                    "200ms timing collision — arrests speech motor cadence"
                                ),
                                JammerMode.INFINITE_ECHO to (
                                    "Infinite Echo Chamber" to
                                    "Multi-tap noise-free recursive acoustic reflections with warm damping"
                                )
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                modes.forEach { (modeOption, details) ->
                                    val isSelected = selectedMode == modeOption
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(
                                                if (isSelected) colors.primaryContainer.copy(alpha = 0.40f)
                                                else colors.surfaceContainerHigh.copy(alpha = 0.50f)
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.8.dp,
                                                color = if (isSelected) colors.primary else colors.outlineVariant.copy(alpha = 0.20f),
                                                shape = RoundedCornerShape(18.dp)
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedMode = modeOption
                                                engine.mode = modeOption
                                            }
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isSelected) colors.primaryContainer
                                                            else colors.surfaceContainerHighest
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (modeOption == JammerMode.INFINITE_ECHO) Icons.Rounded.GraphicEq else Icons.Rounded.Psychology,
                                                        contentDescription = null,
                                                        tint = if (isSelected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                                                        modifier = Modifier.size(19.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = details.first,
                                                        color = colors.onSurface,
                                                        fontSize = 14.sp,
                                                        fontFamily = GoogleSans,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = details.second,
                                                        color = colors.onSurfaceVariant,
                                                        fontSize = 11.5.sp,
                                                        fontFamily = GoogleSans,
                                                        lineHeight = 15.sp
                                                    )
                                                }
                                            }

                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(colors.primary),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = "Selected",
                                                        tint = colors.onPrimary,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (selectedMode == JammerMode.INFINITE_ECHO) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SurroundSound,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "ACOUSTIC REVERB DECAY",
                                        color = colors.primary,
                                        fontSize = 11.sp,
                                        fontFamily = GoogleSans,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                val decays = listOf(
                                    0.55f to "0.55x (Room)",
                                    0.68f to "0.68x (Chamber)",
                                    0.85f to "0.85x (Stadium)"
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    decays.forEach { (decay, label) ->
                                        val isSelected = (selectedEchoDecay - decay).let { kotlin.math.abs(it) < 0.05f }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(
                                                    if (isSelected) colors.primaryContainer
                                                    else colors.surfaceContainerHigh.copy(alpha = 0.50f)
                                                )
                                                .border(
                                                    width = if (isSelected) 1.2.dp else 0.8.dp,
                                                    color = if (isSelected) colors.primary else colors.outlineVariant.copy(alpha = 0.20f),
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    selectedEchoDecay = decay
                                                    engine.echoDecay = decay
                                                }
                                                .padding(vertical = 11.dp, horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSelected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                                                fontSize = 12.sp,
                                                fontFamily = GoogleSans,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "Dynamic noise-gate squelch cuts background room noise so silence produces clean reflections without amplifier hiss.",
                                    color = colors.onSurfaceVariant.copy(alpha = 0.70f),
                                    fontSize = 11.5.sp,
                                    fontFamily = GoogleSans,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Speed,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "DISRUPTION DELAY CADENCE",
                                    color = colors.primary,
                                    fontSize = 11.sp,
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }

                            val delays = listOf(
                                160 to "160ms • Fast Tempo",
                                180 to "180ms • Moderate Delay",
                                200 to "200ms • Optimal / Severe",
                                240 to "240ms • Deep Stutter"
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                delays.forEach { (ms, label) ->
                                    val isSelected = selectedDelayMs == ms
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (isSelected) colors.primaryContainer.copy(alpha = 0.40f)
                                                else colors.surfaceContainerHigh.copy(alpha = 0.50f)
                                            )
                                            .border(
                                                width = if (isSelected) 1.2.dp else 0.8.dp,
                                                color = if (isSelected) colors.primary else colors.outlineVariant.copy(alpha = 0.20f),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedDelayMs = ms
                                                engine.delayMs = ms
                                            }
                                            .padding(horizontal = 14.dp, vertical = 11.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                                                fontSize = 13.5.sp,
                                                fontFamily = GoogleSans,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                            if (isSelected) {
                                                Text(
                                                    text = "ACTIVE",
                                                    color = colors.primary,
                                                    fontSize = 10.sp,
                                                    fontFamily = GoogleSans,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.VolumeUp,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "DISRUPTION INTENSITY (ECHO GAIN)",
                                    color = colors.primary,
                                    fontSize = 11.sp,
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }

                            val gains = listOf(
                                1.2f to "1.2x (Subtle)",
                                1.6f to "1.6x (Optimal)",
                                2.2f to "2.2x (Aggressive)"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                gains.forEach { (gain, label) ->
                                    val isSelected = selectedGain == gain
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (isSelected) colors.primaryContainer
                                                else colors.surfaceContainerHigh.copy(alpha = 0.50f)
                                            )
                                            .border(
                                                width = if (isSelected) 1.2.dp else 0.8.dp,
                                                color = if (isSelected) colors.primary else colors.outlineVariant.copy(alpha = 0.20f),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedGain = gain
                                                engine.intensityGain = gain
                                            }
                                            .padding(vertical = 11.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                                            fontSize = 12.sp,
                                            fontFamily = GoogleSans,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(colors.surfaceContainerHigh.copy(alpha = 0.50f))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Auto-Jam Unknown Callers",
                                            color = colors.onSurface,
                                            fontSize = 14.sp,
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Engages acoustic DAF immediately when non-contacts speak",
                                            color = colors.onSurfaceVariant,
                                            fontSize = 11.5.sp,
                                            fontFamily = GoogleSans
                                        )
                                    }

                                    Switch(
                                        checked = autoJamUnknown,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            autoJamUnknown = it
                                            engine.autoJamUnknown = it
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF2EAA55),
                                            uncheckedThumbColor = colors.onSurfaceVariant,
                                            uncheckedTrackColor = colors.surfaceContainerHighest
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.6.dp)
                                        .background(colors.outlineVariant.copy(alpha = 0.25f))
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (echoUserVoice) Icons.Filled.Mic else Icons.Filled.MicOff,
                                                contentDescription = null,
                                                tint = if (echoUserVoice) Color(0xFF2EAA55) else colors.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Echo My Voice (Two-Way)",
                                                color = colors.onSurface,
                                                fontSize = 14.sp,
                                                fontFamily = GoogleSans,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Your mic auto-unmutes and your voice echoes to the caller in the same chamber",
                                            color = colors.onSurfaceVariant,
                                            fontSize = 11.5.sp,
                                            fontFamily = GoogleSans
                                        )
                                    }

                                    Switch(
                                        checked = echoUserVoice,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            echoUserVoice = it
                                            engine.setEchoUserVoice(it)
                                            try {
                                                com.callscreener.app.session.CallSessionManager.getInstance(context).setEchoUserVoice(it)
                                            } catch (_: Exception) {}
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF2EAA55),
                                            uncheckedThumbColor = colors.onSurfaceVariant,
                                            uncheckedTrackColor = colors.surfaceContainerHighest
                                        )
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(colors.surfaceContainerHigh.copy(alpha = 0.55f))
                                .border(1.dp, colors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(colors.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Headphones,
                                            contentDescription = null,
                                            tint = colors.onPrimaryContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = if (selectedMode == JammerMode.INFINITE_ECHO) "Live Echo Chamber Test Lab" else "Live DAF Jammer Test Lab",
                                            color = colors.onSurface,
                                            fontSize = 14.5.sp,
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Interactive Local Microphone Feedback",
                                            color = colors.onSurfaceVariant,
                                            fontSize = 11.5.sp,
                                            fontFamily = GoogleSans
                                        )
                                    }
                                }

                                Text(
                                    text = if (selectedMode == JammerMode.INFINITE_ECHO)
                                        "Put on headphones or hold phone to ear. Speak aloud to experience warm, noise-free cascading reflections in an infinite acoustic chamber!"
                                    else
                                        "Put on headphones or hold phone to ear. Speak aloud while testing to feel how your brain struggles to maintain speech rhythm under delayed feedback!",
                                    color = colors.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontFamily = GoogleSans,
                                    lineHeight = 16.sp
                                )

                                if (isTestRunning) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "FEEDBACK DISRUPTION LEVEL",
                                                color = colors.primary,
                                                fontSize = 10.sp,
                                                fontFamily = GoogleSans,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = "${(liveDisruptionLevel * 100).toInt()}%",
                                                color = colors.primary,
                                                fontSize = 10.sp,
                                                fontFamily = GoogleSans,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(colors.surfaceContainerHighest)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(liveDisruptionLevel.coerceIn(0.05f, 1.0f))
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            listOf(
                                                                colors.primary,
                                                                Color(0xFF2EAA55)
                                                            )
                                                        )
                                                    )
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        if (isTestRunning) {
                                            engine.stopJammer()
                                            isTestRunning = false
                                        } else {
                                            val ok = engine.startJammer(forTestLab = true)
                                            if (ok) {
                                                isTestRunning = true
                                                val msg = if (selectedMode == JammerMode.INFINITE_ECHO)
                                                    "Echo Chamber Active: Speak into mic to hear cascading reflections!"
                                                else
                                                    "DAF Active: Speak into mic to feel the jammer!"
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Could not start audio stream", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isTestRunning) Color(0xFFF44336) else colors.primary,
                                        contentColor = if (isTestRunning) Color.White else colors.onPrimary
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isTestRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val btnText = if (isTestRunning) {
                                        if (selectedMode == JammerMode.INFINITE_ECHO) "Stop Echo Test" else "Stop DAF Test"
                                    } else {
                                        if (selectedMode == JammerMode.INFINITE_ECHO) "Start Live Echo Test" else "Start Live DAF Test"
                                    }
                                    Text(
                                        text = btnText,
                                        fontFamily = GoogleSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
