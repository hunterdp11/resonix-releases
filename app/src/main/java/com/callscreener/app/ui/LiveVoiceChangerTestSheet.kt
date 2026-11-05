package com.callscreener.app.ui

import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.callscreener.app.engine.RealtimeVoiceChangerEngine
import com.callscreener.app.model.VoiceEffectType
import com.callscreener.app.model.VoicePersona
import com.callscreener.app.ui.theme.AppleBlue
import com.callscreener.app.ui.theme.AppleGreen
import com.callscreener.app.ui.theme.ApplePurple
import com.callscreener.app.ui.theme.AppleRed
import com.callscreener.app.ui.theme.AppleYellow
import com.callscreener.app.ui.theme.TextPrimary
import com.callscreener.app.ui.theme.TextSecondary
import com.callscreener.app.ui.theme.TextTertiary
import kotlin.math.sin

/**
 * Dedicated Voice Changer & Pitch Shifter Testing Lab.
 *
 * Allows real-time live testing of microphone input through the in-memory DSP
 * (Pitch Shifting, Formant Smoothing, Robot Ring Modulator, Walkie-Talkie Bandpass)
 * with instant feedback via headphones or speaker, WITHOUT needing to place an active call.
 */
@Composable
fun LiveVoiceChangerTestSheet(
    engine: RealtimeVoiceChangerEngine,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val isActive by engine.isVoiceChangerActive.collectAsState()
    val audioLevel by engine.liveAudioLevel.collectAsState()

    var selectedPersona by remember { mutableStateOf(engine.getCurrentPersona()) }
    var currentPitchSlider by remember {
        mutableFloatStateOf(engine.getCustomPitch() ?: selectedPersona.dspPitchFactor)
    }

    // Safety: Stop test stream cleanly when the sheet is closed/dismissed
    DisposableEffect(Unit) {
        onDispose {
            if (engine.isVoiceChangerActive.value) {
                engine.stop()
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (engine.isVoiceChangerActive.value) {
                engine.stop()
            }
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07090E)),
            color = Color(0xFF07090E)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Subtle Ambient Gradient Background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF140D26),
                                    Color(0xFF0B0E18),
                                    Color(0xFF07090E)
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
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) AppleGreen else ApplePurple)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Voice Morph Studio",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                            Text(
                                text = "Real-Time DSP Pitch Shifter & Mic Routing Lab",
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                if (engine.isVoiceChangerActive.value) {
                                    engine.stop()
                                }
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x20FFFFFF))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card 1: Live Status & Safety Routing Specs
                        item {
                            VoiceEngineStatusCard(isActive = isActive)
                        }

                        // Card 2: Interactive Mic Test Controller + VU Meter
                        item {
                            VoiceMicVisualizerCard(
                                isActive = isActive,
                                audioLevel = audioLevel,
                                onToggleTest = {
                                    if (isActive) {
                                        engine.stop()
                                    } else {
                                        engine.setPersona(selectedPersona)
                                        engine.setCustomPitch(currentPitchSlider)
                                        engine.start(
                                            streamType = AudioManager.STREAM_MUSIC,
                                            context = context,
                                            isTestMode = true
                                        )
                                    }
                                }
                            )
                        }

                        // Card 3: Quick Voice Presets
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SELECT VOICE PERSONA",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${VoicePersona.ALL_PERSONAS.size} Profiles Available",
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    )
                                }

                                // Quick category chips: Real Girls, Deterrent, Effects
                                val samplePersonas = remember {
                                    listOf(
                                        VoicePersona.HINDI_PERSONAS.first { it.id == "hindi_pooja" },
                                        VoicePersona.PURE_GIRL_PERSONAS.first { it.id == "girl_chloe" },
                                        VoicePersona.PURE_GIRL_PERSONAS.first { it.id == "girl_zoe" },
                                        VoicePersona.CLASSIC_PERSONAS.first { it.id == "deep_guard" },
                                        VoicePersona.HINDI_PERSONAS.first { it.id == "hindi_vikram" },
                                        VoicePersona.CLASSIC_PERSONAS.first { it.id == "cyber_cyborg" },
                                        VoicePersona.CLASSIC_PERSONAS.first { it.id == "walkie_talkie" }
                                    )
                                }

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(samplePersonas) { persona ->
                                        PersonaCardItem(
                                            persona = persona,
                                            isSelected = selectedPersona.id == persona.id,
                                            onClick = {
                                                selectedPersona = persona
                                                currentPitchSlider = persona.dspPitchFactor
                                                engine.setPersona(persona)
                                                engine.setCustomPitch(persona.dspPitchFactor)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Card 4: Fine-Tuning Pitch Slider
                        item {
                            PitchFineTuningCard(
                                pitch = currentPitchSlider,
                                defaultPitch = selectedPersona.dspPitchFactor,
                                onPitchChange = { newPitch ->
                                    currentPitchSlider = newPitch
                                    engine.setCustomPitch(newPitch)
                                },
                                onReset = {
                                    currentPitchSlider = selectedPersona.dspPitchFactor
                                    engine.setCustomPitch(selectedPersona.dspPitchFactor)
                                }
                            )
                        }

                        // Card 5: In-Call Root Hardware Isolation Info
                        item {
                            CallIsolationExplainerCard()
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Engine status & routing specs badge card.
 */
@Composable
private fun VoiceEngineStatusCard(isActive: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x18FFFFFF))
            .border(0.8.dp, Color(0x28FFFFFF), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = if (isActive) AppleGreen else ApplePurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isActive) "DSP ACTIVE (STREAM_MUSIC)" else "ENGINE READY (STANDBY)",
                        color = if (isActive) AppleGreen else TextPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isActive) AppleGreen.copy(alpha = 0.2f) else Color(0x1AFFFFFF))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isActive) "16kHz • ~15ms" else "16kHz PCM",
                        color = if (isActive) AppleGreen else TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpecBadge(
                    label = "Acoustic Shift",
                    value = "Dual Hann Delay",
                    color = ApplePurple
                )
                SpecBadge(
                    label = "Hardware Isolation",
                    value = "Root ALSA Uplink",
                    color = AppleBlue
                )
                SpecBadge(
                    label = "Mic Leakage",
                    value = "0.0% (Clean)",
                    color = AppleGreen
                )
            }
        }
    }
}

