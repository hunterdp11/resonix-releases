package com.callscreener.app.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.callscreener.app.engine.SpeechJammerEngine
import com.callscreener.app.ui.theme.AppleAmber
import com.callscreener.app.ui.theme.AppleBlue
import com.callscreener.app.ui.theme.AppleGreen
import com.callscreener.app.ui.theme.ApplePurple
import com.callscreener.app.ui.theme.AppleRed
import com.callscreener.app.ui.theme.TextMuted
import com.callscreener.app.ui.theme.TextPrimary
import com.callscreener.app.ui.theme.TextSecondary
import com.callscreener.app.ui.theme.TextTertiary

/**
 * Speech Jammer (DAF) Configuration & Interactive Audio Lab Modal Sheet.
 */
@Composable
fun SpeechJammerDialog(
    engine: SpeechJammerEngine,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var isEnabled by remember { mutableStateOf(engine.isEnabled) }
    var selectedDelayMs by remember { mutableIntStateOf(engine.delayMs) }
    var selectedGain by remember { mutableFloatStateOf(engine.intensityGain) }
    var autoJamUnknown by remember { mutableStateOf(engine.autoJamUnknown) }

    var isTestRunning by remember { mutableStateOf(false) }
    val liveDisruptionLevel by engine.liveDisruptionLevel.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            if (isTestRunning) {
                engine.stopJammer()
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (isTestRunning) engine.stopJammer()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090812)),
            color = Color(0xFF090812)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Atmospheric Aura
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x357828C8),
                                    Color(0x18C82848),
                                    Color(0xFF090812)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x309C27B0))
                                    .border(1.dp, Color(0x60BA68C8), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Psychology,
                                    contentDescription = null,
                                    tint = Color(0xFFE1BEE7),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Speech Jammer (DAF)",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.4).sp
                                )
                                Text(
                                    text = "Acoustic Delayed Auditory Feedback",
                                    color = Color(0xFFBA68C8),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isTestRunning) engine.stopJammer()
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // Explanatory Scientific Banner
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x189C27B0))
                                    .border(1.dp, Color(0x389C27B0), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Bolt,
                                            contentDescription = null,
                                            tint = Color(0xFFFF4081),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "How It Impacts The Caller",
                                            color = Color(0xFFFF80AB),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Human speech requires auditory feedback to monitor syllable cadence. By reflecting the caller's voice back with a ~200ms delay, their brain experiences a timing collision—inducing immediate stuttering, slurred speech, and total verbal confusion.",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.5.sp
                                    )
                                }
                            }
                        }

                        // Section 1: Master Enable Toggle
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0x14FFFFFF))
                                    .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(18.dp))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Enable Speech Jammer Engine",
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isEnabled) "Active: Speech Jammer button appears in all calls" else "Disabled: Jammer controls hidden",
                                            color = if (isEnabled) ApplePurple else TextSecondary,
                                            fontSize = 12.sp
                                        )
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
                                            checkedTrackColor = Color(0xFF9C27B0),
                                            uncheckedThumbColor = TextMuted,
                                            uncheckedTrackColor = Color(0x22FFFFFF)
                                        )
                                    )
                                }
                            }
                        }

                        // Section 2: Cognitive Disruption Delay Selector
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Speed,
                                        contentDescription = null,
                                        tint = Color(0xFFBA68C8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "COGNITIVE DISRUPTION DELAY",
                                        color = Color(0xFFCE93D8),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                val delays = listOf(
                                    160 to "160ms (Fast Tempo)",
                                    180 to "180ms (Moderate)",
                                    200 to "200ms (Optimal)",
                                    240 to "240ms (Deep Stutter)"
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    delays.forEach { (ms, label) ->
                                        val isSelected = selectedDelayMs == ms
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) Color(0x359C27B0) else Color(0x10FFFFFF))
                                                .border(
                                                    width = if (isSelected) 1.2.dp else 0.8.dp,
                                                    color = if (isSelected) Color(0xFFBA68C8) else Color(0x18FFFFFF),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    selectedDelayMs = ms
                                                    engine.delayMs = ms
                                                }
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) Color.White else TextSecondary,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (isSelected) {
                                                    Text(
                                                        text = "ACTIVE",
                                                        color = Color(0xFFE1BEE7),
                                                        fontSize = 10.sp,
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

                        // Section 3: Disruption Intensity Gain
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.VolumeUp,
                                        contentDescription = null,
                                        tint = Color(0xFFBA68C8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "DISRUPTION INTENSITY (ECHO GAIN)",
                                        color = Color(0xFFCE93D8),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                val gains = listOf(
                                    1.2f to "1.2x (Subtle Echo)",
                                    1.6f to "1.6x (Optimal / Balanced)",
                                    2.2f to "2.2x (Aggressive / Overpowering)"
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
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) Color(0x359C27B0) else Color(0x10FFFFFF))
                                                .border(
                                                    width = if (isSelected) 1.2.dp else 0.8.dp,
                                                    color = if (isSelected) Color(0xFFBA68C8) else Color(0x18FFFFFF),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    selectedGain = gain
                                                    engine.intensityGain = gain
                                                }
                                                .padding(vertical = 10.dp, horizontal = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label.split(" ")[0],
                                                color = if (isSelected) Color.White else TextSecondary,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Section 4: Auto-Jam Unknown Callers
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x14FFFFFF))
                                    .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Auto-Jam Unknown Callers",
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Automatically engages 200ms DAF when non-contacts speak",
                                            color = TextSecondary,
                                            fontSize = 11.5.sp
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
                                            checkedTrackColor = Color(0xFF9C27B0),
                                            uncheckedThumbColor = TextMuted,
                                            uncheckedTrackColor = Color(0x22FFFFFF)
                                        )
                                    )
                                }
                            }
                        }

                        // Section 5: Interactive Live DAF Test Lab
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0x251C1236))
                                    .border(1.dp, Color(0x50BA68C8), RoundedCornerShape(18.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Headphones,
                                            contentDescription = null,
                                            tint = Color(0xFFFF80AB),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Live DAF Test Lab (Experience It Yourself)",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = "Put on headphones or hold phone to ear. Speak aloud while testing to feel how your brain struggles to speak with 200ms delayed auditory feedback!",
                                        color = TextSecondary,
                                        fontSize = 11.5.sp,
                                        lineHeight = 15.sp
                                    )

                                    // Real-Time VU Meter Bar
                                    if (isTestRunning) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "ACOUSTIC FEEDBACK LEVEL",
                                                    color = Color(0xFFBA68C8),
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${(liveDisruptionLevel * 100).toInt()}%",
                                                    color = Color(0xFFFF80AB),
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0x30FFFFFF))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(liveDisruptionLevel.coerceIn(0.05f, 1.0f))
                                                        .height(8.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                listOf(
                                                                    Color(0xFF9C27B0),
                                                                    Color(0xFFFF4081)
                                                                )
                                                            )
                                                        )
                                                )
                                            }
                                        }
                                    }

                                    // Test Button
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
                                                    Toast.makeText(context, "DAF Active: Speak into mic to feel the jammer!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Could not start audio stream", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isTestRunning) AppleRed else Color(0xFF9C27B0)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isTestRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isTestRunning) "Stop DAF Test" else "Start Live DAF Test",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
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
}

