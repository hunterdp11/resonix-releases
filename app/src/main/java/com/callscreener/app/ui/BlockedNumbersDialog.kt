package com.callscreener.app.ui

import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.callscreener.app.data.ContactItem
import com.callscreener.app.data.CallerProfileManager
import com.callscreener.app.data.rememberContactPhotoBitmap
import com.callscreener.app.data.DialerRepository
import com.callscreener.app.engine.BlockedContact
import com.callscreener.app.engine.CallBlockManager
import com.callscreener.app.ui.theme.AppFont
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PixelBlockedNumbersScreen(
    onDismiss: () -> Unit,
    monetColors: ColorScheme = getPixelMonetDarkColorScheme()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val blockManager = remember { CallBlockManager.getInstance(context) }
    val blockedList by blockManager.blockedListFlow.collectAsState()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    fun animateDismiss() {
        if (!isVisible) return
        coroutineScope.launch {
            isVisible = false
            delay(180)
            onDismiss()
        }
    }

    BackHandler(enabled = isVisible) {
        animateDismiss()
    }

    val blockPrefs = remember { context.getSharedPreferences("call_screener_block_prefs", Context.MODE_PRIVATE) }
    var blockUnknownCallers by remember {
        mutableStateOf(blockPrefs.getBoolean("block_unknown_callers", false))
    }

    var showAddManualDialog by remember { mutableStateOf(false) }
    var showContactPicker by remember { mutableStateOf(false) }

    var contacts by remember { mutableStateOf<List<ContactItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        contacts = DialerRepository.loadContacts(context)
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
                .background(Color(0xFF0F1218))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            animateDismiss()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Blocked numbers",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = AppFont
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x40000000))
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFF191C23))
                                .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Unknown",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        fontFamily = AppFont
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Block calls from unidentified callers",
                                        fontSize = 12.5.sp,
                                        color = monetColors.onSurfaceVariant,
                                        fontFamily = AppFont
                                    )
                                }

                                Switch(
                                    checked = blockUnknownCallers,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        blockUnknownCallers = it
                                        blockPrefs.edit().putBoolean("block_unknown_callers", it).apply()
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2EAA55),
                                        uncheckedThumbColor = monetColors.onSurfaceVariant,
                                        uncheckedTrackColor = monetColors.surfaceContainerHighest
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .height(0.6.dp)
                                    .background(monetColors.outlineVariant.copy(alpha = 0.25f))
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showAddManualDialog = true
                                    }
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(monetColors.primary.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Add",
                                        tint = monetColors.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Add a phone number",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        fontFamily = AppFont
                                    )
                                    Text(
                                        text = "Type any carrier number to block",
                                        fontSize = 12.sp,
                                        color = monetColors.onSurfaceVariant,
                                        fontFamily = AppFont
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = monetColors.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .height(0.6.dp)
                                    .background(monetColors.outlineVariant.copy(alpha = 0.25f))
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showContactPicker = true
                                    }
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2EAA55).copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = "Contact",
                                        tint = Color(0xFF2EAA55),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Choose from contacts",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        fontFamily = AppFont
                                    )
                                    Text(
                                        text = "Select an existing contact to block",
                                        fontSize = 12.sp,
                                        color = monetColors.onSurfaceVariant,
                                        fontFamily = AppFont
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = monetColors.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "BLOCKED CALLERS (${blockedList.size})",
                                fontSize = 11.5.sp,
                                fontFamily = AppFont,
                                fontWeight = FontWeight.Bold,
                                color = monetColors.primary,
                                letterSpacing = 0.8.sp
                            )
                            if (blockedList.isNotEmpty()) {
                                Text(
                                    text = "Tap X to unblock",
                                    fontSize = 10.5.sp,
                                    color = monetColors.onSurfaceVariant,
                                    fontFamily = AppFont
                                )
                            }
                        }
                    }

                    if (blockedList.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(Color(0xFF191C23))
                                    .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
                                    .padding(vertical = 40.dp, horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(monetColors.outlineVariant.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Block,
                                        contentDescription = null,
                                        tint = monetColors.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "No blocked numbers",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = AppFont
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Calls and messages from numbers you block won't reach your phone.",
                                    fontSize = 12.5.sp,
                                    color = monetColors.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 17.sp,
                                    fontFamily = AppFont
                                )
                            }
                        }
                    } else {
                        items(blockedList, key = { it.number }) { item ->
                            BlockedContactRow(
                                contact = item,
                                monetColors = monetColors,
                                onUnblock = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    blockManager.unblockNumber(item.number)
                                    Toast.makeText(
                                        context,
                                        "Unblocked ${item.name ?: item.number}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
    }

    if (showAddManualDialog) {
        AddManualBlockDialog(
            monetColors = monetColors,
            onDismiss = { showAddManualDialog = false },
            onBlock = { number, name ->
                if (number.isNotBlank()) {
                    blockManager.blockNumber(number = number, name = name.ifBlank { null })
                    Toast.makeText(context, "Blocked $number", Toast.LENGTH_SHORT).show()
                }
                showAddManualDialog = false
            }
        )
    }

    if (showContactPicker) {
        BlockContactPickerModal(
            contacts = contacts,
            monetColors = monetColors,
            onDismiss = { showContactPicker = false },
            onSelectContact = { contact ->
                blockManager.blockNumber(number = contact.number, name = contact.name)
                Toast.makeText(context, "Blocked ${contact.name}", Toast.LENGTH_SHORT).show()
                showContactPicker = false
            }
        )
    }
}

@Composable
private fun BlockedContactRow(
    contact: BlockedContact,
    monetColors: ColorScheme,
    onUnblock: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF191C23))
            .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val context = LocalContext.current
                val photoVersion = CallerProfileManager.photoVersionState.intValue
                val resolvedPhoto = remember(contact.number, photoVersion) {
                    CallerProfileManager.getCustomPhotoPath(context, contact.number)
                }
                val contactBitmap = rememberContactPhotoBitmap(resolvedPhoto, reloadKey = photoVersion)

                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (contactBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = contactBitmap,
                            contentDescription = contact.name,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5252)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Block,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(9.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5252).copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Block,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name ?: DialerRepository.formatPhoneNumber(contact.number),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = AppFont,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (contact.name != null)
                            DialerRepository.formatPhoneNumber(contact.number)
                        else
                            "Blocked Number",
                        fontSize = 12.sp,
                        color = monetColors.onSurfaceVariant,
                        fontFamily = AppFont,
                        maxLines = 1
                    )
                }
            }

            IconButton(
                onClick = onUnblock,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x18FFFFFF))
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Unblock",
                    tint = monetColors.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddManualBlockDialog(
    monetColors: ColorScheme,
    onDismiss: () -> Unit,
    onBlock: (String, String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var numberText by remember { mutableStateOf("") }
    var nameText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E222B))
                .border(1.dp, monetColors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Block a number",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = AppFont
                )

                Text(
                    text = "Calls from this phone number will be silently dropped or answered with SIT dead-tone audio.",
                    fontSize = 12.5.sp,
                    color = monetColors.onSurfaceVariant,
                    fontFamily = AppFont,
                    lineHeight = 16.sp
                )

                OutlinedTextField(
                    value = numberText,
                    onValueChange = { numberText = it },
                    label = { Text("Phone number", fontFamily = AppFont, fontSize = 13.sp) },
                    placeholder = { Text("e.g. +1 555 123 4567", fontFamily = AppFont, fontSize = 13.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = monetColors.primary,
                        unfocusedBorderColor = monetColors.outlineVariant.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Name (optional)", fontFamily = AppFont, fontSize = 13.sp) },
                    placeholder = { Text("e.g. Telemarketer", fontFamily = AppFont, fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = monetColors.primary,
                        unfocusedBorderColor = monetColors.outlineVariant.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = monetColors.onSurfaceVariant)
                    ) {
                        Text("Cancel", fontFamily = AppFont, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBlock(numberText.trim(), nameText.trim())
                        },
                        enabled = numberText.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5252),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Block", fontFamily = AppFont, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockContactPickerModal(
    contacts: List<ContactItem>,
    monetColors: ColorScheme,
    onDismiss: () -> Unit,
    onSelectContact: (ContactItem) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) contacts
        else contacts.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.number.contains(searchQuery)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F1218))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Select contact to block",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = AppFont
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF191C23))
                        .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = monetColors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = AppFont
                            ),
                            cursorBrush = SolidColor(monetColors.primary),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search contacts...",
                                        color = monetColors.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 14.sp,
                                        fontFamily = AppFont
                                    )
                                }
                                inner()
                            }
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear",
                                    tint = monetColors.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered, key = { "block_contact_${it.id}_${it.number}" }) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF191C23))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelectContact(contact)
                                }
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val pickerContext = LocalContext.current
                            val pickerPhotoVer = CallerProfileManager.photoVersionState.intValue
                            val resolvedPhoto = remember(contact.number, contact.photoUri, pickerPhotoVer) {
                                CallerProfileManager.getCustomPhotoPath(pickerContext, contact.number) ?: contact.photoUri
                            }
                            val contactBitmap = rememberContactPhotoBitmap(resolvedPhoto, reloadKey = pickerPhotoVer)

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(monetColors.primary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (contactBitmap != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = contactBitmap,
                                        contentDescription = contact.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = contact.name.trim().take(1).uppercase(),
                                        color = monetColors.primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = AppFont
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontFamily = AppFont,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = DialerRepository.formatPhoneNumber(contact.number),
                                    fontSize = 12.sp,
                                    color = monetColors.onSurfaceVariant,
                                    fontFamily = AppFont
                                )
                            }

                            Text(
                                text = "Block",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252),
                                fontFamily = AppFont
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedNumbersDialog(
    blockManager: CallBlockManager,
    onDismiss: () -> Unit
) {
    PixelBlockedNumbersScreen(onDismiss = onDismiss)
}
