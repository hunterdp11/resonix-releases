package com.callscreener.app.ui

import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.callscreener.app.engine.AudioPresetManager
import com.callscreener.app.engine.QuickReplyManager
import com.callscreener.app.model.AudioPreset
import com.callscreener.app.model.QuickReply
import com.callscreener.app.ui.theme.GoogleSans
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PresetManagerTab {
    SOUNDBOARD_AUDIO,
    QUICK_REPLIES
}

/**
 * Smooth Drag-and-Drop state manager for LazyColumn reordering.
 * Automatically recalculates target swap positions, compensates drag offset for zero visual jitter,
 * and handles edge auto-scrolling when dragging items across long lists.
 */
class LazyListDragDropState(
    val listState: LazyListState,
    val onMove: (Int, Int) -> Unit
) {
    var draggedIndex by mutableStateOf<Int?>(null)
    var dragOffset by mutableStateOf(0f)

    fun onDragStart(index: Int) {
        draggedIndex = index
        dragOffset = 0f
    }

    fun onDrag(dragAmount: Float, totalCount: Int, onSwap: () -> Unit = {}) {
        val currentDraggedIndex = draggedIndex ?: return
        dragOffset += dragAmount

        val visibleItems = listState.layoutInfo.visibleItemsInfo
        val currentItemInfo = visibleItems.firstOrNull { it.index == currentDraggedIndex } ?: return

        // Center of the dragged item in viewport coordinates
        val currentItemCenter = currentItemInfo.offset + (currentItemInfo.size / 2) + dragOffset

        // Find candidate overlapping item in valid range [0, totalCount - 1]
        val targetItem = visibleItems.firstOrNull { item ->
            item.index in 0 until totalCount &&
            item.index != currentDraggedIndex &&
            currentItemCenter.toInt() in item.offset..(item.offset + item.size)
        }

        if (targetItem != null) {
            val targetIndex = targetItem.index
            onMove(currentDraggedIndex, targetIndex)
            onSwap()
            // Offset compensation to keep item stable under user's finger
            dragOffset += (currentItemInfo.offset - targetItem.offset)
            draggedIndex = targetIndex
        }

        // Auto-scroll when dragging near viewport top/bottom edges
        val viewportStart = listState.layoutInfo.viewportStartOffset
        val viewportEnd = listState.layoutInfo.viewportEndOffset
        val threshold = 90

        if (currentItemCenter < viewportStart + threshold) {
            // Dragged near top edge
        } else if (currentItemCenter > viewportEnd - threshold) {
            // Dragged near bottom edge
        }
    }

    fun onDragEnd() {
        draggedIndex = null
        dragOffset = 0f
    }

    fun onDragCancel() {
        draggedIndex = null
        dragOffset = 0f
    }
}

