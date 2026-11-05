package com.callscreener.app.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Settings
import android.telecom.TelecomManager
import android.util.Log
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.DisposableEffect
import androidx.core.content.FileProvider
import com.callscreener.app.recorder.RecordedCallItem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import com.callscreener.app.data.T9Helper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callscreener.app.engine.AudioPresetManager
import com.callscreener.app.model.AudioPreset
import com.callscreener.app.model.VoicePersona
import com.callscreener.app.root.AppOperationMode
import com.callscreener.app.root.RootPrivilegeManager
import com.callscreener.app.session.CallSessionManager
import com.callscreener.app.ui.theme.AppleAmber
import com.callscreener.app.ui.theme.AppleAmberGlassFill
import com.callscreener.app.ui.theme.AppleBlue
import com.callscreener.app.ui.theme.AppleBlueGlassFill
import com.callscreener.app.ui.theme.AppleGreen
import com.callscreener.app.ui.theme.AppleGreenGlassFill
import com.callscreener.app.ui.theme.AppleIndigo
import com.callscreener.app.ui.theme.ApplePurple
import com.callscreener.app.ui.theme.AppleRed
import com.callscreener.app.ui.theme.AppleRedGlassFill
import com.callscreener.app.ui.theme.AppleTeal
import com.callscreener.app.ui.theme.CallScreenerTheme
import com.callscreener.app.ui.theme.GlassBackgroundGradient
import com.callscreener.app.ui.theme.LiquidGlassBorder
import com.callscreener.app.ui.theme.LiquidGlassBorderSubtle
import com.callscreener.app.ui.theme.LiquidGlassDockBorder
import com.callscreener.app.ui.theme.LiquidGlassDockFill
import com.callscreener.app.ui.theme.LiquidGlassFill
import com.callscreener.app.ui.theme.LiquidGlassFillElevated
import com.callscreener.app.ui.theme.LiquidGlassFillSubtle
import com.callscreener.app.ui.theme.TextMuted
import com.callscreener.app.ui.theme.TextPrimary
import com.callscreener.app.ui.theme.TextSecondary
import com.callscreener.app.ui.theme.TextTertiary
import com.callscreener.app.ui.theme.GlassBackground
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.callscreener.app.data.CallLogItem
import com.callscreener.app.data.ContactItem
import com.callscreener.app.data.DialerRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val isDefaultDialerState = mutableStateOf(false)

    private val roleRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        isDefaultDialerState.value = isDefaultDialer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            } catch (_: Exception) {}
        }
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        val insets = WindowCompat.getInsetsController(window, window.decorView)
        insets.isAppearanceLightStatusBars = false
        insets.isAppearanceLightNavigationBars = false

        isDefaultDialerState.value = isDefaultDialer()

        setContent {
            CallScreenerTheme {
                val isDefault by isDefaultDialerState
                MainDashboard(
                    isDefaultDialer = isDefault,
                    onRequestDefaultDialer = { requestDefaultDialer() },
                    onStartSimulation = {
                        val sessionManager = CallSessionManager.getInstance(this)
                        sessionManager.startSimulatedCall()
                        startActivity(Intent(this, ScreeningCallActivity::class.java))
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isDefaultDialerState.value = isDefaultDialer()
    }

    private fun isDefaultDialer(): Boolean {
        return try {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            val isPackageDefault = telecomManager?.defaultDialerPackage == packageName

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(RoleManager::class.java)
                val isRoleHeld = roleManager?.isRoleHeld(RoleManager.ROLE_DIALER) == true
                isRoleHeld || isPackageDefault
            } else {
                isPackageDefault
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking default dialer status", e)
            false
        }
    }

    private fun requestDefaultDialer() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(RoleManager::class.java)
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                    roleRequestLauncher.launch(intent)
                    return
                }
            }

            @Suppress("DEPRECATION")
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error requesting default dialer", e)
            try {
                val fallbackIntent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Toast.makeText(
                    this,
                    "Please set Call Screener as Default Phone App in Settings -> Apps -> Default Apps",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

enum class DialerTab {
    RECENTS,
    CONTACTS,
    KEYPAD,
    ASSISTANT
}

private fun formatCallDate(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val calNow = java.util.Calendar.getInstance()
    val calCall = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }

    val isSameDay = calNow.get(java.util.Calendar.YEAR) == calCall.get(java.util.Calendar.YEAR) &&
            calNow.get(java.util.Calendar.DAY_OF_YEAR) == calCall.get(java.util.Calendar.DAY_OF_YEAR)

    val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    val dateFormat = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())

    return when {
        diff < 60_000L -> "Just now"
        isSameDay -> timeFormat.format(java.util.Date(timestamp))
        diff < 48 * 3600_000L && calNow.get(java.util.Calendar.DAY_OF_YEAR) - calCall.get(java.util.Calendar.DAY_OF_YEAR) == 1 -> "Yesterday"
        else -> dateFormat.format(java.util.Date(timestamp))
    }
}

private fun formatDuration(durationSeconds: Long): String {
    if (durationSeconds <= 0L) return "0s"
    val mins = durationSeconds / 60
    val secs = durationSeconds % 60
    return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
}

private fun formatExactTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val sdf = java.text.SimpleDateFormat("EEEE, MMM d, yyyy • h:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

private fun getCallDateGroup(timestamp: Long): String {
    if (timestamp == 0L) return "OLDER"
    val calNow = java.util.Calendar.getInstance()
    val calCall = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
    val isSameDay = calNow.get(java.util.Calendar.YEAR) == calCall.get(java.util.Calendar.YEAR) &&
            calNow.get(java.util.Calendar.DAY_OF_YEAR) == calCall.get(java.util.Calendar.DAY_OF_YEAR)
    val isYesterday = calNow.get(java.util.Calendar.YEAR) == calCall.get(java.util.Calendar.YEAR) &&
            calNow.get(java.util.Calendar.DAY_OF_YEAR) - calCall.get(java.util.Calendar.DAY_OF_YEAR) == 1
    return when {
        isSameDay -> "TODAY"
        isYesterday -> "YESTERDAY"
        else -> "OLDER"
    }
}

@Composable
fun MainDashboard(
    isDefaultDialer: Boolean,
    onRequestDefaultDialer: () -> Unit,
    onStartSimulation: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { CallSessionManager.getInstance(context) }
    val sessionState by sessionManager.sessionState.collectAsState()

    val initialTab = remember {
        val extra = (context as? android.app.Activity)?.intent?.getStringExtra("OPEN_TAB")
        when (extra) {
            "RECENTS" -> DialerTab.RECENTS
            "CONTACTS" -> DialerTab.CONTACTS
            "ASSISTANT" -> DialerTab.ASSISTANT
            else -> DialerTab.KEYPAD
        }
    }
    var currentTab by remember { mutableStateOf(initialTab) }

    DisposableEffect(Unit) {
        val activity = context as? androidx.activity.ComponentActivity
        val listener: androidx.core.util.Consumer<Intent> = androidx.core.util.Consumer { intent ->
            val extra = intent.getStringExtra("OPEN_TAB")
            if (extra == "RECENTS") {
                currentTab = DialerTab.RECENTS
            }
        }
        activity?.addOnNewIntentListener(listener)
        onDispose {
            activity?.removeOnNewIntentListener(listener)
        }
    }
    var autoScreenUnknown by remember { mutableStateOf(true) }
    var selectedPersona by remember { mutableStateOf(VoicePersona.DEFAULT) }
    var isLiveMicActive by remember { mutableStateOf(false) }

    var contacts by remember { mutableStateOf<List<ContactItem>>(emptyList()) }
    var callLogs by remember { mutableStateOf<List<CallLogItem>>(emptyList()) }
    var isLoadingContacts by remember { mutableStateOf(false) }
    var isLoadingLogs by remember { mutableStateOf(false) }
    var permissionTrigger by remember { mutableStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        permissionTrigger++
    }

    LaunchedEffect(Unit) {
        val requiredPerms = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPerms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(requiredPerms.toTypedArray())
    }

    LaunchedEffect(permissionTrigger) {
        isLoadingContacts = true
        contacts = DialerRepository.loadContacts(context)
        isLoadingContacts = false

        isLoadingLogs = true
        callLogs = DialerRepository.loadCallLogs(context, contacts)
        isLoadingLogs = false
    }

    var selectedContactForDetails by remember { mutableStateOf<ContactItem?>(null) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isPresetManagerOpen by remember { mutableStateOf(false) }
    val hazeState = remember { HazeState() }
    val presets by sessionManager.presetManager.presets.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // Container captured by Haze: Both the flowing caustics backdrop and the tab content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState)
        ) {
            // Pure Apple Liquid Glass Canvas Backdrop with chromatic caustics flowing directly behind dock
            AppleLiquidBackdrop()

            // Tab Content extending full edge-to-edge behind floating glass dock
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (currentTab) {
                    DialerTab.RECENTS -> RecentsScreen(
                        callLogs = callLogs,
                        isLoading = isLoadingLogs,
                        presets = presets,
                        onPlayPreset = { sessionManager.playAudioPreset(it) },
                        onCall = { number -> DialerRepository.placeCall(context, number) },
                        onScreen = { log ->
                            sessionManager.startSimulatedCall(
                                simulatedNumber = log.number,
                                simulatedName = log.name ?: "Unknown Caller"
                            )
                            context.startActivity(Intent(context, ScreeningCallActivity::class.java))
                        },
                        onOpenDetails = { contact -> selectedContactForDetails = contact }
                    )
                    DialerTab.CONTACTS -> ContactsScreen(
                        contacts = contacts,
                        isLoading = isLoadingContacts,
                        onCall = { number -> DialerRepository.placeCall(context, number) },
                        onOpenDetails = { contact -> selectedContactForDetails = contact }
                    )
                    DialerTab.KEYPAD -> KeypadScreen(
                        contacts = contacts,
                        callLogs = callLogs,
                        onCall = { number -> DialerRepository.placeCall(context, number) },
                        onOpenDetails = { contact -> selectedContactForDetails = contact },
                        onOpenSettings = { isSettingsOpen = true }
                    )
                    DialerTab.ASSISTANT -> AssistantTabScreen(
                        onStartSimulation = onStartSimulation,
                        selectedPersona = selectedPersona,
                        onSelectPersona = { selectedPersona = it },
                        isLiveMicActive = isLiveMicActive,
                        onToggleLiveMic = { isLiveMicActive = !isLiveMicActive },
                        isGreetingEnabled = sessionState.isGreetingEnabled,
                        onToggleGreeting = { sessionManager.setGreetingEnabled(it) },
                        presetManager = sessionManager.presetManager,
                        onOpenPresetManager = { isPresetManagerOpen = true }
                    )
                }
            }
        }

        // Docked Liquid Glass Floating Bottom Navigation Bar with Hardware GPU Backdrop Blur
        LiquidGlassBottomBar(
            currentTab = currentTab,
            onTabSelected = { currentTab = it },
            hazeState = hazeState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
        )

        // iOS 26 Full Specular Liquid Glass Contact Details Modal Sheet
        selectedContactForDetails?.let { contact ->
            val contactHistory = remember(contact.number, callLogs) {
                val cleanTarget = contact.number.replace(Regex("[^0-9]"), "")
                callLogs.filter { log ->
                    val cleanLog = log.number.replace(Regex("[^0-9]"), "")
                    if (cleanTarget.length >= 7 && cleanLog.length >= 7) {
                        cleanLog.endsWith(cleanTarget.takeLast(7)) || cleanTarget.endsWith(cleanLog.takeLast(7))
                    } else {
                        cleanLog == cleanTarget
                    }
                }
            }

            Ios26LiquidContactDetailsSheet(
                contact = contact,
                callHistory = contactHistory,
                presets = presets,
                onPlayPreset = { sessionManager.playAudioPreset(it) },
                onDismiss = { selectedContactForDetails = null },
                onCall = { num ->
                    DialerRepository.placeCall(context, num)
                    selectedContactForDetails = null
                },
                onScreen = { num, name ->
                    sessionManager.startSimulatedCall(
                        simulatedNumber = num,
                        simulatedName = name
                    )
                    context.startActivity(Intent(context, ScreeningCallActivity::class.java))
                    selectedContactForDetails = null
                }
            )
        }

        // Dedicated Telephony & System Settings Sheet (Opened via 3-dots in Keypad header)
        if (isSettingsOpen) {
            DialerSettingsSheet(
                isDefaultDialer = isDefaultDialer,
                onRequestDefaultDialer = onRequestDefaultDialer,
                operationMode = sessionState.operationMode,
                onSelectMode = { sessionManager.setOperationMode(it) },
                autoScreenUnknown = autoScreenUnknown,
                onToggleAutoScreen = { autoScreenUnknown = it },
                isHideNumberOnCallerId = sessionState.isHideNumberOnCallerId,
                onToggleHideNumber = { sessionManager.setHideNumberEnabled(it) },
                isAutoRecordEnabled = sessionState.isAutoRecordEnabled,
                onToggleAutoRecord = { sessionManager.setAutoRecordEnabled(it) },
                onDismiss = { isSettingsOpen = false }
            )
        }

        // Dedicated Presets & Quick Replies Management Screen (Reorder, Align, Edit, Delete)
        if (isPresetManagerOpen) {
            PresetManagementScreen(
                presetManager = sessionManager.presetManager,
                quickReplyManager = sessionManager.quickReplyManager,
                onDismiss = { isPresetManagerOpen = false }
            )
        }
    }
}