@Composable
private fun SpecBadge(label: String, value: String, color: Color) {
    Column {
        Text(text = label, color = TextTertiary, fontSize = 9.sp)
        Text(text = value, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Interactive mic test button + live dynamic frequency VU meter.
 */
@Composable
private fun VoiceMicVisualizerCard(
    isActive: Boolean,
    audioLevel: Float,
    onToggleTest: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "idleWave")
    val idlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "idlePhase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1B1832),
                        Color(0xFF101222)
                    )
                )
            )
            .border(
                1.dp,
                if (isActive) Color(0x808E2DE2) else Color(0x30FFFFFF),
                RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Dynamic VU Meter Oscilloscope
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x400A0C16))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val barCount = 28
                for (i in 0 until barCount) {
                    val normalizedIdx = i.toFloat() / barCount.toFloat()
                    val barHeightFraction = if (isActive) {
                        val wave = (sin(idlePhase + normalizedIdx * 4f) * 0.15f + 0.15f).coerceIn(0.05f, 0.3f)
                        val speechEnergy = audioLevel * (0.4f + 0.6f * sin(normalizedIdx * 3.14159f))
                        (wave + speechEnergy).coerceIn(0.08f, 1.0f)
                    } else {
                        0.06f
                    }

                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height((50 * barHeightFraction).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (isActive) {
                                    if (audioLevel > 0.15f) {
                                        Brush.verticalGradient(
                                            listOf(ApplePurple, Color(0xFF00D2FF))
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(Color(0x808E2DE2), Color(0x504A00E0))
                                        )
                                    }
                                } else {
                                    Brush.verticalGradient(
                                        listOf(Color(0x30FFFFFF), Color(0x10FFFFFF))
                                    )
                                }
                            )
                    )
                }
            }

            // Big Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isActive) {
                            Brush.horizontalGradient(
                                listOf(Color(0xFFE52D27), Color(0xFFB31217))
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                            )
                        }
                    )
                    .clickable(onClick = onToggleTest),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isActive) "Stop Live Mic Test" else "Start Live Mic Test (Listen Live)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Headset feedback warning
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Use earphones or Bluetooth buds to avoid acoustic feedback",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Persona selection card item.
 */