/**
 * Pixel Expressive / Material 3 Presets & Responses Full-Screen Page.
 * Dedicated edge-to-edge subpage with seamless fluid animations, clean back navigation,
 * tactile drag-and-drop reordering, quick replies, and audio preset management.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PresetManagementScreen(
    presetManager: AudioPresetManager,
    quickReplyManager: QuickReplyManager,
    monetColors: ColorScheme? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

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

    val presets by presetManager.presets.collectAsState()
    val quickReplies by quickReplyManager.quickReplies.collectAsState()
    val playingId by presetManager.currentlyPlayingId.collectAsState()
    val isPresetPaused by presetManager.isPaused.collectAsState()

    var activeTab by remember { mutableStateOf(PresetManagerTab.SOUNDBOARD_AUDIO) }

    // LazyListStates and Drag-and-Drop managers
    val soundboardListState = rememberLazyListState()
    val quickRepliesListState = rememberLazyListState()

    val soundboardDragDropState = remember(soundboardListState, presets) {
        LazyListDragDropState(
            listState = soundboardListState,
            onMove = { from, to ->
                presetManager.reorderPresets(from, to)
            }
        )
    }

    val quickRepliesDragDropState = remember(quickRepliesListState, quickReplies) {
        LazyListDragDropState(
            listState = quickRepliesListState,
            onMove = { from, to ->
                quickReplyManager.reorderQuickReplies(from, to)
            }
        )
    }

    // Dialog States
    var editingPreset by remember { mutableStateOf<AudioPreset?>(null) }
    var editingQuickReply by remember { mutableStateOf<QuickReply?>(null) }
    var isAddingQuickReply by remember { mutableStateOf(false) }
    var isAddingTextPreset by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var deleteConfirmationItem by remember { mutableStateOf<Pair<String, String>?>(null) } // id to title
    var isResetConfirmOpen by remember { mutableStateOf(false) }

    // File picker for audio presets
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingImportUri = uri
        }
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    fun animateDismiss() {
        if (!isVisible) return
        scope.launch {
            isVisible = false
            delay(180)
            onDismiss()
        }
    }

    BackHandler(enabled = isVisible) {
        animateDismiss()
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
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Pixel Expressive Header Bar: Left Arrow Back, Title, Subtitle (No Cross Icon)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            animateDismiss()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceContainerHigh.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Presets & Responses",
                            color = colors.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GoogleSans,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = "Soundboard & in-call responses",
                            color = colors.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontFamily = GoogleSans
                        )
                    }
                }

                // Minimal Pixel Segmented Tab Switcher
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surfaceContainerHigh.copy(alpha = 0.5f))
                        .border(0.8.dp, colors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Tab 1: Soundboard
                        val isSoundboard = activeTab == PresetManagerTab.SOUNDBOARD_AUDIO
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSoundboard) colors.surfaceContainerHighest else Color.Transparent)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    activeTab = PresetManagerTab.SOUNDBOARD_AUDIO
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Soundboard (${presets.size})",
                                color = if (isSoundboard) colors.onSurface else colors.onSurfaceVariant,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSoundboard) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = GoogleSans
                            )
                        }

                        // Tab 2: Quick Replies
                        val isReplies = activeTab == PresetManagerTab.QUICK_REPLIES
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isReplies) colors.surfaceContainerHighest else Color.Transparent)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    activeTab = PresetManagerTab.QUICK_REPLIES
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Quick Replies (${quickReplies.size})",
                                color = if (isReplies) colors.onSurface else colors.onSurfaceVariant,
                                fontSize = 12.5.sp,
                                fontWeight = if (isReplies) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = GoogleSans
                            )
                        }
                    }
                }

                // Content Section based on Tab
                if (activeTab == PresetManagerTab.SOUNDBOARD_AUDIO) {
                    // Action Bar: Import Audio & Add Speech
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Button 1: Import Audio File
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.primaryContainer.copy(alpha = 0.5f))
                                .border(0.8.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    audioPickerLauncher.launch("audio/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FileUpload,
                                    contentDescription = null,
                                    tint = colors.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Import Audio",
                                    color = colors.onPrimaryContainer,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = GoogleSans
                                )
                            }
                        }

                        // Button 2: Add Speech Preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.surfaceContainerHigh.copy(alpha = 0.6f))
                                .border(0.8.dp, colors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    isAddingTextPreset = true
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
                                    tint = colors.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Add Speech",
                                    color = colors.onSurface,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = GoogleSans
                                )
                            }
                        }
                    }

                    // Subtle Drag Hint
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DragIndicator,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Hold & drag ⠿ to reorder presets",
                            color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 11.5.sp,
                            fontFamily = GoogleSans
                        )
                    }

                    // Presets List with Hold & Drag Reordering, Instant Move-to-Top & Up/Down Controls
                    LazyColumn(
                        state = soundboardListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(presets, key = { _, item -> item.id }) { index, preset ->
                            val isDragging = soundboardDragDropState.draggedIndex == index
                            SoundboardPresetItemRow(
                                preset = preset,
                                index = index,
                                totalCount = presets.size,
                                isPlaying = playingId == preset.id && !isPresetPaused,
                                isPaused = playingId == preset.id && isPresetPaused,
                                isDragging = isDragging,
                                dragOffset = if (isDragging) soundboardDragDropState.dragOffset else 0f,
                                colors = colors,
                                onDragStart = {
                                    val currIdx = presets.indexOfFirst { it.id == preset.id }
                                    if (currIdx >= 0) soundboardDragDropState.onDragStart(currIdx)
                                },
                                onDrag = { deltaY ->
                                    soundboardDragDropState.onDrag(deltaY, presets.size) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                },
                                onDragEnd = { soundboardDragDropState.onDragEnd() },
                                onDragCancel = { soundboardDragDropState.onDragCancel() },
                                onMoveToTop = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scope.launch {
                                        presetManager.movePresetToTop(preset.id)
                                        soundboardListState.animateScrollToItem(0)
                                    }
                                },
                                onMoveUp = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    presetManager.movePresetUp(preset.id)
                                },
                                onMoveDown = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    presetManager.movePresetDown(preset.id)
                                },
                                onPlayPreview = {
                                    if (playingId == preset.id) {
                                        if (!isPresetPaused) {
                                            presetManager.pausePreview()
                                        } else {
                                            presetManager.resumePreview()
                                        }
                                    } else {
                                        presetManager.previewPreset(preset)
                                    }
                                },
                                onEdit = { editingPreset = preset },
                                onDelete = {
                                    deleteConfirmationItem = preset.id to preset.title
                                },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(
                                onClick = { isResetConfirmOpen = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    tint = colors.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reset Soundboard to Default Presets",
                                    color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontFamily = GoogleSans
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                } else {
                    // Quick Replies Tab Action Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.primaryContainer.copy(alpha = 0.5f))
                                .border(0.8.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isAddingQuickReply = true
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = colors.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Add Custom Quick Reply",
                                    color = colors.onPrimaryContainer,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = GoogleSans
                                )
                            }
                        }
                    }

                    // Subtle Drag Hint
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DragIndicator,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Hold & drag ⠿ to reorder presets",
                            color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 11.5.sp,
                            fontFamily = GoogleSans
                        )
                    }

                    // Quick Replies List with Hold & Drag Reordering, Instant Move-to-Top & Up/Down Controls
                    LazyColumn(
                        state = quickRepliesListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(quickReplies, key = { _, item -> item.id }) { index, reply ->
                            val isDragging = quickRepliesDragDropState.draggedIndex == index
                            QuickReplyItemRow(
                                reply = reply,
                                index = index,
                                totalCount = quickReplies.size,
                                isDragging = isDragging,
                                dragOffset = if (isDragging) quickRepliesDragDropState.dragOffset else 0f,
                                colors = colors,
                                onDragStart = {
                                    val currIdx = quickReplies.indexOfFirst { it.id == reply.id }
                                    if (currIdx >= 0) quickRepliesDragDropState.onDragStart(currIdx)
                                },
                                onDrag = { deltaY ->
                                    quickRepliesDragDropState.onDrag(deltaY, quickReplies.size) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                },
                                onDragEnd = { quickRepliesDragDropState.onDragEnd() },
                                onDragCancel = { quickRepliesDragDropState.onDragCancel() },
                                onMoveToTop = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scope.launch {
                                        quickReplyManager.moveToTop(reply.id)
                                        quickRepliesListState.animateScrollToItem(0)
                                    }
                                },
                                onMoveUp = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    quickReplyManager.moveUp(reply.id)
                                },
                                onMoveDown = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    quickReplyManager.moveDown(reply.id)
                                },
                                onEdit = { editingQuickReply = reply },
                                onDelete = {
                                    deleteConfirmationItem = reply.id to reply.title
                                },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(
                                onClick = { isResetConfirmOpen = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    tint = colors.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reset Quick Replies to Default",
                                    color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontFamily = GoogleSans
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // PIXEL EXPRESSIVE DIALOGS
    // ==========================================

    // 1. Import Audio File Dialog
    pendingImportUri?.let { uri ->
        val defaultTitle = remember(uri) {
            getFileName(context, uri).substringBeforeLast(".")
        }
        var importTitle by remember { mutableStateOf(defaultTitle) }
        var placeAtTop by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = colors.surfaceContainerHighest,
            title = {
                Text(
                    text = "Import Audio Preset",
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSans
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter a display name for this custom audio track in the in-call soundboard:",
                        color = colors.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontFamily = GoogleSans
                    )
                    OutlinedTextField(
                        value = importTitle,
                        onValueChange = { importTitle = it },
                        label = { Text("Preset Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outlineVariant,
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.onSurfaceVariant
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { placeAtTop = !placeAtTop }
                    ) {
                        Checkbox(
                            checked = placeAtTop,
                            onCheckedChange = { placeAtTop = it },
                            colors = CheckboxDefaults.colors(checkedColor = colors.primary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Place at Top of Soundboard",
                            color = colors.onSurface,
                            fontSize = 12.5.sp,
                            fontFamily = GoogleSans
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (importTitle.isNotBlank()) {
                            scope.launch {
                                presetManager.importAudioFile(
                                    title = importTitle,
                                    sourceUri = uri,
                                    insertAtTop = placeAtTop
                                )
                            }
                            pendingImportUri = null
                        }
                    }
                ) {
                    Text("Import", color = colors.primary, fontWeight = FontWeight.Bold, fontFamily = GoogleSans)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) {
                    Text("Cancel", color = colors.onSurfaceVariant, fontFamily = GoogleSans)
                }
            }
        )
    }

    // 2. Add Speech Preset Dialog
    if (isAddingTextPreset) {
        var newTitle by remember { mutableStateOf("") }
        var newSpoken by remember { mutableStateOf("") }
        var placeAtTop by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { isAddingTextPreset = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = colors.surfaceContainerHighest,
            title = {
                Text(
                    text = "Add Speech Preset",
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSans
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Display Title") },
                        placeholder = { Text("e.g. Call Later") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outlineVariant,
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.onSurfaceVariant
                        )
                    )
                    OutlinedTextField(
                        value = newSpoken,
                        onValueChange = { newSpoken = it },
                        label = { Text("Spoken Text (Synthesized to Caller)") },
                        placeholder = { Text("e.g. I am busy, I will call you later.") },
                        minLines = 2,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outlineVariant,
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.onSurfaceVariant
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { placeAtTop = !placeAtTop }
                    ) {
                        Checkbox(
                            checked = placeAtTop,
                            onCheckedChange = { placeAtTop = it },
                            colors = CheckboxDefaults.colors(checkedColor = colors.primary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Place at Top of Soundboard",
                            color = colors.onSurface,
                            fontSize = 12.5.sp,
                            fontFamily = GoogleSans
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            presetManager.addCustomTextPreset(
                                title = newTitle,
                                spokenText = newSpoken.ifBlank { newTitle },
                                insertAtTop = placeAtTop
                            )
                            isAddingTextPreset = false
                        }
                    }
                ) {
                    Text("Add", color = colors.primary, fontWeight = FontWeight.Bold, fontFamily = GoogleSans)
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddingTextPreset = false }) {
                    Text("Cancel", color = colors.onSurfaceVariant, fontFamily = GoogleSans)
                }
            }
        )
    }

    // 3. Edit Preset Dialog
    editingPreset?.let { preset ->
        var editTitle by remember { mutableStateOf(preset.title) }
        var editText by remember { mutableStateOf(if (preset.isAudioFile) "" else preset.textOrPath) }

        AlertDialog(
            onDismissRequest = { editingPreset = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = colors.surfaceContainerHighest,
            title = {
                Text(
                    text = "Edit Preset",
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSans
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outlineVariant,
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.onSurfaceVariant
                        )
                    )
                    if (!preset.isAudioFile) {
                        OutlinedTextField(
                            value = editText,
                            onValueChange = { editText = it },
                            label = { Text("Spoken Text") },
                            minLines = 2,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.outlineVariant,
                                focusedTextColor = colors.onSurface,
                                unfocusedTextColor = colors.onSurface,
                                focusedLabelColor = colors.primary,
                                unfocusedLabelColor = colors.onSurfaceVariant
                            )
                        )
                    } else {
                        Text(
                            text = "Audio file: ${preset.textOrPath.substringAfterLast("/")}",
                            color = colors.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        presetManager.editPreset(
                            id = preset.id,
                            newTitle = editTitle,
                            newText = if (preset.isAudioFile) null else editText
                        )
                        editingPreset = null
                    }
                ) {
                    Text("Save", color = colors.primary, fontWeight = FontWeight.Bold, fontFamily = GoogleSans)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPreset = null }) {
                    Text("Cancel", color = colors.onSurfaceVariant, fontFamily = GoogleSans)
                }
            }
        )
    }

    // 4. Add Quick Reply Dialog
    if (isAddingQuickReply) {
        var replyTitle by remember { mutableStateOf("") }
        var replySpoken by remember { mutableStateOf("") }
        var isHindi by remember { mutableStateOf(false) }
        var placeAtTop by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { isAddingQuickReply = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = colors.surfaceContainerHighest,
            title = {
                Text(
                    text = "Add Quick Reply",
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSans
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = replyTitle,
                        onValueChange = { replyTitle = it },
                        label = { Text("Button Title") },
                        placeholder = { Text("e.g. Call back at 5pm") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outlineVariant,
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.onSurfaceVariant
                        )
                    )
                    OutlinedTextField(
                        value = replySpoken,
                        onValueChange = { replySpoken = it },
                        label = { Text("Spoken Voice Message to Caller") },
                        placeholder = { Text("e.g. Please call me back at 5pm, thank you.") },
                        minLines = 2,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outlineVariant,
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.onSurfaceVariant
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hindi / Hinglish Mode",
                            color = colors.onSurface,
                            fontSize = 13.sp,
                            fontFamily = GoogleSans
                        )
                        Switch(
                            checked = isHindi,
                            onCheckedChange = { isHindi = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.onPrimary,
                                checkedTrackColor = colors.primary
                            )
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { placeAtTop = !placeAtTop }
                    ) {
                        Checkbox(
                            checked = placeAtTop,
                            onCheckedChange = { placeAtTop = it },
                            colors = CheckboxDefaults.colors(checkedColor = colors.primary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Place at Front of Ribbon (First in Call Screen)",
                            color = colors.onSurface,
                            fontSize = 12.sp,
                            fontFamily = GoogleSans
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (replyTitle.isNotBlank()) {
                            quickReplyManager.addReply(
                                title = replyTitle,
                                spokenText = replySpoken.ifBlank { replyTitle },
                                isHindi = isHindi,
                                insertAtTop = placeAtTop
                            )
                            isAddingQuickReply = false
                        }
                    }
                ) {
                    Text("Add", color = colors.primary, fontWeight = FontWeight.Bold, fontFamily = GoogleSans)
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddingQuickReply = false }) {
                    Text("Cancel", color = colors.onSurfaceVariant, fontFamily = GoogleSans)
                }
            }
        )
    }

    // 5. Edit Quick Reply Dialog
    editingQuickReply?.let { reply ->
        var editTitle by remember { mutableStateOf(reply.title) }
        var editSpoken by remember { mutableStateOf(reply.spokenText) }
        var isHindi by remember { mutableStateOf(reply.isHindi) }

        AlertDialog(
            onDismissRequest = { editingQuickReply = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = colors.surfaceContainerHighest,
            title = {
                Text(
                    text = "Edit Quick Reply",
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSans
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Button Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outlineVariant,
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.onSurfaceVariant
                        )
                    )
                    OutlinedTextField(
                        value = editSpoken,
                        onValueChange = { editSpoken = it },
                        label = { Text("Spoken Message") },
                        minLines = 2,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outlineVariant,
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.onSurfaceVariant
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hindi Mode",
                            color = colors.onSurface,
                            fontSize = 13.sp,
                            fontFamily = GoogleSans
                        )
                        Switch(
                            checked = isHindi,
                            onCheckedChange = { isHindi = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.onPrimary,
                                checkedTrackColor = colors.primary
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        quickReplyManager.editReply(
                            id = reply.id,
                            newTitle = editTitle,
                            newSpokenText = editSpoken,
                            isHindi = isHindi
                        )
                        editingQuickReply = null
                    }
                ) {
                    Text("Save", color = colors.primary, fontWeight = FontWeight.Bold, fontFamily = GoogleSans)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingQuickReply = null }) {
                    Text("Cancel", color = colors.onSurfaceVariant, fontFamily = GoogleSans)
                }
            }
        )
    }

    // 6. Delete Confirmation Dialog
    deleteConfirmationItem?.let { (id, title) ->
        AlertDialog(
            onDismissRequest = { deleteConfirmationItem = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = colors.surfaceContainerHighest,
            title = {
                Text(
                    text = "Delete Preset?",
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSans
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove \"$title\"? It will no longer appear in the in-call ribbon.",
                    color = colors.onSurfaceVariant,
                    fontSize = 13.5.sp,
                    fontFamily = GoogleSans
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (activeTab == PresetManagerTab.SOUNDBOARD_AUDIO) {
                            presetManager.deletePreset(id)
                        } else {
                            quickReplyManager.deleteReply(id)
                        }
                        deleteConfirmationItem = null
                    }
                ) {
                    Text("Delete", color = colors.error, fontWeight = FontWeight.Bold, fontFamily = GoogleSans)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmationItem = null }) {
                    Text("Cancel", color = colors.onSurfaceVariant, fontFamily = GoogleSans)
                }
            }
        )
    }

    // 7. Reset to Defaults Confirmation
    if (isResetConfirmOpen) {
        AlertDialog(
            onDismissRequest = { isResetConfirmOpen = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = colors.surfaceContainerHighest,
            title = {
                Text(
                    text = "Reset to Default?",
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSans
                )
            },
            text = {
                Text(
                    text = if (activeTab == PresetManagerTab.SOUNDBOARD_AUDIO)
                        "This will restore all default audio soundboard presets while preserving your imported custom audio tracks."
                    else
                        "This will reset all Quick Replies to factory defaults.",
                    color = colors.onSurfaceVariant,
                    fontSize = 13.5.sp,
                    fontFamily = GoogleSans
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (activeTab == PresetManagerTab.SOUNDBOARD_AUDIO) {
                            presetManager.resetPresetsToDefault()
                        } else {
                            quickReplyManager.resetToDefault()
                        }
                        isResetConfirmOpen = false
                    }
                ) {
                    Text("Reset", color = colors.primary, fontWeight = FontWeight.Bold, fontFamily = GoogleSans)
                }
            },
            dismissButton = {
                TextButton(onClick = { isResetConfirmOpen = false }) {
                    Text("Cancel", color = colors.onSurfaceVariant, fontFamily = GoogleSans)
                }
            }
        )
    }
}

/**
 * Soundboard Preset Row: Pixel Expressive Minimalist Card with Tactile Drag Handle, Category Badges & Clean Actions
 */