/**
 * Authentic Apple Liquid Chromatic Backdrop:
 * Deep obsidian OLED black space with vibrant liquid metallic caustics sweeping directly across
 * the bottom dock region (inspired by deepika-builds/liquid-glass & Apple iOS 26 liquid glass).
 * Provides rich chromatic light for the dock's backdrop-filter blur (hazeChild) to refract.
 */
@Composable
fun AppleLiquidBackdrop() {
    val infiniteTransition = rememberInfiniteTransition(label = "liquidCaustic")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "causticShift"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Pure Obsidian OLED Black Base
        drawRect(color = Color(0xFF000000))

        // 2. Subtle Top Status Bar Titanium Sheen
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF161C26),
                    Color(0xFF0B0E14),
                    Color(0xFF000000)
                ),
                startY = 0f,
                endY = height * 0.30f
            )
        )

        // 3. Ethereal Top Radial Titanium Radiance
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x18202E42),
                    Color(0x06121C2A),
                    Color.Transparent
                ),
                center = Offset(width * 0.5f, height * 0.10f),
                radius = width * 0.90f
            )
        )

        // 4. Vibrant Liquid Caustics directly behind dock zone (for liquid glass backdrop-filter blur)
        // Left: Rich Molten Amber/Bronze Caustic
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xCCE67E22), // Vibrant molten amber
                    Color(0x88D35400),
                    Color(0x25A04000),
                    Color.Transparent
                ),
                center = Offset(width * 0.20f + waveOffset, height * 0.94f),
                radius = width * 0.55f
            )
        )

        // Center: Luminous Electric Cyan / Platinum Specular Caustic
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xBB00E5FF), // Electric cyan
                    Color(0x5500B0FF),
                    Color.Transparent
                ),
                center = Offset(width * 0.50f, height * 0.93f - (waveOffset * 0.4f)),
                radius = width * 0.42f
            )
        )

        // Right: Rich Royal Sapphire / Electric Violet Caustic
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xCC3B82F6), // Royal sapphire
                    Color(0x886366F1), // Electric indigo
                    Color(0x221E1B4B),
                    Color.Transparent
                ),
                center = Offset(width * 0.82f - waveOffset, height * 0.94f),
                radius = width * 0.55f
            )
        )

        // Luminous liquid caustics ribbon curving across the dock line
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    Color(0x50E67E22),
                    Color(0x7000E5FF),
                    Color(0x603B82F6),
                    Color.Transparent
                )
            ),
            start = Offset(0f, height * 0.91f),
            end = Offset(width, height * 0.92f),
            strokeWidth = 3f
        )
    }
}

/**
 * Authentic Apple Liquid Glass Dock (Engineered after deepika-builds/liquid-glass):
 * Uses the proven Apple liquid glass formula:
 * - Real-time GPU backdrop blur (hazeChild blur = 24.dp, ultra-clear optical transmission)
 * - CSS Dressing recipe:
 *     background: linear-gradient(180deg, rgba(255, 255, 255, 0.14) 0%, rgba(255, 255, 255, 0.03) 100%)
 *     box-shadow: 0 24px 60px rgba(0,0,0,0.45)
 *     inset 0 1.5px 1px rgba(255,255,255,0.65) (specular top highlight)
 *     inset 0 -1px 1px rgba(255,255,255,0.25) (bottom reflection)
 *     inset 0 0 24px rgba(255,255,255,0.08) (ambient inner luminescence)
 *     border: 1px solid rgba(255,255,255,0.22)
 * - Refraction bulge rim with subtle chromatic fringe (chroma dispersion)
 * - Clean, minimalist luxury tabs & sliding liquid droplet indicator
 */
@Composable
fun LiquidGlassBottomBar(
    currentTab: DialerTab,
    onTabSelected: (DialerTab) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val tabs = remember {
        listOf(
            Triple(DialerTab.RECENTS, Icons.Rounded.Schedule, "Recents"),
            Triple(DialerTab.CONTACTS, Icons.Rounded.Person, "Contacts"),
            Triple(DialerTab.KEYPAD, Icons.Rounded.Dialpad, "Keypad"),
            Triple(DialerTab.ASSISTANT, Icons.Rounded.GraphicEq, "Assistant")
        )
    }

    val selectedIndex = when (currentTab) {
        DialerTab.RECENTS -> 0
        DialerTab.CONTACTS -> 1
        DialerTab.KEYPAD -> 2
        DialerTab.ASSISTANT -> 3
    }

    // Outer Liquid Glass Capsule (deepika-builds/liquid-glass specification)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            // 1. Deep Drop Shadow: box-shadow: 0 24px 60px rgba(0, 0, 0, 0.45)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color(0x75000000),
                ambientColor = Color(0x35000000)
            )
            // 2. Real-Time Backdrop Blur: backdrop-filter: blur(24px) saturate(180%)
            .hazeChild(
                state = hazeState,
                shape = RoundedCornerShape(32.dp),
                style = HazeStyle(
                    tint = Color(0x0CFFFFFF), // Ultra-clear optical transmission
                    blurRadius = 24.dp,
                    noiseFactor = 0f
                )
            )
            .clip(RoundedCornerShape(32.dp))
            // 3. Liquid Glass Body: background: linear-gradient(180deg, rgba(255, 255, 255, 0.14), rgba(255, 255, 255, 0.03))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0x24FFFFFF), // 14% white top sheen
                        Color(0x08FFFFFF)  // 3% white optical body
                    )
                )
            )
            // 4. Glass Structural Border: border: 1px solid rgba(255, 255, 255, 0.22)
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0x85FFFFFF), // Razor-sharp top specular catch
                        Color(0x35FFFFFF), // Translucent sides
                        Color(0x18FFFFFF),
                        Color(0x45FFFFFF)  // Bottom caustic return rim
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        // Inset Layer 1: Ambient Inner Glass Luminescence (inset 0 0 24px rgba(255, 255, 255, 0.08))
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(1.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x14FFFFFF),
                            Color.Transparent
                        )
                    )
                )
        )

        // Inset Layer 2: Chromatic Refraction Fringe along the rim (deepika-builds/liquid-glass chroma simulation)
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(1.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0x1500E5FF), // Subtle cyan dispersion
                            Color.Transparent,
                            Color(0x15FF007F)  // Subtle magenta dispersion
                        )
                    ),
                    shape = RoundedCornerShape(31.dp)
                )
        )

        // Inset Layer 3: Razor-Sharp Specular Top Highlight (inset 0 1.5px 1px rgba(255, 255, 255, 0.65))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x90FFFFFF),
                            Color(0x25FFFFFF),
                            Color.Transparent
                        )
                    )
                )
        )

        // Inset Layer 4: Specular Bottom Return Rim (inset 0 -1px 1px rgba(255, 255, 255, 0.25))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Color(0x40FFFFFF))
        )

        // Interactive Layer: Sliding Liquid Droplet Lens + Clean Minimalist Tab Icons
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            val totalWidth = maxWidth
            val tabWidth = totalWidth / 4

            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "dockIndicatorOffset"
            )

            // Ultra-Clear Liquid Glass Sliding Active Droplet Lens
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color(0x50000000),
                        ambientColor = Color(0x20000000)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x40FFFFFF), // Specular crown
                                Color(0x12FFFFFF), // Crystalline body
                                Color(0x25FFFFFF)  // Base caustic return
                            )
                        )
                    )
                    .border(
                        1.2.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color(0xB5FFFFFF), // Razor top catch
                                Color(0x35FFFFFF),
                                Color(0x15FFFFFF)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                // Specular Light Dome on the Sliding Droplet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x50FFFFFF),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Clean Minimalist Tab Icons & Text centered in equal 4ths with spring bounce
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { (tab, icon, label) ->
                    val isSelected = currentTab == tab
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 0.94f,
                        animationSpec = spring(dampingRatio = 0.72f),
                        label = "tabIconScale"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else Color(0x80FFFFFF),
                        animationSpec = tween(durationMillis = 180),
                        label = "tabContentColor"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTabSelected(tab)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = contentColor,
                                modifier = Modifier
                                    .size(21.dp)
                                    .scale(iconScale)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = label,
                                color = contentColor,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                letterSpacing = (-0.1).sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen 1: Recents (Clean OEM Frosted Glass Call History, Edge-to-Edge Status Bar Scroll)
 */
@Composable
fun RecentsScreen(
    callLogs: List<CallLogItem>,
    isLoading: Boolean,
    presets: List<AudioPreset> = emptyList(),
    onPlayPreset: (AudioPreset) -> Unit = {},
    onCall: (String) -> Unit,
    onScreen: (CallLogItem) -> Unit = {},
    onOpenDetails: (ContactItem) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedLogForDetails by remember { mutableStateOf<CallLogItem?>(null) }

    val filteredLogs = remember(callLogs, selectedFilter) {
        if (selectedFilter == "Missed") {
            callLogs.filter { it.type == CallLog.Calls.MISSED_TYPE || it.type == CallLog.Calls.REJECTED_TYPE }
        } else {
            callLogs
        }
    }

    val groupedLogs = remember(filteredLogs) {
        filteredLogs.groupBy { getCallDateGroup(it.timestamp) }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && callLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Loading call logs...", color = TextSecondary, fontSize = 14.sp)
            }
        } else if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0x14FFFFFF))
                            .border(1.dp, Color(0x25FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedFilter == "Missed") "No Missed Calls" else "No Recent Calls",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Call history will appear here",
                        color = TextTertiary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            // LazyColumn extends edge-to-edge; items scroll fluidly underneath the transparent status bar!
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = statusBarTop + 68.dp, bottom = 120.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedLogs.forEach { (dateGroup, logsInGroup) ->
                    item(key = "header_$dateGroup") {
                        Text(
                            text = dateGroup,
                            color = TextTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp, start = 4.dp)
                        )
                    }

                    items(logsInGroup, key = { it.id }) { log ->
                        val isMissed = log.type == CallLog.Calls.MISSED_TYPE || log.type == CallLog.Calls.REJECTED_TYPE
                        val isOutgoing = log.type == CallLog.Calls.OUTGOING_TYPE

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0x14FFFFFF))
                                .border(0.8.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp))
                                .clickable { onCall(log.number) }
                                .padding(horizontal = 14.dp, vertical = 11.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Direction Icon in subtle frosted glass circle
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x18FFFFFF))
                                        .border(0.8.dp, Color(0x28FFFFFF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isMissed -> Icons.Filled.CallMissed
                                            isOutgoing -> Icons.Filled.CallMade
                                            else -> Icons.Filled.CallReceived
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            isMissed -> AppleRed
                                            isOutgoing -> Color(0xCCFFFFFF)
                                            else -> AppleGreen
                                        },
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Caller info
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = log.name ?: log.number,
                                        color = if (isMissed) AppleRed else Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (log.name != null) DialerRepository.formatPhoneNumber(log.number) else "Mobile",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = formatCallDate(log.timestamp),
                                        color = TextTertiary,
                                        fontSize = 12.sp
                                    )

                                    // Info Sheet Button (i) -> Opens iOS 26 Details
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x1AFFFFFF))
                                            .border(0.8.dp, Color(0x30FFFFFF), CircleShape)
                                            .clickable {
                                                val contact = ContactItem(
                                                    id = log.id,
                                                    name = log.name ?: log.number,
                                                    number = log.number
                                                )
                                                onOpenDetails(contact)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Info,
                                            contentDescription = "Details",
                                            tint = AppleBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pinned Top Header with Segmented Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarTop + 6.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recents",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            // Minimalist Monochromatic Frosted Capsule: All | Missed
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x18FFFFFF))
                    .border(0.8.dp, Color(0x30FFFFFF), RoundedCornerShape(18.dp))
                    .padding(3.dp)
            ) {
                Row {
                    listOf("All", "Missed").forEach { filter ->
                        val isSel = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(15.dp))
                                .background(if (isSel) Color(0x35FFFFFF) else Color.Transparent)
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (isSel) Color.White else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }

    // Call Log Details Dialog
    selectedLogForDetails?.let { log ->
        CallLogDetailsDialog(
            log = log,
            presets = presets,
            onPlayPreset = onPlayPreset,
            onDismiss = { selectedLogForDetails = null },
            onCall = onCall,
            onScreen = onScreen
        )
    }
}

