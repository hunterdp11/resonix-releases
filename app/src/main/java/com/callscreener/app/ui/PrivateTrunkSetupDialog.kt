package com.callscreener.app.ui

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.callscreener.app.engine.PrivateTrunkConfig
import com.callscreener.app.engine.PrivateTrunkEngine
import com.callscreener.app.engine.TrunkProviderType
import com.callscreener.app.ui.theme.AppleBlue
import com.callscreener.app.ui.theme.AppleGreen
import com.callscreener.app.ui.theme.ApplePurple
import com.callscreener.app.ui.theme.AppleRed
import com.callscreener.app.ui.theme.TextMuted
import com.callscreener.app.ui.theme.TextPrimary
import com.callscreener.app.ui.theme.TextSecondary
import com.callscreener.app.ui.theme.TextTertiary
import kotlinx.coroutines.launch

/**
 * Dedicated Private VoIP / SIP Trunk Configuration Modal Sheet.
 *
 * Configures the true anonymous routing gateway that completely bypasses
 * consumer carrier SIM restrictions (e.g., Airtel/Jio disabling #31# at cellular towers).
 */
@Composable
fun PrivateTrunkSetupDialog(
    engine: PrivateTrunkEngine,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val initialConfig by engine.trunkConfig.collectAsState()

    var isEnabled by remember { mutableStateOf(initialConfig.isEnabled) }
    var providerType by remember { mutableStateOf(initialConfig.providerType) }
    var accountSid by remember { mutableStateOf(initialConfig.accountSid) }
    var authToken by remember { mutableStateOf(initialConfig.authToken) }
    var callerIdDisplay by remember { mutableStateOf(initialConfig.callerIdDisplay) }
    var webhookUrl by remember { mutableStateOf(initialConfig.webhookUrl) }
    var testStatusMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07090E)),
            color = Color(0xFF07090E)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1B0B2E),
                                    Color(0xFF0C0E18),
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
                                        .background(if (isEnabled) AppleGreen else ApplePurple)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Private Trunk Gateway",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Bypass Airtel/Jio SIM Restrictions • 100% Guaranteed Private Number",
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
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
                        // Card 1: Master Enable / Status
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0x18FFFFFF))
                                    .border(0.8.dp, Color(0x30FFFFFF), RoundedCornerShape(18.dp))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Private Calling Route",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isEnabled)
                                                "Active: private calls route over data trunk (SIM number shielded)"
                                            else
                                                "Disabled: calls fallback to standard SIM line",
                                            color = if (isEnabled) AppleGreen else TextSecondary,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                    Switch(
                                        checked = isEnabled,
                                        onCheckedChange = { isEnabled = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = ApplePurple,
                                            uncheckedThumbColor = TextMuted,
                                            uncheckedTrackColor = Color(0x22FFFFFF)
                                        )
                                    )
                                }
                            }
                        }

                        // Card 2: Provider Selector
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "SELECT PRIVATE GATEWAY TYPE",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp
                                )

                                val providers = listOf(
                                    Triple(TrunkProviderType.BUILTIN_SANDBOX, "Built-In Privacy Relay", "Zero setup, works immediately for private testing"),
                                    Triple(TrunkProviderType.TWILIO_VOICE, "Twilio Voice REST API", "Enterprise global carrier trunk, high reliability"),
                                    Triple(TrunkProviderType.TELNYX_VOICE, "Telnyx Elastic SIP / REST", "Low cost, flexible anonymous Caller ID headers"),
                                    Triple(TrunkProviderType.GENERIC_SIP_WEBHOOK, "Custom Webhook / FreePBX", "Your private Asterisk, FreePBX, or SIP server")
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    providers.forEach { (type, title, desc) ->
                                        val isSelected = providerType == type
                                        val borderColor by animateColorAsState(
                                            targetValue = if (isSelected) ApplePurple else Color(0x20FFFFFF),
                                            animationSpec = tween(180),
                                            label = "providerBorder"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(if (isSelected) Color(0x268E2DE2) else Color(0x14FFFFFF))
                                                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                                                .clickable { providerType = type }
                                                .padding(14.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = title,
                                                        color = Color.White,
                                                        fontSize = 13.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = desc,
                                                        color = if (isSelected) Color(0xFFD4B0FF) else TextSecondary,
                                                        fontSize = 11.sp
                                                    )
                                                }

                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Selected",
                                                        tint = ApplePurple,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Card 3: Credentials Input based on Provider
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "GATEWAY CREDENTIALS & CALLER ID",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0x18FFFFFF))
                                        .border(0.8.dp, Color(0x28FFFFFF), RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        // Caller ID Display String
                                        Column {
                                            Text(text = "Transmitted Caller ID", color = TextSecondary, fontSize = 11.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = callerIdDisplay,
                                                onValueChange = { callerIdDisplay = it },
                                                placeholder = { Text("Anonymous", color = TextTertiary) },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = ApplePurple,
                                                    unfocusedBorderColor = Color(0x30FFFFFF),
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                )
                                            )
                                            Text(
                                                text = "Default: 'Anonymous'. The recipient will see 'Private Number'.",
                                                color = TextTertiary,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }

                                        when (providerType) {
                                            TrunkProviderType.BUILTIN_SANDBOX -> {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(AppleGreen.copy(alpha = 0.15f))
                                                        .border(0.8.dp, AppleGreen.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                                        .padding(10.dp)
                                                ) {
                                                    Text(
                                                        text = "Ready to use immediately. When you place a Private Call, it bypasses the Airtel SIM line and connects via the local privacy bridge.",
                                                        color = AppleGreen,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }

                                            TrunkProviderType.TWILIO_VOICE -> {
                                                Column {
                                                    Text(text = "Twilio Account SID", color = TextSecondary, fontSize = 11.sp)
                                                    OutlinedTextField(
                                                        value = accountSid,
                                                        onValueChange = { accountSid = it },
                                                        placeholder = { Text("ACxxxxxxxxxxxxxxxxxxxxxxxxxxxx", color = TextTertiary) },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }

                                                Column {
                                                    Text(text = "Twilio Auth Token", color = TextSecondary, fontSize = 11.sp)
                                                    OutlinedTextField(
                                                        value = authToken,
                                                        onValueChange = { authToken = it },
                                                        placeholder = { Text("Your Twilio Auth Token", color = TextTertiary) },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }

                                            TrunkProviderType.TELNYX_VOICE -> {
                                                Column {
                                                    Text(text = "Telnyx API Token (Bearer)", color = TextSecondary, fontSize = 11.sp)
                                                    OutlinedTextField(
                                                        value = authToken,
                                                        onValueChange = { authToken = it },
                                                        placeholder = { Text("KEYxxxxxxxxxxxxxxxxxxxxxxxx", color = TextTertiary) },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }

                                                Column {
                                                    Text(text = "Connection ID", color = TextSecondary, fontSize = 11.sp)
                                                    OutlinedTextField(
                                                        value = accountSid,
                                                        onValueChange = { accountSid = it },
                                                        placeholder = { Text("Your Call Control Connection ID", color = TextTertiary) },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }

                                            TrunkProviderType.GENERIC_SIP_WEBHOOK -> {
                                                Column {
                                                    Text(text = "Webhook / SIP Gateway URL", color = TextSecondary, fontSize = 11.sp)
                                                    OutlinedTextField(
                                                        value = webhookUrl,
                                                        onValueChange = { webhookUrl = it },
                                                        placeholder = { Text("https://your-sip-server.com/api/call", color = TextTertiary) },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Status message if test was run
                        testStatusMessage?.let { msg ->
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x228E2DE2))
                                        .border(0.8.dp, Color(0x608E2DE2), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(text = msg, color = Color(0xFFD4B0FF), fontSize = 11.5.sp)
                                }
                            }
                        }

                        // Action Buttons: Save & Activate
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        val newConfig = PrivateTrunkConfig(
                                            isEnabled = isEnabled,
                                            providerType = providerType,
                                            accountSid = accountSid.trim(),
                                            authToken = authToken.trim(),
                                            callerIdDisplay = if (callerIdDisplay.isBlank()) "Anonymous" else callerIdDisplay.trim(),
                                            webhookUrl = webhookUrl.trim()
                                        )
                                        engine.saveConfig(newConfig)
                                        Toast.makeText(context, "Private Trunk Config Saved & Activated!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ApplePurple)
                                ) {
                                    Text(
                                        text = "Save & Activate Private Trunk",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "When activated, any call placed with 'Private' toggled ON will route through this gateway. Your Airtel SIM card will not be used, completely protecting your phone number from the recipient.",
                                    color = TextTertiary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
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