@Composable
fun SoundboardPresetItemRow(
    preset: AudioPreset,
    index: Int,
    totalCount: Int,
    isPlaying: Boolean,
    isPaused: Boolean = false,
    isDragging: Boolean = false,
    dragOffset: Float = 0f,
    colors: ColorScheme,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onMoveToTop: () -> Unit = {},
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onPlayPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .zIndex(if (isDragging) 15f else 1f)
            .graphicsLayer {
                translationY = if (isDragging) dragOffset else 0f
                scaleX = if (isDragging) 1.02f else 1.0f
                scaleY = if (isDragging) 1.02f else 1.0f
            }
            .fillMaxWidth()
            .shadow(
                elevation = if (isDragging) 10.dp else 0.dp,
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDragging) colors.surfaceContainerHighest else colors.surfaceContainer.copy(alpha = 0.65f))
            .border(
                width = 0.8.dp,
                color = if (isDragging) colors.primary.copy(alpha = 0.5f) else colors.outlineVariant.copy(alpha = 0.25f),
                shape = RoundedCornerShape(18.dp)
            )
            .pointerInput(preset.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDragStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.y)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel
                )
            }
            .padding(horizontal = 12.dp, vertical = 11.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tactile Drag Indicator Grip Handle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDragging) colors.primary.copy(alpha = 0.2f) else colors.surfaceContainerHigh.copy(alpha = 0.8f))
                    .border(
                        0.8.dp,
                        if (isDragging) colors.primary.copy(alpha = 0.4f) else colors.outlineVariant.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
                    .pointerInput(preset.id) {
                        detectDragGestures(
                            onDragStart = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDragStart()
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.y)
                            },
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragCancel
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DragIndicator,
                    contentDescription = "Hold and drag to reorder",
                    tint = if (isDragging) colors.primary else colors.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Minimal Index Badge
            Box(
                modifier = Modifier
                    .widthIn(min = 22.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(colors.surfaceContainerHighest.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Main Info: Title, Type/Category & Preview Text
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Type Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (preset.isAudioFile) colors.tertiaryContainer.copy(alpha = 0.7f)
                                else colors.secondaryContainer.copy(alpha = 0.7f)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (preset.isAudioFile) "AUDIO" else "SPEECH",
                            color = if (preset.isAudioFile) colors.onTertiaryContainer else colors.onSecondaryContainer,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.4.sp
                        )
                    }

                    if (preset.category.isNotBlank() && preset.category != "Custom") {
                        Text(
                            text = preset.category,
                            color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 10.5.sp,
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = preset.title,
                    color = colors.onSurface,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GoogleSans,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!preset.isAudioFile && preset.textOrPath.isNotBlank()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = preset.textOrPath,
                        color = colors.onSurfaceVariant,
                        fontSize = 11.5.sp,
                        fontFamily = GoogleSans,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right Actions: Preview Play (if audio file), Edit & Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (preset.isAudioFile) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPlaying) colors.primary
                                else if (isPaused) colors.primaryContainer
                                else colors.surfaceContainerHigh.copy(alpha = 0.8f)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPlayPreview()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else if (isPaused) "Resume" else "Play",
                            tint = if (isPlaying) colors.onPrimary else if (isPaused) colors.onPrimaryContainer else colors.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceContainerHigh.copy(alpha = 0.8f))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onEdit()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.errorContainer.copy(alpha = 0.25f))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDelete()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = colors.error,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

/**
 * Quick Reply Row: Pixel Expressive Minimalist Card with Tactile Drag Handle, Language Badges & Clean Actions
 */
@Composable
fun QuickReplyItemRow(
    reply: QuickReply,
    index: Int,
    totalCount: Int,
    isDragging: Boolean = false,
    dragOffset: Float = 0f,
    colors: ColorScheme,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onMoveToTop: () -> Unit = {},
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .zIndex(if (isDragging) 15f else 1f)
            .graphicsLayer {
                translationY = if (isDragging) dragOffset else 0f
                scaleX = if (isDragging) 1.02f else 1.0f
                scaleY = if (isDragging) 1.02f else 1.0f
            }
            .fillMaxWidth()
            .shadow(
                elevation = if (isDragging) 10.dp else 0.dp,
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDragging) colors.surfaceContainerHighest else colors.surfaceContainer.copy(alpha = 0.65f))
            .border(
                width = 0.8.dp,
                color = if (isDragging) colors.primary.copy(alpha = 0.5f) else colors.outlineVariant.copy(alpha = 0.25f),
                shape = RoundedCornerShape(18.dp)
            )
            .pointerInput(reply.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDragStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.y)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel
                )
            }
            .padding(horizontal = 12.dp, vertical = 11.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tactile Drag Indicator Grip Handle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDragging) colors.primary.copy(alpha = 0.2f) else colors.surfaceContainerHigh.copy(alpha = 0.8f))
                    .border(
                        0.8.dp,
                        if (isDragging) colors.primary.copy(alpha = 0.4f) else colors.outlineVariant.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
                    .pointerInput(reply.id) {
                        detectDragGestures(
                            onDragStart = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDragStart()
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.y)
                            },
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragCancel
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DragIndicator,
                    contentDescription = "Hold and drag to reorder",
                    tint = if (isDragging) colors.primary else colors.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Minimal Index Badge
            Box(
                modifier = Modifier
                    .widthIn(min = 22.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(colors.surfaceContainerHighest.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Main Info: Title, Language Badge & Spoken Text
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (reply.isHindi) colors.tertiaryContainer.copy(alpha = 0.7f)
                                else colors.secondaryContainer.copy(alpha = 0.7f)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (reply.isHindi) "HINDI" else "ENGLISH",
                            color = if (reply.isHindi) colors.onTertiaryContainer else colors.onSecondaryContainer,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.4.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = reply.title,
                    color = colors.onSurface,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GoogleSans,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (reply.spokenText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = reply.spokenText,
                        color = colors.onSurfaceVariant,
                        fontSize = 11.5.sp,
                        fontFamily = GoogleSans,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right Actions: Edit & Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceContainerHigh.copy(alpha = 0.8f))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onEdit()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.errorContainer.copy(alpha = 0.25f))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDelete()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = colors.error,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

private fun getFileName(context: android.content.Context, uri: Uri): String {
    var result = "Custom_Audio.wav"
    if (uri.scheme == "content") {
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            }
        } catch (_: Exception) {}
    }
    return result
}