/**
 * iOS 26 Full Liquid Glass Specular Contact Details Modal Sheet (Minimalist Monochrome Luxury)
 */
@Composable
fun Ios26LiquidContactDetailsSheet(
    contact: ContactItem,
    callHistory: List<CallLogItem> = emptyList(),
    presets: List<AudioPreset> = emptyList(),
    onPlayPreset: (AudioPreset) -> Unit = {},
    onDismiss: () -> Unit,
    onCall: (String) -> Unit,
    onScreen: (String, String) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val formattedNum = remember(contact.number) {
        DialerRepository.formatPhoneNumber(contact.number)
    }

    val initials = remember(contact.name) {
        val parts = contact.name.trim().split(" ").filter { it.isNotBlank() }
        if (parts.size >= 2) {
            "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        } else {
            contact.name.take(2).uppercase()
        }
    }

    var selectedPersona by remember { mutableStateOf(VoicePersona.DEFAULT) }
    var autoScreenThisContact by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x95000000))
                .clickable { onDismiss() }
                .navigationBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .shadow(
                        elevation = 40.dp,
                        shape = RoundedCornerShape(topStart = 44.dp, topEnd = 44.dp),
                        spotColor = Color(0xD0000000),
                        ambientColor = Color(0x99000000)
                    )
                    .clip(RoundedCornerShape(topStart = 44.dp, topEnd = 44.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xF2121722),
                                Color(0xF80A0E16),
                                Color(0xFF04060A)
                            )
                        )
                    )
                    .border(
                        1.2.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color(0x80FFFFFF),
                                Color(0x20FFFFFF),
                                Color(0x06FFFFFF)
                            )
                        ),
                        RoundedCornerShape(topStart = 44.dp, topEnd = 44.dp)
                    )
                    .padding(horizontal = 22.dp, vertical = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Grabber handle
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color(0x40FFFFFF))
                    )

                    // Top Bar (ColorOS / MIUI Minimalist Bar: Close, Title, Share)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x1EFFFFFF))
                                .border(0.8.dp, Color(0x30FFFFFF), CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text("Contact Info", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x1EFFFFFF))
                                .border(0.8.dp, Color(0x30FFFFFF), CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, contact.name)
                                        putExtra(Intent.EXTRA_TEXT, "${contact.name}: ${contact.number}")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Contact"))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Hero Avatar (ColorOS / MIUI Refined 84dp frosted titanium glass orb)
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                            .border(1.2.dp, Color(0x40FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Display Name & Verified Status Badge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = contact.name,
                            color = Color.White,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.4).sp
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x14FFFFFF))
                                .border(0.8.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AppleGreen)
                            )
                            Text(
                                text = "Mobile • Carrier Verified",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // 4 ColorOS / MIUI Minimalist Frosted Action Orbs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ContactHeroActionPill(
                            icon = Icons.Filled.Call,
                            label = "Call",
                            accentColor = AppleGreen,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onCall(contact.number)
                            }
                        )

                        ContactHeroActionPill(
                            icon = Icons.Rounded.Chat,
                            label = "Message",
                            accentColor = Color.White,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                DialerRepository.sendSms(context, contact.number)
                            }
                        )

                        ContactHeroActionPill(
                            icon = Icons.Rounded.Chat,
                            label = "WhatsApp",
                            accentColor = AppleGreen,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val digits = contact.number.filter { it.isDigit() }
                                val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits"))
                                try { context.startActivity(waIntent) } catch (_: Exception) {}
                            }
                        )

                        ContactHeroActionPill(
                            icon = Icons.Rounded.Security,
                            label = "Screen AI",
                            accentColor = Color.White,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onScreen(contact.number, contact.name)
                            }
                        )
                    }

                    // Card 1: Primary Phone Number (Clean ColorOS Card)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x14FFFFFF))
                            .border(0.8.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("MOBILE", color = TextTertiary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formattedNum,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x1CFFFFFF))
                                        .border(0.8.dp, Color(0x30FFFFFF), CircleShape)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            DialerRepository.copyToClipboard(context, "Phone Number", contact.number)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(16.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AppleGreen)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onCall(contact.number)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Card 2: AI Voice Screening Policy
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x14FFFFFF))
                            .border(0.8.dp, Color(0x25FFFFFF), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Always Screen This Caller", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text("Assistant intercepts before phone rings", color = TextSecondary, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = autoScreenThisContact,
                                    onCheckedChange = { autoScreenThisContact = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = AppleGreen,
                                        uncheckedThumbColor = Color(0xFF8E8E93),
                                        uncheckedTrackColor = Color(0x30FFFFFF)
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Screening Persona", color = TextSecondary, fontSize = 12.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x22FFFFFF))
                                        .border(0.8.dp, Color(0x35FFFFFF), RoundedCornerShape(10.dp))
                                        .clickable {
                                            val all = VoicePersona.ALL_PERSONAS
                                            val nextIdx = (all.indexOf(selectedPersona) + 1) % all.size
                                            selectedPersona = all[nextIdx]
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${selectedPersona.name} (${selectedPersona.code})",
                                        color = AppleGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Card 3: Recent Call History with this contact
                    if (callHistory.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0x14FFFFFF))
                                .border(0.8.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Call History", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${callHistory.size} records", color = TextTertiary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                                callHistory.take(4).forEach { log ->
                                    val isMissed = log.type == CallLog.Calls.MISSED_TYPE || log.type == CallLog.Calls.REJECTED_TYPE
                                    val isOut = log.type == CallLog.Calls.OUTGOING_TYPE
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(
                                                imageVector = if (isMissed) Icons.Filled.CallMissed else if (isOut) Icons.Filled.CallMade else Icons.Filled.CallReceived,
                                                contentDescription = null,
                                                tint = if (isMissed) AppleRed else if (isOut) TextSecondary else AppleGreen,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = if (isMissed) "Missed" else if (isOut) "Outgoing (${formatDuration(log.durationSeconds)})" else "Incoming (${formatDuration(log.durationSeconds)})",
                                                color = if (isMissed) AppleRed else TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Text(formatCallDate(log.timestamp), color = TextTertiary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Card 4: Quick Utilities (WhatsApp, Block)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x14FFFFFF))
                            .border(0.8.dp, Color(0x25FFFFFF), RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val digits = contact.number.filter { it.isDigit() }
                                        val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits"))
                                        try { context.startActivity(waIntent) } catch (_: Exception) {}
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Send WhatsApp Message", color = AppleGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Icon(Icons.Rounded.Chat, contentDescription = null, tint = AppleGreen, modifier = Modifier.size(14.dp))
                            }

                            Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(Color(0x18FFFFFF)))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        Toast.makeText(context, "${contact.name} added to Auto-Screen blocklist", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Block & Auto-Screen This Number", color = AppleRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = AppleRed, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun ContactHeroActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0x1EFFFFFF))
                .border(1.dp, Color(0x35FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = accentColor, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CallLogDetailsDialog(
    log: CallLogItem,
    presets: List<AudioPreset> = emptyList(),
    onPlayPreset: (AudioPreset) -> Unit = {},
    onDismiss: () -> Unit,
    onCall: (String) -> Unit,
    onScreen: (CallLogItem) -> Unit
) {
    val contact = remember(log) {
        ContactItem(id = log.id, name = log.name ?: log.number, number = log.number)
    }
    Ios26LiquidContactDetailsSheet(
        contact = contact,
        callHistory = listOf(log),
        presets = presets,
        onPlayPreset = onPlayPreset,
        onDismiss = onDismiss,
        onCall = onCall,
        onScreen = { _, _ -> onScreen(log) }
    )
}

/**
 * Screen 2: Contacts (Searchable, Alphabetical Indexing, Clean Minimal Frosted Glass Rows)
 */
@Composable
fun ContactsScreen(
    contacts: List<ContactItem>,
    isLoading: Boolean,
    onCall: (String) -> Unit,
    onOpenDetails: (ContactItem) -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) {
            contacts
        } else {
            contacts.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.number.contains(searchQuery)
            }
        }
    }

    val groupedContacts = remember(filteredContacts) {
        filteredContacts.groupBy { contact ->
            val firstChar = contact.name.trim().firstOrNull()?.uppercaseChar() ?: '#'
            if (firstChar in 'A'..'Z') firstChar.toString() else "#"
        }.toSortedMap { a, b ->
            if (a == "#") 1 else if (b == "#") -1 else a.compareTo(b)
        }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && contacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Loading contacts...", color = TextSecondary, fontSize = 14.sp)
            }
        } else if (filteredContacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0x14FFFFFF))
                            .border(1.dp, Color(0x25FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No Matching Contacts" else "No Contacts Found",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Allow Contacts permission to view phonebook",
                        color = TextTertiary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            // LazyColumn extends edge-to-edge; items scroll fluidly underneath the transparent status bar!
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = statusBarTop + 116.dp, bottom = 120.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedContacts.forEach { (sectionKey, contactsInSection) ->
                    item(key = "section_$sectionKey") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 4.dp, start = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sectionKey,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .height(0.6.dp)
                                    .weight(1f)
                                    .background(Color(0x18FFFFFF))
                            )
                        }
                    }

                    items(contactsInSection, key = { it.id }) { contact ->
                        val initials = remember(contact.name) {
                            val parts = contact.name.trim().split(" ").filter { it.isNotBlank() }
                            if (parts.size >= 2) {
                                "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                            } else {
                                contact.name.take(2).uppercase()
                            }
                        }

                        // Minimalist, uniform frosted glass contact row
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0x14FFFFFF))
                                .border(0.8.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp))
                                .clickable { onOpenDetails(contact) }
                                .padding(horizontal = 14.dp, vertical = 11.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Frosted titanium glass initials avatar (clean monochrome)
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x26FFFFFF))
                                        .border(0.8.dp, Color(0x40FFFFFF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contact.name,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = DialerRepository.formatPhoneNumber(contact.number),
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Send Message Button
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x1EFFFFFF))
                                            .border(0.8.dp, Color(0x35FFFFFF), CircleShape)
                                            .clickable {
                                                DialerRepository.sendSms(context, contact.number)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Chat,
                                            contentDescription = "Message",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    // Call Button
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(AppleGreen)
                                            .clickable { onCall(contact.number) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Call,
                                            contentDescription = "Call",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pinned Top Header and Search Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarTop + 6.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contacts",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x18FFFFFF))
                        .border(0.8.dp, Color(0x30FFFFFF), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${contacts.size} Total",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Minimalist Frosted Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x18FFFFFF))
                    .border(0.8.dp, Color(0x30FFFFFF), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search contacts...",
                                    color = TextTertiary,
                                    fontSize = 15.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0x35FFFFFF))
                            .clickable { searchQuery = "" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * OEM Specular Dialpad Key Button (Authentic Minimalist Apple Glass Style, Spacious 75dp Keys)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OemDialerKeyButton(
    digit: String,
    letters: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "keyScale"
    )

    Box(
        modifier = Modifier
            .size(75.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isPressed) Color(0x35FFFFFF)
                else Color(0x18FFFFFF)
            )
            .border(
                1.dp,
                if (isPressed) Color(0x60FFFFFF) else Color(0x24FFFFFF),
                CircleShape
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                lineHeight = 30.sp
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    color = Color(0xAAFFFFFF),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp,
                    lineHeight = 11.sp
                )
            } else if (digit == "0") {
                Text(
                    text = "+",
                    color = Color(0xAAFFFFFF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 11.sp
                )
            }
        }
    }
}

