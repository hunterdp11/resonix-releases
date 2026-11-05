package com.callscreener.app.ui

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowUp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.callscreener.app.engine.AudioPresetManager
import com.callscreener.app.engine.QuickReplyManager
import com.callscreener.app.model.AudioPreset
import com.callscreener.app.model.QuickReply
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
        val topThreshold = viewportStart + 130f
        val bottomThreshold = viewportEnd - 130f

        val itemTop = currentItemInfo.offset + dragOffset
        val itemBottom = itemTop + currentItemInfo.size

        if (itemTop < topThreshold) {
            val scrollDelta = ((itemTop - topThreshold) * 0.22f).coerceIn(-30f, -4f)
            val consumed = listState.dispatchRawDelta(scrollDelta)
            dragOffset -= consumed
        } else if (itemBottom > bottomThreshold) {
            val scrollDelta = ((itemBottom - bottomThreshold) * 0.22f).coerceIn(4f, 30f)
            val consumed = listState.dispatchRawDelta(scrollDelta)
            dragOffset -= consumed
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
 * Dedicated Full-Screen Frosted Glass Management Page:
 * Allows user to Reorder / Align, Edit, Delete, Import Audio, and Add Custom Quick Replies.
 * Changes immediately reflect in both the soundboard and in-call screening ribbons!
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PresetManagementScreen(
    presetManager: AudioPresetManager,
    quickReplyManager: QuickReplyManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val presets by presetManager.presets.collectAsState()
    val quickReplies by quickReplyManager.quickReplies.collectAsState()
    val playingId by presetManager.currentlyPlayingId.collectAsState()

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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090B10)),
            color = Color(0xFF090B10)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Ambient Glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF141926),
                                    Color(0xFF0B0E14),
                                    Color(0xFF06080C)
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x18FFFFFF))
                                    .border(0.8.dp, Color(0x30FFFFFF), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Presets & Responses",
                                    color = Color.White,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.4).sp
                                )
                                Text(
                                    text = "Align, reorder, edit and manage in-call speech",
                                    color = Color(0x99FFFFFF),
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x18FFFFFF))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Frosted Segmented Tab Switcher
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x18FFFFFF))
                            .border(0.8.dp, Color(0x25FFFFFF), RoundedCornerShape(16.dp))
                            .padding(3.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Tab 1: Soundboard
                            val isSoundboard = activeTab == PresetManagerTab.SOUNDBOARD_AUDIO
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(if (isSoundboard) Color(0x35FFFFFF) else Color.Transparent)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        activeTab = PresetManagerTab.SOUNDBOARD_AUDIO
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Soundboard (${presets.size})",
                                    color = if (isSoundboard) Color.White else Color(0x80FFFFFF),
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSoundboard) FontWeight.Bold else FontWeight.Medium
                                )
                            }

                            // Tab 2: Quick Replies
                            val isReplies = activeTab == PresetManagerTab.QUICK_REPLIES
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(if (isReplies) Color(0x35FFFFFF) else Color.Transparent)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        activeTab = PresetManagerTab.QUICK_REPLIES
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Quick Replies (${quickReplies.size})",
                                    color = if (isReplies) Color.White else Color(0x80FFFFFF),
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isReplies) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Content Section based on Tab
                    if (activeTab == PresetManagerTab.SOUNDBOARD_AUDIO) {
                        // Action Bar: Import Audio & Add Text
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Button 1: Import Audio File
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF2855A4).copy(alpha = 0.35f))
                                    .border(1.dp, Color(0xFF3872E0).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        audioPickerLauncher.launch("audio/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF64B5F6),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Import Audio (WAV/MP3)",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Button 2: Add Speech Preset
                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x18FFFFFF))
                                    .border(0.8.dp, Color(0x30FFFFFF), RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isAddingTextPreset = true
                                    }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+ Add Speech",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Reorder & Arrangement Tip Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DragIndicator,
                                contentDescription = null,
                                tint = Color(0xFF64B5F6),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Hold & drag ⠿ or tap ⏫ to jump any item straight to the top",
                                color = Color(0x99FFFFFF),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        // Presets List with Hold & Drag Reordering, Instant Move-to-Top & Up/Down Controls
                        LazyColumn(
                            state = soundboardListState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(presets, key = { _, item -> item.id }) { index, preset ->
                                val isDragging = soundboardDragDropState.draggedIndex == index
                                SoundboardPresetItemRow(
                                    preset = preset,
                                    index = index,
                                    totalCount = presets.size,
                                    isPlaying = playingId == preset.id,
                                    isDragging = isDragging,
                                    dragOffset = if (isDragging) soundboardDragDropState.dragOffset else 0f,
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
                                            presetManager.stopPreview()
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
                                        tint = Color(0x80FFFFFF),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Reset Soundboard to Default Presets",
                                        color = Color(0x80FFFFFF),
                                        fontSize = 12.sp
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
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF2E7D32).copy(alpha = 0.35f))
                                    .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isAddingQuickReply = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF81C784),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Add Custom Quick Reply",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Reorder & Arrangement Tip Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DragIndicator,
                                contentDescription = null,
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Hold & drag ⠿ or tap ⏫ to jump any item straight to the top",
                                color = Color(0x99FFFFFF),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        // Quick Replies List with Hold & Drag Reordering, Instant Move-to-Top & Up/Down Controls
                        LazyColumn(
                            state = quickRepliesListState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
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
                                        tint = Color(0x80FFFFFF),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Reset Quick Replies to Default",
                                        color = Color(0x80FFFFFF),
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOGS: IMPORT AUDIO, EDIT, ADD & DELETE
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
            title = { Text(text = "Import Audio Preset", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter a display name for this custom audio track in the in-call soundboard:",
                        color = Color(0xCCFFFFFF),
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = importTitle,
                        onValueChange = { importTitle = it },
                        label = { Text("Preset Title") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF64B5F6),
                            unfocusedBorderColor = Color(0x40FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { placeAtTop = !placeAtTop }
                    ) {
                        Checkbox(
                            checked = placeAtTop,
                            onCheckedChange = { placeAtTop = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3872E0))
                        )
                        Text(
                            text = "Place at Top of Soundboard (Instant Access)",
                            color = Color.White,
                            fontSize = 12.5.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val res = presetManager.importAudioFile(
                                title = importTitle.ifBlank { "Custom Audio" },
                                sourceUri = uri,
                                insertAtTop = placeAtTop
                            )
                            if (res.isSuccess) {
                                Toast.makeText(context, "Audio preset added!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to import audio", Toast.LENGTH_SHORT).show()
                            }
                            pendingImportUri = null
                        }
                    }
                ) {
                    Text("Import", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) {
                    Text("Cancel", color = Color(0x80FFFFFF))
                }
            },
            containerColor = Color(0xFF181C26)
        )
    }

    // 2. Add Speech Preset Dialog
    if (isAddingTextPreset) {
        var newTitle by remember { mutableStateOf("") }
        var newSpoken by remember { mutableStateOf("") }
        var placeAtTop by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { isAddingTextPreset = false },
            title = { Text(text = "Add Speech Preset", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Display Title") },
                        placeholder = { Text("e.g. Call Later") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF64B5F6),
                            unfocusedBorderColor = Color(0x40FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = newSpoken,
                        onValueChange = { newSpoken = it },
                        label = { Text("Spoken Text (Synthesized to Caller)") },
                        placeholder = { Text("e.g. I am busy, I will call you later.") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF64B5F6),
                            unfocusedBorderColor = Color(0x40FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { placeAtTop = !placeAtTop }
                    ) {
                        Checkbox(
                            checked = placeAtTop,
                            onCheckedChange = { placeAtTop = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3872E0))
                        )
                        Text(
                            text = "Place at Top of Soundboard",
                            color = Color.White,
                            fontSize = 12.5.sp
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
                    Text("Add", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddingTextPreset = false }) {
                    Text("Cancel", color = Color(0x80FFFFFF))
                }
            },
            containerColor = Color(0xFF181C26)
        )
    }

    // 3. Edit Preset Dialog
    editingPreset?.let { preset ->
        var editTitle by remember { mutableStateOf(preset.title) }
        var editText by remember { mutableStateOf(if (preset.isAudioFile) "" else preset.textOrPath) }

        AlertDialog(
            onDismissRequest = { editingPreset = null },
            title = { Text(text = "Edit Preset", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF64B5F6),
                            unfocusedBorderColor = Color(0x40FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    if (!preset.isAudioFile) {
                        OutlinedTextField(
                            value = editText,
                            onValueChange = { editText = it },
                            label = { Text("Spoken Text") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF64B5F6),
                                unfocusedBorderColor = Color(0x40FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    } else {
                        Text(
                            text = "Audio file: ${preset.textOrPath.substringAfterLast("/")}",
                            color = Color(0x80FFFFFF),
                            fontSize = 11.sp,
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
                    Text("Save", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPreset = null }) {
                    Text("Cancel", color = Color(0x80FFFFFF))
                }
            },
            containerColor = Color(0xFF181C26)
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
            title = { Text(text = "Add Quick Reply", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = replyTitle,
                        onValueChange = { replyTitle = it },
                        label = { Text("Button Title") },
                        placeholder = { Text("e.g. Call back at 5pm") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF81C784),
                            unfocusedBorderColor = Color(0x40FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = replySpoken,
                        onValueChange = { replySpoken = it },
                        label = { Text("Spoken Voice Message to Caller") },
                        placeholder = { Text("e.g. Please call me back at 5pm, thank you.") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF81C784),
                            unfocusedBorderColor = Color(0x40FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Hindi / Hinglish Mode", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = isHindi,
                            onCheckedChange = { isHindi = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF4CAF50))
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { placeAtTop = !placeAtTop }
                    ) {
                        Checkbox(
                            checked = placeAtTop,
                            onCheckedChange = { placeAtTop = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4CAF50))
                        )
                        Text(
                            text = "Place at Front of Ribbon (First in Call Screen)",
                            color = Color.White,
                            fontSize = 12.sp
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
                    Text("Add", color = Color(0xFF81C784), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddingQuickReply = false }) {
                    Text("Cancel", color = Color(0x80FFFFFF))
                }
            },
            containerColor = Color(0xFF181C26)
        )
    }

    // 5. Edit Quick Reply Dialog
    editingQuickReply?.let { reply ->
        var editTitle by remember { mutableStateOf(reply.title) }
        var editSpoken by remember { mutableStateOf(reply.spokenText) }
        var isHindi by remember { mutableStateOf(reply.isHindi) }

        AlertDialog(
            onDismissRequest = { editingQuickReply = null },
            title = { Text(text = "Edit Quick Reply", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Button Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF81C784),
                            unfocusedBorderColor = Color(0x40FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = editSpoken,
                        onValueChange = { editSpoken = it },
                        label = { Text("Spoken Message") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF81C784),
                            unfocusedBorderColor = Color(0x40FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Hindi Mode", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = isHindi,
                            onCheckedChange = { isHindi = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF4CAF50))
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
                    Text("Save", color = Color(0xFF81C784), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingQuickReply = null }) {
                    Text("Cancel", color = Color(0x80FFFFFF))
                }
            },
            containerColor = Color(0xFF181C26)
        )
    }

    // 6. Delete Confirmation Dialog
    deleteConfirmationItem?.let { (id, title) ->
        AlertDialog(
            onDismissRequest = { deleteConfirmationItem = null },
            title = { Text(text = "Delete Preset?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to remove \"$title\"? It will no longer appear in the in-call ribbon.",
                    color = Color(0xCCFFFFFF),
                    fontSize = 13.5.sp
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
                    Text("Delete", color = Color(0xFFFF453A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmationItem = null }) {
                    Text("Cancel", color = Color(0x80FFFFFF))
                }
            },
            containerColor = Color(0xFF181C26)
        )
    }

    // 7. Reset to Defaults Confirmation
    if (isResetConfirmOpen) {
        AlertDialog(
            onDismissRequest = { isResetConfirmOpen = false },
            title = { Text(text = "Reset to Default?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = if (activeTab == PresetManagerTab.SOUNDBOARD_AUDIO)
                        "This will restore all default audio soundboard presets while preserving your imported custom audio tracks."
                    else
                        "This will reset all Quick Replies to factory defaults.",
                    color = Color(0xCCFFFFFF),
                    fontSize = 13.5.sp
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
                    Text("Reset", color = Color(0xFFFF9500), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isResetConfirmOpen = false }) {
                    Text("Cancel", color = Color(0x80FFFFFF))
                }
            },
            containerColor = Color(0xFF181C26)
        )
    }
}

/**
 * Soundboard Preset Row: With Tactile Drag Handle, Long-Press Drag, Instant Move-to-Top, Up/Down controls, Type badge, Preview Play & Edit/Delete
 */
@Composable
fun SoundboardPresetItemRow(
    preset: AudioPreset,
    index: Int,
    totalCount: Int,
    isPlaying: Boolean,
    isDragging: Boolean = false,
    dragOffset: Float = 0f,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onMoveToTop: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
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
                scaleX = if (isDragging) 1.03f else 1.0f
                scaleY = if (isDragging) 1.03f else 1.0f
            }
            .fillMaxWidth()
            .shadow(
                elevation = if (isDragging) 16.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0xFF64B5F6)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDragging) Color(0x38FFFFFF) else Color(0x1AFFFFFF))
            .border(
                width = if (isDragging) 1.5.dp else 1.dp,
                color = if (isDragging) Color(0xFF64B5F6) else if (preset.isAudioFile) Color(0x40FFA726) else Color(0x22FFFFFF),
                shape = RoundedCornerShape(16.dp)
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
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tactile Drag Indicator Grip Handle (Instant Drag on Touch)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDragging) Color(0xFF64B5F6).copy(alpha = 0.35f) else Color(0x12FFFFFF))
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
                    tint = if (isDragging) Color(0xFF64B5F6) else Color(0xB0FFFFFF),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Reorder Controls: Instant Move-To-Top + Micro Up/Down + Position Index
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Instant Jump to Top Button
                IconButton(
                    onClick = onMoveToTop,
                    enabled = index > 0,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardDoubleArrowUp,
                        contentDescription = "Move Directly to Top",
                        tint = if (index > 0) Color(0xFF64B5F6) else Color(0x20FFFFFF),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = "Move Up",
                            tint = if (index > 0) Color.White else Color(0x20FFFFFF),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "#${index + 1}",
                        color = if (preset.isAudioFile) Color(0xFFFFA726) else Color(0x70FFFFFF),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )

                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Move Down",
                            tint = if (index < totalCount - 1) Color.White else Color(0x20FFFFFF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Main Info
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
                                if (preset.isAudioFile) Color(0xFFFFA726).copy(alpha = 0.25f)
                                else Color(0xFF29B6F6).copy(alpha = 0.20f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (preset.isAudioFile) "CUSTOM AUDIO" else "SPEECH TTS",
                            color = if (preset.isAudioFile) Color(0xFFFFA726) else Color(0xFF81D4FA),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (preset.category.isNotBlank() && preset.category != "Custom") {
                        Text(
                            text = preset.category,
                            color = Color(0x60FFFFFF),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = preset.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!preset.isAudioFile && preset.textOrPath.isNotBlank()) {
                    Text(
                        text = preset.textOrPath,
                        color = Color(0x80FFFFFF),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right Actions: Preview Play (if audio file), Edit & Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (preset.isAudioFile) {
                    IconButton(
                        onClick = onPlayPreview,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) Color(0xFFFF9500) else Color(0x22FFFFFF))
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Stop" else "Play",
                            tint = if (isPlaying) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = Color(0x90FFFFFF),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xDDFF453A),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Quick Reply Row: With Tactile Drag Handle, Long-Press Drag, Instant Move-to-Top, Up/Down controls, Language tag, Edit & Delete
 */
@Composable
fun QuickReplyItemRow(
    reply: QuickReply,
    index: Int,
    totalCount: Int,
    isDragging: Boolean = false,
    dragOffset: Float = 0f,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onMoveToTop: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
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
                scaleX = if (isDragging) 1.03f else 1.0f
                scaleY = if (isDragging) 1.03f else 1.0f
            }
            .fillMaxWidth()
            .shadow(
                elevation = if (isDragging) 16.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0xFF81C784)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDragging) Color(0x38FFFFFF) else Color(0x1AFFFFFF))
            .border(
                width = if (isDragging) 1.5.dp else 1.dp,
                color = if (isDragging) Color(0xFF81C784) else if (reply.isHindi) Color(0x3566BB6A) else Color(0x22FFFFFF),
                shape = RoundedCornerShape(16.dp)
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
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tactile Drag Indicator Grip Handle (Instant Drag on Touch)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDragging) Color(0xFF81C784).copy(alpha = 0.35f) else Color(0x12FFFFFF))
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
                    tint = if (isDragging) Color(0xFF81C784) else Color(0xB0FFFFFF),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Reorder Controls: Instant Move-To-Top + Micro Up/Down + Position Index
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Instant Jump to Top Button
                IconButton(
                    onClick = onMoveToTop,
                    enabled = index > 0,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardDoubleArrowUp,
                        contentDescription = "Move Directly to Top",
                        tint = if (index > 0) Color(0xFF81C784) else Color(0x20FFFFFF),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = "Move Up",
                            tint = if (index > 0) Color.White else Color(0x20FFFFFF),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "#${index + 1}",
                        color = if (reply.isHindi) Color(0xFF81C784) else Color(0x70FFFFFF),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )

                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Move Down",
                            tint = if (index < totalCount - 1) Color.White else Color(0x20FFFFFF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (reply.isHindi) Color(0xFF4CAF50).copy(alpha = 0.25f)
                                else Color(0xFF29B6F6).copy(alpha = 0.20f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (reply.isHindi) "HINDI / HINGLISH" else "ENGLISH",
                            color = if (reply.isHindi) Color(0xFF81C784) else Color(0xFF81D4FA),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = reply.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = reply.spokenText,
                    color = Color(0x80FFFFFF),
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right Actions: Edit & Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = Color(0x90FFFFFF),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xDDFF453A),
                        modifier = Modifier.size(16.dp)
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

