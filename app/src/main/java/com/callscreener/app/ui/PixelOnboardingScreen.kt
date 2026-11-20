package com.callscreener.app.ui

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callscreener.app.root.AppOperationMode
import com.callscreener.app.root.RootPrivilegeManager
import com.callscreener.app.ui.theme.AppFont
import com.callscreener.app.ui.theme.rememberPixelMonetColorScheme
import kotlinx.coroutines.launch

@Composable
fun PixelOnboardingScreen(
    isDefaultDialer: Boolean,
    onRequestDefaultDialer: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val monetColors: ColorScheme = rememberPixelMonetColorScheme()

    var currentStep by remember { mutableIntStateOf(0) }
    var selectedMode by remember { mutableStateOf(RootPrivilegeManager.getOperationMode(context)) }
    val isDeviceRooted = remember { RootPrivilegeManager.isRootAvailable() }

    var isInstallingModule by remember { mutableStateOf(false) }
    var isModuleInstalled by remember {
        mutableStateOf(RootPrivilegeManager.isMagiskModuleInstalled(context))
    }

    BackHandler(enabled = currentStep > 0) {
        if (currentStep > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (currentStep == 3 && selectedMode == AppOperationMode.STANDARD) {
                currentStep = 1
            } else {
                currentStep--
            }
        }
    }

    fun completeOnboarding() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val prefs = context.getSharedPreferences("call_screener_onboarding", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        RootPrivilegeManager.setOperationMode(context, selectedMode)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(monetColors.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            monetColors.primary.copy(alpha = 0.12f),
                            Color.Transparent,
                            monetColors.surfaceContainerLowest
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 0) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (currentStep == 3 && selectedMode == AppOperationMode.STANDARD) {
                                currentStep = 1
                            } else {
                                currentStep--
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(monetColors.surfaceContainerHigh.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = "Back",
                            tint = monetColors.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(36.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val totalSteps = if (selectedMode == AppOperationMode.ROOT_ENHANCED) 4 else 3
                    for (i in 0 until totalSteps) {
                        val active = (i == currentStep) || (currentStep == 3 && i == 2 && selectedMode == AppOperationMode.STANDARD)
                        val pillWidth = if (active) 24.dp else 7.dp
                        val pillColor by animateColorAsState(
                            targetValue = if (active) monetColors.primary else monetColors.outlineVariant.copy(alpha = 0.35f),
                            animationSpec = tween(200),
                            label = "onboardPill"
                        )
                        Box(
                            modifier = Modifier
                                .height(5.dp)
                                .width(pillWidth)
                                .clip(RoundedCornerShape(3.dp))
                                .background(pillColor)
                        )
                    }
                }

                if (currentStep == 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentStep = 1
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Skip",
                            color = monetColors.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontFamily = AppFont,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(36.dp))
                }
            }

            AnimatedContent(
                targetState = currentStep,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                transitionSpec = {
                    val forward = targetState > initialState
                    (slideInHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        initialOffsetX = { if (forward) it else -it }
                    ) + fadeIn(tween(220))).togetherWith(
                        slideOutHorizontally(
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            targetOffsetX = { if (forward) -it else it }
                        ) + fadeOut(tween(160))
                    )
                },
                label = "onboardingPager"
            ) { step ->
                when (step) {
                    0 -> OnboardingFeaturesStep(monetColors = monetColors)
                    1 -> OnboardingArchitectureStep(
                        selectedMode = selectedMode,
                        isDeviceRooted = isDeviceRooted,
                        onSelectMode = { selectedMode = it },
                        monetColors = monetColors
                    )
                    2 -> OnboardingModuleStep(
                        isModuleInstalled = isModuleInstalled,
                        isInstalling = isInstallingModule,
                        onInstallModule = {
                            coroutineScope.launch {
                                isInstallingModule = true
                                val result = RootPrivilegeManager.installSystemModule(context)
                                isInstallingModule = false
                                isModuleInstalled = RootPrivilegeManager.isMagiskModuleInstalled(context)
                                Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                            }
                        },
                        onRebootNow = {
                            coroutineScope.launch {
                                RootPrivilegeManager.rebootDevice()
                            }
                        },
                        onSwitchToStandard = {
                            selectedMode = AppOperationMode.STANDARD
                            RootPrivilegeManager.setOperationMode(context, AppOperationMode.STANDARD)
                            currentStep = 3
                        },
                        monetColors = monetColors
                    )
                    else -> OnboardingPermissionsStep(
                        isDefaultDialer = isDefaultDialer,
                        onRequestDefaultDialer = onRequestDefaultDialer,
                        monetColors = monetColors
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (currentStep == 3 && !isDefaultDialer) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OnboardingPrimaryButton(
                        text = "Set as Default Phone App",
                        monetColors = monetColors,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onRequestDefaultDialer()
                        }
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                completeOnboarding()
                            }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Skip for now & enter app",
                            color = monetColors.onSurfaceVariant,
                            fontSize = 12.5.sp,
                            fontFamily = AppFont,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                OnboardingPrimaryButton(
                    text = when (currentStep) {
                        0 -> "Get Started"
                        1 -> if (selectedMode == AppOperationMode.ROOT_ENHANCED) "Proceed to Root Setup" else "Continue to Default App"
                        2 -> if (isModuleInstalled) "Reboot Later & Continue" else "Skip to Next Step"
                        else -> "Start Using Resonix"
                    },
                    monetColors = monetColors,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        when (currentStep) {
                            0 -> currentStep = 1
                            1 -> {
                                if (selectedMode == AppOperationMode.ROOT_ENHANCED) {
                                    currentStep = 2
                                } else {
                                    RootPrivilegeManager.setOperationMode(context, AppOperationMode.STANDARD)
                                    currentStep = 3
                                }
                            }
                            2 -> currentStep = 3
                            3 -> completeOnboarding()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun OnboardingFeaturesStep(monetColors: ColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(16.dp, CircleShape, spotColor = Color(0xFF2EAA55).copy(alpha = 0.5f))
                .clip(CircleShape)
                .background(Color(0xFF2EAA55)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        Text(
            text = "Welcome to Resonix",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = monetColors.onSurface,
            fontFamily = AppFont,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Hardware-grade call protection, acoustic speech jamming, and lossless internal audio routing.",
            fontSize = 13.5.sp,
            color = monetColors.onSurfaceVariant,
            fontFamily = AppFont,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        OnboardingFeatureCard(
            icon = Icons.Rounded.GraphicEq,
            iconTint = Color(0xFF2EAA55),
            title = "Acoustic DAF Speech Jammer",
            description = "Delayed Auditory Feedback interrupts spam callers, telemarketers, and fraudsters in real time directly over the call uplink.",
            monetColors = monetColors
        )

        OnboardingFeatureCard(
            icon = Icons.Rounded.Headphones,
            iconTint = Color(0xFF4285F4),
            title = "Native Lossless Audio Injection",
            description = "Direct hardware audio stream routing injects presets, AI voice studio, and speech audio with zero ambient leakage.",
            monetColors = monetColors
        )

        OnboardingFeatureCard(
            icon = Icons.Rounded.Tune,
            iconTint = Color(0xFFFFB74D),
            title = "Echo Cancellation & DSP",
            description = "Advanced hardware acoustic isolation eliminates speakerphone bleed-through while keeping call latency near 0ms.",
            monetColors = monetColors
        )

        OnboardingFeatureCard(
            icon = Icons.Rounded.Security,
            iconTint = monetColors.primary,
            title = "Intelligent Call Interception",
            description = "Screen unknown and suspicious numbers automatically with realistic on-device voice assistant personas.",
            monetColors = monetColors
        )

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun OnboardingArchitectureStep(
    selectedMode: AppOperationMode,
    isDeviceRooted: Boolean,
    onSelectMode: (AppOperationMode) -> Unit,
    monetColors: ColorScheme
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Choose Your Setup",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = monetColors.onSurface,
            fontFamily = AppFont
        )

        Text(
            text = "Select an operating architecture based on your device privileges. CallScreener can run as a standard dialer or as a root-powered hardware engine.",
            fontSize = 13.5.sp,
            color = monetColors.onSurfaceVariant,
            fontFamily = AppFont,
            lineHeight = 18.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isDeviceRooted) Color(0xFF2EAA55).copy(alpha = 0.14f)
                    else monetColors.surfaceContainerHigh.copy(alpha = 0.6f)
                )
                .border(
                    0.8.dp,
                    if (isDeviceRooted) Color(0xFF2EAA55).copy(alpha = 0.45f)
                    else monetColors.outlineVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (isDeviceRooted) Icons.Filled.CheckCircle else Icons.Rounded.Info,
                    contentDescription = null,
                    tint = if (isDeviceRooted) Color(0xFF2EAA55) else monetColors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = if (isDeviceRooted) "Root Privileges Detected" else "Standard Device Detected",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = monetColors.onSurface,
                        fontFamily = AppFont
                    )
                    Text(
                        text = if (isDeviceRooted)
                            "su binary found. Root-Enhanced Mode is recommended."
                        else
                            "No su binary found. Standard Mode is recommended for everyday use.",
                        fontSize = 11.5.sp,
                        color = monetColors.onSurfaceVariant,
                        fontFamily = AppFont
                    )
                }
            }
        }

        OnboardingModeOptionCard(
            title = "Root-Enhanced Mode",
            badge = "HARDWARE HAL INJECTION",
            badgeColor = Color(0xFFFFB74D),
            description = "Enables ALSA mixer routing, direct in-call audio HAL uplink injection, hardware speech jamming, and lossless dual-side recording.",
            isSelected = selectedMode == AppOperationMode.ROOT_ENHANCED,
            monetColors = monetColors,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSelectMode(AppOperationMode.ROOT_ENHANCED)
            }
        )

        OnboardingModeOptionCard(
            title = "Standard Mode",
            badge = "EVERYDAY DIALER • NO ROOT",
            badgeColor = Color(0xFF2EAA55),
            description = "100% safe everyday calling for all Android devices. Uses natural microphone, standard call recording, speed dial, and call screening with zero risk.",
            isSelected = selectedMode == AppOperationMode.STANDARD,
            monetColors = monetColors,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSelectMode(AppOperationMode.STANDARD)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun OnboardingModuleStep(
    isModuleInstalled: Boolean,
    isInstalling: Boolean,
    onInstallModule: () -> Unit,
    onRebootNow: () -> Unit,
    onSwitchToStandard: () -> Unit,
    monetColors: ColorScheme
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Magisk / KernelSU Module",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = monetColors.onSurface,
            fontFamily = AppFont
        )

        Text(
            text = "To bypass Android's restricted in-call audio sandbox without speakerphone leakage, CallScreener creates a system priv-app module.",
            fontSize = 13.5.sp,
            color = monetColors.onSurfaceVariant,
            fontFamily = AppFont,
            lineHeight = 18.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(monetColors.surfaceContainerHigh.copy(alpha = 0.75f))
                .border(
                    0.8.dp,
                    if (isModuleInstalled) Color(0xFF2EAA55).copy(alpha = 0.6f)
                    else monetColors.outlineVariant.copy(alpha = 0.35f),
                    RoundedCornerShape(20.dp)
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isModuleInstalled) Color(0xFF2EAA55).copy(alpha = 0.2f)
                                else monetColors.primary.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isModuleInstalled) Icons.Filled.CheckCircle else Icons.Rounded.Extension,
                            contentDescription = null,
                            tint = if (isModuleInstalled) Color(0xFF2EAA55) else monetColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isModuleInstalled) "Module Installed • Reboot Pending" else "Module Not Installed",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = monetColors.onSurface,
                            fontFamily = AppFont
                        )
                        Text(
                            text = if (isModuleInstalled)
                                "Module is placed in /data/adb/modules/resonix. Reboot your device to activate."
                            else
                                "Generates system/priv-app/Resonix and XML permissions whitelist.",
                            fontSize = 12.sp,
                            color = monetColors.onSurfaceVariant,
                            fontFamily = AppFont
                        )
                    }
                }

                if (!isModuleInstalled) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onInstallModule()
                        },
                        enabled = !isInstalling,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = monetColors.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isInstalling) {
                            CircularProgressIndicator(
                                color = monetColors.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Flashing Module...", fontSize = 13.5.sp, fontFamily = AppFont)
                        } else {
                            Text("Install System Module", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onRebootNow()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EAA55)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("Reboot Device", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSwitchToStandard()
                }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Don't want to install module? Use Standard Mode instead",
                color = monetColors.onSurfaceVariant,
                fontSize = 12.sp,
                fontFamily = AppFont
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun OnboardingPermissionsStep(
    isDefaultDialer: Boolean,
    onRequestDefaultDialer: () -> Unit,
    monetColors: ColorScheme
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = if (isDefaultDialer) Color(0xFF2EAA55).copy(alpha = 0.6f) else monetColors.primary.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(
                    if (isDefaultDialer) Color(0xFF2EAA55) else monetColors.primary
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDefaultDialer) Icons.Filled.CheckCircle else Icons.Rounded.PhoneInTalk,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Text(
            text = if (isDefaultDialer) "Default Phone App Active" else "Set as default phone app",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = monetColors.onSurface,
            fontFamily = AppFont,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (isDefaultDialer)
                "Resonix is set up to handle incoming and outgoing calls, block telemarketers, and screen numbers."
            else
                "To intercept incoming calls, screen telemarketers, and manage phone audio, Android requires Resonix to be your default dialer.",
            fontSize = 13.5.sp,
            color = monetColors.onSurfaceVariant,
            fontFamily = AppFont,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        OnboardingPermissionBenefitRow(
            icon = Icons.Rounded.Security,
            title = "Live Call Screening",
            description = "Intercept and screen incoming callers before your phone rings.",
            monetColors = monetColors
        )

        OnboardingPermissionBenefitRow(
            icon = Icons.Rounded.Block,
            title = "Spam & Telemarketer Shield",
            description = "Silently drop unwanted calls or inject carrier SIT dead-tone audio.",
            monetColors = monetColors
        )

        OnboardingPermissionBenefitRow(
            icon = Icons.Rounded.PhoneInTalk,
            title = "Carrier Calls & Audio Engine",
            description = "Seamless outgoing & incoming carrier calling with native lossless audio.",
            monetColors = monetColors
        )

        OnboardingPermissionBenefitRow(
            icon = Icons.Rounded.Contacts,
            title = "Contacts, Recents & Mic",
            description = "Resonix accesses contacts, logs, and microphone so caller ID, recents, and live voice calls work instantly.",
            monetColors = monetColors
        )

        if (!isDefaultDialer) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(monetColors.primary.copy(alpha = 0.12f))
                    .border(1.dp, monetColors.primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onRequestDefaultDialer()
                    }
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = monetColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Tap below to open the Android system default app dialog and select Resonix.",
                        fontSize = 12.sp,
                        color = monetColors.onSurface,
                        fontFamily = AppFont,
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF2EAA55).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFF2EAA55).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2EAA55),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Verified: Resonix is active as your default phone app.",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = monetColors.onSurface,
                        fontFamily = AppFont
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun OnboardingPermissionBenefitRow(
    icon: ImageVector,
    title: String,
    description: String,
    monetColors: ColorScheme
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(monetColors.surfaceContainer)
            .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2EAA55).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF2EAA55),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = monetColors.onSurface,
                    fontFamily = AppFont
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    color = monetColors.onSurfaceVariant,
                    lineHeight = 15.sp,
                    fontFamily = AppFont
                )
            }
        }
    }
}