/**
 * OEM Floating Liquid Call Button with Apple Green Fill & Tactile Press (75dp Pro Size)
 */
@Composable
fun OemCallButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "callBtnScale"
    )

    Box(
        modifier = Modifier
            .size(75.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(AppleGreen)
            .border(1.dp, Color(0x50FFFFFF), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Call,
            contentDescription = "Call",
            tint = Color.Black,
            modifier = Modifier.size(30.dp)
        )
    }
}

/**
 * Screen 3: Keypad (100% Rock-Solid Stable OEM Dialpad, Fixed-Height Immobile Keys, DTMF Acoustics & T9 Lookup)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeypadScreen(
    contacts: List<ContactItem> = emptyList(),
    callLogs: List<CallLogItem> = emptyList(),
    onCall: (String) -> Unit,
    onOpenDetails: (ContactItem) -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var typedNumber by remember { mutableStateOf("") }

    val toneGenerator = remember {
        try { ToneGenerator(AudioManager.STREAM_DTMF, 70) } catch (_: Exception) { null }
    }
    DisposableEffect(Unit) {
        onDispose {
            try { toneGenerator?.release() } catch (_: Exception) {}
        }
    }

    fun playTone(digit: String) {
        val tone = when (digit) {
            "1" -> ToneGenerator.TONE_DTMF_1
            "2" -> ToneGenerator.TONE_DTMF_2
            "3" -> ToneGenerator.TONE_DTMF_3
            "4" -> ToneGenerator.TONE_DTMF_4
            "5" -> ToneGenerator.TONE_DTMF_5
            "6" -> ToneGenerator.TONE_DTMF_6
            "7" -> ToneGenerator.TONE_DTMF_7
            "8" -> ToneGenerator.TONE_DTMF_8
            "9" -> ToneGenerator.TONE_DTMF_9
            "0" -> ToneGenerator.TONE_DTMF_0
            "*" -> ToneGenerator.TONE_DTMF_S
            "#" -> ToneGenerator.TONE_DTMF_P
            else -> -1
        }
        if (tone >= 0) {
            try { toneGenerator?.startTone(tone, 90) } catch (_: Exception) {}
        }
    }

    val formattedNumber = remember(typedNumber) {
        DialerRepository.formatPhoneNumber(typedNumber)
    }

    val t9Matches = remember(typedNumber, contacts) {
        if (typedNumber.isBlank()) emptyList()
        else contacts.filter { T9Helper.matches(it, typedNumber) }.take(3)
    }

    val keys = listOf(
        Pair("1", ""),
        Pair("2", "A B C"),
        Pair("3", "D E F"),
        Pair("4", "G H I"),
        Pair("5", "J K L"),
        Pair("6", "M N O"),
        Pair("7", "P Q R S"),
        Pair("8", "T U V"),
        Pair("9", "W X Y Z"),
        Pair("*", ""),
        Pair("0", "+"),
        Pair("#", "")
    )

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = statusBarTop + 14.dp)
            .navigationBarsPadding()
            .padding(bottom = 86.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ColorOS Top Header: Title & 3-Dots Settings Action with Generous Status Bar Clearance
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Phone",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
            )

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenSettings()
                },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0x18FFFFFF))
                    .border(0.8.dp, Color(0x30FFFFFF), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Top Section: Purposeful Compact Display Area (No huge empty void!)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (typedNumber.isEmpty()) {
                // Frosted Search Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x14FFFFFF))
                        .border(0.8.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Search contacts or dial number",
                        color = TextTertiary,
                        fontSize = 13.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Recents / Frequent Dial Carousel
                val recentLogs = remember(callLogs) { callLogs.distinctBy { it.number }.take(4) }
                if (recentLogs.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentLogs) { log ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x16FFFFFF))
                                    .border(0.8.dp, Color(0x26FFFFFF), RoundedCornerShape(14.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        typedNumber = log.number
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(AppleGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Schedule,
                                        contentDescription = null,
                                        tint = AppleGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = log.name ?: DialerRepository.formatPhoneNumber(log.number),
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Press & hold 0 for +, 1 for Voicemail",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                // T9 Matched Contact Capsule
                if (t9Matches.isNotEmpty()) {
                    val match = t9Matches.first()
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x18FFFFFF))
                            .border(0.8.dp, Color(0x30FFFFFF), RoundedCornerShape(14.dp))
                            .clickable { onOpenDetails(match) }
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = match.name,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = DialerRepository.formatPhoneNumber(match.number),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Dialed Number Display (Auto-scaling clean ColorOS typography)
                val textSize = when {
                    typedNumber.length > 15 -> 22.sp
                    typedNumber.length > 12 -> 26.sp
                    typedNumber.length > 9 -> 32.sp
                    else -> 38.sp
                }

                Text(
                    text = formattedNumber,
                    color = Color.White,
                    fontSize = textSize,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                type = ContactsContract.RawContacts.CONTENT_TYPE
                                putExtra(ContactsContract.Intents.Insert.PHONE, typedNumber)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        }
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PersonAdd,
                        contentDescription = null,
                        tint = AppleBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Add to Contacts",
                        color = AppleBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Keypad Grid: 100% FIXED-HEIGHT, IMMOBILE, ZERO-JUMP DIALPAD (Spacious 75dp Keys)
        Column(
            modifier = Modifier.wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in 0..3) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val (digit, letters) = keys[index]

                        OemDialerKeyButton(
                            digit = digit,
                            letters = letters,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                playTone(digit)
                                typedNumber += digit
                            },
                            onLongClick = {
                                if (digit == "0") {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    playTone(digit)
                                    typedNumber += "+"
                                }
                            }
                        )
                    }
                }
            }

            // 5th Row: Symmetrical Call & Backspace Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Invisible 75dp placeholder for perfect geometric centering
                Box(modifier = Modifier.size(75.dp))

                // Column 2: Green Call Button (Rock-solid directly under '0' key)
                OemCallButton(
                    onClick = {
                        if (typedNumber.isNotBlank()) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCall(typedNumber)
                        } else {
                            val lastLog = callLogs.firstOrNull()
                            if (lastLog != null) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                typedNumber = lastLog.number
                            }
                        }
                    }
                )

                // Column 3: Backspace Button in 75dp slot (never causes dialpad to shift!)
                Box(
                    modifier = Modifier.size(75.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (typedNumber.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .combinedClickable(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        typedNumber = typedNumber.dropLast(1)
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        typedNumber = ""
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Backspace,
                                contentDescription = "Delete",
                                tint = Color(0xCCFFFFFF),
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen 4: Assistant & Audio Studio (AI Voice Personas, Voice Relay, Audio Presets & Simulator)
 */
