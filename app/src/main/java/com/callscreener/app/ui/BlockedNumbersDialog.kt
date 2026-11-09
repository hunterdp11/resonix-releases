package com.callscreener.app.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.callscreener.app.data.ContactItem
import com.callscreener.app.data.DialerRepository
import com.callscreener.app.engine.BlockRejectMode
import com.callscreener.app.engine.BlockedContact
import com.callscreener.app.engine.CallBlockManager
import com.callscreener.app.root.RootPrivilegeManager
import com.callscreener.app.ui.theme.AppleBlue
import com.callscreener.app.ui.theme.AppleGreen
import com.callscreener.app.ui.theme.ApplePurple
import com.callscreener.app.ui.theme.AppleRed
import com.callscreener.app.ui.theme.TextMuted
import com.callscreener.app.ui.theme.TextPrimary
import com.callscreener.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DialogBg = Color(0xFF07070A)
private val CardBg = Color(0xFF13121D)

/**
 * Blocked Numbers & Call Forwarding Trap Manager.
 * Allows inspecting, adding, and redirecting unwanted callers to any chosen contact with Root hardware protection.
 */
@Composable
fun BlockedNumbersDialog(
    blockManager: CallBlockManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val blockedList by blockManager.blockedListFlow.collectAsState()
    val isRootActive = remember { RootPrivilegeManager.isRootAvailable() }

    var contacts by remember { mutableStateOf<List<ContactItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        contacts = DialerRepository.loadContacts(context)
    }

    var selectedMode by remember { mutableStateOf(blockManager.blockMode) }
    var defaultForwardNumber by remember { mutableStateOf(blockManager.defaultForwardNumber) }
    var defaultForwardName by remember { mutableStateOf(blockManager.defaultForwardName) }

    var isAddingNumber by remember { mutableStateOf(false) }
    var inputNumber by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }
    var addForwardToNumber by remember { mutableStateOf<String?>(null) }
    var addForwardToName by remember { mutableStateOf<String?>(null) }
    var isDivertTargetEnabledForAdd by remember { mutableStateOf(false) }

    // Contact Picker State
    var showContactPickerForAdd by remember { mutableStateOf(false) }
    var showContactPickerForDefault by remember { mutableStateOf(false) }
    var editingContactForDivert by remember { mutableStateOf<BlockedContact?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    // 1. Contact Picker Modal for Adding New Blocked Number
    if (showContactPickerForAdd) {
        ContactPickerModal(
            title = "Forward Target for New Blocked Caller",
            contacts = contacts,
            onDismiss = { showContactPickerForAdd = false },
            onSelectContact = { contact ->
                addForwardToNumber = contact.number
                addForwardToName = contact.name
                isDivertTargetEnabledForAdd = true
                showContactPickerForAdd = false
            }
        )
    }

    // 2. Contact Picker Modal for Default Forward Mode
    if (showContactPickerForDefault) {
        ContactPickerModal(
            title = "Global Forward Target Contact",
            contacts = contacts,
            onDismiss = { showContactPickerForDefault = false },
            onSelectContact = { contact ->
                blockManager.defaultForwardNumber = contact.number
                blockManager.defaultForwardName = contact.name
                defaultForwardNumber = contact.number
                defaultForwardName = contact.name
                showContactPickerForDefault = false
                blockManager.syncCarrierCallForwarding(contact.number) { ok, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(context, "Arming carrier forward (**67*) for ${contact.name}...", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 3. Contact Picker Modal for Editing Existing Blocked Contact's Forward Target
    editingContactForDivert?.let { targetBlocked ->
        ContactPickerModal(
            title = "Forward Calls From: ${targetBlocked.name ?: targetBlocked.number}",
            contacts = contacts,
            onDismiss = { editingContactForDivert = null },
            onSelectContact = { contact ->
                blockManager.updateForwardTarget(targetBlocked.number, contact.number, contact.name)
                editingContactForDivert = null
                blockManager.syncCarrierCallForwarding(contact.number)
                Toast.makeText(context, "Calls will divert to ${contact.name}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DialogBg
        ) {
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x28FF3B30))
                                .border(1.dp, Color(0x60FF3B30), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Block,
                                contentDescription = null,
                                tint = AppleRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Blocked & Call Forward Trap",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isRootActive)
                                    "Root Zero-Ring Hardware Protection"
                                else
                                    "Telecom Call Screening Shield",
                                color = if (isRootActive) AppleGreen else AppleBlue,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x1AFFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Status & Add Button Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardBg)
                                .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${blockedList.size} Numbers Managed",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Active mode: ${selectedMode.displayName}",
                                        color = if (selectedMode == BlockRejectMode.FORWARD_TO_TARGET) AppleBlue else AppleRed,
                                        fontSize = 12.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isAddingNumber = !isAddingNumber
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAddingNumber) Color(0x25FFFFFF) else Color(0x25FF3B30),
                                        contentColor = if (isAddingNumber) Color.White else AppleRed
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isAddingNumber) Icons.Default.Close else Icons.Rounded.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isAddingNumber) "Cancel" else "Add Number", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Call Interception Mode Selector
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = null,
                                    tint = AppleBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "UNWANTED CALL INTERCEPTION ACTION",
                                    color = Color(0xFF90CAF9),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                            }

                            val modes = BlockRejectMode.entries
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                modes.forEach { mode ->
                                    val isSelected = selectedMode == mode
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Color(0x282196F3) else Color(0x10FFFFFF))
                                            .border(
                                                width = if (isSelected) 1.2.dp else 0.8.dp,
                                                color = if (isSelected) AppleBlue else Color(0x18FFFFFF),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedMode = mode
                                                blockManager.blockMode = mode
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = mode.displayName,
                                                    color = if (isSelected) Color.White else TextPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                                Text(
                                                    text = mode.description,
                                                    color = TextSecondary,
                                                    fontSize = 11.sp,
                                                    lineHeight = 14.sp
                                                )
                                            }
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(start = 8.dp)
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(AppleBlue),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Default Forward Target Card (Active when Mode is FORWARD_TO_TARGET)
                    if (selectedMode == BlockRejectMode.FORWARD_TO_TARGET) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x1E2196F3))
                                    .border(1.dp, Color(0x402196F3), RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Default Forwarding Contact",
                                                color = Color.White,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (!defaultForwardNumber.isNullOrBlank())
                                                    "Diverting to: ${defaultForwardName ?: defaultForwardNumber} (${defaultForwardNumber})"
                                                else
                                                    "No target chosen. Tap to choose a saved contact.",
                                                color = if (!defaultForwardNumber.isNullOrBlank()) AppleGreen else TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                showContactPickerForDefault = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (defaultForwardNumber.isNullOrBlank()) "Select" else "Change", fontSize = 11.5.sp)
                                        }
                                    }

                                    if (!defaultForwardNumber.isNullOrBlank()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0x2200E5FF))
                                                    .border(0.8.dp, Color(0x5000E5FF), RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        blockManager.syncCarrierCallForwarding(defaultForwardNumber) { ok, msg ->
                                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                        }
                                                        Toast.makeText(context, "Arming carrier CFB (**67*)...", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .padding(vertical = 7.dp, horizontal = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(Icons.Rounded.PhoneInTalk, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(13.dp))
                                                    Text("Arm Carrier CFB", color = Color(0xFF80D8FF), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0x22FFFFFF))
                                                    .border(0.8.dp, Color(0x40FFFFFF), RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        try {
                                                            context.startActivity(blockManager.getCarrierArmIntent(defaultForwardNumber!!))
                                                        } catch (_: Exception) {}
                                                    }
                                                    .padding(vertical = 7.dp, horizontal = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("Dial **67*", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Add Number Form
                    item {
                        AnimatedVisibility(visible = isAddingNumber) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CardBg)
                                    .border(1.dp, Color(0x40FF3B30), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "BLOCK & CONFIGURE FORWARDING",
                                    color = AppleRed,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = inputNumber,
                                    onValueChange = { inputNumber = it },
                                    placeholder = { Text("Phone Number (+1 555 123 4567)", color = TextMuted, fontSize = 13.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppleRed,
                                        unfocusedBorderColor = Color(0x30FFFFFF),
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        cursorColor = AppleRed
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = inputName,
                                    onValueChange = { inputName = it },
                                    placeholder = { Text("Name / Label (e.g. Scammer, Telemarketer)", color = TextMuted, fontSize = 13.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppleRed,
                                        unfocusedBorderColor = Color(0x30FFFFFF),
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        cursorColor = AppleRed
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Forward to contact section
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x152196F3))
                                        .border(0.8.dp, Color(0x302196F3), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                                    imageVector = Icons.Rounded.PhoneInTalk,
                                                    contentDescription = null,
                                                    tint = AppleBlue,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Forward this caller to a contact",
                                                    color = Color.White,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }

                                            Switch(
                                                checked = isDivertTargetEnabledForAdd,
                                                onCheckedChange = {
                                                    isDivertTargetEnabledForAdd = it
                                                    if (it && addForwardToNumber == null) {
                                                        showContactPickerForAdd = true
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = AppleBlue,
                                                    uncheckedThumbColor = TextMuted,
                                                    uncheckedTrackColor = Color(0x22FFFFFF)
                                                )
                                            )
                                        }

                                        if (isDivertTargetEnabledForAdd) {
                                            if (addForwardToNumber != null) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0x2230D158))
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = "Target: ${addForwardToName ?: addForwardToNumber}",
                                                            color = AppleGreen,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = addForwardToNumber ?: "",
                                                            color = TextSecondary,
                                                            fontSize = 11.sp,
                                                            fontFamily = FontFamily.Monospace
                                                        )
                                                    }
                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        IconButton(
                                                            onClick = { showContactPickerForAdd = true },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Rounded.Edit, contentDescription = "Change", tint = AppleBlue, modifier = Modifier.size(14.dp))
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                addForwardToNumber = null
                                                                addForwardToName = null
                                                                isDivertTargetEnabledForAdd = false
                                                            },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = AppleRed, modifier = Modifier.size(14.dp))
                                                        }
                                                    }
                                                }
                                            } else {
                                                Button(
                                                    onClick = { showContactPickerForAdd = true },
                                                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                                ) {
                                                    Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Choose Saved Contact", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val num = inputNumber.trim()
                                        if (num.isBlank()) {
                                            Toast.makeText(context, "Please enter a phone number", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        blockManager.blockNumber(
                                            number = num,
                                            name = inputName.trim().ifBlank { null },
                                            forwardToNumber = if (isDivertTargetEnabledForAdd) addForwardToNumber else null,
                                            forwardToName = if (isDivertTargetEnabledForAdd) addForwardToName else null
                                        )
                                        Toast.makeText(context, "🛡️ $num added to blocklist", Toast.LENGTH_SHORT).show()
                                        inputNumber = ""
                                        inputName = ""
                                        addForwardToNumber = null
                                        addForwardToName = null
                                        isDivertTargetEnabledForAdd = false
                                        isAddingNumber = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppleRed,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                ) {
                                    Icon(Icons.Rounded.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Block This Number", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Empty State
                    if (blockedList.isEmpty() && !isAddingNumber) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x18FFFFFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Security,
                                            contentDescription = null,
                                            tint = AppleGreen,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Text("No Blocked Numbers", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Calls will ring normally through Call Screener.", color = TextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Blocked Contacts List
                    itemsIndexed(blockedList, key = { index, item -> "blocked_${item.number}_$index" }) { _, item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardBg)
                                .border(0.8.dp, Color(0x20FFFFFF), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (item.forwardToNumber != null) Color(0x252196F3) else Color(0x20FF3B30)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (item.forwardToNumber != null) Icons.Rounded.PhoneInTalk else Icons.Rounded.Block,
                                            contentDescription = null,
                                            tint = if (item.forwardToNumber != null) AppleBlue else AppleRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = item.name ?: DialerRepository.formatPhoneNumber(item.number),
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (item.name != null) {
                                            Text(
                                                text = DialerRepository.formatPhoneNumber(item.number),
                                                color = TextSecondary,
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        // Forwarding Tag
                                        if (!item.forwardToNumber.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0x282196F3))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "↪ Forwards to: ${item.forwardToName ?: item.forwardToNumber}",
                                                    color = Color(0xFF90CAF9),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        } else if (selectedMode == BlockRejectMode.FORWARD_TO_TARGET && !defaultForwardNumber.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0x2030D158))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "↪ Global Divert: ${defaultForwardName ?: defaultForwardNumber}",
                                                    color = AppleGreen,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Blocked on ${dateFormat.format(Date(item.blockedAt))}",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Change forward target button
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            editingContactForDivert = item
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x182196F3))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.PhoneInTalk,
                                            contentDescription = "Forward Target",
                                            tint = AppleBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Delete/Unblock button
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            blockManager.unblockNumber(item.number)
                                            Toast.makeText(context, "${item.name ?: item.number} unblocked", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x18FFFFFF))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Unblock",
                                            tint = AppleRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (blockedList.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        blockManager.clearAllBlocked()
                                        Toast.makeText(context, "All numbers unblocked", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Clear All Blocked Numbers",
                                    color = AppleRed,
                                    fontSize = 13.sp,
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

/**
 * Universal Contact Picker Modal:
 * Allows user to pick any contact from their saved address book with live real-time filtering.
 */
@Composable
fun ContactPickerModal(
    title: String = "Select Target Contact",
    contacts: List<ContactItem>,
    onDismiss: () -> Unit,
    onSelectContact: (ContactItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) contacts
        else contacts.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.number.contains(searchQuery)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF090812)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Unwanted calls will silently deflect to this person",
                            color = AppleBlue,
                            fontSize = 11.5.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x1AFFFFFF))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or number...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleBlue,
                        unfocusedBorderColor = Color(0x30FFFFFF),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AppleBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Contacts List
                if (filteredContacts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (contacts.isEmpty()) "No device contacts available" else "No matching contacts found",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(filteredContacts, key = { index, contact -> "contact_${contact.id}_${contact.number}_$index" }) { _, contact ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x14FFFFFF))
                                    .border(0.8.dp, Color(0x18FFFFFF), RoundedCornerShape(12.dp))
                                    .clickable { onSelectContact(contact) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF3F51B5), Color(0xFF2196F3))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contact.name.firstOrNull()?.uppercase() ?: "#",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = contact.name,
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = DialerRepository.formatPhoneNumber(contact.number),
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Rounded.PhoneInTalk,
                                        contentDescription = "Select",
                                        tint = AppleBlue,
                                        modifier = Modifier.size(18.dp)
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