@Composable
private fun OnboardingFeatureCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    monetColors: ColorScheme
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(monetColors.surfaceContainer)
            .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = monetColors.onSurface,
                    fontFamily = AppFont
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    color = monetColors.onSurfaceVariant,
                    lineHeight = 15.sp,
                    fontFamily = AppFont
                )
            }
        }
    }
}

@Composable
private fun OnboardingModeOptionCard(
    title: String,
    badge: String,
    badgeColor: Color,
    description: String,
    isSelected: Boolean,
    monetColors: ColorScheme,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) monetColors.primary else monetColors.outlineVariant.copy(alpha = 0.3f),
        animationSpec = tween(180),
        label = "modeBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) monetColors.primaryContainer.copy(alpha = 0.28f)
        else monetColors.surfaceContainer,
        animationSpec = tween(180),
        label = "modeBg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 0.8.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = monetColors.onSurface,
                    fontFamily = AppFont
                )

                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) monetColors.primary else Color.Transparent
                        )
                        .border(
                            1.5.dp,
                            if (isSelected) monetColors.primary else monetColors.outlineVariant,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = monetColors.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor.copy(alpha = 0.18f))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeColor,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = description,
                fontSize = 12.sp,
                color = monetColors.onSurfaceVariant,
                lineHeight = 16.sp,
                fontFamily = AppFont
            )
        }
    }
}

@Composable
private fun OnboardingPrimaryButton(
    text: String,
    monetColors: ColorScheme,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnScale"
    )

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2EAA55),
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFF2EAA55).copy(alpha = 0.5f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AppFont
            )
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