@Composable
fun AssistantTabScreen(
    onStartSimulation: () -> Unit,
    selectedPersona: VoicePersona,
    onSelectPersona: (VoicePersona) -> Unit,
    isLiveMicActive: Boolean,
    onToggleLiveMic: () -> Unit,
    isGreetingEnabled: Boolean,
    onToggleGreeting: (Boolean) -> Unit,
    presetManager: AudioPresetManager? = null,
    onOpenPresetManager: () -> Unit = {}
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = statusBarTop + 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header: Minimalist Luxury Glass
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color(0x16FFFFFF))
                    .border(0.8.dp, Color(0x30FFFFFF), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Assistant",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Neural speech synthesis, voice personas & soundboard",
                    color = Color(0x99FFFFFF),
                    fontSize = 12.5.sp
                )
            }
        }

        // Section 1: Real-Time Voice Studio & Live DSP
        SettingsSectionHeader(title = "VOICE STUDIO")
        GlassVoiceStudioCard(
            selectedPersona = selectedPersona,
            onSelectPersona = onSelectPersona,
            isLiveMicActive = isLiveMicActive,
            onToggleLiveMic = onToggleLiveMic
        )

        // Section 2: Audio Presets & Direct Hardware Uplink Soundboard
        presetManager?.let { pm ->
            SettingsSectionHeader(title = "SOUNDBOARD & PRESETS")
            GlassAudioPresetsCard(
                presetManager = pm,
                onOpenManager = onOpenPresetManager
            )
        }

        // Section 3: Call Screening & Simulation
        SettingsSectionHeader(title = "SCREENING BEHAVIOR & SIMULATOR")
        SettingsGroupCard {
            // Row 1: Automated Voice Greeting
            SettingsItemRow(
                icon = Icons.Rounded.RecordVoiceOver,
                iconTint = Color.White,
                iconBgColor = Color(0x18FFFFFF),
                title = "Automated Voice Greeting",
                subtitle = if (isGreetingEnabled)
                    "Speaks automated assistant greeting on call pickup"
                else
                    "Silent pickup: ready for speech relay without intro",
                trailingContent = {
                    Switch(
                        checked = isGreetingEnabled,
                        onCheckedChange = onToggleGreeting,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0x50FFFFFF),
                            uncheckedThumbColor = Color(0x80FFFFFF),
                            uncheckedTrackColor = Color(0x18FFFFFF)
                        )
                    )
                }
            )

            SettingsDivider()

            // Row 2: Call Screener Simulator
            SettingsItemRow(
                icon = Icons.Rounded.Headphones,
                iconTint = Color.White,
                iconBgColor = Color(0x18FFFFFF),
                title = "Call Screener Simulator",
                subtitle = "Test speech relay with Bluetooth earbuds or speaker",
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x22FFFFFF))
                            .border(0.8.dp, Color(0x35FFFFFF), RoundedCornerShape(10.dp))
                            .clickable { onStartSimulation() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PhoneInTalk,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Launch",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * iOS Liquid Specular Card: Operating Mode Switcher (Standard OEM vs Root-Enhanced)
 */
@Composable
fun GlassOperationModeCard(
    currentMode: AppOperationMode,
    onSelectMode: (AppOperationMode) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isCheckingRoot by remember { mutableStateOf(false) }
    var rootAvailable by remember { mutableStateOf(RootPrivilegeManager.isRootAvailable()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x60000000), ambientColor = Color(0x40000000))
            .clip(RoundedCornerShape(26.dp))
            .background(LiquidGlassFill)
            .border(
                1.2.dp,
                if (currentMode == AppOperationMode.ROOT_ENHANCED)
                    Brush.verticalGradient(listOf(AppleAmber.copy(alpha = 0.8f), ApplePurple.copy(alpha = 0.4f)))
                else
                    LiquidGlassBorder,
                RoundedCornerShape(26.dp)
            )
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row: Icon + Title + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentMode == AppOperationMode.ROOT_ENHANCED) AppleAmber.copy(alpha = 0.25f)
                                else AppleBlue.copy(alpha = 0.25f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentMode == AppOperationMode.ROOT_ENHANCED) Icons.Rounded.Security else Icons.Filled.Call,
                            contentDescription = null,
                            tint = if (currentMode == AppOperationMode.ROOT_ENHANCED) AppleAmber else AppleBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Dialer Profile & Privileges",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = if (currentMode == AppOperationMode.ROOT_ENHANCED) "Root Superpowers Active" else "Standard Non-Root Profile",
                            color = if (currentMode == AppOperationMode.ROOT_ENHANCED) AppleAmber else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Status Badge Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (currentMode == AppOperationMode.ROOT_ENHANCED)
                                (if (rootAvailable) AppleGreen.copy(alpha = 0.2f) else AppleAmber.copy(alpha = 0.2f))
                            else
                                AppleBlue.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (currentMode == AppOperationMode.ROOT_ENHANCED)
                            (if (rootAvailable) "ROOT READY" else "SU NEEDED")
                        else
                            "STANDARD",
                        color = if (currentMode == AppOperationMode.ROOT_ENHANCED)
                            (if (rootAvailable) AppleGreen else AppleAmber)
                        else
                            AppleBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Segmented Liquid Glass Mode Selector Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LiquidGlassFillSubtle)
                    .border(1.dp, LiquidGlassBorderSubtle, RoundedCornerShape(16.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Segment 1: Standard (Non-Root)
                val isStandard = currentMode == AppOperationMode.STANDARD
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (isStandard) AppleBlueGlassFill else SolidColor(Color.Transparent))
                        .clickable { onSelectMode(AppOperationMode.STANDARD) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Standard (Non-Root)",
                        color = if (isStandard) TextPrimary else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isStandard) FontWeight.Bold else FontWeight.Medium
                    )
                }

                // Segment 2: Root Enhanced
                val isRoot = currentMode == AppOperationMode.ROOT_ENHANCED
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (isRoot) AppleAmberGlassFill else SolidColor(Color.Transparent))
                        .clickable {
                            onSelectMode(AppOperationMode.ROOT_ENHANCED)
                            coroutineScope.launch {
                                rootAvailable = RootPrivilegeManager.isRootAvailable()
                            }
                        }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Root Enhanced",
                        color = if (isRoot) TextPrimary else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isRoot) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // Mode Details Description & Capability List
            if (currentMode == AppOperationMode.STANDARD) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Standard Mode is built for normal everyday calling on any Android phone. Calls use your natural microphone directly with zero root modifications, zero risk, and a clutter-free interface.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    // Features checklist
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LiquidGlassFillSubtle)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ModeFeatureRow("Natural Direct Microphone Phone Calls", true)
                        ModeFeatureRow("Tactile T9 Keypad & Contact Book", true)
                        ModeFeatureRow("Speakerphone Call Screening & AI Replies", true)
                        ModeFeatureRow("Standard Microphone Call Recording", true)
                        ModeFeatureRow("Root / Magisk / KernelSU Superpowers", false)
                    }

                    if (rootAvailable) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppleGreen.copy(alpha = 0.12f))
                                .border(0.8.dp, AppleGreen.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Root (su) detected on this device. Switch to 'Root Enhanced' above anytime to unlock dual-side ALSA recording & live voice changer.",
                                color = AppleGreen,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            } else {
                // ROOT_ENHANCED Details
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Root Enhanced Mode unlocks low-level hardware mixer control, hardware ALSA dual-side call recording, Qualcomm Audio HAL direct line injection, and real-time DSP Voice Changer.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    // Features checklist
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LiquidGlassFillSubtle)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ModeFeatureRow("Hardware ALSA Dual-Side Call Recording (Crystal Clear Both Sides)", true)
                        ModeFeatureRow("Audio HAL Cellular Line Injection (Zero Loudspeaker Noise)", true)
                        ModeFeatureRow("Live DSP Voice Changer (Microphone Pitch Shift)", true)
                        ModeFeatureRow("Magisk / KernelSU / APatch /system/priv-app Module", true)
                    }

                    // Root Check Action Button
                    if (!rootAvailable) {
                        Button(
                            onClick = {
                                isCheckingRoot = true
                                coroutineScope.launch {
                                    val available = RootPrivilegeManager.isRootAvailable()
                                    rootAvailable = available
                                    isCheckingRoot = false
                                    val msg = if (available) "Root privilege granted successfully!" else "Root (su) not detected or denied. Ensure root manager is configured."
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isCheckingRoot,
                            colors = ButtonDefaults.buttonColors(containerColor = AppleAmber),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, LiquidGlassBorder, RoundedCornerShape(14.dp))
                        ) {
                            Text(
                                text = if (isCheckingRoot) "Checking su..." else "Verify / Grant Root Access",
                                color = Color(0xFF0F172A),
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

@Composable
fun ModeFeatureRow(text: String, isIncluded: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isIncluded) Icons.Filled.CheckCircle else Icons.Filled.Clear,
            contentDescription = null,
            tint = if (isIncluded) AppleGreen else TextMuted,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = if (isIncluded) TextPrimary else TextMuted,
            fontSize = 11.sp,
            fontWeight = if (isIncluded) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/**
 * iOS Frosted Glass Card: Default Phone App
 */
@Composable
fun GlassDefaultAppCard(
    isDefaultDialer: Boolean,
    onRequestDefaultDialer: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x60000000), ambientColor = Color(0x40000000))
            .clip(RoundedCornerShape(26.dp))
            .background(LiquidGlassFill)
            .border(1.2.dp, LiquidGlassBorder, RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isDefaultDialer) AppleGreen.copy(alpha = 0.25f) else AppleAmber.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDefaultDialer) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (isDefaultDialer) AppleGreen else AppleAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isDefaultDialer) "Default Phone App Active" else "Default Dialer Required",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = if (isDefaultDialer)
                            "Telecom in-call subsystem bound and operational"
                        else
                            "Required by Android to intercept and screen incoming carrier calls",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            if (!isDefaultDialer) {
                Button(
                    onClick = onRequestDefaultDialer,
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LiquidGlassBorder, RoundedCornerShape(16.dp))
                ) {
                    Text("Set as Default Phone App", color = Color.White, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error opening app settings", e)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LiquidGlassBorderSubtle, RoundedCornerShape(16.dp))
                ) {
                    Text("Open App System Settings", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * iOS Frosted Glass Card: Root & System Priv-App Uplink
 */
@Composable
fun GlassRootPowerCard() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRootAvailable by remember { mutableStateOf(RootPrivilegeManager.isRootAvailable()) }
    var isPrivileged by remember { mutableStateOf(RootPrivilegeManager.isPrivilegedApp(context)) }
    var isModuleInstalled by remember { mutableStateOf(RootPrivilegeManager.isMagiskModuleInstalled()) }
    var installStatus by remember { mutableStateOf<String?>(null) }
    var isInstalling by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x60000000), ambientColor = Color(0x40000000))
            .clip(RoundedCornerShape(26.dp))
            .background(LiquidGlassFill)
            .border(1.2.dp, LiquidGlassBorder, RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPrivileged) AppleGreen.copy(alpha = 0.25f)
                            else if (isModuleInstalled) AppleAmber.copy(alpha = 0.25f)
                            else AppleBlue.copy(alpha = 0.25f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        tint = if (isPrivileged) AppleGreen else if (isModuleInstalled) AppleAmber else AppleTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    val titleText = when {
                        isPrivileged -> "System Priv-App Mode: Active"
                        isModuleInstalled -> "Magisk Module Installed (Reboot Required)"
                        else -> "Root Priv-App Architecture"
                    }
                    val titleColor = when {
                        isPrivileged -> AppleGreen
                        isModuleInstalled -> AppleAmber
                        else -> AppleTeal
                    }

                    Text(
                        text = titleText,
                        color = titleColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = if (isPrivileged)
                            "Running as OEM privileged app. Zero loudspeaker noise — digital audio injects directly into cellular line."
                        else if (isModuleInstalled)
                            "Module installed in /data/adb/modules. Please restart device to complete system mounting."
                        else
                            "Enables silent in-call digital audio injection and real-time DSP routing.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            if (isPrivileged) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(LiquidGlassFillSubtle)
                        .border(1.dp, AppleGreen.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "System privileged app status verified with CAPTURE_AUDIO_OUTPUT & MODIFY_PHONE_STATE permissions. Cellular audio uplink direct injection enabled.",
                        color = AppleGreen,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val result = RootPrivilegeManager.uninstallSystemModule()
                            installStatus = result.second
                            isModuleInstalled = RootPrivilegeManager.isMagiskModuleInstalled()
                            isPrivileged = RootPrivilegeManager.isPrivilegedApp(context)
                            Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LiquidGlassBorderSubtle, RoundedCornerShape(16.dp))
                ) {
                    Text("Uninstall System Module", fontSize = 12.sp)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(LiquidGlassFillSubtle)
                        .border(1.dp, LiquidGlassBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (isModuleInstalled)
                            "Module configuration created. Tap 'Restart Device Now' so Android system_server mounts the application into /system/priv-app."
                        else if (isRootAvailable)
                            "Root access verified. Tap below to generate a systemless Magisk/KernelSU module that places Call Screener into /system/priv-app with direct audio routing privileges."
                        else
                            "Standard Telecom mode active. Grant root access to unlock direct digital cellular injection.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                if (isRootAvailable) {
                    if (isModuleInstalled) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    RootPrivilegeManager.rebootDevice()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleAmber),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, LiquidGlassBorder, RoundedCornerShape(16.dp))
                        ) {
                            Text("Restart Device Now", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            isInstalling = true
                            coroutineScope.launch {
                                val result = RootPrivilegeManager.installSystemModule(context)
                                installStatus = result.second
                                isInstalling = false
                                isModuleInstalled = RootPrivilegeManager.isMagiskModuleInstalled()
                                isPrivileged = RootPrivilegeManager.isPrivilegedApp(context)
                                Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = !isInstalling,
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, LiquidGlassBorder, RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            text = if (isInstalling) "Configuring Module..." else if (isModuleInstalled) "Re-install / Update Module" else "Activate Priv-App Module",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isModuleInstalled) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val result = RootPrivilegeManager.uninstallSystemModule()
                                    installStatus = result.second
                                    isModuleInstalled = RootPrivilegeManager.isMagiskModuleInstalled()
                                    Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, LiquidGlassBorderSubtle, RoundedCornerShape(16.dp))
                        ) {
                            Text("Remove Module", fontSize = 12.sp)
                        }
                    }
                }
            }

            installStatus?.let { status ->
                Text(
                    text = status,
                    color = AppleTeal,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * iOS Frosted Glass Card: Call Simulator
 */
@Composable
fun GlassSimulatorCard(onStartSimulation: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x60000000), ambientColor = Color(0x40000000))
            .clip(RoundedCornerShape(26.dp))
            .background(LiquidGlassFill)
            .border(1.2.dp, LiquidGlassBorder, RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AppleIndigo.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Headphones,
                        contentDescription = null,
                        tint = AppleIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Call Screener Simulator",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Bluetooth Earbuds & Speaker Output Ready",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = "Simulates incoming call screening with speech recognition and voice synthesis. Routes audio via media stream directly into connected Bluetooth earbuds or headphones.",
                color = TextTertiary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Button(
                onClick = onStartSimulation,
                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LiquidGlassBorder, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Rounded.PhoneInTalk, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Launch Call Simulator", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Minimalist Luxury Monochromatic Voice Studio:
 * Streamlined category pill switch (Hindi & Hinglish, English Studio, Specialty Voices),
 * Monochromatic frosted glass chips with clean typography,
 * Monochromatic sample playback & speech relay mic controls with zero rainbow clutter.
 */
@Composable
fun GlassVoiceStudioCard(
    selectedPersona: VoicePersona,
    onSelectPersona: (VoicePersona) -> Unit,
    isLiveMicActive: Boolean = false,
    onToggleLiveMic: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val sessionManager = remember { CallSessionManager.getInstance(context) }
    val sessionState by sessionManager.sessionState.collectAsState()

    var activeCategoryIndex by remember { mutableStateOf(0) }
    val categories = remember { listOf("Hindi & Hinglish", "English Studio", "Specialty") }

    val currentPersonas = remember(activeCategoryIndex) {
        when (activeCategoryIndex) {
            0 -> VoicePersona.HINDI_PERSONAS
            1 -> VoicePersona.PURE_GIRL_PERSONAS
            else -> VoicePersona.CLASSIC_PERSONAS
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x60000000), ambientColor = Color(0x40000000))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x14FFFFFF))
            .border(0.8.dp, Color(0x28FFFFFF), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row: Minimalist Titanium Glass
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x18FFFFFF))
                        .border(0.8.dp, Color(0x30FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RecordVoiceOver,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "AI Voice Studio",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Select neural voice for live calls & screening",
                        color = Color(0x99FFFFFF),
                        fontSize = 11.5.sp
                    )
                }
            }

            // Monochromatic Category Segmented Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x10FFFFFF))
                    .border(0.8.dp, Color(0x20FFFFFF), RoundedCornerShape(14.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEachIndexed { idx, categoryTitle ->
                    val isTabActive = activeCategoryIndex == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (isTabActive) Color(0x28FFFFFF) else Color.Transparent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                activeCategoryIndex = idx
                            }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = categoryTitle,
                            color = if (isTabActive) Color.White else Color(0x80FFFFFF),
                            fontSize = 11.5.sp,
                            fontWeight = if (isTabActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // Horizontal Persona Carousel
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(currentPersonas, key = { it.id }) { persona ->
                    val isSelected = persona.id == selectedPersona.id
                    Box(
                        modifier = Modifier
                            .width(155.dp)
                            .shadow(
                                elevation = if (isSelected) 10.dp else 0.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = Color(0x60000000)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0x32FFFFFF) else Color(0x10FFFFFF))
                            .border(
                                width = if (isSelected) 1.2.dp else 0.8.dp,
                                color = if (isSelected) Color(0xE0FFFFFF) else Color(0x20FFFFFF),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectPersona(persona)
                                sessionManager.setVoicePersona(persona)
                            }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) Color(0x40FFFFFF) else Color(0x18FFFFFF))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = persona.code,
                                        color = Color.White,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }

                            Text(
                                text = persona.name,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = persona.tag,
                                color = Color(0x85FFFFFF),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Description Showcase of selected persona
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x0CFFFFFF))
                    .border(0.8.dp, Color(0x18FFFFFF), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "${selectedPersona.name} • ${selectedPersona.tag}: ${selectedPersona.description}",
                    color = Color(0x95FFFFFF),
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            }

            // Live AI Voice Relay Transcript Capsule
            AnimatedVisibility(visible = sessionState.isListeningToUserSpeech || sessionState.userLiveTranscript.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x20FFFFFF))
                        .border(1.dp, Color(0x60FFFFFF), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (sessionState.userLiveTranscript.isNotBlank())
                                "Recognized: \"${sessionState.userLiveTranscript}\""
                            else
                                "AI Voice Relay: Listening to microphone... Speak now",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Monochromatic Action Controls: Sample Voice & Test AI Voice Relay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1: Sample Voice
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x18FFFFFF))
                        .border(0.8.dp, Color(0x35FFFFFF), RoundedCornerShape(14.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val sampleText = when (selectedPersona.id) {
                                "hindi_pooja" -> "नमस्ते! मैं पूजा बोल रही हूँ। बताइए, क्या काम था? यह आवाज़ बिल्कुल असली लगती है।"
                                "hindi_ananya" -> "नमस्ते जी, मैं अनन्या हूँ। आपकी कॉल स्क्रीन की जा रही है, आप अपना संदेश बताइए।"
                                "hindi_kavya" -> "Hello! मैं काव्या बोल रही हूँ, how can I help you? सब कुछ एकदम natural sound कर रहा है।"
                                "hindi_aarav" -> "नमस्ते, मैं आरव बोल रहा हूँ। कृपया बताइए आपने किस सिलसिले में कॉल किया है?"
                                "hindi_vikram" -> "नमस्ते। विक्रम बात कर रहा हूँ। इस नंबर पर फालतू कॉल करना तुरंत बंद कर दें।"
                                "girl_chloe" -> "Hey! How is it going? I am Chloe. Ready to screen your call?"
                                "girl_zoe" -> "Hi there! Zoe here, everything sounds completely natural and real."
                                "girl_mia" -> "Hey, what is up? It is Mia. I am all set for your call."
                                else -> "Hello, this is ${selectedPersona.name}. How can I assist you today?"
                            }
                            Toast.makeText(context, "Playing ${selectedPersona.name} sample...", Toast.LENGTH_SHORT).show()
                            sessionManager.testSpeech(sampleText)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Sample Voice",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Button 2: Test AI Voice Relay Mic
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (sessionState.isListeningToUserSpeech) Color(0x40FFFFFF) else Color(0x14FFFFFF))
                        .border(
                            0.8.dp,
                            if (sessionState.isListeningToUserSpeech) Color.White else Color(0x28FFFFFF),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            sessionManager.toggleAiVoiceListening()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (sessionState.isListeningToUserSpeech) Icons.Filled.Mic else Icons.Filled.MicOff,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (sessionState.isListeningToUserSpeech) "Listening..." else "Test Relay Mic",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * iOS Minimalist Liquid Card: In-Call Audio Presets & Direct Hardware Uplink Soundboard
 * Clean, non-colourful, titanium glass aesthetic with direct link to the reorder/management hub.
 */
@Composable
fun GlassAudioPresetsCard(
    presetManager: AudioPresetManager,
    onOpenManager: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val presets by presetManager.presets.collectAsState()
    val currentlyPlayingId by presetManager.currentlyPlayingId.collectAsState()

    var showAddTextDialog by remember { mutableStateOf(false) }
    var textPresetTitle by remember { mutableStateOf("") }
    var textPresetPhrase by remember { mutableStateOf("") }

    var showImportDialog by remember { mutableStateOf(false) }
    var audioPresetTitle by remember { mutableStateOf("") }
    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var presetToRenameId by remember { mutableStateOf<String?>(null) }
    var renamePresetTitle by remember { mutableStateOf("") }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAudioUri = uri
            val resolvedName = runCatching {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIdx >= 0) cursor.getString(nameIdx) else null
                }
            }.getOrNull()?.substringBeforeLast(".")?.replace("_", " ")?.replace("-", " ")
            audioPresetTitle = resolvedName ?: "Custom Recording"
            showImportDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x60000000), ambientColor = Color(0x40000000))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x14FFFFFF))
            .border(0.8.dp, Color(0x28FFFFFF), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row: Minimalist Titanium Glass
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x18FFFFFF))
                            .border(0.8.dp, Color(0x30FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Audio Presets",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.3).sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x1AFFFFFF))
                                    .border(0.6.dp, Color(0x30FFFFFF), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ALSA",
                                    color = Color(0xDDFFFFFF),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Direct hardware soundboard (${presets.size} items)",
                            color = Color(0x99FFFFFF),
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Manage / Reorder Pill Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0x22FFFFFF))
                        .border(0.8.dp, Color(0x38FFFFFF), RoundedCornerShape(11.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenManager()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Manage",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Arrange",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Quick Access Preview (Top 3 Presets in Clean Monochromatic Glass)
            val previewPresets = remember(presets) { presets.take(3) }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                previewPresets.forEach { preset ->
                    val isPlaying = currentlyPlayingId == preset.id

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isPlaying) Color(0x25FFFFFF) else Color(0x0EFFFFFF))
                            .border(
                                0.8.dp,
                                if (isPlaying) Color.White else Color(0x1AFFFFFF),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x16FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (preset.isAudioFile) Icons.Rounded.GraphicEq else Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.title,
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (preset.isAudioFile) "Custom Audio Track" else preset.textOrPath,
                                    color = Color(0x75FFFFFF),
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) Color(0x40FFFFFF) else Color(0x18FFFFFF))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        if (isPlaying) {
                                            presetManager.stopPreview()
                                        } else {
                                            presetManager.previewPreset(preset)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                    contentDescription = "Preview",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Primary Hub Banner Button: Opens the dedicated Preset Management Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x14FFFFFF))
                    .border(0.8.dp, Color(0x28FFFFFF), RoundedCornerShape(14.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenManager()
                    }
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DragIndicator,
                            contentDescription = null,
                            tint = Color(0xCCFFFFFF),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Hold & Drag to Reorder (${presets.size} Presets)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Align in-call audio tracks, arrange & organize",
                                color = Color(0x80FFFFFF),
                                fontSize = 10.5.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Open",
                        tint = Color(0x80FFFFFF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Action Buttons: Add Custom Phrase & Import Audio File
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0x12FFFFFF))
                        .border(0.8.dp, Color(0x28FFFFFF), RoundedCornerShape(11.dp))
                        .clickable {
                            textPresetTitle = ""
                            textPresetPhrase = ""
                            showAddTextDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Rounded.RecordVoiceOver, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Text("Add Phrase", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0x20FFFFFF))
                        .border(0.8.dp, Color(0x38FFFFFF), RoundedCornerShape(11.dp))
                        .clickable {
                            audioPickerLauncher.launch("audio/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Text("Import Audio", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // Dialog: Add Custom Text Preset
    if (showAddTextDialog) {
        Dialog(onDismissRequest = { showAddTextDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xF0182234))
                    .border(1.2.dp, LiquidGlassBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add Custom Voice Phrase", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Assistant speaks this response directly into caller's ear with 0% background noise.", color = TextSecondary, fontSize = 11.sp)

                    BasicTextField(
                        value = textPresetTitle,
                        onValueChange = { textPresetTitle = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x25FFFFFF))
                                    .padding(12.dp)
                            ) {
                                if (textPresetTitle.isBlank()) Text("Preset Title (e.g. Can't talk)", color = TextTertiary, fontSize = 14.sp)
                                inner()
                            }
                        }
                    )

                    BasicTextField(
                        value = textPresetPhrase,
                        onValueChange = { textPresetPhrase = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x25FFFFFF))
                                    .padding(12.dp)
                            ) {
                                if (textPresetPhrase.isBlank()) Text("Phrase to speak (e.g. I am in a meeting, call back later)", color = TextTertiary, fontSize = 14.sp)
                                inner()
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showAddTextDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (textPresetPhrase.isNotBlank()) {
                                    val title = textPresetTitle.ifBlank { "Quick Response" }
                                    presetManager.addCustomTextPreset(title, textPresetPhrase)
                                    showAddTextDialog = false
                                    Toast.makeText(context, "Added preset \"$title\"", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleTeal)
                        ) {
                            Text("Save Preset")
                        }
                    }
                }
            }
        }
    }

    // Dialog: Import Audio File Preset
    if (showImportDialog && selectedAudioUri != null) {
        Dialog(onDismissRequest = { showImportDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xF0182234))
                    .border(1.2.dp, LiquidGlassBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Name Audio Preset", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("File imported for hardware uplink injection during active calls.", color = TextSecondary, fontSize = 11.sp)

                    BasicTextField(
                        value = audioPresetTitle,
                        onValueChange = { audioPresetTitle = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x25FFFFFF))
                                    .padding(12.dp)
                            ) {
                                if (audioPresetTitle.isBlank()) Text("Preset Name (e.g. Office Ambient / Ringtone)", color = TextTertiary, fontSize = 14.sp)
                                inner()
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showImportDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val uri = selectedAudioUri ?: return@Button
                                val title = audioPresetTitle.ifBlank { "Custom Audio" }
                                coroutineScope.launch {
                                    val result = presetManager.importAudioFile(title, uri)
                                    if (result.isSuccess) {
                                        Toast.makeText(context, "Imported \"$title\" successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to import audio file", Toast.LENGTH_SHORT).show()
                                    }
                                    showImportDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleAmber)
                        ) {
                            Text("Import")
                        }
                    }
                }
            }
        }
    }

    // Dialog: Rename Preset
    if (showRenameDialog && presetToRenameId != null) {
        Dialog(onDismissRequest = { showRenameDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xF0182234))
                    .border(1.2.dp, LiquidGlassBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Rename Preset", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Enter a custom title so you know which preset is playing during calls.", color = TextSecondary, fontSize = 11.5.sp)

                    BasicTextField(
                        value = renamePresetTitle,
                        onValueChange = { renamePresetTitle = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x25FFFFFF))
                                    .padding(12.dp)
                            ) {
                                if (renamePresetTitle.isBlank()) Text("Preset Name", color = TextTertiary, fontSize = 14.sp)
                                inner()
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showRenameDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val targetId = presetToRenameId
                                if (targetId != null && renamePresetTitle.isNotBlank()) {
                                    presetManager.renamePreset(targetId, renamePresetTitle)
                                    Toast.makeText(context, "Renamed to \"$renamePresetTitle\"", Toast.LENGTH_SHORT).show()
                                }
                                showRenameDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleTeal)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * iOS Liquid Specular Card: Automated Voice Greeting (Assistant Studio)
 */
