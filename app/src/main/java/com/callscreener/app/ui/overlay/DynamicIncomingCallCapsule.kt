package com.callscreener.app.ui.overlay

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callscreener.app.data.CallerProfileManager
import com.callscreener.app.data.DialerRepository
import com.callscreener.app.data.rememberContactPhotoBitmap
import com.callscreener.app.ui.theme.rememberPixelMonetColorScheme
import com.callscreener.app.ui.theme.AppFont
import kotlin.math.roundToInt

@Composable
fun DynamicIncomingCallCapsule(
    callerNumber: String,
    callerName: String?,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onScreen: () -> Unit,
    onExpand: () -> Unit,
    onSilenceOrDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val monetColors = rememberPixelMonetColorScheme()

    var isVisible by remember { mutableStateOf(false) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val cleanNumber = remember(callerNumber) { callerNumber.trim() }
    val resolvedName = remember(callerNumber, callerName) {
        if (!callerName.isNullOrBlank() && callerName != "Unknown Caller") {
            callerName
        } else {
            val saved = DialerRepository.lookupContactName(context, cleanNumber)
            if (!saved.isNullOrBlank()) saved else null
        }
    }

    val displayName = resolvedName ?: DialerRepository.formatPhoneNumber(cleanNumber)
    val subtitleText = if (resolvedName != null) {
        DialerRepository.formatPhoneNumber(cleanNumber)
    } else {
        "Incoming Call"
    }

    val customPhotoPath = remember(cleanNumber) {
        CallerProfileManager.getCustomPhotoPath(context, cleanNumber)
    }
    val contactBitmap = rememberContactPhotoBitmap(customPhotoPath)

    val infiniteTransition = rememberInfiniteTransition(label = "capsulePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val assistantGlintPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "assistantGlintPhase"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(animationSpec = tween(220)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(220)
        ) + fadeOut(animationSpec = tween(180))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(x = 0, y = dragOffsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (dragOffsetY < -35f) {
                                isVisible = false
                                onSilenceOrDismiss()
                            } else if (dragOffsetY > 45f) {
                                isVisible = false
                                onExpand()
                            } else {
                                dragOffsetY = 0f
                            }
                        },
                        onDragCancel = {
                            dragOffsetY = 0f
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetY = (dragOffsetY + dragAmount).coerceIn(-120f, 80f)
                        }
                    )
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onExpand()
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = monetColors.primary.copy(alpha = 0.35f),
                        ambientColor = Color(0x60000000)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                monetColors.surfaceContainerHighest.copy(alpha = 0.96f),
                                monetColors.surfaceContainerHigh.copy(alpha = 0.94f)
                            )
                        )
                    )
                    .border(
                        BorderStroke(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.45f),
                                    monetColors.primary.copy(alpha = 0.50f),
                                    Color.White.copy(alpha = 0.15f)
                                )
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .width(36.dp)
                        .height(3.5.dp)
                        .clip(CircleShape)
                        .background(monetColors.onSurfaceVariant.copy(alpha = 0.35f))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .scale(pulseScale)
                                .border(
                                    1.5.dp,
                                    monetColors.primary.copy(alpha = pulseAlpha),
                                    CircleShape
                                )
                        )

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(monetColors.primaryContainer)
                                .border(1.dp, Color.White.copy(alpha = 0.30f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (contactBitmap != null) {
                                Image(
                                    bitmap = contactBitmap,
                                    contentDescription = "Contact photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                val initial = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "#"
                                Text(
                                    text = initial,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AppFont,
                                    color = monetColors.onPrimaryContainer
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                                .border(1.5.dp, monetColors.surfaceContainerHighest, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 2.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = AppFont,
                            color = monetColors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            letterSpacing = 0.15.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = subtitleText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = AppFont,
                                color = monetColors.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(monetColors.primaryContainer.copy(alpha = 0.65f))
                                    .border(0.5.dp, monetColors.primary.copy(alpha = 0.40f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "HD",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AppFont,
                                    color = monetColors.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CapsuleLiquidActionButton(
                            icon = Icons.Default.CallEnd,
                            contentDescription = "Decline",
                            gradientColors = listOf(
                                Color(0xFFFF334B),
                                Color(0xFFD00826)
                            ),
                            tiltOnPress = -12f,
                            size = 38.dp,
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                isVisible = false
                                onDecline()
                            }
                        )

                        CapsuleAssistantButton(
                            monetColors = monetColors,
                            glintPhase = assistantGlintPhase,
                            size = 38.dp,
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                isVisible = false
                                onScreen()
                            }
                        )

                        CapsuleLiquidActionButton(
                            icon = Icons.Default.Call,
                            contentDescription = "Answer",
                            gradientColors = listOf(
                                Color(0xFF00E676),
                                Color(0xFF009624)
                            ),
                            tiltOnPress = 10f,
                            size = 38.dp,
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                isVisible = false
                                onAnswer()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CapsuleLiquidActionButton(
    icon: ImageVector,
    contentDescription: String,
    gradientColors: List<Color>,
    tiltOnPress: Float,
    size: Dp,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleX by animateFloatAsState(
        targetValue = if (isPressed) 1.06f else 1.0f,
        animationSpec = spring(dampingRatio = 0.54f, stiffness = Spring.StiffnessMediumLow),
        label = "capsuleBtnScaleX"
    )
    val scaleY by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1.0f,
        animationSpec = spring(dampingRatio = 0.54f, stiffness = Spring.StiffnessMediumLow),
        label = "capsuleBtnScaleY"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 12.dp else 19.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "capsuleBtnCorner"
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (isPressed) tiltOnPress else 0f,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMediumLow),
        label = "capsuleBtnIconRot"
    )

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                this.scaleX = scaleX
                this.scaleY = scaleY
            }
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = gradientColors.first().copy(alpha = 0.55f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(Brush.verticalGradient(gradientColors))
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = if (isPressed) 0.65f else 0.45f),
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                RoundedCornerShape(cornerRadius)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.30f),
                            Color.Transparent
                        )
                    )
                )
        )

        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier
                .size(size * 0.52f)
                .graphicsLayer {
                    rotationZ = iconRotation
                }
        )
    }
}

@Composable
private fun CapsuleAssistantButton(
    monetColors: ColorScheme,
    glintPhase: Float,
    size: Dp,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleX by animateFloatAsState(
        targetValue = if (isPressed) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.54f, stiffness = Spring.StiffnessMediumLow),
        label = "asstScaleX"
    )
    val scaleY by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.54f, stiffness = Spring.StiffnessMediumLow),
        label = "asstScaleY"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 12.dp else 19.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "asstCorner"
    )

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                this.scaleX = scaleX
                this.scaleY = scaleY
            }
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = Color(0xFF4285F4).copy(alpha = 0.50f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF4285F4),
                        monetColors.primary,
                        Color(0xFF34A853)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = if (isPressed) 0.65f else 0.45f),
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                RoundedCornerShape(cornerRadius)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val glintW = this.size.width * 0.45f
            val currentX = (this.size.width + glintW * 2) * glintPhase - glintW
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    startX = currentX - glintW * 0.5f,
                    endX = currentX + glintW * 0.5f
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.30f),
                            Color.Transparent
                        )
                    )
                )
        )

        Icon(
            imageVector = Icons.Rounded.RecordVoiceOver,
            contentDescription = "Screen Call with AI",
            tint = Color.White,
            modifier = Modifier.size(size * 0.50f)
        )
    }
}