@Composable
private fun PersonaCardItem(
    persona: VoicePersona,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) ApplePurple else Color(0x25FFFFFF),
        animationSpec = tween(200),
        label = "personaBorder"
    )

    Box(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0x288E2DE2) else Color(0x16FFFFFF))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = persona.code,
                    color = if (isSelected) ApplePurple else TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(ApplePurple)
                    )
                }
            }

            Text(
                text = persona.name,
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = persona.tag,
                color = if (isSelected) Color(0xFFD4B0FF) else TextSecondary,
                fontSize = 10.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x20FFFFFF))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                val effectLabel = when (persona.effectType) {
                    VoiceEffectType.ROBOTIC_RING_MOD -> "Cyborg Ring Mod"
                    VoiceEffectType.WALKIE_TALKIE_RADIO -> "Walkie-Talkie"
                    VoiceEffectType.DEEP_ANONYMOUS -> "Deep Baritone"
                    VoiceEffectType.NONE -> if (persona.dspPitchFactor > 1.15f) "Real Female Shift" else "Natural Pitch"
                }
                Text(
                    text = effectLabel,
                    color = TextPrimary,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Interactive fine-tuning pitch slider.
 */
@Composable
private fun PitchFineTuningCard(
    pitch: Float,
    defaultPitch: Float,
    onPitchChange: (Float) -> Unit,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x18FFFFFF))
            .border(0.8.dp, Color(0x28FFFFFF), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        tint = ApplePurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Fine-Tune Pitch Shifter",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onReset)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = TextTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Reset (${String.format("%.2f", defaultPitch)}x)", color = TextTertiary, fontSize = 11.sp)
                }
            }

            // Pitch label & multiplier
            val percentDelta = ((pitch - 1.0f) * 100).toInt()
            val deltaStr = if (percentDelta >= 0) "+$percentDelta%" else "$percentDelta%"
            val typeDescription = when {
                pitch >= 1.25f -> "Feminine / Sweet Voice"
                pitch in 1.10f..1.24f -> "High Natural Voice"
                pitch in 0.90f..1.09f -> "Natural Pitch"
                pitch in 0.75f..0.89f -> "Deep Voice"
                else -> "Ultra-Deep Deterrent"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format("%.2f", pitch)}x ($deltaStr)",
                    color = ApplePurple,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = typeDescription,
                    color = TextSecondary,
                    fontSize = 11.5.sp
                )
            }

            Slider(
                value = pitch,
                onValueChange = onPitchChange,
                valueRange = 0.65f..1.45f,
                colors = SliderDefaults.colors(
                    thumbColor = ApplePurple,
                    activeTrackColor = ApplePurple,
                    inactiveTrackColor = Color(0x30FFFFFF)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "0.65x (Deep Bass)", color = TextTertiary, fontSize = 10.sp)
                Text(text = "1.00x (Natural)", color = TextTertiary, fontSize = 10.sp)
                Text(text = "1.45x (Ultra High)", color = TextTertiary, fontSize = 10.sp)
            }
        }
    }
}

/**
 * Explainer card on how the voice changer routes audio in live phone calls.
 */
@Composable
private fun CallIsolationExplainerCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x10FFFFFF))
            .border(0.8.dp, Color(0x20FFFFFF), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    tint = AppleGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Zero-Leakage In-Call Voice Routing",
                    color = AppleGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "When you switch to 'Voice Morph' in live phone calls:\n" +
                        "1. Hardware mic is isolated via root ALSA mixer routing.\n" +
                        "2. Carrier voice uplink is muted at telephony layer.\n" +
                        "3. Your speech is captured via clean AudioRecord, shifted by DSP in ~15ms, and routed into MultiMedia1.\n" +
                        "4. The caller hears ONLY the transformed voice. Your natural voice NEVER leaks.",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