@Composable
fun GlassAssistantGreetingCard(
    isGreetingEnabled: Boolean,
    onToggleGreeting: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x60000000), ambientColor = Color(0x40000000))
            .clip(RoundedCornerShape(26.dp))
            .background(LiquidGlassFill)
            .border(1.2.dp, LiquidGlassBorder, RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RecordVoiceOver,
                            contentDescription = null,
                            tint = AppleIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Automated Voice Greeting",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.3).sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isGreetingEnabled)
                            "Speaks automated assistant announcement on pickup"
                        else
                            "Instant silent pickup: ready for speech relay without robotic announcement",
                        color = if (isGreetingEnabled) AppleIndigo else TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Switch(
                    checked = isGreetingEnabled,
                    onCheckedChange = onToggleGreeting,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AppleIndigo,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Color(0x22FFFFFF)
                    )
                )
            }
        }
    }
}

/**
 * iOS Frosted Glass Card: System Telephony & Call Recording Engine
 */
@Composable
fun DialerSystemSettingsCard(
    autoScreenUnknown: Boolean,
    onToggleAutoScreen: (Boolean) -> Unit,
    isHideNumberOnCallerId: Boolean = false,
    onToggleHideNumber: (Boolean) -> Unit = {},
    isAutoRecordEnabled: Boolean = false,
    onToggleAutoRecord: (Boolean) -> Unit = {}
) {
    var showRecordingsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x60000000), ambientColor = Color(0x40000000))
            .clip(RoundedCornerShape(26.dp))
            .background(LiquidGlassFill)
            .border(1.2.dp, LiquidGlassBorder, RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Telephony & Recording Configuration",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.3).sp
            )

            // Preference 1: Auto-Record Calls (Root Dual-Side)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = null,
                            tint = AppleRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Auto-Record Calls (Dual-Side Audio)",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isAutoRecordEnabled)
                            "Automatic 2-way call recording active: captures caller and mic directly"
                        else
                            "Automatic recording off: you can still record anytime during calls with the REC button",
                        color = if (isAutoRecordEnabled) AppleRed else TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = isAutoRecordEnabled,
                    onCheckedChange = onToggleAutoRecord,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AppleRed,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Color(0x22FFFFFF)
                    )
                )
            }

            // Saved Recordings Manager Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x18FFFFFF))
                    .border(1.dp, LiquidGlassBorderSubtle, RoundedCornerShape(16.dp))
                    .clickable { showRecordingsDialog = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = AppleTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Saved Call Recordings",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Listen, share WAV files or delete past recordings",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppleTeal.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Manage",
                            color = AppleTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.8.dp)
                    .background(Color(0x18FFFFFF))
            )

            // Preference 2: Hide Phone Number on Caller ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.VisibilityOff,
                            contentDescription = null,
                            tint = AppleTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Hide Number on Incoming / Caller ID",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isHideNumberOnCallerId)
                            "Stealth mode active: raw numbers masked as 'Private Caller' on incoming screens"
                        else
                            "Standard display: full caller phone numbers shown on incoming screens",
                        color = if (isHideNumberOnCallerId) AppleTeal else TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = isHideNumberOnCallerId,
                    onCheckedChange = onToggleHideNumber,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AppleTeal,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Color(0x22FFFFFF)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.8.dp)
                    .background(Color(0x18FFFFFF))
            )

            // Preference 3: Auto-Screen Unknown Numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-Screen Unknown Numbers",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Automatically activates assistant when non-contact calls ring",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = autoScreenUnknown,
                    onCheckedChange = onToggleAutoScreen,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AppleBlue,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Color(0x22FFFFFF)
                    )
                )
            }
        }
    }

    if (showRecordingsDialog) {
        SavedRecordingsDialog(onDismiss = { showRecordingsDialog = false })
    }
}

/**
 * iOS / ColorOS Inset Grouped Settings Components
 */
@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = TextTertiary,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 6.dp, top = 6.dp, bottom = 2.dp)
    )
}

@Composable
fun SettingsGroupCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0x60000000),
                ambientColor = Color(0x35000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(LiquidGlassFill)
            .border(1.2.dp, LiquidGlassBorder, RoundedCornerShape(22.dp))
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
fun SettingsItemRow(
    icon: ImageVector,
    iconTint: Color = Color.White,
    iconBgColor: Color = Color(0x18FFFFFF),
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Uniform 36dp rounded square icon container with clean minimal glass
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBgColor)
                .border(0.8.dp, Color(0x28FFFFFF), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Aligned title and subtitle column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.5.sp,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        trailingContent()
    }
}

@Composable
fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 66.dp, end = 16.dp)
            .height(0.6.dp)
            .background(Color(0x16FFFFFF))
    )
}

/**
 * Dedicated Dialer & System Settings Modal Sheet
 * Opened via 3-dots in Keypad header.
 */
@Composable
fun DialerSettingsSheet(
    isDefaultDialer: Boolean,
    onRequestDefaultDialer: () -> Unit,
    operationMode: AppOperationMode,
    onSelectMode: (AppOperationMode) -> Unit,
    autoScreenUnknown: Boolean,
    onToggleAutoScreen: (Boolean) -> Unit,
    isHideNumberOnCallerId: Boolean,
    onToggleHideNumber: (Boolean) -> Unit,
    isAutoRecordEnabled: Boolean,
    onToggleAutoRecord: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var showRecordingsDialog by remember { mutableStateOf(false) }
    var rootAvailable by remember { mutableStateOf(RootPrivilegeManager.isRootAvailable()) }
    var isPrivileged by remember { mutableStateOf(RootPrivilegeManager.isPrivilegedApp(context)) }
    var isModuleInstalled by remember { mutableStateOf(RootPrivilegeManager.isMagiskModuleInstalled()) }
    var isInstalling by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x95000000))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {}
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        spotColor = Color(0xB5000000),
                        ambientColor = Color(0x70000000)
                    )
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFA141824),
                                Color(0xF50D111A)
                            )
                        )
                    )
                    .border(
                        1.2.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color(0x60FFFFFF),
                                Color(0x18FFFFFF)
                            )
                        ),
                        RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Drag Handle Pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(42.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0x40FFFFFF))
                    )

                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Settings",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Telephony, Recording & Privileges",
                                color = TextSecondary,
                                fontSize = 12.5.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x1AFFFFFF))
                                .border(0.8.dp, Color(0x30FFFFFF), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // ==========================================
                    // GROUP 1: CALL HANDLING & SCREENING
                    // ==========================================
                    SettingsSectionHeader(title = "CALL HANDLING & SCREENING")
                    SettingsGroupCard {
                        // Row 1: Default Phone App
                        SettingsItemRow(
                            icon = if (isDefaultDialer) Icons.Rounded.PhoneInTalk else Icons.Filled.Warning,
                            iconTint = Color.White,
                            iconBgColor = Color(0x18FFFFFF),
                            title = "Default Phone App",
                            subtitle = if (isDefaultDialer)
                                "Active • Managing incoming & outgoing carrier calls"
                            else
                                "Required by Android to intercept and screen incoming calls",
                            trailingContent = {
                                if (isDefaultDialer) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AppleGreen.copy(alpha = 0.18f))
                                            .border(0.8.dp, AppleGreen.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 9.dp, vertical = 5.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = AppleGreen,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "ACTIVE",
                                                color = AppleGreen,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(AppleBlue)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onRequestDefaultDialer()
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Set Default",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        )

                        SettingsDivider()

                        // Row 2: Auto-Screen Unknown
                        SettingsItemRow(
                            icon = Icons.Rounded.Security,
                            iconTint = Color.White,
                            iconBgColor = Color(0x18FFFFFF),
                            title = "Auto-Screen Unknown",
                            subtitle = if (autoScreenUnknown)
                                "Assistant answers callers not saved in your contacts"
                            else
                                "Manual screening: tap Screen on incoming call canopy",
                            trailingContent = {
                                Switch(
                                    checked = autoScreenUnknown,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onToggleAutoScreen(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0x40FFFFFF),
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = Color(0x22FFFFFF)
                                    )
                                )
                            }
                        )

                        SettingsDivider()

                        // Row 3: Stealth Caller ID
                        SettingsItemRow(
                            icon = Icons.Rounded.VisibilityOff,
                            iconTint = Color.White,
                            iconBgColor = Color(0x18FFFFFF),
                            title = "Stealth Caller ID",
                            subtitle = if (isHideNumberOnCallerId)
                                "Private Caller: raw numbers masked on in-call screens"
                            else
                                "Standard: full caller phone numbers displayed",
                            trailingContent = {
                                Switch(
                                    checked = isHideNumberOnCallerId,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onToggleHideNumber(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0x40FFFFFF),
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = Color(0x22FFFFFF)
                                    )
                                )
                            }
                        )
                    }

                    // ==========================================
                    // GROUP 2: CALL RECORDING ENGINE
                    // ==========================================
                    SettingsSectionHeader(title = "CALL RECORDING ENGINE")
                    SettingsGroupCard {
                        // Row 1: Auto-Record Calls
                        SettingsItemRow(
                            icon = Icons.Filled.Mic,
                            iconTint = Color.White,
                            iconBgColor = Color(0x18FFFFFF),
                            title = "Auto-Record Calls",
                            subtitle = if (isAutoRecordEnabled)
                                "Dual-side capture: records caller and mic directly"
                            else
                                "Manual recording: tap in-call REC button anytime",
                            trailingContent = {
                                Switch(
                                    checked = isAutoRecordEnabled,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onToggleAutoRecord(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0x40FFFFFF),
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = Color(0x22FFFFFF)
                                    )
                                )
                            }
                        )

                        SettingsDivider()

                        // Row 2: Saved Call Recordings Manager
                        SettingsItemRow(
                            icon = Icons.Rounded.GraphicEq,
                            iconTint = Color.White,
                            iconBgColor = Color(0x18FFFFFF),
                            title = "Saved Recordings",
                            subtitle = "Playback audio, share WAV files, or manage storage",
                            modifier = Modifier.clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showRecordingsDialog = true
                            },
                            trailingContent = {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x18FFFFFF))
                                        .border(0.8.dp, Color(0x30FFFFFF), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 9.dp, vertical = 5.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Manage",
                                            color = Color.White,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Icon(
                                            imageVector = Icons.Rounded.ChevronRight,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }

                    // ==========================================
                    // GROUP 3: OPERATING PROFILE & PRIVILEGES
                    // ==========================================
                    SettingsSectionHeader(title = "OPERATING PROFILE & PRIVILEGES")
                    SettingsGroupCard {
                        // Row 1: Profile Summary & Status Pill
                        SettingsItemRow(
                            icon = if (operationMode == AppOperationMode.ROOT_ENHANCED) Icons.Rounded.Security else Icons.Filled.Call,
                            iconTint = Color.White,
                            iconBgColor = Color(0x18FFFFFF),
                            title = "Operating Profile",
                            subtitle = if (operationMode == AppOperationMode.ROOT_ENHANCED)
                                "Root Superpowers & Direct Audio Uplink"
                            else
                                "Standard Non-Root Telecom Profile",
                            trailingContent = {
                                val badgeText = if (operationMode == AppOperationMode.ROOT_ENHANCED) {
                                    if (rootAvailable) "ROOT READY" else "SU NEEDED"
                                } else {
                                    "STANDARD"
                                }
                                val badgeColor = if (operationMode == AppOperationMode.ROOT_ENHANCED) {
                                    if (rootAvailable) AppleGreen else AppleAmber
                                } else {
                                    AppleBlue
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeColor.copy(alpha = 0.18f))
                                        .border(0.8.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        color = badgeColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        )

                        SettingsDivider()

                        // Segmented Liquid Glass Mode Selector Pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x18FFFFFF))
                                    .border(1.dp, LiquidGlassBorderSubtle, RoundedCornerShape(14.dp))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val isStandard = operationMode == AppOperationMode.STANDARD
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(if (isStandard) AppleBlueGlassFill else SolidColor(Color.Transparent))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onSelectMode(AppOperationMode.STANDARD)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Standard (Non-Root)",
                                        color = if (isStandard) Color.White else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isStandard) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                }

                                val isRoot = operationMode == AppOperationMode.ROOT_ENHANCED
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(if (isRoot) AppleAmberGlassFill else SolidColor(Color.Transparent))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onSelectMode(AppOperationMode.ROOT_ENHANCED)
                                            coroutineScope.launch {
                                                rootAvailable = RootPrivilegeManager.isRootAvailable()
                                                isPrivileged = RootPrivilegeManager.isPrivilegedApp(context)
                                                isModuleInstalled = RootPrivilegeManager.isMagiskModuleInstalled()
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Root Enhanced",
                                        color = if (isRoot) Color.White else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isRoot) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Feature Bullets Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x0CFFFFFF))
                                .border(0.8.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (operationMode == AppOperationMode.STANDARD) {
                                    Text(
                                        text = "Standard Mode is built for normal everyday calling on any Android phone. Calls use your natural microphone directly with zero root modifications, zero risk, and maximum stability.",
                                        color = TextSecondary,
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    ModeFeatureRow(text = "Standard Telecom framework (works on all devices)", isIncluded = true)
                                    ModeFeatureRow(text = "Instant call screening & neural AI assistant", isIncluded = true)
                                    ModeFeatureRow(text = "Zero unlocked bootloader or root required", isIncluded = true)
                                } else {
                                    Text(
                                        text = "Root Enhanced Mode unlocks privileged digital audio routing, zero-noise digital cellular injection, and system priv-app integration via Magisk or KernelSU.",
                                        color = AppleAmber,
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    ModeFeatureRow(text = "Direct cellular uplink audio injection (zero mic noise)", isIncluded = true)
                                    ModeFeatureRow(text = "High-fidelity dual-side in-call audio recording", isIncluded = true)
                                    ModeFeatureRow(text = "Magisk / KernelSU system priv-app integration", isIncluded = true)
                                }
                            }
                        }

                        // Root Actions & Module Management
                        if (operationMode == AppOperationMode.ROOT_ENHANCED) {
                            SettingsDivider()

                            SettingsItemRow(
                                icon = Icons.Rounded.Extension,
                                iconTint = Color.White,
                                iconBgColor = Color(0x18FFFFFF),
                                title = when {
                                    isPrivileged -> "System Priv-App: Active"
                                    isModuleInstalled -> "Module Installed (Pending Reboot)"
                                    else -> "Magisk / KernelSU Module"
                                },
                                subtitle = when {
                                    isPrivileged -> "Verified in /system/priv-app with CAPTURE_AUDIO_OUTPUT"
                                    isModuleInstalled -> "Restart device to finish mounting into system_server"
                                    rootAvailable -> "Tap below to build and activate systemless module"
                                    else -> "Grant root (su) access to unlock digital injection"
                                },
                                trailingContent = {
                                    if (isPrivileged) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AppleGreen.copy(alpha = 0.18f))
                                                .border(0.8.dp, AppleGreen.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "PRIV-APP",
                                                color = AppleGreen,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isPrivileged) {
                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val result = RootPrivilegeManager.uninstallSystemModule()
                                                isModuleInstalled = RootPrivilegeManager.isMagiskModuleInstalled()
                                                isPrivileged = RootPrivilegeManager.isPrivilegedApp(context)
                                                Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                        modifier = Modifier.fillMaxWidth().height(40.dp)
                                    ) {
                                        Text("Uninstall System Module", fontSize = 12.sp)
                                    }
                                } else if (rootAvailable) {
                                    if (isModuleInstalled) {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    RootPrivilegeManager.rebootDevice()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AppleAmber),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().height(42.dp)
                                        ) {
                                            Text("Restart Device Now", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            isInstalling = true
                                            coroutineScope.launch {
                                                val result = RootPrivilegeManager.installSystemModule(context)
                                                isInstalling = false
                                                isModuleInstalled = RootPrivilegeManager.isMagiskModuleInstalled()
                                                isPrivileged = RootPrivilegeManager.isPrivilegedApp(context)
                                                Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        enabled = !isInstalling,
                                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().height(42.dp)
                                    ) {
                                        Text(
                                            text = if (isInstalling) "Configuring Module..." else if (isModuleInstalled) "Re-install / Update Module" else "Activate Priv-App Module",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    if (isModuleInstalled) {
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val result = RootPrivilegeManager.uninstallSystemModule()
                                                    isModuleInstalled = RootPrivilegeManager.isMagiskModuleInstalled()
                                                    Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                            modifier = Modifier.fillMaxWidth().height(40.dp)
                                        ) {
                                            Text("Uninstall Module", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            SettingsDivider()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(16.dp).padding(top = 1.dp)
                                )
                                Text(
                                    text = "Android security isolates cellular voice channels from user apps. System priv-app installation mounts Call Screener into /system/priv-app with CAPTURE_AUDIO_OUTPUT permissions, bypassing loudspeaker audio loopback.",
                                    color = TextTertiary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showRecordingsDialog) {
        SavedRecordingsDialog(onDismiss = { showRecordingsDialog = false })
    }
}

/**
 * Saved Call Recordings Manager Dialog with In-App Media Playback & Sharing
 */
@Composable
fun SavedRecordingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sessionManager = remember { CallSessionManager.getInstance(context) }
    var recordings by remember { mutableStateOf<List<RecordedCallItem>>(emptyList()) }
    var playingId by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        recordings = sessionManager.recordingEngine.getSavedRecordings()
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            } catch (_: Exception) {}
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x95000000))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {}
                    .shadow(elevation = 28.dp, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), spotColor = Color(0xB5000000))
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFA141824),
                                Color(0xF50D111A)
                            )
                        )
                    )
                    .border(
                        1.2.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color(0x60FFFFFF),
                                Color(0x18FFFFFF)
                            )
                        ),
                        RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Drag Handle Pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(42.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0x40FFFFFF))
                    )

                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Call Recordings",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.4).sp
                            )
                            Text(
                                text = "${recordings.size} files recorded • Root Audio HAL",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x1AFFFFFF))
                                .border(0.8.dp, Color(0x30FFFFFF), CircleShape)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }

                    if (recordings.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Mic, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No Call Recordings Yet", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Recorded calls will appear here in uncompressed WAV", color = TextTertiary, fontSize = 12.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(recordings, key = { it.id }) { item ->
                                val isPlaying = playingId == item.id
                                val formattedDate = remember(item.timestamp) {
                                    SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(item.timestamp))
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (isPlaying) AppleRed.copy(alpha = 0.15f) else Color(0x14FFFFFF))
                                        .border(
                                            1.dp,
                                            if (isPlaying) Brush.verticalGradient(listOf(AppleRed, AppleRed.copy(alpha = 0.4f)))
                                            else LiquidGlassBorderSubtle,
                                            RoundedCornerShape(18.dp)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Play / Pause Circle with Liquid Sheen
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .shadow(
                                                    elevation = if (isPlaying) 8.dp else 3.dp,
                                                    shape = CircleShape,
                                                    spotColor = if (isPlaying) AppleRed else Color.Black
                                                )
                                                .clip(CircleShape)
                                                .background(
                                                    if (isPlaying) {
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                AppleRed,
                                                                Color(0xFFD70015)
                                                            )
                                                        )
                                                    } else {
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                Color(0x38FFFFFF),
                                                                Color(0x18FFFFFF)
                                                            )
                                                        )
                                                    }
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isPlaying) Color.White.copy(alpha = 0.5f) else Color(0x40FFFFFF),
                                                    CircleShape
                                                )
                                                .clickable {
                                                    if (isPlaying) {
                                                        try {
                                                            mediaPlayer?.pause()
                                                            playingId = null
                                                        } catch (_: Exception) {}
                                                    } else {
                                                        try {
                                                            mediaPlayer?.release()
                                                            mediaPlayer = MediaPlayer().apply {
                                                                setDataSource(item.file.absolutePath)
                                                                prepare()
                                                                start()
                                                                setOnCompletionListener {
                                                                    playingId = null
                                                                }
                                                            }
                                                            playingId = item.id
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Cannot play recording", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                contentDescription = if (isPlaying) "Pause" else "Play",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = DialerRepository.formatPhoneNumber(item.callerNumber),
                                                color = TextPrimary,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = formattedDate,
                                                    color = TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isPlaying) AppleRed.copy(alpha = 0.25f) else AppleTeal.copy(alpha = 0.2f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = formatDuration(item.durationSeconds),
                                                        color = if (isPlaying) AppleRed else AppleTeal,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                                Text(
                                                    text = item.fileSizeFormatted,
                                                    color = TextTertiary,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        // Share Button
                                        IconButton(
                                            onClick = {
                                                try {
                                                    val uri = FileProvider.getUriForFile(
                                                        context,
                                                        "${context.packageName}.fileprovider",
                                                        item.file
                                                    )
                                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "audio/wav"
                                                        putExtra(Intent.EXTRA_STREAM, uri)
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    context.startActivity(Intent.createChooser(intent, "Share Call Recording"))
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Error sharing recording: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x18FFFFFF))
                                                .border(0.8.dp, Color(0x30FFFFFF), CircleShape)
                                        ) {
                                            Icon(Icons.Filled.Share, contentDescription = "Share", tint = AppleTeal, modifier = Modifier.size(16.dp))
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Delete Button
                                        IconButton(
                                            onClick = {
                                                if (isPlaying) {
                                                    try { mediaPlayer?.stop() } catch (_: Exception) {}
                                                    playingId = null
                                                }
                                                scope.launch {
                                                    sessionManager.recordingEngine.deleteRecording(item.file)
                                                    recordings = sessionManager.recordingEngine.getSavedRecordings()
                                                }
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x18FFFFFF))
                                                .border(0.8.dp, AppleRed.copy(alpha = 0.35f), CircleShape)
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AppleRed.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
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
}

/**
 * iOS Frosted Glass Card: Architecture Information
 */
@Composable
fun GlassArchitectureCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x60000000), ambientColor = Color(0x40000000))
            .clip(RoundedCornerShape(26.dp))
            .background(LiquidGlassFillSubtle)
            .border(1.2.dp, LiquidGlassBorderSubtle, RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "System Priv-App Architecture",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Standard Android security isolates cellular voice channels from 3rd-party user apps. System priv-app installation mounts this package into /system/priv-app with CAPTURE_AUDIO_OUTPUT permissions, bypassing loudspeaker audio loopback.",
                color = TextTertiary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
