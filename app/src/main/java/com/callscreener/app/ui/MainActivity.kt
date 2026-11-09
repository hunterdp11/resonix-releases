package com.callscreener.app.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
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
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.CameraAlt
import com.callscreener.app.data.rememberContactPhotoBitmap
import com.callscreener.app.data.CallerProfileManager
import com.callscreener.app.data.CallerProfile
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Block
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
import androidx.compose.material.icons.rounded.Psychology
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Voicemail
import kotlin.math.absoluteValue
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
import com.callscreener.app.ui.theme.GoogleSans
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.callscreener.app.data.CallLogItem
import com.callscreener.app.data.ContactItem
import com.callscreener.app.data.DialerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    var contacts by remember { mutableStateOf(DialerRepository.getCachedContacts(context)) }
    var callLogs by remember { mutableStateOf(DialerRepository.getCachedCallLogs(context)) }
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
        val allGranted = requiredPerms.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!allGranted) {
            permissionLauncher.launch(requiredPerms.toTypedArray())
        }
    }

    LaunchedEffect(permissionTrigger) {
        withContext(Dispatchers.IO) {
            val freshContacts = DialerRepository.loadContacts(context)
            val freshLogs = DialerRepository.loadCallLogs(context, freshContacts)
            withContext(Dispatchers.Main) {
                contacts = freshContacts
                callLogs = freshLogs
            }
        }
    }

    var selectedContactForDetails by remember { mutableStateOf<ContactItem?>(null) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isPresetManagerOpen by remember { mutableStateOf(false) }
    val presets by sessionManager.presetManager.presets.collectAsState()

    val monetColors: ColorScheme = remember(context) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(monetColors.surface)
    ) {
        // Material You Dynamic Backdrop
        PixelLiquidBackdrop(monetColors = monetColors)

        // Tab Content extending full edge-to-edge behind floating dock
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    val isForward = targetState.ordinal > initialState.ordinal
                    (fadeIn(
                        animationSpec = tween(durationMillis = 180)
                    ) + slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        initialOffsetX = { fullWidth ->
                            if (isForward) fullWidth / 8 else -fullWidth / 8
                        }
                    )).togetherWith(
                        fadeOut(
                            animationSpec = tween(durationMillis = 100)
                        ) + slideOutHorizontally(
                            animationSpec = spring(
                                dampingRatio = 0.85f,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            targetOffsetX = { fullWidth ->
                                if (isForward) -fullWidth / 8 else fullWidth / 8
                            }
                        )
                    )
                },
                label = "MainTabTransition",
                modifier = Modifier.fillMaxSize()
            ) { targetTab ->
                when (targetTab) {
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
                        onOpenDetails = { contact -> selectedContactForDetails = contact },
                        onOpenSettings = { isSettingsOpen = true },
                        onOpenPresetManager = { isPresetManagerOpen = true }
                    )
                    DialerTab.CONTACTS -> ContactsScreen(
                        contacts = contacts,
                        callLogs = callLogs,
                        isLoading = isLoadingContacts,
                        onCall = { number -> DialerRepository.placeCall(context, number) },
                        onOpenDetails = { contact -> selectedContactForDetails = contact },
                        onOpenSettings = { isSettingsOpen = true },
                        onOpenPresetManager = { isPresetManagerOpen = true }
                    )
                    DialerTab.KEYPAD -> KeypadScreen(
                        contacts = contacts,
                        callLogs = callLogs,
                        onCall = { number -> DialerRepository.placeCall(context, number) },
                        onOpenDetails = { contact -> selectedContactForDetails = contact },
                        onOpenSettings = { isSettingsOpen = true },
                        onOpenPresetManager = { isPresetManagerOpen = true },
                        onSwitchTab = { currentTab = it }
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

        // Google Pixel Material You Dynamic Floating Dock Bar
        LiquidGlassBottomBar(
            currentTab = currentTab,
            onTabSelected = { currentTab = it },
            monetColors = monetColors,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 10.dp)
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
                callLogs = callLogs,
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
 * Google Pixel Material You Dynamic Backdrop:
 * Cohesive dynamic Monet surface with subtle ambient top radiance.
 * Edge-to-edge, seamlessly unified with no black voids or harsh cutoffs.
 */
@Composable
fun PixelLiquidBackdrop(monetColors: ColorScheme) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val height = size.height

        // 1. Dynamic Monet Surface Base
        drawRect(color = monetColors.surface)

        // 2. Subtle Static Top Status Bar Ambient Sheen
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    monetColors.surfaceContainerHigh.copy(alpha = 0.45f),
                    monetColors.surfaceContainer.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = height * 0.25f
            )
        )
    }
}

/**
 * Google Pixel Expressive / Material You Floating Dock Navigation Bar:
 * - Authentic Pixel Material 3 Expressive design inspired by Google Phone (dial)
 * - Dynamic Material You Monet colors (surfaceContainer, secondaryContainer, onSecondaryContainer)
 * - Active pill indicator (58dp x 32dp capsule) behind the active tab icon
 * - Expressive iconography (History, People, Dialpad, AutoAwesome Gemini Sparkles)
 * - Fluid gestural swipe/scrubbing across tabs with real-time finger tracking, dynamic liquid pill expansion,
 *   crisp boundary-crossing haptics, and seamless release commitment
 * - Concentric, mathematically balanced layout with Google Sans typography
 */
@Composable
fun LiquidGlassBottomBar(
    currentTab: DialerTab,
    onTabSelected: (DialerTab) -> Unit,
    monetColors: ColorScheme,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val tabs = remember {
        listOf(
            Triple(DialerTab.RECENTS, Icons.Rounded.History, "Recents"),
            Triple(DialerTab.CONTACTS, Icons.Rounded.People, "Contacts"),
            Triple(DialerTab.KEYPAD, Icons.Rounded.Dialpad, "Keypad"),
            Triple(DialerTab.ASSISTANT, Icons.Rounded.AutoAwesome, "Assistant")
        )
    }

    val selectedIndex = when (currentTab) {
        DialerTab.RECENTS -> 0
        DialerTab.CONTACTS -> 1
        DialerTab.KEYPAD -> 2
        DialerTab.ASSISTANT -> 3
    }

    var isSwiping by remember { mutableStateOf(false) }
    var swipeOffsetPx by remember { mutableFloatStateOf(0f) }
    var hoveredIndex by remember { mutableIntStateOf(selectedIndex) }

    LaunchedEffect(selectedIndex) {
        if (!isSwiping) {
            hoveredIndex = selectedIndex
        }
    }

    // Outer Floating Material You Capsule Dock Container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .widthIn(max = 410.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color(0x55000000),
                ambientColor = Color(0x22000000)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        monetColors.surfaceContainerHigh.copy(alpha = 0.94f),
                        monetColors.surfaceContainer.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = monetColors.outlineVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        // Subtle top specular hairline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            monetColors.primary.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Interactive Layer: Sliding Indicator Pill + Expressive Tab Items + Swipe/Scrub Gesture
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val totalTabs = tabs.size
            val tabWidthPx = totalWidthPx / totalTabs.toFloat()

            val standardPillWidthDp = 58.dp

            val widthExpansion by animateDpAsState(
                targetValue = if (isSwiping) 8.dp else 0.dp,
                animationSpec = spring(
                    dampingRatio = 0.80f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "dockPillWidthExpansion"
            )

            val pillWidthDp = standardPillWidthDp + widthExpansion

            // Center of the active tab
            val targetCenterPx = if (isSwiping) {
                swipeOffsetPx
            } else {
                tabWidthPx * (selectedIndex + 0.5f)
            }

            val targetLeftDp = with(density) {
                (targetCenterPx - (with(density) { pillWidthDp.toPx() } / 2f)).toDp()
            }

            val indicatorOffset by animateDpAsState(
                targetValue = targetLeftDp,
                animationSpec = if (isSwiping) {
                    spring(dampingRatio = 0.92f, stiffness = Spring.StiffnessHigh)
                } else {
                    spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                },
                label = "dockIndicatorOffset"
            )

            // Sleek Material 3 Expressive Active Indicator Pill (centered behind the icon)
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = indicatorOffset.roundToPx(),
                            y = with(density) { 5.dp.roundToPx() }
                        )
                    }
                    .width(pillWidthDp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(monetColors.secondaryContainer)
                    .border(
                        width = 0.8.dp,
                        color = monetColors.outlineVariant.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            // Tab Items Row (Concentric icons and typography with unified pointer gesture)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(tabs) {
                        val slop = viewConfiguration.touchSlop
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var isDragging = false
                            var currentX = down.position.x
                            var currentHover = (currentX / tabWidthPx).toInt().coerceIn(0, totalTabs - 1)

                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                    if (change.pressed) {
                                        val totalMovement = (change.position.x - down.position.x).absoluteValue
                                        if (!isDragging && totalMovement > slop) {
                                            isDragging = true
                                            isSwiping = true
                                        }

                                        if (isDragging) {
                                            change.consume()
                                            currentX = change.position.x
                                            swipeOffsetPx = currentX.coerceIn(
                                                tabWidthPx * 0.5f,
                                                totalWidthPx - tabWidthPx * 0.5f
                                            )

                                            val newHover = (currentX / tabWidthPx).toInt().coerceIn(0, totalTabs - 1)
                                            if (newHover != currentHover) {
                                                currentHover = newHover
                                                hoveredIndex = newHover
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                        }
                                    } else {
                                        // Pointer released
                                        change.consume()
                                        if (isDragging) {
                                            isSwiping = false
                                            val finalIndex = (currentX / tabWidthPx).toInt().coerceIn(0, totalTabs - 1)
                                            hoveredIndex = finalIndex
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onTabSelected(tabs[finalIndex].first)
                                        } else {
                                            // Quick tap
                                            val tapIndex = (down.position.x / tabWidthPx).toInt().coerceIn(0, totalTabs - 1)
                                            hoveredIndex = tapIndex
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onTabSelected(tabs[tapIndex].first)
                                        }
                                        break
                                    }
                                }
                            } finally {
                                isSwiping = false
                            }
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, (_, icon, label) ->
                    val isTargetActive = if (isSwiping) (hoveredIndex == index) else (selectedIndex == index)

                    val iconScale by animateFloatAsState(
                        targetValue = if (isTargetActive) 1.08f else 0.94f,
                        animationSpec = spring(
                            dampingRatio = 0.78f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "tabIconScale_$index"
                    )

                    val iconTint by animateColorAsState(
                        targetValue = if (isTargetActive) monetColors.onSecondaryContainer else monetColors.onSurfaceVariant.copy(alpha = 0.8f),
                        animationSpec = tween(durationMillis = 150),
                        label = "tabIconTint_$index"
                    )

                    val labelColor by animateColorAsState(
                        targetValue = if (isTargetActive) monetColors.onSurface else monetColors.onSurfaceVariant.copy(alpha = 0.75f),
                        animationSpec = tween(durationMillis = 150),
                        label = "tabLabelColor_$index"
                    )

                    val labelWeight = if (isTargetActive) FontWeight.SemiBold else FontWeight.Medium

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier.size(width = 58.dp, height = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = iconTint,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .scale(iconScale)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = label,
                                color = labelColor,
                                fontSize = 11.5.sp,
                                fontFamily = GoogleSans,
                                fontWeight = labelWeight,
                                letterSpacing = if (isTargetActive) 0.1.sp else 0.sp,
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
 * Helper to format call duration into human-readable string (e.g. "3m 12s", "45s")
 */
private fun formatCallDuration(seconds: Long): String {
    return if (seconds >= 3600) {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        "${h}h ${m}m"
    } else if (seconds >= 60) {
        val m = seconds / 60
        val s = seconds % 60
        if (s > 0) "${m}m ${s}s" else "${m}m"
    } else {
        "${seconds}s"
    }
}

/**
 * Helper to compute authentic Material You dynamic Monet pastel avatar colors
 */
private fun getMonetAvatarColors(name: String, monetColors: ColorScheme): Pair<Color, Color> {
    val hash = kotlin.math.abs(name.hashCode()) % 3
    return when (hash) {
        0 -> monetColors.primaryContainer to monetColors.onPrimaryContainer
        1 -> monetColors.secondaryContainer to monetColors.onSecondaryContainer
        else -> monetColors.tertiaryContainer to monetColors.onTertiaryContainer
    }
}

/**
 * Helper to compute 1-2 character initials for contact avatars
 */
private fun getContactInitials(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return if (parts.size >= 2) {
        "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
    } else if (name.isNotBlank()) {
        name.take(2).uppercase()
    } else {
        "?"
    }
}

/**
 * Screen 1: Recents (Material You / Expressive Pixel Call History, Dynamic Monet Squircles & Direction Badges)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentsScreen(
    callLogs: List<CallLogItem>,
    isLoading: Boolean,
    presets: List<AudioPreset> = emptyList(),
    onPlayPreset: (AudioPreset) -> Unit = {},
    onCall: (String) -> Unit,
    onScreen: (CallLogItem) -> Unit = {},
    onOpenDetails: (ContactItem) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenPresetManager: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedLogForDetails by remember { mutableStateOf<CallLogItem?>(null) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    val menuRotation by animateFloatAsState(
        targetValue = if (isMenuExpanded) 90f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "recentsMenuRotate"
    )

    val monetColors: ColorScheme = remember(context) {
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

    val filteredLogs = remember(callLogs, selectedFilter, searchQuery) {
        val base = if (selectedFilter == "Missed") {
            callLogs.filter { it.type == CallLog.Calls.MISSED_TYPE || it.type == CallLog.Calls.REJECTED_TYPE }
        } else {
            callLogs
        }
        if (searchQuery.isBlank()) {
            base
        } else {
            val q = searchQuery.trim().lowercase()
            val digits = q.filter { it.isDigit() || it == '+' }
            base.filter { log ->
                val nameMatch = log.name?.lowercase()?.contains(q) == true
                val numMatch = log.number.lowercase().contains(q) ||
                        (digits.isNotEmpty() && log.number.filter { it.isDigit() || it == '+' }.contains(digits))
                nameMatch || numMatch
            }
        }
    }

    val groupedLogs = remember(filteredLogs) {
        filteredLogs.groupBy { getCallDateGroup(it.timestamp) }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && callLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Loading call history...",
                    color = monetColors.onSurfaceVariant,
                    fontSize = 15.sp,
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(monetColors.primaryContainer.copy(alpha = 0.6f))
                            .border(1.dp, monetColors.primary.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (searchQuery.isNotEmpty()) Icons.Filled.Search else Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = monetColors.primary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No Matching Calls"
                            else if (selectedFilter == "Missed") "No Missed Calls"
                            else "No Recent Calls",
                        color = monetColors.onSurface,
                        fontSize = 18.sp,
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty())
                            "No call logs found matching \"$searchQuery\""
                        else if (selectedFilter == "Missed")
                            "You don't have any missed or rejected calls"
                        else
                            "Calls you make and receive will show up here",
                        color = monetColors.onSurfaceVariant,
                        fontSize = 13.5.sp,
                        fontFamily = GoogleSans,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // LazyColumn extends edge-to-edge; items scroll fluidly underneath the transparent status bar!
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = statusBarTop + 162.dp, bottom = 120.dp, start = 18.dp, end = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groupedLogs.forEach { (dateGroup, logsInGroup) ->
                    item(key = "header_$dateGroup") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(monetColors.primaryContainer.copy(alpha = 0.65f))
                                    .padding(horizontal = 9.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dateGroup,
                                    color = monetColors.onPrimaryContainer,
                                    fontFamily = GoogleSans,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .height(0.8.dp)
                                    .weight(1f)
                                    .background(monetColors.outlineVariant.copy(alpha = 0.25f))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${logsInGroup.size} calls",
                                color = monetColors.onSurfaceVariant.copy(alpha = 0.7f),
                                fontFamily = GoogleSans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    itemsIndexed(logsInGroup, key = { index, log -> "log_${dateGroup}_${log.id}_${log.timestamp}_$index" }) { _, log ->
                        val isMissed = log.type == CallLog.Calls.MISSED_TYPE || log.type == CallLog.Calls.REJECTED_TYPE
                        val isOutgoing = log.type == CallLog.Calls.OUTGOING_TYPE
                        val callerName = log.name ?: log.number

                        val (avatarBg, avatarFg) = remember(callerName) {
                            getMonetAvatarColors(callerName, monetColors)
                        }
                        val initials = remember(callerName) {
                            getContactInitials(callerName)
                        }

                        val rowInteraction = remember { MutableInteractionSource() }
                        val isRowPressed by rowInteraction.collectIsPressedAsState()
                        val rowScale by animateFloatAsState(
                            targetValue = if (isRowPressed) 0.98f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
                            label = "recentRowScale"
                        )

                        // Material You / Expressive Squircle Call Log Card
                        Box(
                            modifier = Modifier
                                .animateItemPlacement()
                                .scale(rowScale)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isRowPressed) monetColors.primaryContainer.copy(alpha = 0.35f)
                                    else Color(0xFF191C23)
                                )
                                .border(
                                    0.8.dp,
                                    if (isRowPressed) monetColors.primary.copy(alpha = 0.5f)
                                    else monetColors.outlineVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable(
                                    interactionSource = rowInteraction,
                                    indication = null,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onCall(log.number)
                                    }
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Dynamic Monet Pastel Avatar
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(avatarBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        color = avatarFg,
                                        fontSize = 15.sp,
                                        fontFamily = GoogleSans,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Caller info & Call Subtitle
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = callerName,
                                        color = if (isMissed) Color(0xFFFF6B6B) else monetColors.onSurface,
                                        fontSize = 15.sp,
                                        fontFamily = GoogleSans,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Direction Icon
                                        val (directionIcon, directionColor) = when {
                                            isMissed -> Icons.Filled.CallMissed to Color(0xFFFF5252)
                                            isOutgoing -> Icons.Filled.CallMade to Color(0xFF2196F3)
                                            else -> Icons.Filled.CallReceived to Color(0xFF4CAF50)
                                        }
                                        Icon(
                                            imageVector = directionIcon,
                                            contentDescription = null,
                                            tint = directionColor,
                                            modifier = Modifier.size(13.dp)
                                        )

                                        val subText = if (log.name != null)
                                            DialerRepository.formatPhoneNumber(log.number)
                                        else when {
                                            isMissed -> "Missed call"
                                            isOutgoing -> "Outgoing call"
                                            else -> "Incoming call"
                                        }

                                        Text(
                                            text = if (log.durationSeconds > 0)
                                                "$subText • ${formatCallDuration(log.durationSeconds)}"
                                            else
                                                subText,
                                            color = monetColors.onSurfaceVariant,
                                            fontSize = 12.sp,
                                            fontFamily = GoogleSans,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = formatCallDate(log.timestamp),
                                        color = monetColors.onSurfaceVariant.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        fontFamily = GoogleSans
                                    )

                                    // Quick Emerald Call Button
                                    val callInteraction = remember { MutableInteractionSource() }
                                    val isCallPressed by callInteraction.collectIsPressedAsState()
                                    val callScale by animateFloatAsState(
                                        targetValue = if (isCallPressed) 0.88f else 1.0f,
                                        animationSpec = spring(dampingRatio = 0.65f),
                                        label = "recentCallScale"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .scale(callScale)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(Color(0xFF2EAA55))
                                            .clickable(
                                                interactionSource = callInteraction,
                                                indication = null,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    onCall(log.number)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Call,
                                            contentDescription = "Call",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Info Button (Details)
                                    val infoInteraction = remember { MutableInteractionSource() }
                                    val isInfoPressed by infoInteraction.collectIsPressedAsState()
                                    val infoScale by animateFloatAsState(
                                        targetValue = if (isInfoPressed) 0.88f else 1.0f,
                                        animationSpec = spring(dampingRatio = 0.65f),
                                        label = "infoBtnScale"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .scale(infoScale)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(monetColors.surfaceContainerHighest.copy(alpha = 0.7f))
                                            .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(11.dp))
                                            .clickable(
                                                interactionSource = infoInteraction,
                                                indication = null,
                                                onClick = {
                                                    val contact = ContactItem(
                                                        id = log.id,
                                                        name = log.name ?: log.number,
                                                        number = log.number
                                                    )
                                                    onOpenDetails(contact)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Info,
                                            contentDescription = "Details",
                                            tint = monetColors.primary,
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

        // Pinned Top Header with Pixel Search Bar & Segmented Filter
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F1116),
                            Color(0xFF0F1116),
                            Color(0xF50F1116),
                            Color(0xD00F1116),
                            Color.Transparent
                        )
                    )
                )
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 12.dp)
        ) {
            // 1. Section Header: Big Title & Segmented Filter ("All" | "Missed")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recents",
                    color = monetColors.onSurface,
                    fontSize = 32.sp,
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.8).sp
                )

                // Material You Expressive Segmented Capsule: All | Missed
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(monetColors.surfaceContainerHigh.copy(alpha = 0.8f))
                        .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                        .padding(3.dp)
                ) {
                    Row {
                        listOf("All", "Missed").forEach { filter ->
                            val isSel = selectedFilter == filter
                            val pillBg by animateColorAsState(
                                targetValue = if (isSel) monetColors.primaryContainer else Color.Transparent,
                                animationSpec = tween(150),
                                label = "filterPillBg"
                            )
                            val pillFg by animateColorAsState(
                                targetValue = if (isSel) monetColors.onPrimaryContainer else monetColors.onSurfaceVariant,
                                animationSpec = tween(150),
                                label = "filterPillFg"
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(pillBg)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedFilter = filter
                                    }
                                    .padding(horizontal = 13.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = filter,
                                    color = pillFg,
                                    fontSize = 12.5.sp,
                                    fontFamily = GoogleSans,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                PixelDialerSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search contacts & numbers...",
                    monetColors = monetColors,
                    onMenuClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isMenuExpanded = !isMenuExpanded
                    },
                    menuRotation = menuRotation,
                    menuContent = {
                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            offset = androidx.compose.ui.unit.DpOffset(x = 0.dp, y = 6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xF0181B22))
                                .border(1.dp, monetColors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .padding(vertical = 4.dp)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = monetColors.primary, modifier = Modifier.size(18.dp))
                                        Text("Call Settings", color = monetColors.onSurface, fontSize = 14.sp, fontFamily = GoogleSans)
                                    }
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    onOpenSettings()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Rounded.Tune, contentDescription = null, tint = AppleAmber, modifier = Modifier.size(18.dp))
                                        Text("Audio Presets & Personas", color = monetColors.onSurface, fontSize = 14.sp, fontFamily = GoogleSans)
                                    }
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    onOpenPresetManager()
                                }
                            )
                        }
                    }
                )
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
 * Pixel Expressive / Material You Minimalist Contact Info Modal Sheet
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
    val coroutineScope = rememberCoroutineScope()

    val monetColors: ColorScheme = remember(context) {
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

    val (avatarBg, avatarFg) = remember(contact.name) {
        getMonetAvatarColors(contact.name, monetColors)
    }
    val initials = remember(contact.name) {
        getContactInitials(contact.name)
    }
    val formattedNum = remember(contact.number) {
        DialerRepository.formatPhoneNumber(contact.number)
    }

    val isUnknownNumber = remember(contact.number, contact.name) {
        val cleanName = contact.name.trim()
        val cleanNum = contact.number.trim()
        val isNameNumber = cleanName == cleanNum || cleanName.isBlank() ||
                cleanName.equals("Unknown Caller", ignoreCase = true) ||
                cleanName.equals("Unknown", ignoreCase = true)
        if (isNameNumber) {
            !DialerRepository.isNumberInContacts(context, cleanNum)
        } else {
            false
        }
    }

    // Custom Profile Photo Management
    var customPhotoPath by remember(contact.number) {
        mutableStateOf(CallerProfileManager.getCustomPhotoPath(context, contact.number) ?: contact.photoUri)
    }
    val contactPhotoBitmap = rememberContactPhotoBitmap(customPhotoPath)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val saved = CallerProfileManager.saveCustomPhotoFromUri(context, contact.number, uri)
                if (saved != null) {
                    customPhotoPath = saved
                    Toast.makeText(context, "Custom photo updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Persistent Caller Profile Settings
    val savedProfile = remember(contact.number) {
        CallerProfileManager.getProfile(context, contact.number)
    }
    var autoScreenThisContact by remember(savedProfile) {
        mutableStateOf(savedProfile?.autoScreen ?: false)
    }
    var isVipBypass by remember(savedProfile) {
        mutableStateOf(savedProfile?.isVipBypass ?: false)
    }
    var selectedPersona by remember(savedProfile) {
        mutableStateOf(
            VoicePersona.ALL_PERSONAS.firstOrNull { it.name.equals(savedProfile?.assignedPersona, ignoreCase = true) }
                ?: VoicePersona.DEFAULT
        )
    }

    fun persistProfile(autoScreen: Boolean, vip: Boolean, persona: VoicePersona) {
        val cur = CallerProfileManager.getProfile(context, contact.number) ?: CallerProfile()
        CallerProfileManager.saveProfile(
            context,
            contact.number,
            cur.copy(
                autoScreen = autoScreen,
                isVipBypass = vip,
                assignedPersona = persona.name
            )
        )
    }

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

    Dialog(
        onDismissRequest = { animateDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val view = LocalView.current
        SideEffect {
            var parent: Any? = view.parent
            var dialogWindow: android.view.Window? = null
            while (parent != null) {
                if (parent is DialogWindowProvider) {
                    dialogWindow = parent.window
                    break
                }
                parent = (parent as? android.view.View)?.parent
            }
            if (dialogWindow == null) {
                dialogWindow = (view.rootView.parent as? DialogWindowProvider)?.window
            }

            dialogWindow?.let { win ->
                win.setDimAmount(0f)
                win.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                win.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                win.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                win.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                win.setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.MATCH_PARENT
                )
                win.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                WindowCompat.setDecorFitsSystemWindows(win, false)
                win.statusBarColor = android.graphics.Color.TRANSPARENT
                win.navigationBarColor = android.graphics.Color.TRANSPARENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    win.isNavigationBarContrastEnforced = false
                    win.isStatusBarContrastEnforced = false
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    win.attributes = win.attributes.apply {
                        layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                }
                val insets = WindowCompat.getInsetsController(win, win.decorView)
                insets.isAppearanceLightStatusBars = false
                insets.isAppearanceLightNavigationBars = false
                win.decorView.fitsSystemWindows = false
                (win.decorView as? android.view.ViewGroup)?.clipToPadding = false
            }
            (view.parent as? android.view.View)?.fitsSystemWindows = false
            (view as? android.view.View)?.fitsSystemWindows = false
        }

        val scrimAlpha by animateFloatAsState(
            targetValue = if (isVisible) 0.60f else 0f,
            animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing),
            label = "detailsScrimAlpha"
        )

        BackHandler(enabled = isVisible) {
            animateDismiss()
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .clickable { animateDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            val maxSheetHeight = maxHeight * 0.92f

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(animationSpec = tween(180)),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(180, easing = FastOutLinearInEasing)
                ) + fadeOut(animationSpec = tween(140))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxSheetHeight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {}
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(monetColors.surfaceContainerHigh)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 10.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Pixel OEM Drag Handle
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(monetColors.outlineVariant.copy(alpha = 0.40f))
                        )

                        // 2. Redesigned Pixel OEM Top App Bar Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back / Close Button (Pixel tonal disc)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(monetColors.surfaceContainerHighest.copy(alpha = 0.70f))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        animateDismiss()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = monetColors.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = if (isUnknownNumber) "Caller details" else "Contact info",
                                color = monetColors.onSurface,
                                fontSize = 17.5.sp,
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.2).sp
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isUnknownNumber) {
                                    // Add to contacts button
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(monetColors.primaryContainer)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                DialerRepository.openSystemSaveContact(
                                                    context,
                                                    contact.number,
                                                    if (contact.name != contact.number && !contact.name.equals("Unknown Caller", true) && !contact.name.equals("Unknown", true)) contact.name else null
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.PersonAdd,
                                            contentDescription = "Add Contact",
                                            tint = monetColors.onPrimaryContainer,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                } else {
                                    // Star / Favorite
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (contact.isStarred) Color(0xFFFFB74D).copy(alpha = 0.20f)
                                                else monetColors.surfaceContainerHighest.copy(alpha = 0.70f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = if (contact.isStarred) "Starred" else "Not starred",
                                            tint = if (contact.isStarred) Color(0xFFFFB74D) else monetColors.onSurfaceVariant.copy(alpha = 0.60f),
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }

                                    // Edit Contact
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(monetColors.surfaceContainerHighest.copy(alpha = 0.70f))
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                try {
                                                    val intent = Intent(Intent.ACTION_EDIT).apply {
                                                        setDataAndType(
                                                            ContactsContract.Contacts.CONTENT_URI,
                                                            ContactsContract.Contacts.CONTENT_ITEM_TYPE
                                                        )
                                                        putExtra(ContactsContract.Intents.Insert.PHONE, contact.number)
                                                        putExtra(ContactsContract.Intents.Insert.NAME, contact.name)
                                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                    }
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {
                                                    DialerRepository.openSystemSaveContact(context, contact.number, contact.name)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Edit,
                                            contentDescription = "Edit Contact",
                                            tint = monetColors.onSurface,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // Share Contact
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(monetColors.surfaceContainerHighest.copy(alpha = 0.70f))
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
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = "Share",
                                        tint = monetColors.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // 3. Pixel Hero Avatar & Identity Section
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(avatarBg)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            photoPickerLauncher.launch("image/*")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (contactPhotoBitmap != null) {
                                        Image(
                                            bitmap = contactPhotoBitmap,
                                            contentDescription = contact.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text(
                                            text = initials,
                                            color = avatarFg,
                                            fontSize = 34.sp,
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(monetColors.primary)
                                        .border(2.dp, monetColors.surfaceContainerHigh, CircleShape)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            photoPickerLauncher.launch("image/*")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CameraAlt,
                                        contentDescription = "Set photo",
                                        tint = monetColors.onPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            if (customPhotoPath != null) {
                                Row(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(monetColors.surfaceContainerHighest.copy(alpha = 0.60f))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            CallerProfileManager.removeCustomPhoto(context, contact.number)
                                            customPhotoPath = null
                                            Toast.makeText(context, "Custom photo removed", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = null,
                                        tint = monetColors.error,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Remove custom photo",
                                        color = monetColors.onSurfaceVariant,
                                        fontSize = 11.5.sp,
                                        fontFamily = GoogleSans,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Text(
                                text = contact.name,
                                color = monetColors.onSurface,
                                fontSize = 24.sp,
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                letterSpacing = (-0.4).sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = formattedNum,
                                color = monetColors.onSurfaceVariant,
                                fontSize = 15.sp,
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(monetColors.surfaceContainerHighest.copy(alpha = 0.70f))
                                    .padding(horizontal = 10.dp, vertical = 3.5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isUnknownNumber) monetColors.onSurfaceVariant.copy(alpha = 0.50f)
                                                else Color(0xFF2EAA55)
                                            )
                                    )
                                    Text(
                                        text = if (isUnknownNumber) "Not in contacts" else "Mobile • Carrier Verified",
                                        color = monetColors.onSurfaceVariant,
                                        fontSize = 11.5.sp,
                                        fontFamily = GoogleSans,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // 4. Pixel Expressive Tactile Action Row (Call, Message, Screen AI, WhatsApp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ContactHeroActionPill(
                                icon = Icons.Filled.Call,
                                label = "Call",
                                accentColor = Color.White,
                                buttonBg = Color(0xFF2EAA55),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onCall(contact.number)
                                }
                            )

                            ContactHeroActionPill(
                                icon = Icons.Rounded.Chat,
                                label = "Message",
                                accentColor = monetColors.onSurface,
                                buttonBg = monetColors.surfaceContainerHighest,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    DialerRepository.sendSms(context, contact.number)
                                }
                            )

                            ContactHeroActionPill(
                                icon = Icons.Rounded.AutoAwesome,
                                label = "Screen AI",
                                accentColor = monetColors.onPrimaryContainer,
                                buttonBg = monetColors.primaryContainer,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onScreen(contact.number, contact.name)
                                }
                            )

                            ContactHeroActionPill(
                                icon = Icons.Rounded.Chat,
                                label = "WhatsApp",
                                accentColor = Color(0xFF25D366),
                                buttonBg = monetColors.surfaceContainerHighest,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val digits = contact.number.filter { it.isDigit() }
                                    val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits"))
                                    try { context.startActivity(waIntent) } catch (_: Exception) {}
                                }
                            )
                        }

                        // Card 1: Phone Details Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(monetColors.surfaceContainerHighest.copy(alpha = 0.50f))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(monetColors.surfaceContainerHighest),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Call,
                                                contentDescription = null,
                                                tint = monetColors.primary,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = formattedNum,
                                                color = monetColors.onSurface,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = GoogleSans
                                            )
                                            Text(
                                                text = "Mobile",
                                                color = monetColors.onSurfaceVariant,
                                                fontSize = 12.sp,
                                                fontFamily = GoogleSans
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(monetColors.surfaceContainerHighest)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                DialerRepository.copyToClipboard(context, "Phone Number", contact.number)
                                                Toast.makeText(context, "Number copied", Toast.LENGTH_SHORT).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ContentCopy,
                                            contentDescription = "Copy number",
                                            tint = monetColors.onSurface,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (isUnknownNumber) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(0.6.dp)
                                            .background(monetColors.outlineVariant.copy(alpha = 0.25f))
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                DialerRepository.openSystemSaveContact(
                                                    context,
                                                    contact.number,
                                                    if (contact.name != contact.number && !contact.name.equals("Unknown Caller", true) && !contact.name.equals("Unknown", true)) contact.name else null
                                                )
                                            }
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.PersonAdd,
                                                contentDescription = null,
                                                tint = monetColors.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Add to contacts",
                                                color = monetColors.primary,
                                                fontSize = 14.sp,
                                                fontFamily = GoogleSans,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Rounded.ChevronRight,
                                            contentDescription = null,
                                            tint = monetColors.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Card 2: Call Screener AI Options Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(monetColors.surfaceContainerHighest.copy(alpha = 0.50f))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = monetColors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "CALL SCREENER AI",
                                        color = monetColors.primary,
                                        fontSize = 11.sp,
                                        fontFamily = GoogleSans,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Always screen this caller",
                                            color = monetColors.onSurface,
                                            fontSize = 14.sp,
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "AI assistant intercepts before phone rings",
                                            color = monetColors.onSurfaceVariant,
                                            fontSize = 11.5.sp,
                                            fontFamily = GoogleSans
                                        )
                                    }
                                    Switch(
                                        checked = autoScreenThisContact,
                                        onCheckedChange = {
                                            autoScreenThisContact = it
                                            persistProfile(it, isVipBypass, selectedPersona)
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
                                        .height(0.6.dp)
                                        .background(monetColors.outlineVariant.copy(alpha = 0.25f))
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "VIP caller (Never screen)",
                                            color = monetColors.onSurface,
                                            fontSize = 14.sp,
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Calls ring through directly without screening",
                                            color = monetColors.onSurfaceVariant,
                                            fontSize = 11.5.sp,
                                            fontFamily = GoogleSans
                                        )
                                    }
                                    Switch(
                                        checked = isVipBypass,
                                        onCheckedChange = {
                                            isVipBypass = it
                                            persistProfile(autoScreenThisContact, it, selectedPersona)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = monetColors.primary,
                                            uncheckedThumbColor = monetColors.onSurfaceVariant,
                                            uncheckedTrackColor = monetColors.surfaceContainerHighest
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.6.dp)
                                        .background(monetColors.outlineVariant.copy(alpha = 0.25f))
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Screening Persona",
                                            color = monetColors.onSurface,
                                            fontSize = 13.5.sp,
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Voice applied to this caller",
                                            color = monetColors.onSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontFamily = GoogleSans
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(monetColors.primaryContainer)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                val all = VoicePersona.ALL_PERSONAS
                                                val nextIdx = (all.indexOf(selectedPersona) + 1) % all.size
                                                selectedPersona = all[nextIdx]
                                                persistProfile(autoScreenThisContact, isVipBypass, selectedPersona)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${selectedPersona.name} (${selectedPersona.code})",
                                            color = monetColors.onPrimaryContainer,
                                            fontSize = 12.sp,
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Card 3: Recent Call History Card (only if records exist)
                        if (callHistory.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(monetColors.surfaceContainerHighest.copy(alpha = 0.50f))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                                imageVector = Icons.Rounded.History,
                                                contentDescription = null,
                                                tint = monetColors.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "RECENT CALLS",
                                                color = monetColors.primary,
                                                fontSize = 11.sp,
                                                fontFamily = GoogleSans,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp
                                            )
                                        }
                                        Text(
                                            text = "${callHistory.size} recorded",
                                            color = monetColors.onSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontFamily = GoogleSans
                                        )
                                    }

                                    callHistory.take(4).forEach { log ->
                                        val isMissed = log.type == CallLog.Calls.MISSED_TYPE || log.type == CallLog.Calls.REJECTED_TYPE
                                        val isOut = log.type == CallLog.Calls.OUTGOING_TYPE
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isMissed) Color(0xFFF44336).copy(alpha = 0.15f)
                                                            else if (isOut) Color(0xFF2196F3).copy(alpha = 0.15f)
                                                            else Color(0xFF4CAF50).copy(alpha = 0.15f)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (isMissed) Icons.Filled.CallMissed else if (isOut) Icons.Filled.CallMade else Icons.Filled.CallReceived,
                                                        contentDescription = null,
                                                        tint = if (isMissed) Color(0xFFF44336) else if (isOut) Color(0xFF2196F3) else Color(0xFF4CAF50),
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = if (isMissed) "Missed" else if (isOut) "Outgoing" else "Incoming",
                                                        color = if (isMissed) Color(0xFFF44336) else monetColors.onSurface,
                                                        fontSize = 13.sp,
                                                        fontFamily = GoogleSans,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    if (!isMissed && log.durationSeconds > 0) {
                                                        Text(
                                                            text = formatDuration(log.durationSeconds),
                                                            color = monetColors.onSurfaceVariant,
                                                            fontSize = 11.sp,
                                                            fontFamily = GoogleSans
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = formatCallDate(log.timestamp),
                                                color = monetColors.onSurfaceVariant,
                                                fontSize = 11.5.sp,
                                                fontFamily = GoogleSans
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Card 4: Security (Block & Report Spam)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(monetColors.surfaceContainerHighest.copy(alpha = 0.50f))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    Toast.makeText(context, "${contact.name} added to Auto-Screen blocklist", Toast.LENGTH_SHORT).show()
                                    animateDismiss()
                                }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF44336).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Block,
                                            contentDescription = null,
                                            tint = Color(0xFFF44336),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "Block & report spam",
                                        color = Color(0xFFF44336),
                                        fontSize = 14.sp,
                                        fontFamily = GoogleSans,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFFF44336).copy(alpha = 0.70f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Bottom Spacer for Gesture Navigation Bar
                        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
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
    buttonBg: Color = Color(0x1EFFFFFF),
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "heroActionScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (isPressed) buttonBg.copy(alpha = 0.75f) else buttonBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color(0xFFE2E2E6),
            fontSize = 11.5.sp,
            fontFamily = GoogleSans,
            fontWeight = FontWeight.Medium
        )
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
 * Unified Premium Obsidian Glass Search Bar (Clean glowing border, smooth clear transition & tactile feedback)
 */
@Composable
fun PremiumGlassSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search contacts...",
    modifier: Modifier = Modifier,
    onSearchFocused: (Boolean) -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color(0x65FFFFFF) else Color(0x24FFFFFF),
        animationSpec = tween(durationMillis = 200),
        label = "searchBorderGlow"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF1E2432) else Color(0xFF151924),
        animationSpec = tween(durationMillis = 200),
        label = "searchBgColor"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x80FFFFFF),
        animationSpec = tween(durationMillis = 200),
        label = "searchIconTint"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isFocused) 10.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0x60000000)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = iconTint,
                modifier = Modifier.size(19.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 14.5.sp,
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(Color.White),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        isFocused = it.isFocused
                        onSearchFocused(it.isFocused)
                    },
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0x66FFFFFF),
                            fontFamily = GoogleSans,
                            fontSize = 14.5.sp
                        )
                    }
                    innerTextField()
                }
            )

            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(tween(120)) + scaleIn(spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(tween(100)) + scaleOut(tween(100))
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0x28FFFFFF))
                        .clickable { onQueryChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}



/**
 * Screen 2: Contacts (Material You / Expressive Pixel Contacts, Dynamic Monet Squircles & Quick Actions)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactsScreen(
    contacts: List<ContactItem>,
    callLogs: List<CallLogItem> = emptyList(),
    isLoading: Boolean,
    onCall: (String) -> Unit,
    onOpenDetails: (ContactItem) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenPresetManager: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }

    val monetColors: ColorScheme = remember(context) {
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

    // Intelligent Frequent Contacts: Starred Contacts + Highest Call Frequency & Recency
    val frequentContacts = remember(contacts, callLogs) {
        if (contacts.isEmpty()) return@remember emptyList<ContactItem>()

        fun norm(num: String): String {
            val digits = num.filter { it.isDigit() }
            return if (digits.length >= 7) digits.takeLast(10) else digits
        }

        val callStats = mutableMapOf<String, Pair<Int, Long>>()
        for (log in callLogs) {
            val n = norm(log.number)
            if (n.isNotEmpty()) {
                val (count, latest) = callStats[n] ?: Pair(0, 0L)
                callStats[n] = Pair(count + 1, maxOf(latest, log.timestamp))
            }
        }

        val now = System.currentTimeMillis()
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000

        val scored = contacts.mapNotNull { contact ->
            val n = norm(contact.number)
            val stats = if (n.isNotEmpty()) callStats[n] else null
            val callCount = stats?.first ?: 0
            val latestTime = stats?.second ?: 0L

            var score = 0
            if (contact.isStarred) {
                score += 10000
            }
            if (callCount > 0) {
                score += callCount * 100
                if (latestTime > 0 && (now - latestTime) < thirtyDaysMs) {
                    val recencyFactor = 1.0 - ((now - latestTime).toDouble() / thirtyDaysMs)
                    score += (recencyFactor * 50).toInt()
                }
            }

            if (score > 0) {
                Triple(contact, score, latestTime)
            } else {
                null
            }
        }

        if (scored.isNotEmpty()) {
            scored.sortedWith(
                compareByDescending<Triple<ContactItem, Int, Long>> { it.second }
                    .thenByDescending { it.third }
            )
            .map { it.first }
            .distinctBy { norm(it.number).ifEmpty { it.name } }
            .take(8)
        } else {
            contacts.filter { it.name.isNotBlank() && it.name != "Unknown" && it.number.isNotBlank() }.take(6)
        }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && contacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Loading contacts...",
                        color = monetColors.onSurfaceVariant,
                        fontSize = 15.sp,
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else if (filteredContacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(monetColors.primaryContainer.copy(alpha = 0.6f))
                            .border(1.dp, monetColors.primary.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = monetColors.primary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No Matching Contacts" else "No Contacts Yet",
                        color = monetColors.onSurface,
                        fontSize = 18.sp,
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty())
                            "No contacts match \"$searchQuery\""
                        else
                            "Add contacts to your phonebook or grant permission to view them",
                        color = monetColors.onSurfaceVariant,
                        fontSize = 13.5.sp,
                        fontFamily = GoogleSans,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    // Add Contact Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(monetColors.primaryContainer)
                            .border(1.dp, monetColors.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    type = ContactsContract.Contacts.CONTENT_TYPE
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PersonAdd,
                            contentDescription = null,
                            tint = monetColors.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Add Contact",
                            color = monetColors.onPrimaryContainer,
                            fontSize = 14.sp,
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // LazyColumn with generous top clearance for status bar + header
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = statusBarTop + 124.dp, bottom = 120.dp, start = 18.dp, end = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (searchQuery.isBlank() && frequentContacts.isNotEmpty()) {
                    item(key = "favorites_section") {
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FREQUENT CONTACTS",
                                    fontFamily = GoogleSans,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = monetColors.primary,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "${frequentContacts.size} quick access",
                                    fontFamily = GoogleSans,
                                    fontSize = 10.5.sp,
                                    color = monetColors.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(frequentContacts, key = { index, contact -> "fav_${contact.id}_${contact.number}_$index" }) { _, contact ->
                                    val (avatarBg, avatarFg) = remember(contact.name) {
                                        getMonetAvatarColors(contact.name, monetColors)
                                    }
                                    val favInitials = remember(contact.name) {
                                        getContactInitials(contact.name)
                                    }
                                    val favInteraction = remember { MutableInteractionSource() }
                                    val isFavPressed by favInteraction.collectIsPressedAsState()
                                    val favScale by animateFloatAsState(
                                        targetValue = if (isFavPressed) 0.94f else 1.0f,
                                        animationSpec = spring(dampingRatio = 0.65f),
                                        label = "favCardScale"
                                    )

                                    Column(
                                        modifier = Modifier
                                            .width(108.dp)
                                            .scale(favScale)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                if (isFavPressed) monetColors.primaryContainer.copy(alpha = 0.35f)
                                                else Color(0xFF1A1D24)
                                            )
                                            .border(
                                                0.8.dp,
                                                if (isFavPressed) monetColors.primary.copy(alpha = 0.5f)
                                                else monetColors.outlineVariant.copy(alpha = 0.3f),
                                                RoundedCornerShape(20.dp)
                                            )
                                            .clickable(
                                                interactionSource = favInteraction,
                                                indication = null,
                                                onClick = { onOpenDetails(contact) }
                                            )
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier.size(42.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(avatarBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = favInitials,
                                                    color = avatarFg,
                                                    fontSize = 14.sp,
                                                    fontFamily = GoogleSans,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (contact.isStarred) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .size(15.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFFFB74D)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Star,
                                                        contentDescription = "Starred",
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(9.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(7.dp))

                                        Text(
                                            text = contact.name,
                                            color = monetColors.onSurface,
                                            fontSize = 12.5.sp,
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Mini Call Button
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF2EAA55))
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    onCall(contact.number)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Call,
                                                contentDescription = "Call",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // Grouped Alphabetical Contacts
                groupedContacts.forEach { (sectionKey, contactsInSection) ->
                    item(key = "section_$sectionKey") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(monetColors.primaryContainer.copy(alpha = 0.65f))
                                    .padding(horizontal = 9.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sectionKey,
                                    color = monetColors.onPrimaryContainer,
                                    fontFamily = GoogleSans,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .height(0.8.dp)
                                    .weight(1f)
                                    .background(monetColors.outlineVariant.copy(alpha = 0.25f))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${contactsInSection.size}",
                                color = monetColors.onSurfaceVariant.copy(alpha = 0.7f),
                                fontFamily = GoogleSans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    itemsIndexed(contactsInSection, key = { index, contact -> "contact_${sectionKey}_${contact.id}_${contact.number}_$index" }) { _, contact ->
                        val (avatarBg, avatarFg) = remember(contact.name) {
                            getMonetAvatarColors(contact.name, monetColors)
                        }
                        val initials = remember(contact.name) {
                            getContactInitials(contact.name)
                        }
                        val contactPhotoBitmap = rememberContactPhotoBitmap(contact.photoUri)

                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val rowScale by animateFloatAsState(
                            targetValue = if (isPressed) 0.98f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
                            label = "contactRowScale"
                        )

                        // Material You / Expressive Squircle Contact Row
                        Box(
                            modifier = Modifier
                                .animateItemPlacement()
                                .scale(rowScale)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isPressed) monetColors.primaryContainer.copy(alpha = 0.35f)
                                    else Color(0xFF191C23)
                                )
                                .border(
                                    0.8.dp,
                                    if (isPressed) monetColors.primary.copy(alpha = 0.5f)
                                    else monetColors.outlineVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = { onOpenDetails(contact) }
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Dynamic Monet Pastel Avatar or Custom Photo
                                Box(
                                    modifier = Modifier.size(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (contactPhotoBitmap != null) {
                                        Image(
                                            bitmap = contactPhotoBitmap,
                                            contentDescription = contact.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(avatarBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = initials,
                                                color = avatarFg,
                                                fontSize = 15.sp,
                                                fontFamily = GoogleSans,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    if (contact.isStarred) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(15.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFB74D)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "Starred",
                                                tint = Color.Black,
                                                modifier = Modifier.size(9.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contact.name,
                                        color = monetColors.onSurface,
                                        fontSize = 15.sp,
                                        fontFamily = GoogleSans,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = DialerRepository.formatPhoneNumber(contact.number),
                                        color = monetColors.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontFamily = GoogleSans,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val msgInteraction = remember { MutableInteractionSource() }
                                    val isMsgPressed by msgInteraction.collectIsPressedAsState()
                                    val msgScale by animateFloatAsState(
                                        targetValue = if (isMsgPressed) 0.88f else 1.0f,
                                        animationSpec = spring(dampingRatio = 0.65f),
                                        label = "msgBtnScale"
                                    )

                                    // Send Message Button
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .scale(msgScale)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(monetColors.surfaceContainerHighest.copy(alpha = 0.7f))
                                            .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .clickable(
                                                interactionSource = msgInteraction,
                                                indication = null,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    DialerRepository.sendSms(context, contact.number)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Chat,
                                            contentDescription = "Message",
                                            tint = monetColors.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Contact Info Button (Per Contact Details Pill)
                                    val infoInteraction = remember { MutableInteractionSource() }
                                    val isInfoPressed by infoInteraction.collectIsPressedAsState()
                                    val infoScale by animateFloatAsState(
                                        targetValue = if (isInfoPressed) 0.88f else 1.0f,
                                        animationSpec = spring(dampingRatio = 0.65f),
                                        label = "infoBtnScale"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .scale(infoScale)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(monetColors.surfaceContainerHighest.copy(alpha = 0.7f))
                                            .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .clickable(
                                                interactionSource = infoInteraction,
                                                indication = null,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    onOpenDetails(contact)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Info,
                                            contentDescription = "Contact Info",
                                            tint = monetColors.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    val callInteraction = remember { MutableInteractionSource() }
                                    val isCallPressed by callInteraction.collectIsPressedAsState()
                                    val callScale by animateFloatAsState(
                                        targetValue = if (isCallPressed) 0.88f else 1.0f,
                                        animationSpec = spring(dampingRatio = 0.65f),
                                        label = "callBtnScale"
                                    )

                                    // Call Button
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .scale(callScale)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF2EAA55))
                                            .clickable(
                                                interactionSource = callInteraction,
                                                indication = null,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    onCall(contact.number)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Call,
                                            contentDescription = "Call",
                                            tint = Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pinned Material 3 Top Header & Search Bar (Solid Gradient Background)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F1116),
                            Color(0xFF0F1116),
                            Color(0xF50F1116),
                            Color(0xD00F1116),
                            Color.Transparent
                        )
                    )
                )
                .statusBarsPadding()
                .padding(top = 10.dp, start = 18.dp, end = 18.dp, bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contacts",
                    color = monetColors.onSurface,
                    fontSize = 28.sp,
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // + Add Contact Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(monetColors.primaryContainer.copy(alpha = 0.8f))
                            .border(0.8.dp, monetColors.primary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    type = ContactsContract.Contacts.CONTENT_TYPE
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            }
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PersonAdd,
                            contentDescription = "Add Contact",
                            tint = monetColors.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "New",
                            color = monetColors.onPrimaryContainer,
                            fontSize = 12.sp,
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Total Count Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(monetColors.surfaceContainerHigh.copy(alpha = 0.7f))
                            .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${contacts.size}",
                            color = monetColors.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    var isMenuExpanded by remember { mutableStateOf(false) }
                    val menuRotation by animateFloatAsState(
                        targetValue = if (isMenuExpanded) 90f else 0f,
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
                        label = "contactsMenuRotate"
                    )

                    Box {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(monetColors.surfaceContainerHigh.copy(alpha = 0.7f))
                                .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isMenuExpanded = !isMenuExpanded
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Options",
                                tint = monetColors.onSurface,
                                modifier = Modifier
                                    .size(18.dp)
                                    .graphicsLayer { rotationZ = menuRotation }
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xF0181B22))
                                .border(1.dp, monetColors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .padding(vertical = 4.dp)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = monetColors.primary, modifier = Modifier.size(18.dp))
                                        Text("Call Settings", color = monetColors.onSurface, fontSize = 14.sp, fontFamily = GoogleSans)
                                    }
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    onOpenSettings()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Rounded.Tune, contentDescription = null, tint = AppleAmber, modifier = Modifier.size(18.dp))
                                        Text("Audio Presets & Personas", color = monetColors.onSurface, fontSize = 14.sp, fontFamily = GoogleSans)
                                    }
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    onOpenPresetManager()
                                }
                            )
                        }
                    }
                }
            }

            // Expressive Material 3 Search Bar
            PremiumGlassSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search ${contacts.size} contacts or numbers..."
            )
        }
    }
}

/**
 * Modern Google Pixel Material 3 Expressive Search Capsule
 * Pill-shaped, dynamic Monet styling, integrated clear/mic & 3-dots overflow menu.
 */
@Composable
fun PixelDialerSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search contacts & numbers...",
    monetColors: ColorScheme = getPixelMonetDarkColorScheme(),
    onMenuClick: () -> Unit = {},
    menuRotation: Float = 0f,
    modifier: Modifier = Modifier,
    menuContent: @Composable () -> Unit = {}
) {
    var isSearchFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (isSearchFocused) monetColors.primary.copy(alpha = 0.65f)
        else monetColors.outlineVariant.copy(alpha = 0.3f),
        animationSpec = tween(durationMillis = 180),
        label = "searchPillBorder"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSearchFocused) monetColors.surfaceContainerHighest.copy(alpha = 0.95f)
        else monetColors.surfaceContainerHigh.copy(alpha = 0.85f),
        animationSpec = tween(durationMillis = 180),
        label = "searchPillBg"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation = if (isSearchFocused) 6.dp else 2.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x50000000)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(bgColor)
            .border(0.8.dp, borderColor, RoundedCornerShape(26.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Search",
            tint = if (isSearchFocused) monetColors.primary else monetColors.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isSearchFocused = it.isFocused },
            textStyle = TextStyle(
                fontFamily = GoogleSans,
                fontSize = 15.sp,
                color = monetColors.onSurface,
                fontWeight = FontWeight.Normal
            ),
            singleLine = true,
            cursorBrush = SolidColor(monetColors.primary),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = GoogleSans,
                        fontSize = 15.sp,
                        color = monetColors.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Normal
                    )
                }
                innerTextField()
            }
        )

        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Clear",
                    tint = monetColors.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "Voice Search",
                tint = monetColors.onSurfaceVariant.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Box(contentAlignment = Alignment.TopEnd) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Menu",
                    tint = monetColors.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = menuRotation }
                )
            }

            menuContent()
        }
    }
}

/**
 * Modern Material 3 Expressive Pixel Dialpad Key Button
 * Squircle/Pill (28dp), tactile spring press scaling, Monet accent container response, Google Sans
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OemDialerKeyButton(
    digit: String,
    letters: String,
    monetColors: ColorScheme = getPixelMonetDarkColorScheme(),
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow),
        label = "keyScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isPressed) monetColors.primaryContainer.copy(alpha = 0.9f)
        else monetColors.surfaceContainerHigh.copy(alpha = 0.95f),
        animationSpec = tween(durationMillis = 100),
        label = "keyBgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isPressed) monetColors.primary.copy(alpha = 0.6f)
        else monetColors.outlineVariant.copy(alpha = 0.3f),
        animationSpec = tween(durationMillis = 100),
        label = "keyBorderColor"
    )

    Box(
        modifier = Modifier
            .size(74.dp, 72.dp)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 1.dp else 3.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color(0x40000000)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(bgColor)
            .border(0.8.dp, borderColor, RoundedCornerShape(28.dp))
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
                color = if (isPressed) monetColors.onPrimaryContainer else monetColors.onSurface,
                fontSize = if (digit == "*" || digit == "#") 32.sp else 30.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GoogleSans,
                lineHeight = 30.sp
            )
            if (digit == "1") {
                // Modern Google Pixel Dialer Voicemail icon
                Icon(
                    imageVector = Icons.Rounded.Voicemail,
                    contentDescription = "Voicemail",
                    tint = if (isPressed) monetColors.onPrimaryContainer.copy(alpha = 0.85f)
                    else monetColors.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.size(13.dp)
                )
            } else if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    color = if (isPressed) monetColors.onPrimaryContainer.copy(alpha = 0.85f)
                    else monetColors.onSurfaceVariant.copy(alpha = 0.85f),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSans,
                    letterSpacing = 1.8.sp,
                    lineHeight = 11.sp
                )
            } else if (digit == "0") {
                Text(
                    text = "+",
                    color = if (isPressed) monetColors.onPrimaryContainer else monetColors.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSans,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

/**
 * Modern Material 3 Expressive Pixel Emerald Call Button
 * Google Phone Call Green, tactile spring press scaling, elevated squircle (28dp), white handset icon
 */
@Composable
fun OemCallButton(
    monetColors: ColorScheme = getPixelMonetDarkColorScheme(),
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.89f else 1.0f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow),
        label = "callBtnScale"
    )

    Box(
        modifier = Modifier
            .size(74.dp, 72.dp)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 8.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color(0x9034A853),
                ambientColor = Color(0x5034A853)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF34A853), // Google Phone Call Green 500
                        Color(0xFF1E8E3E)  // Google Phone Call Green 700
                    )
                )
            )
            .border(
                1.dp,
                if (isPressed) Color(0x90FFFFFF) else Color(0x40FFFFFF),
                RoundedCornerShape(28.dp)
            )
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
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

private data class KeypadRecentItem(
    val id: Long,
    val name: String,
    val number: String,
    val type: Int,
    val timestamp: Long,
    val isStarred: Boolean = false,
    val photoUri: String? = null
)

/**
 * Screen 3: Keypad (Google Pixel Material 3 Expressive Dialpad, T9 Suggestions, 100% Fixed Immobile Keys)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeypadScreen(
    contacts: List<ContactItem> = emptyList(),
    callLogs: List<CallLogItem> = emptyList(),
    onCall: (String) -> Unit,
    onOpenDetails: (ContactItem) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenPresetManager: () -> Unit = {},
    onSwitchTab: (DialerTab) -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var typedNumber by remember { mutableStateOf("") }
    var keypadSearchQuery by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }

    val monetColors: ColorScheme = remember(context) {
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

    val t9Matches = remember(typedNumber, contacts, callLogs) {
        if (typedNumber.isBlank()) emptyList()
        else {
            val numToFreq = callLogs.groupingBy { log ->
                val d = log.number.filter { it.isDigit() }
                if (d.length >= 7) d.takeLast(10) else d
            }.eachCount()

            contacts.filter { T9Helper.matches(it, typedNumber) }
                .sortedWith(
                    compareByDescending<ContactItem> { it.isStarred }
                        .thenByDescending { contact ->
                            val d = contact.number.filter { it.isDigit() }
                            val key = if (d.length >= 7) d.takeLast(10) else d
                            numToFreq[key] ?: 0
                        }
                        .thenBy { it.name }
                )
                .take(8)
        }
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

    val menuRotation by animateFloatAsState(
        targetValue = if (isMenuExpanded) 90f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "keypadMenuRotate"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = statusBarTop + 10.dp)
            .navigationBarsPadding()
            .padding(bottom = 86.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Pinned Header: "Keypad" Title & Voicemail Shortcut
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Keypad",
                color = monetColors.onSurface,
                fontSize = 28.sp,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            // Voicemail / Speed Dial Capsule
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(monetColors.surfaceContainerHigh.copy(alpha = 0.7f))
                    .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCall("1")
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Voicemail,
                    contentDescription = "Voicemail",
                    tint = monetColors.primary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Voicemail",
                    color = monetColors.onSurface,
                    fontSize = 12.sp,
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Google Pixel Search Pill with integrated Menu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            PixelDialerSearchBar(
                query = keypadSearchQuery,
                onQueryChange = { keypadSearchQuery = it },
                placeholder = "Search contacts or numbers...",
                monetColors = monetColors,
                onMenuClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isMenuExpanded = !isMenuExpanded
                },
                menuRotation = menuRotation,
                menuContent = {
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false },
                        offset = androidx.compose.ui.unit.DpOffset(x = 0.dp, y = 6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xF0181B22))
                            .border(1.dp, monetColors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(vertical = 4.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Rounded.Settings, contentDescription = null, tint = monetColors.primary, modifier = Modifier.size(18.dp))
                                    Text("Call Settings", color = monetColors.onSurface, fontSize = 14.sp, fontFamily = GoogleSans)
                                }
                            },
                            onClick = {
                                isMenuExpanded = false
                                onOpenSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Rounded.Tune, contentDescription = null, tint = AppleAmber, modifier = Modifier.size(18.dp))
                                    Text("Audio Presets & Personas", color = monetColors.onSurface, fontSize = 14.sp, fontFamily = GoogleSans)
                                }
                            },
                            onClick = {
                                isMenuExpanded = false
                                onOpenPresetManager()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = Color(0xFF2EAA55), modifier = Modifier.size(18.dp))
                                    Text("AI Screening Studio", color = monetColors.onSurface, fontSize = 14.sp, fontFamily = GoogleSans)
                                }
                            },
                            onClick = {
                                isMenuExpanded = false
                                onSwitchTab(DialerTab.ASSISTANT)
                            }
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Upper Dynamic Section: Recents / T9 Suggestions / Dialed Number
        AnimatedContent(
            targetState = typedNumber.isEmpty(),
            transitionSpec = {
                (fadeIn(animationSpec = tween(200, easing = LinearOutSlowInEasing)) +
                 scaleIn(initialScale = 0.96f, animationSpec = tween(200, easing = LinearOutSlowInEasing)))
                .togetherWith(
                    fadeOut(animationSpec = tween(140, easing = FastOutLinearInEasing)) +
                    scaleOut(targetScale = 0.96f, animationSpec = tween(140, easing = FastOutLinearInEasing))
                )
            },
            label = "KeypadDisplayTransition",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) { isEmpty ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isEmpty) {
                    val keypadMatches = remember(keypadSearchQuery, contacts) {
                        if (keypadSearchQuery.isBlank()) emptyList()
                        else contacts.filter {
                            it.name.contains(keypadSearchQuery, ignoreCase = true) ||
                            it.number.contains(keypadSearchQuery)
                        }.take(4)
                    }

                    if (keypadSearchQuery.isNotEmpty()) {
                        if (keypadMatches.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF181B22))
                                    .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                keypadMatches.forEach { contact ->
                                    val (avatarBg, avatarFg) = remember(contact.name) {
                                        getMonetAvatarColors(contact.name, monetColors)
                                    }
                                    val matchInitials = remember(contact.name) {
                                        getContactInitials(contact.name)
                                    }
                                    val rowInteraction = remember { MutableInteractionSource() }
                                    val isRowPressed by rowInteraction.collectIsPressedAsState()
                                    val rowScale by animateFloatAsState(
                                        targetValue = if (isRowPressed) 0.97f else 1.0f,
                                        animationSpec = spring(dampingRatio = 0.7f),
                                        label = "keypadSearchMatchScale"
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .scale(rowScale)
                                            .clickable(
                                                interactionSource = rowInteraction,
                                                indication = null,
                                                onClick = { onOpenDetails(contact) }
                                            )
                                            .padding(horizontal = 14.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(avatarBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = matchInitials,
                                                color = avatarFg,
                                                fontSize = 12.sp,
                                                fontFamily = GoogleSans,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = contact.name,
                                                color = monetColors.onSurface,
                                                fontSize = 13.5.sp,
                                                fontFamily = GoogleSans,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = DialerRepository.formatPhoneNumber(contact.number),
                                                color = monetColors.onSurfaceVariant,
                                                fontSize = 11.5.sp,
                                                fontFamily = GoogleSans,
                                                maxLines = 1
                                            )
                                        }

                                        val callInteraction = remember { MutableInteractionSource() }
                                        val isCallPressed by callInteraction.collectIsPressedAsState()
                                        val callScale by animateFloatAsState(
                                            targetValue = if (isCallPressed) 0.88f else 1.0f,
                                            animationSpec = spring(dampingRatio = 0.65f),
                                            label = "keypadMatchCallScale"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .scale(callScale)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF2EAA55))
                                                .clickable(
                                                    interactionSource = callInteraction,
                                                    indication = null,
                                                    onClick = { onCall(contact.number) }
                                                ),
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
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(monetColors.surfaceContainerHigh.copy(alpha = 0.6f))
                                    .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                    .clickable {
                                        typedNumber = keypadSearchQuery
                                        keypadSearchQuery = ""
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Dialpad,
                                    contentDescription = null,
                                    tint = monetColors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dial \"$keypadSearchQuery\" on keypad",
                                    color = monetColors.onSurface,
                                    fontSize = 13.sp,
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // Premium Pixel Expressive Favorites & Recent Contacts
                        val keypadRecents = remember(callLogs, contacts) {
                            val list = mutableListOf<KeypadRecentItem>()
                            val seen = HashSet<String>()

                            fun norm(num: String): String {
                                val d = num.filter { it.isDigit() }
                                return if (d.length >= 7) d.takeLast(10) else d.ifEmpty { num }
                            }

                            // 1. Prioritize Starred / Favorite Contacts
                            for (c in contacts.filter { it.isStarred }) {
                                val digits = norm(c.number)
                                if (digits.isNotEmpty() && seen.add(digits)) {
                                    list.add(
                                        KeypadRecentItem(
                                            id = c.id,
                                            name = c.name,
                                            number = c.number,
                                            type = CallLog.Calls.OUTGOING_TYPE,
                                            timestamp = 0L,
                                            isStarred = true,
                                            photoUri = c.photoUri
                                        )
                                    )
                                    if (list.size >= 5) break
                                }
                            }

                            // 2. Add Recent Calls
                            for (log in callLogs) {
                                val digits = norm(log.number)
                                if (seen.add(digits)) {
                                    val matched = contacts.firstOrNull { c -> norm(c.number) == digits }
                                    list.add(
                                        KeypadRecentItem(
                                            id = log.id,
                                            name = log.name ?: matched?.name ?: DialerRepository.formatPhoneNumber(log.number),
                                            number = log.number,
                                            type = log.type,
                                            timestamp = log.timestamp,
                                            isStarred = matched?.isStarred ?: false,
                                            photoUri = matched?.photoUri
                                        )
                                    )
                                    if (list.size >= 12) break
                                }
                            }

                            // 3. Fallback to contacts if call history is empty
                            if (list.isEmpty()) {
                                contacts.take(8).map { c ->
                                    KeypadRecentItem(
                                        id = c.id,
                                        name = c.name,
                                        number = c.number,
                                        type = CallLog.Calls.OUTGOING_TYPE,
                                        timestamp = 0L,
                                        isStarred = c.isStarred,
                                        photoUri = c.photoUri
                                    )
                                }
                            } else {
                                list
                            }
                        }

                        if (keypadRecents.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = monetColors.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Favorites & Recents",
                                        fontFamily = GoogleSans,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = monetColors.onSurface,
                                        letterSpacing = (-0.2).sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(monetColors.surfaceContainerHigh.copy(alpha = 0.6f))
                                        .border(0.6.dp, monetColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${keypadRecents.size} quick access",
                                        fontFamily = GoogleSans,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = monetColors.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(keypadRecents, key = { _, item -> "rec_${item.id}_${item.number}" }) { _, item ->
                                    val (avatarBg, avatarFg) = remember(item.name) {
                                        getMonetAvatarColors(item.name, monetColors)
                                    }
                                    val itemInitials = remember(item.name) {
                                        getContactInitials(item.name)
                                    }
                                    val contactPhotoBitmap = rememberContactPhotoBitmap(item.photoUri)

                                    val itemInteraction = remember { MutableInteractionSource() }
                                    val isItemPressed by itemInteraction.collectIsPressedAsState()
                                    val itemScale by animateFloatAsState(
                                        targetValue = if (isItemPressed) 0.95f else 1.0f,
                                        animationSpec = spring(dampingRatio = 0.7f),
                                        label = "recentCardScale"
                                    )

                                    Row(
                                        modifier = Modifier
                                            .width(180.dp)
                                            .height(68.dp)
                                            .scale(itemScale)
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(
                                                if (isItemPressed) monetColors.primaryContainer.copy(alpha = 0.35f)
                                                else monetColors.surfaceContainerHigh.copy(alpha = 0.85f)
                                            )
                                            .border(
                                                0.8.dp,
                                                if (isItemPressed) monetColors.primary.copy(alpha = 0.55f)
                                                else monetColors.outlineVariant.copy(alpha = 0.35f),
                                                RoundedCornerShape(22.dp)
                                            )
                                            .clickable(
                                                interactionSource = itemInteraction,
                                                indication = null,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    typedNumber = item.number
                                                }
                                            )
                                            .padding(horizontal = 11.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Large Avatar with Photo or Star badge
                                        Box(
                                            modifier = Modifier.size(44.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(avatarBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (contactPhotoBitmap != null) {
                                                    androidx.compose.foundation.Image(
                                                        bitmap = contactPhotoBitmap,
                                                        contentDescription = item.name,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                    )
                                                } else {
                                                    Text(
                                                        text = itemInitials,
                                                        color = avatarFg,
                                                        fontSize = 15.sp,
                                                        fontFamily = GoogleSans,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            if (item.isStarred) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFFFB74D)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Star,
                                                        contentDescription = null,
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(9.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = item.name,
                                                color = monetColors.onSurface,
                                                fontSize = 13.5.sp,
                                                fontFamily = GoogleSans,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                val (typeIcon, typeTint) = when {
                                                    item.isStarred -> Icons.Filled.Star to Color(0xFFFFB74D)
                                                    item.type == CallLog.Calls.INCOMING_TYPE -> Icons.Filled.CallReceived to Color(0xFF4CAF50)
                                                    item.type == CallLog.Calls.OUTGOING_TYPE -> Icons.Filled.CallMade to Color(0xFF2196F3)
                                                    item.type == CallLog.Calls.MISSED_TYPE -> Icons.Filled.CallMissed to Color(0xFFFF5252)
                                                    else -> Icons.Filled.Call to monetColors.onSurfaceVariant
                                                }
                                                Icon(
                                                    imageVector = typeIcon,
                                                    contentDescription = null,
                                                    tint = typeTint,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                val subText = when {
                                                    item.isStarred -> "Favorite"
                                                    item.type == CallLog.Calls.MISSED_TYPE -> "Missed"
                                                    item.type == CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                                                    item.type == CallLog.Calls.INCOMING_TYPE -> "Incoming"
                                                    else -> "Recent"
                                                }
                                                Text(
                                                    text = subText,
                                                    color = monetColors.onSurfaceVariant.copy(alpha = 0.8f),
                                                    fontSize = 11.sp,
                                                    fontFamily = GoogleSans,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        // Quick Emerald Call Button
                                        val callInteraction = remember { MutableInteractionSource() }
                                        val isCallPressed by callInteraction.collectIsPressedAsState()
                                        val callScale by animateFloatAsState(
                                            targetValue = if (isCallPressed) 0.88f else 1.0f,
                                            animationSpec = spring(dampingRatio = 0.65f),
                                            label = "recentCallScale"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .scale(callScale)
                                                .clip(RoundedCornerShape(11.dp))
                                                .background(Color(0xFF2EAA55))
                                                .clickable(
                                                    interactionSource = callInteraction,
                                                    indication = null,
                                                    onClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        onCall(item.number)
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Call,
                                                contentDescription = "Call",
                                                tint = Color.White,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(monetColors.surfaceContainerHigh.copy(alpha = 0.45f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = monetColors.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Hold 0 for + • Hold 1 for Voicemail",
                                color = monetColors.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontFamily = GoogleSans
                            )
                        }
                    }
                } else {
                    // TYPING STATE: DIALED NUMBER & T9 MATCHES
                    val textSize = when {
                        typedNumber.length > 15 -> 22.sp
                        typedNumber.length > 12 -> 26.sp
                        typedNumber.length > 9 -> 32.sp
                        else -> 38.sp
                    }

                    Text(
                        text = formattedNumber,
                        color = monetColors.onSurface,
                        fontSize = textSize,
                        fontWeight = FontWeight.Normal,
                        fontFamily = GoogleSans,
                        letterSpacing = (-0.5).sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(monetColors.surfaceContainerHigh.copy(alpha = 0.5f))
                            .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    type = ContactsContract.RawContacts.CONTENT_TYPE
                                    putExtra(ContactsContract.Intents.Insert.PHONE, typedNumber)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PersonAdd,
                            contentDescription = null,
                            tint = monetColors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Add to Contacts",
                            color = monetColors.primary,
                            fontSize = 12.5.sp,
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // T9 SUGGESTIONS CAROUSEL
                    AnimatedVisibility(
                        visible = t9Matches.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
                        exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SUGGESTIONS (${t9Matches.size})",
                                    fontFamily = GoogleSans,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = monetColors.primary,
                                    letterSpacing = 0.6.sp
                                )
                                Text(
                                    text = "Tap to call",
                                    fontFamily = GoogleSans,
                                    fontSize = 10.5.sp,
                                    color = monetColors.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(t9Matches, key = { index, match -> "t9_${match.id}_${match.number}_$index" }) { _, match ->
                                    val (avatarBg, avatarFg) = remember(match.name) {
                                        getMonetAvatarColors(match.name, monetColors)
                                    }
                                    val matchInitials = remember(match.name) {
                                        getContactInitials(match.name)
                                    }

                                    val cardInteraction = remember { MutableInteractionSource() }
                                    val isCardPressed by cardInteraction.collectIsPressedAsState()
                                    val cardScale by animateFloatAsState(
                                        targetValue = if (isCardPressed) 0.95f else 1.0f,
                                        animationSpec = spring(dampingRatio = 0.7f),
                                        label = "t9CardScale"
                                    )

                                    Row(
                                        modifier = Modifier
                                            .width(184.dp)
                                            .height(56.dp)
                                            .scale(cardScale)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(
                                                if (isCardPressed) monetColors.primaryContainer.copy(alpha = 0.35f)
                                                else monetColors.surfaceContainerHigh.copy(alpha = 0.75f)
                                            )
                                            .border(
                                                0.8.dp,
                                                if (isCardPressed) monetColors.primary.copy(alpha = 0.6f)
                                                else monetColors.outlineVariant.copy(alpha = 0.35f),
                                                RoundedCornerShape(18.dp)
                                            )
                                            .clickable(
                                                interactionSource = cardInteraction,
                                                indication = null,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    onCall(match.number)
                                                }
                                            )
                                            .padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(36.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(avatarBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = matchInitials,
                                                    color = avatarFg,
                                                    fontSize = 12.sp,
                                                    fontFamily = GoogleSans,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (match.isStarred) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .size(13.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFFFB74D)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Star,
                                                        contentDescription = null,
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(8.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = match.name,
                                                color = monetColors.onSurface,
                                                fontSize = 13.sp,
                                                fontFamily = GoogleSans,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = DialerRepository.formatPhoneNumber(match.number),
                                                color = monetColors.onSurfaceVariant,
                                                fontSize = 11.sp,
                                                fontFamily = GoogleSans,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Quick Call Pill
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF2EAA55))
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    onCall(match.number)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Call,
                                                contentDescription = "Call",
                                                tint = Color.White,
                                                modifier = Modifier.size(15.dp)
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

        Spacer(modifier = Modifier.weight(1f))

        // 3. Pixel Material 3 Expressive Dialpad (Seamless, Floating, Dynamic Monet Surface)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in 0..3) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val (digit, letters) = keys[index]

                        OemDialerKeyButton(
                            digit = digit,
                            letters = letters,
                            monetColors = monetColors,
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
                                } else if (digit == "1") {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    playTone(digit)
                                    if (typedNumber.isEmpty()) {
                                        onCall("1")
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // 5th Row: Symmetrical Call & Backspace Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Invisible 74dp placeholder for geometric centering under column 1
                Box(modifier = Modifier.size(74.dp, 72.dp))

                // Column 2: Pixel Emerald Call Button (Rock-solid directly under '0' key)
                OemCallButton(
                    monetColors = monetColors,
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

                // Column 3: Backspace Button in 74dp slot with spring pop animation
                Box(
                    modifier = Modifier.size(74.dp, 72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = typedNumber.isNotEmpty(),
                        enter = scaleIn(
                            initialScale = 0.65f,
                            animationSpec = spring(
                                dampingRatio = 0.72f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(animationSpec = tween(150)),
                        exit = scaleOut(
                            targetScale = 0.65f,
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeOut(animationSpec = tween(120))
                    ) {
                        val backspaceInteraction = remember { MutableInteractionSource() }
                        val isBackspacePressed by backspaceInteraction.collectIsPressedAsState()
                        val backspaceScale by animateFloatAsState(
                            targetValue = if (isBackspacePressed) 0.88f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.65f),
                            label = "backspaceScale"
                        )

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .scale(backspaceScale)
                                .clip(RoundedCornerShape(22.dp))
                                .background(monetColors.surfaceContainerHigh.copy(alpha = 0.85f))
                                .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                                .combinedClickable(
                                    interactionSource = backspaceInteraction,
                                    indication = null,
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
                                tint = monetColors.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
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
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val monetColors: ColorScheme = remember(context) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = statusBarTop + 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header: Material You Pixel Expressive Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(monetColors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = monetColors.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Assistant",
                    color = monetColors.onSurface,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSans,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Neural speech synthesis, voice personas & soundboard",
                    color = monetColors.onSurfaceVariant,
                    fontSize = 12.5.sp,
                    fontFamily = GoogleSans
                )
            }
        }

        // Section 1: Real-Time Voice Studio & Live DSP
        SettingsSectionHeader(title = "VOICE STUDIO", monetColors = monetColors)
        GlassVoiceStudioCard(
            selectedPersona = selectedPersona,
            onSelectPersona = onSelectPersona,
            isLiveMicActive = isLiveMicActive,
            onToggleLiveMic = onToggleLiveMic,
            monetColors = monetColors
        )

        // Section 2: Audio Presets & Direct Hardware Uplink Soundboard
        presetManager?.let { pm ->
            SettingsSectionHeader(title = "SOUNDBOARD & PRESETS", monetColors = monetColors)
            GlassAudioPresetsCard(
                presetManager = pm,
                onOpenManager = onOpenPresetManager,
                monetColors = monetColors
            )
        }

        // Section 3: Call Screening & Simulation
        SettingsSectionHeader(title = "SCREENING BEHAVIOR & SIMULATOR", monetColors = monetColors)
        SettingsGroupCard(monetColors = monetColors) {
            // Row 1: Automated Voice Greeting
            SettingsItemRow(
                icon = Icons.Rounded.RecordVoiceOver,
                iconTint = monetColors.primary,
                iconBgColor = monetColors.primary.copy(alpha = 0.14f),
                title = "Automated Voice Greeting",
                subtitle = if (isGreetingEnabled)
                    "Speaks automated assistant greeting on call pickup"
                else
                    "Silent pickup: ready for speech relay without intro",
                monetColors = monetColors,
                trailingContent = {
                    Switch(
                        checked = isGreetingEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleGreeting(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = monetColors.onPrimary,
                            checkedTrackColor = monetColors.primary,
                            uncheckedThumbColor = monetColors.outline,
                            uncheckedTrackColor = monetColors.surfaceVariant
                        )
                    )
                }
            )

            SettingsDivider(monetColors = monetColors)

            // Row 2: Call Screener Simulator
            SettingsItemRow(
                icon = Icons.Rounded.Headphones,
                iconTint = Color(0xFF81C995),
                iconBgColor = Color(0xFF81C995).copy(alpha = 0.16f),
                title = "Call Screener Simulator",
                subtitle = "Test speech relay with Bluetooth earbuds or speaker",
                monetColors = monetColors,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF81C995).copy(alpha = 0.18f))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onStartSimulation()
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PhoneInTalk,
                                contentDescription = null,
                                tint = Color(0xFF81C995),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Launch",
                                color = Color(0xFF81C995),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = GoogleSans
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
    onToggleLiveMic: () -> Unit = {},
    monetColors: ColorScheme = getPixelMonetDarkColorScheme()
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
            .clip(RoundedCornerShape(24.dp))
            .background(monetColors.surfaceVariant.copy(alpha = 0.45f))
            .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row: Material You Expressive Squircle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(monetColors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RecordVoiceOver,
                        contentDescription = null,
                        tint = monetColors.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "AI Voice Studio",
                        color = monetColors.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GoogleSans,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Select neural voice for live calls & screening",
                        color = monetColors.onSurfaceVariant,
                        fontSize = 11.5.sp,
                        fontFamily = GoogleSans
                    )
                }
            }

            // Material You Category Segmented Pill Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(monetColors.surfaceContainerHigh.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEachIndexed { idx, categoryTitle ->
                    val isTabActive = activeCategoryIndex == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isTabActive) monetColors.primary else Color.Transparent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                activeCategoryIndex = idx
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = categoryTitle,
                            color = if (isTabActive) monetColors.onPrimary else monetColors.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            fontWeight = if (isTabActive) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = GoogleSans
                        )
                    }
                }
            }

            // Horizontal Persona Carousel
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(currentPersonas, key = { it.id }) { persona ->
                    val isSelected = persona.id == selectedPersona.id
                    Box(
                        modifier = Modifier
                            .width(155.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) monetColors.primaryContainer
                                else monetColors.surfaceContainerHighest.copy(alpha = 0.5f)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.8.dp,
                                color = if (isSelected) monetColors.primary else monetColors.outlineVariant.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(20.dp)
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
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) monetColors.primary
                                            else monetColors.surfaceVariant
                                        )
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = persona.code,
                                        color = if (isSelected) monetColors.onPrimary else monetColors.onSurfaceVariant,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = GoogleSans
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(monetColors.primary)
                                    )
                                }
                            }

                            Text(
                                text = persona.name,
                                color = if (isSelected) monetColors.onPrimaryContainer else monetColors.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = GoogleSans,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = persona.tag,
                                color = if (isSelected) monetColors.onPrimaryContainer.copy(alpha = 0.8f) else monetColors.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontFamily = GoogleSans,
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
                    .clip(RoundedCornerShape(16.dp))
                    .background(monetColors.surfaceContainerHigh.copy(alpha = 0.4f))
                    .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "${selectedPersona.name} • ${selectedPersona.tag}: ${selectedPersona.description}",
                    color = monetColors.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    fontFamily = GoogleSans
                )
            }

            // Live AI Voice Relay Transcript Capsule
            AnimatedVisibility(visible = sessionState.isListeningToUserSpeech || sessionState.userLiveTranscript.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(monetColors.secondaryContainer.copy(alpha = 0.7f))
                        .border(1.dp, monetColors.secondary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(monetColors.onSecondaryContainer)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (sessionState.userLiveTranscript.isNotBlank())
                                "Recognized: \"${sessionState.userLiveTranscript}\""
                            else
                                "AI Voice Relay: Listening to microphone... Speak now",
                            color = monetColors.onSecondaryContainer,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = GoogleSans
                        )
                    }
                }
            }

            // Action Controls: Sample Voice & Test AI Voice Relay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button 1: Sample Voice
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(monetColors.primary.copy(alpha = 0.15f))
                        .border(0.8.dp, monetColors.primary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
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
                            tint = monetColors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Sample Voice",
                            color = monetColors.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GoogleSans
                        )
                    }
                }

                // Button 2: Test AI Voice Relay Mic
                val isListening = sessionState.isListeningToUserSpeech
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isListening) Color(0xFF81C995).copy(alpha = 0.22f)
                            else monetColors.surfaceContainerHigh.copy(alpha = 0.7f)
                        )
                        .border(
                            0.8.dp,
                            if (isListening) Color(0xFF81C995) else monetColors.outlineVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
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
                            imageVector = if (isListening) Icons.Filled.Mic else Icons.Filled.MicOff,
                            contentDescription = null,
                            tint = if (isListening) Color(0xFF81C995) else monetColors.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isListening) "Listening..." else "Test Relay Mic",
                            color = if (isListening) Color(0xFF81C995) else monetColors.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GoogleSans
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
    onOpenManager: () -> Unit = {},
    monetColors: ColorScheme = getPixelMonetDarkColorScheme()
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
            .clip(RoundedCornerShape(24.dp))
            .background(monetColors.surfaceVariant.copy(alpha = 0.45f))
            .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row: Material You Expressive Squircle
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(monetColors.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = monetColors.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
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
                                color = monetColors.onSurface,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = GoogleSans,
                                letterSpacing = (-0.3).sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(monetColors.surfaceContainerHighest.copy(alpha = 0.7f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ALSA",
                                    color = monetColors.onSurfaceVariant,
                                    fontSize = 9.sp,
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Direct hardware soundboard (${presets.size} items)",
                            color = monetColors.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            fontFamily = GoogleSans,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Manage / Reorder Pill Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(monetColors.primary.copy(alpha = 0.15f))
                        .border(0.8.dp, monetColors.primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenManager()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Manage",
                            tint = monetColors.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Arrange",
                            color = monetColors.primary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GoogleSans
                        )
                    }
                }
            }

            // Quick Access Preview (Top 3 Presets in Clean Pixel Squircles)
            val previewPresets = remember(presets) { presets.take(3) }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                previewPresets.forEach { preset ->
                    val isPlaying = currentlyPlayingId == preset.id

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isPlaying) monetColors.primaryContainer.copy(alpha = 0.7f)
                                else monetColors.surfaceContainerHigh.copy(alpha = 0.5f)
                            )
                            .border(
                                0.8.dp,
                                if (isPlaying) monetColors.primary else monetColors.outlineVariant.copy(alpha = 0.25f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(monetColors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (preset.isAudioFile) Icons.Rounded.GraphicEq else Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = null,
                                    tint = monetColors.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.title,
                                    color = monetColors.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = GoogleSans,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (preset.isAudioFile) "Custom Audio Track" else preset.textOrPath,
                                    color = monetColors.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontFamily = GoogleSans,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isPlaying) Color(0xFFF28B82)
                                        else monetColors.primary
                                    )
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
                                    tint = if (isPlaying) Color.White else monetColors.onPrimary,
                                    modifier = Modifier.size(16.dp)
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
                    .clip(RoundedCornerShape(16.dp))
                    .background(monetColors.surfaceContainerHigh.copy(alpha = 0.6f))
                    .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenManager()
                    }
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DragIndicator,
                            contentDescription = null,
                            tint = monetColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Hold & Drag to Reorder (${presets.size} Presets)",
                                color = monetColors.onSurface,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = GoogleSans
                            )
                            Text(
                                text = "Align in-call audio tracks, arrange & organize",
                                color = monetColors.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontFamily = GoogleSans
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Open",
                        tint = monetColors.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Action Buttons: Add Custom Phrase & Import Audio File
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(monetColors.surfaceContainerHigh.copy(alpha = 0.7f))
                        .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .clickable {
                            textPresetTitle = ""
                            textPresetPhrase = ""
                            showAddTextDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.RecordVoiceOver, contentDescription = null, tint = monetColors.onSurface, modifier = Modifier.size(15.dp))
                        Text("Add Phrase", color = monetColors.onSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = GoogleSans)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(monetColors.primaryContainer)
                        .border(0.8.dp, monetColors.primary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable {
                            audioPickerLauncher.launch("audio/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = monetColors.onPrimaryContainer, modifier = Modifier.size(15.dp))
                        Text("Import Audio", color = monetColors.onPrimaryContainer, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = GoogleSans)
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
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF1E222D))
                    .border(1.dp, monetColors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
                    .padding(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Add Custom Voice Phrase", color = monetColors.onSurface, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = GoogleSans)
                    Text("Assistant speaks this response directly into caller's ear with 0% background noise.", color = monetColors.onSurfaceVariant, fontSize = 12.sp, fontFamily = GoogleSans)

                    BasicTextField(
                        value = textPresetTitle,
                        onValueChange = { textPresetTitle = it },
                        textStyle = TextStyle(color = monetColors.onSurface, fontSize = 14.sp, fontFamily = GoogleSans),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(monetColors.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(14.dp)
                            ) {
                                if (textPresetTitle.isBlank()) Text("Preset Title (e.g. Can't talk)", color = monetColors.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp, fontFamily = GoogleSans)
                                inner()
                            }
                        }
                    )

                    BasicTextField(
                        value = textPresetPhrase,
                        onValueChange = { textPresetPhrase = it },
                        textStyle = TextStyle(color = monetColors.onSurface, fontSize = 14.sp, fontFamily = GoogleSans),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(monetColors.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(14.dp)
                            ) {
                                if (textPresetPhrase.isBlank()) Text("Phrase to speak (e.g. I am in a meeting, call back later)", color = monetColors.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp, fontFamily = GoogleSans)
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
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = monetColors.onSurfaceVariant)
                        ) {
                            Text("Cancel", fontFamily = GoogleSans)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (textPresetPhrase.isNotBlank()) {
                                    val title = textPresetTitle.ifBlank { "Quick Response" }
                                    presetManager.addCustomTextPreset(title, textPresetPhrase)
                                    showAddTextDialog = false
                                    Toast.makeText(context, "Added preset \"$title\"", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = monetColors.primary)
                        ) {
                            Text("Save Preset", color = monetColors.onPrimary, fontWeight = FontWeight.Bold, fontFamily = GoogleSans)
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
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF1E222D))
                    .border(1.dp, monetColors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
                    .padding(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Name Audio Preset", color = monetColors.onSurface, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = GoogleSans)
                    Text("File imported for hardware uplink injection during active calls.", color = monetColors.onSurfaceVariant, fontSize = 12.sp, fontFamily = GoogleSans)

                    BasicTextField(
                        value = audioPresetTitle,
                        onValueChange = { audioPresetTitle = it },
                        textStyle = TextStyle(color = monetColors.onSurface, fontSize = 14.sp, fontFamily = GoogleSans),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(monetColors.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(14.dp)
                            ) {
                                if (audioPresetTitle.isBlank()) Text("Preset Name (e.g. Office Ambient / Ringtone)", color = monetColors.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp, fontFamily = GoogleSans)
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
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = monetColors.onSurfaceVariant)
                        ) {
                            Text("Cancel", fontFamily = GoogleSans)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
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
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = monetColors.primary)
                        ) {
                            Text("Import", color = monetColors.onPrimary, fontWeight = FontWeight.Bold, fontFamily = GoogleSans)
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
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF1E222D))
                    .border(1.dp, monetColors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
                    .padding(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Rename Preset", color = monetColors.onSurface, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = GoogleSans)
                    Text("Enter a custom title so you know which preset is playing during calls.", color = monetColors.onSurfaceVariant, fontSize = 12.sp, fontFamily = GoogleSans)

                    BasicTextField(
                        value = renamePresetTitle,
                        onValueChange = { renamePresetTitle = it },
                        textStyle = TextStyle(color = monetColors.onSurface, fontSize = 14.sp, fontFamily = GoogleSans),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(monetColors.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(14.dp)
                            ) {
                                if (renamePresetTitle.isBlank()) Text("Preset Name", color = monetColors.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp, fontFamily = GoogleSans)
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
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = monetColors.onSurfaceVariant)
                        ) {
                            Text("Cancel", fontFamily = GoogleSans)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                val targetId = presetToRenameId
                                if (targetId != null && renamePresetTitle.isNotBlank()) {
                                    presetManager.renamePreset(targetId, renamePresetTitle)
                                    Toast.makeText(context, "Renamed to \"$renamePresetTitle\"", Toast.LENGTH_SHORT).show()
                                }
                                showRenameDialog = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = monetColors.primary)
                        ) {
                            Text("Save", color = monetColors.onPrimary, fontWeight = FontWeight.Bold, fontFamily = GoogleSans)
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
 * Material You / Pixel Expressive Settings Components
 */
@Composable
fun SettingsSectionHeader(
    title: String,
    monetColors: ColorScheme = getPixelMonetDarkColorScheme()
) {
    Text(
        text = title,
        color = monetColors.primary,
        fontSize = 11.5.sp,
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 6.dp, top = 10.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsGroupCard(
    monetColors: ColorScheme = getPixelMonetDarkColorScheme(),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0x40000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF191C23))
            .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
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
    monetColors: ColorScheme = getPixelMonetDarkColorScheme(),
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Uniform 40dp rounded squircle icon container
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBgColor)
                .border(0.8.dp, monetColors.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Aligned title and subtitle column in Google Sans
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = monetColors.onSurface,
                fontSize = 15.sp,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = monetColors.onSurfaceVariant,
                fontSize = 12.sp,
                fontFamily = GoogleSans,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        trailingContent()
    }
}

@Composable
fun SettingsDivider(
    monetColors: ColorScheme = getPixelMonetDarkColorScheme()
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 70.dp, end = 16.dp)
            .height(0.6.dp)
            .background(monetColors.outlineVariant.copy(alpha = 0.25f))
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
    callLogs: List<CallLogItem> = emptyList(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val monetColors: ColorScheme = remember(context) {
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

    var showCallStatsScreen by remember { mutableStateOf(false) }
    var showRecordingsDialog by remember { mutableStateOf(false) }
    var showSpeechJammerDialog by remember { mutableStateOf(false) }
    var rootAvailable by remember { mutableStateOf(RootPrivilegeManager.isRootAvailable()) }
    var isPrivileged by remember { mutableStateOf(RootPrivilegeManager.isPrivilegedApp(context)) }
    var isModuleInstalled by remember { mutableStateOf(RootPrivilegeManager.isMagiskModuleInstalled()) }
    var isInstalling by remember { mutableStateOf(false) }

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
                    .background(monetColors.background)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Modern Pixel Expressive Top App Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(monetColors.background)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    animateDismiss()
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(monetColors.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = monetColors.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Settings",
                                    color = monetColors.onSurface,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = GoogleSans,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = "Telephony, Recording & Privileges",
                                    color = monetColors.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontFamily = GoogleSans
                                )
                            }
                        }
                    }

                    // Settings Grouped Content List
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ==========================================
                        // GROUP 1: CALL HANDLING & SCREENING
                        // ==========================================
                        SettingsSectionHeader(title = "CALL HANDLING & SCREENING", monetColors = monetColors)
                        SettingsGroupCard(monetColors = monetColors) {
                            // Row 1: Default Phone App
                            SettingsItemRow(
                                icon = if (isDefaultDialer) Icons.Rounded.PhoneInTalk else Icons.Filled.Warning,
                                iconTint = if (isDefaultDialer) Color(0xFF81C995) else monetColors.error,
                                iconBgColor = if (isDefaultDialer) Color(0xFF81C995).copy(alpha = 0.16f) else monetColors.error.copy(alpha = 0.16f),
                                title = "Default Phone App",
                                subtitle = if (isDefaultDialer)
                                    "Active • Managing incoming & outgoing carrier calls"
                                else
                                    "Required by Android to intercept and screen incoming calls",
                                monetColors = monetColors,
                                trailingContent = {
                                    if (isDefaultDialer) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF81C995).copy(alpha = 0.18f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = null,
                                                    tint = Color(0xFF81C995),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = "ACTIVE",
                                                    color = Color(0xFF81C995),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = GoogleSans
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(monetColors.primary)
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    onRequestDefaultDialer()
                                                }
                                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                        ) {
                                            Text(
                                                text = "Set Default",
                                                color = monetColors.onPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = GoogleSans
                                            )
                                        }
                                    }
                                }
                            )

                            SettingsDivider(monetColors = monetColors)

                            // Row 2: Auto-Screen Unknown
                            SettingsItemRow(
                                icon = Icons.Rounded.Security,
                                iconTint = monetColors.primary,
                                iconBgColor = monetColors.primary.copy(alpha = 0.14f),
                                title = "Auto-Screen Unknown",
                                subtitle = if (autoScreenUnknown)
                                    "Assistant answers callers not saved in your contacts"
                                else
                                    "Manual screening: tap Screen on incoming call canopy",
                                monetColors = monetColors,
                                trailingContent = {
                                    Switch(
                                        checked = autoScreenUnknown,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onToggleAutoScreen(it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = monetColors.onPrimary,
                                            checkedTrackColor = monetColors.primary,
                                            uncheckedThumbColor = monetColors.outline,
                                            uncheckedTrackColor = monetColors.surfaceVariant
                                        )
                                    )
                                }
                            )

                            SettingsDivider(monetColors = monetColors)

                            // Row 3: Stealth Caller ID
                            SettingsItemRow(
                                icon = Icons.Rounded.VisibilityOff,
                                iconTint = monetColors.secondary,
                                iconBgColor = monetColors.secondary.copy(alpha = 0.14f),
                                title = "Stealth Caller ID",
                                subtitle = if (isHideNumberOnCallerId)
                                    "Private Caller: raw numbers masked on in-call screens"
                                else
                                    "Standard: full caller phone numbers displayed",
                                monetColors = monetColors,
                                trailingContent = {
                                    Switch(
                                        checked = isHideNumberOnCallerId,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onToggleHideNumber(it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = monetColors.onPrimary,
                                            checkedTrackColor = monetColors.primary,
                                            uncheckedThumbColor = monetColors.outline,
                                            uncheckedTrackColor = monetColors.surfaceVariant
                                        )
                                    )
                                }
                            )
                        }

                        // ==========================================
                        // GROUP: CALL STATISTICS & INSIGHTS
                        // ==========================================
                        SettingsSectionHeader(title = "CALL STATISTICS & INSIGHTS", monetColors = monetColors)
                        SettingsGroupCard(monetColors = monetColors) {
                            val totalTalkTime = remember(callLogs) { callLogs.sumOf { it.durationSeconds } }
                            val totalCallCount = remember(callLogs) { callLogs.size }

                            SettingsItemRow(
                                icon = Icons.Rounded.Schedule,
                                iconTint = Color(0xFF8AB4F8),
                                iconBgColor = Color(0xFF8AB4F8).copy(alpha = 0.16f),
                                title = "Call Statistics",
                                subtitle = "${formatCallDuration(totalTalkTime)} total talk time • $totalCallCount calls recorded",
                                monetColors = monetColors,
                                modifier = Modifier.clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showCallStatsScreen = true
                                },
                                trailingContent = {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF8AB4F8).copy(alpha = 0.18f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "View",
                                                color = Color(0xFF8AB4F8),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = GoogleSans
                                            )
                                            Icon(
                                                imageVector = Icons.Rounded.ChevronRight,
                                                contentDescription = null,
                                                tint = Color(0xFF8AB4F8),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            )
                        }

                        // ==========================================
                        // GROUP 2: CALL RECORDING ENGINE
                        // ==========================================
                        SettingsSectionHeader(title = "CALL RECORDING ENGINE", monetColors = monetColors)
                        SettingsGroupCard(monetColors = monetColors) {
                            // Row 1: Auto-Record Calls
                            SettingsItemRow(
                                icon = Icons.Filled.Mic,
                                iconTint = Color(0xFFF28B82),
                                iconBgColor = Color(0xFFF28B82).copy(alpha = 0.14f),
                                title = "Auto-Record Calls",
                                subtitle = if (isAutoRecordEnabled)
                                    "Dual-side capture: records caller and mic directly"
                                else
                                    "Manual recording: tap in-call REC button anytime",
                                monetColors = monetColors,
                                trailingContent = {
                                    Switch(
                                        checked = isAutoRecordEnabled,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onToggleAutoRecord(it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = monetColors.onPrimary,
                                            checkedTrackColor = monetColors.primary,
                                            uncheckedThumbColor = monetColors.outline,
                                            uncheckedTrackColor = monetColors.surfaceVariant
                                        )
                                    )
                                }
                            )

                            SettingsDivider(monetColors = monetColors)

                            // Row 2: Saved Call Recordings Manager
                            SettingsItemRow(
                                icon = Icons.Rounded.GraphicEq,
                                iconTint = monetColors.tertiary,
                                iconBgColor = monetColors.tertiary.copy(alpha = 0.14f),
                                title = "Saved Recordings",
                                subtitle = "Playback audio, share WAV files, or manage storage",
                                monetColors = monetColors,
                                modifier = Modifier.clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showRecordingsDialog = true
                                },
                                trailingContent = {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(monetColors.surfaceVariant.copy(alpha = 0.7f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Manage",
                                                color = monetColors.onSurface,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = GoogleSans
                                            )
                                            Icon(
                                                imageVector = Icons.Rounded.ChevronRight,
                                                contentDescription = null,
                                                tint = monetColors.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            )
                        }

                        // ==========================================
                        // GROUP 3: SPEECH JAMMER (ACOUSTIC DAF ENGINE)
                        // ==========================================
                        SettingsSectionHeader(title = "SPEECH JAMMER (ACOUSTIC DAF ENGINE)", monetColors = monetColors)
                        SettingsGroupCard(monetColors = monetColors) {
                            val jammerEngine = remember { com.callscreener.app.engine.SpeechJammerEngine.getInstance(context) }
                            var isJammerEnabled by remember { mutableStateOf(jammerEngine.isEnabled) }

                            SettingsItemRow(
                                icon = Icons.Rounded.Psychology,
                                iconTint = Color(0xFFD0BCFF),
                                iconBgColor = Color(0xFFD0BCFF).copy(alpha = 0.16f),
                                title = "Speech Jammer (DAF)",
                                subtitle = if (isJammerEnabled)
                                    "Acoustic DAF Active: disrupts caller with ${jammerEngine.delayMs}ms delayed feedback"
                                else
                                    "Disabled: in-call Speech Jammer controls hidden",
                                monetColors = monetColors,
                                trailingContent = {
                                    Switch(
                                        checked = isJammerEnabled,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            isJammerEnabled = it
                                            jammerEngine.isEnabled = it
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFFD0BCFF),
                                            checkedTrackColor = Color(0xFF6750A4),
                                            uncheckedThumbColor = monetColors.outline,
                                            uncheckedTrackColor = monetColors.surfaceVariant
                                        )
                                    )
                                }
                            )

                            SettingsDivider(monetColors = monetColors)

                            SettingsItemRow(
                                icon = Icons.Rounded.Tune,
                                iconTint = Color(0xFFE8DEF8),
                                iconBgColor = Color(0xFFE8DEF8).copy(alpha = 0.16f),
                                title = "Configure & Live Test Lab",
                                subtitle = "Adjust 160-240ms delay, intensity gain, or test on your own voice",
                                monetColors = monetColors,
                                modifier = Modifier.clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showSpeechJammerDialog = true
                                },
                                trailingContent = {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFD0BCFF).copy(alpha = 0.2f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Tune / Test",
                                                color = Color(0xFFD0BCFF),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = GoogleSans
                                            )
                                            Icon(
                                                imageVector = Icons.Rounded.ChevronRight,
                                                contentDescription = null,
                                                tint = Color(0xFFD0BCFF),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            )
                        }

                        // ==========================================
                        // GROUP 4: OPERATING PROFILE & PRIVILEGES
                        // ==========================================
                        SettingsSectionHeader(title = "OPERATING PROFILE & PRIVILEGES", monetColors = monetColors)
                        SettingsGroupCard(monetColors = monetColors) {
                            // Row 1: Profile Summary & Status Pill
                            SettingsItemRow(
                                icon = if (operationMode == AppOperationMode.ROOT_ENHANCED) Icons.Rounded.Security else Icons.Filled.Call,
                                iconTint = monetColors.primary,
                                iconBgColor = monetColors.primary.copy(alpha = 0.14f),
                                title = "Operating Profile",
                                subtitle = if (operationMode == AppOperationMode.ROOT_ENHANCED)
                                    "Root Superpowers & Direct Audio Uplink"
                                else
                                    "Standard Non-Root Telecom Profile",
                                monetColors = monetColors,
                                trailingContent = {
                                    val badgeText = if (operationMode == AppOperationMode.ROOT_ENHANCED) {
                                        if (rootAvailable) "ROOT READY" else "SU NEEDED"
                                    } else {
                                        "STANDARD"
                                    }
                                    val badgeColor = if (operationMode == AppOperationMode.ROOT_ENHANCED) {
                                        if (rootAvailable) Color(0xFF81C995) else Color(0xFFFFB74D)
                                    } else {
                                        monetColors.primary
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(badgeColor.copy(alpha = 0.18f))
                                            .padding(horizontal = 9.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            color = badgeColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = GoogleSans
                                        )
                                    }
                                }
                            )

                            SettingsDivider(monetColors = monetColors)

                            // Segmented Pixel Mode Selector Pill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(monetColors.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val isStandard = operationMode == AppOperationMode.STANDARD
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isStandard) monetColors.primary else Color.Transparent)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onSelectMode(AppOperationMode.STANDARD)
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Standard (Non-Root)",
                                            color = if (isStandard) monetColors.onPrimary else monetColors.onSurfaceVariant,
                                            fontSize = 12.sp,
                                            fontWeight = if (isStandard) FontWeight.SemiBold else FontWeight.Medium,
                                            fontFamily = GoogleSans
                                        )
                                    }

                                    val isRoot = operationMode == AppOperationMode.ROOT_ENHANCED
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isRoot) Color(0xFFFFB74D).copy(alpha = 0.28f) else Color.Transparent)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onSelectMode(AppOperationMode.ROOT_ENHANCED)
                                                coroutineScope.launch {
                                                    rootAvailable = RootPrivilegeManager.isRootAvailable()
                                                    isPrivileged = RootPrivilegeManager.isPrivilegedApp(context)
                                                    isModuleInstalled = RootPrivilegeManager.isMagiskModuleInstalled()
                                                }
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Root Enhanced",
                                            color = if (isRoot) Color(0xFFFFB74D) else monetColors.onSurfaceVariant,
                                            fontSize = 12.sp,
                                            fontWeight = if (isRoot) FontWeight.SemiBold else FontWeight.Medium,
                                            fontFamily = GoogleSans
                                        )
                                    }
                                }
                            }

                            // Feature Bullets Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(monetColors.surfaceVariant.copy(alpha = 0.35f))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (operationMode == AppOperationMode.STANDARD) {
                                        Text(
                                            text = "Standard Mode is built for normal everyday calling on any Android phone. Calls use your natural microphone directly with zero root modifications, zero risk, and maximum stability.",
                                            color = monetColors.onSurfaceVariant,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            fontFamily = GoogleSans
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        ModeFeatureRow(text = "Standard Telecom framework (works on all devices)", isIncluded = true)
                                        ModeFeatureRow(text = "Instant call screening & neural AI assistant", isIncluded = true)
                                        ModeFeatureRow(text = "Zero unlocked bootloader or root required", isIncluded = true)
                                    } else {
                                        Text(
                                            text = "Root Enhanced Mode unlocks privileged digital audio routing, zero-noise digital cellular injection, and system priv-app integration via Magisk or KernelSU.",
                                            color = Color(0xFFFFB74D),
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            fontFamily = GoogleSans
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
                                SettingsDivider(monetColors = monetColors)

                                SettingsItemRow(
                                    icon = Icons.Rounded.Extension,
                                    iconTint = monetColors.primary,
                                    iconBgColor = monetColors.primary.copy(alpha = 0.14f),
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
                                    monetColors = monetColors,
                                    trailingContent = {
                                        if (isPrivileged) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFF81C995).copy(alpha = 0.18f))
                                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                                            ) {
                                                Text(
                                                    text = "PRIV-APP",
                                                    color = Color(0xFF81C995),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = GoogleSans
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
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = monetColors.onSurfaceVariant),
                                            modifier = Modifier.fillMaxWidth().height(42.dp)
                                        ) {
                                            Text("Uninstall System Module", fontSize = 12.sp, fontFamily = GoogleSans)
                                        }
                                    } else if (rootAvailable) {
                                        if (isModuleInstalled) {
                                            Button(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        RootPrivilegeManager.rebootDevice()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D)),
                                                shape = RoundedCornerShape(16.dp),
                                                modifier = Modifier.fillMaxWidth().height(44.dp)
                                            ) {
                                                Text("Restart Device Now", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = GoogleSans)
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
                                            colors = ButtonDefaults.buttonColors(containerColor = monetColors.primary),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.fillMaxWidth().height(44.dp)
                                        ) {
                                            Text(
                                                text = if (isInstalling) "Configuring Module..." else if (isModuleInstalled) "Re-install / Update Module" else "Activate Priv-App Module",
                                                color = monetColors.onPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                fontFamily = GoogleSans
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
                                                shape = RoundedCornerShape(16.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = monetColors.onSurfaceVariant),
                                                modifier = Modifier.fillMaxWidth().height(42.dp)
                                            ) {
                                                Text("Uninstall Module", fontSize = 12.sp, fontFamily = GoogleSans)
                                            }
                                        }
                                    }
                                }

                                SettingsDivider(monetColors = monetColors)

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
                                        tint = monetColors.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp).padding(top = 1.dp)
                                    )
                                    Text(
                                        text = "Android security isolates cellular voice channels from user apps. System priv-app installation mounts Call Screener into /system/priv-app with CAPTURE_AUDIO_OUTPUT permissions, bypassing loudspeaker audio loopback.",
                                        color = monetColors.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontSize = 11.5.sp,
                                        lineHeight = 15.sp,
                                        fontFamily = GoogleSans
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp).navigationBarsPadding())
                    }
                }
            }
        }
    if (showSpeechJammerDialog) {
        val jammerEngine = remember { com.callscreener.app.engine.SpeechJammerEngine.getInstance(context) }
        SpeechJammerDialog(
            engine = jammerEngine,
            onDismiss = { showSpeechJammerDialog = false }
        )
    }

    if (showRecordingsDialog) {
        SavedRecordingsDialog(onDismiss = { showRecordingsDialog = false })
    }

    if (showCallStatsScreen) {
        CallStatsScreen(
            callLogs = callLogs,
            monetColors = monetColors,
            onDismiss = { showCallStatsScreen = false },
            onCall = { number ->
                DialerRepository.placeCall(context, number)
            }
        )
    }
}

// =========================================================================
// CALL STATISTICS FEATURE (Google Pixel / Expressive Dialer Design)
// =========================================================================

enum class StatsDateRange(val label: String) {
    ALL_TIME("All time"),
    TODAY("Today"),
    SEVEN_DAYS("Last 7 days"),
    THIRTY_DAYS("Last 30 days")
}

enum class StatsCallType(val label: String) {
    ALL("All"),
    INCOMING("Incoming"),
    OUTGOING("Outgoing"),
    MISSED("Missed")
}

data class ContactCallMetric(
    val name: String,
    val number: String,
    val totalDuration: Long,
    val callCount: Int,
    val incomingCount: Int,
    val outgoingCount: Int,
    val missedCount: Int
)

/**
 * Pixel Expressive Call Statistics Screen
 * Direct match to Google Dialer's CallStatsScreen:
 * - Date Range Filter Row (All time, Today, Last 7 days, Last 30 days)
 * - Call Type Filter Row (All, Incoming, Outgoing, Missed)
 * - Hero Summary Card with full duration, matching calls, and average duration
 * - 3 Mini Metric Cards (Incoming, Outgoing, Missed)
 * - Top Contacts Ranked List with Duration Progress Bars and One-Tap Call Action
 */
@Composable
fun CallStatsScreen(
    callLogs: List<CallLogItem>,
    monetColors: ColorScheme,
    onDismiss: () -> Unit,
    onCall: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
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

    Dialog(
        onDismissRequest = { animateDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val view = LocalView.current
        SideEffect {
            var parent: Any? = view.parent
            var dialogWindow: android.view.Window? = null
            while (parent != null) {
                if (parent is DialogWindowProvider) {
                    dialogWindow = parent.window
                    break
                }
                parent = (parent as? android.view.View)?.parent
            }
            if (dialogWindow == null) {
                dialogWindow = (view.rootView.parent as? DialogWindowProvider)?.window
            }

            dialogWindow?.let { win ->
                win.setDimAmount(0f)
                win.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                win.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                win.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                win.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                win.setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.MATCH_PARENT
                )
                win.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                WindowCompat.setDecorFitsSystemWindows(win, false)
                win.statusBarColor = android.graphics.Color.TRANSPARENT
                win.navigationBarColor = android.graphics.Color.TRANSPARENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    win.isNavigationBarContrastEnforced = false
                    win.isStatusBarContrastEnforced = false
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    win.attributes = win.attributes.apply {
                        layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                }
                val insets = WindowCompat.getInsetsController(win, win.decorView)
                insets.isAppearanceLightStatusBars = false
                insets.isAppearanceLightNavigationBars = false
                win.decorView.fitsSystemWindows = false
                (win.decorView as? android.view.ViewGroup)?.clipToPadding = false
            }
            (view.parent as? android.view.View)?.fitsSystemWindows = false
            (view as? android.view.View)?.fitsSystemWindows = false
        }

        BackHandler(enabled = isVisible) {
            animateDismiss()
        }

        var selectedDateRange by remember { mutableStateOf(StatsDateRange.ALL_TIME) }
        var selectedCallType by remember { mutableStateOf(StatsCallType.ALL) }

        val now = remember { System.currentTimeMillis() }
        val dateRangeCutoff = remember(selectedDateRange, now) {
            when (selectedDateRange) {
                StatsDateRange.ALL_TIME -> 0L
                StatsDateRange.TODAY -> {
                    val cal = java.util.Calendar.getInstance()
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    cal.set(java.util.Calendar.MINUTE, 0)
                    cal.set(java.util.Calendar.SECOND, 0)
                    cal.set(java.util.Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }
                StatsDateRange.SEVEN_DAYS -> now - (7L * 24 * 60 * 60 * 1000L)
                StatsDateRange.THIRTY_DAYS -> now - (30L * 24 * 60 * 60 * 1000L)
            }
        }

        val dateFilteredLogs = remember(callLogs, dateRangeCutoff) {
            if (dateRangeCutoff == 0L) callLogs else callLogs.filter { it.timestamp >= dateRangeCutoff }
        }

        val filteredLogs = remember(dateFilteredLogs, selectedCallType) {
            when (selectedCallType) {
                StatsCallType.ALL -> dateFilteredLogs
                StatsCallType.INCOMING -> dateFilteredLogs.filter { it.type == 1 }
                StatsCallType.OUTGOING -> dateFilteredLogs.filter { it.type == 2 }
                StatsCallType.MISSED -> dateFilteredLogs.filter { it.type == 3 || it.type == 5 || it.type == 6 }
            }
        }

        val totalDuration = remember(filteredLogs) { filteredLogs.sumOf { it.durationSeconds } }
        val totalCallCount = remember(filteredLogs) { filteredLogs.size }
        val avgDuration = remember(totalDuration, totalCallCount) {
            if (totalCallCount > 0) totalDuration / totalCallCount else 0L
        }

        // 3 Metric cards (breakdown for selected date range)
        val inLogs = remember(dateFilteredLogs) { dateFilteredLogs.filter { it.type == 1 } }
        val outLogs = remember(dateFilteredLogs) { dateFilteredLogs.filter { it.type == 2 } }
        val missedLogs = remember(dateFilteredLogs) { dateFilteredLogs.filter { it.type == 3 || it.type == 5 || it.type == 6 } }

        val inDuration = remember(inLogs) { inLogs.sumOf { it.durationSeconds } }
        val outDuration = remember(outLogs) { outLogs.sumOf { it.durationSeconds } }
        val inCount = inLogs.size
        val outCount = outLogs.size
        val missedCount = missedLogs.size

        // Top contacts breakdown
        val contactMetrics = remember(filteredLogs) {
            val grouped = filteredLogs.groupBy { log ->
                val clean = log.number.trim().replace(Regex("[\\s\\-\\(\\)]"), "")
                if (clean.isNotEmpty()) clean else (log.name ?: "Unknown")
            }
            grouped.map { (key, logs) ->
                val bestName = logs.firstOrNull { !it.name.isNullOrBlank() }?.name ?: ""
                val bestNumber = logs.firstOrNull { it.number.isNotBlank() }?.number ?: key
                val totalSecs = logs.sumOf { it.durationSeconds }
                val count = logs.size
                val incCount = logs.count { it.type == 1 }
                val ogCount = logs.count { it.type == 2 }
                val mCount = logs.count { it.type == 3 || it.type == 5 || it.type == 6 }

                ContactCallMetric(
                    name = bestName,
                    number = bestNumber,
                    totalDuration = totalSecs,
                    callCount = count,
                    incomingCount = incCount,
                    outgoingCount = ogCount,
                    missedCount = mCount
                )
            }.sortedWith(
                compareByDescending<ContactCallMetric> { it.totalDuration }
                    .thenByDescending { it.callCount }
            )
        }

        val maxContactDuration = remember(contactMetrics) { contactMetrics.maxOfOrNull { it.totalDuration } ?: 0L }
        val maxContactCount = remember(contactMetrics) { contactMetrics.maxOfOrNull { it.callCount } ?: 0 }

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
                    .background(monetColors.background)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Pixel Expressive Top Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(monetColors.background)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    animateDismiss()
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(monetColors.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = monetColors.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Text(
                                text = "Call statistics",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = GoogleSans,
                                    color = monetColors.onSurface
                                ),
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .weight(1f)
                            )

                            if (selectedDateRange != StatsDateRange.ALL_TIME || selectedCallType != StatsCallType.ALL) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(monetColors.primaryContainer.copy(alpha = 0.6f))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedDateRange = StatsDateRange.ALL_TIME
                                            selectedCallType = StatsCallType.ALL
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Reset filter",
                                            tint = monetColors.onPrimaryContainer,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Reset",
                                            color = monetColors.onPrimaryContainer,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = GoogleSans
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Content
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Date Range Filters
                        item {
                            StatsChoiceRow(
                                items = StatsDateRange.values().toList(),
                                selectedItem = selectedDateRange,
                                onItemSelected = { selectedDateRange = it },
                                labelProvider = { it.label },
                                monetColors = monetColors
                            )
                        }

                        // Call Type Filters
                        item {
                            StatsChoiceRow(
                                items = StatsCallType.values().toList(),
                                selectedItem = selectedCallType,
                                onItemSelected = { selectedCallType = it },
                                labelProvider = { it.label },
                                monetColors = monetColors
                            )
                        }

                        // Hero Summary Card
                        item {
                            StatsSummaryCard(
                                dateRange = selectedDateRange,
                                callType = selectedCallType,
                                totalDuration = totalDuration,
                                callCount = totalCallCount,
                                avgDuration = avgDuration,
                                monetColors = monetColors
                            )
                        }

                        // 3 Mini Metric Cards
                        item {
                            StatsMetricCards(
                                inCount = inCount,
                                inDuration = inDuration,
                                outCount = outCount,
                                outDuration = outDuration,
                                missedCount = missedCount,
                                monetColors = monetColors
                            )
                        }

                        // Top Contacts Section Header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Top contacts",
                                        fontFamily = GoogleSans,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = monetColors.onSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(monetColors.surfaceVariant)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${contactMetrics.size}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = monetColors.onSurfaceVariant,
                                            fontFamily = GoogleSans
                                        )
                                    }
                                }
                            }
                        }

                        // Contact Metrics List
                        if (contactMetrics.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(monetColors.surfaceVariant.copy(alpha = 0.35f))
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Schedule,
                                            contentDescription = null,
                                            tint = monetColors.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Text(
                                            text = "No calls found",
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = monetColors.onSurface
                                        )
                                        Text(
                                            text = "No calls match the selected date range and call type filters.",
                                            fontFamily = GoogleSans,
                                            fontSize = 12.sp,
                                            color = monetColors.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(
                                items = contactMetrics,
                                key = { it.number + "_" + it.name }
                            ) { contact ->
                                ContactMetricCard(
                                    contact = contact,
                                    maxDuration = maxContactDuration,
                                    maxCallCount = maxContactCount,
                                    monetColors = monetColors,
                                    onClick = {
                                        onCall(contact.number)
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp).navigationBarsPadding())
                        }
                    }
                }
            }
        }
    }
}

/**
 * Filter Choice Row (Date Range & Call Type)
 */
@Composable
fun <T> StatsChoiceRow(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    monetColors: ColorScheme,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) monetColors.primaryContainer else monetColors.surfaceVariant.copy(alpha = 0.45f))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onItemSelected(item)
                    }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = labelProvider(item),
                    fontSize = 13.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) monetColors.onPrimaryContainer else monetColors.onSurfaceVariant,
                    fontFamily = GoogleSans
                )
            }
        }
    }
}

/**
 * Google Pixel Hero Summary Card
 */
@Composable
fun StatsSummaryCard(
    dateRange: StatsDateRange,
    callType: StatsCallType,
    totalDuration: Long,
    callCount: Int,
    avgDuration: Long,
    monetColors: ColorScheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(monetColors.primaryContainer.copy(alpha = 0.75f))
            .padding(22.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${dateRange.label.uppercase(java.util.Locale.ROOT)} · ${callType.label.uppercase(java.util.Locale.ROOT)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = monetColors.onPrimaryContainer.copy(alpha = 0.75f),
                fontFamily = GoogleSans
            )

            Text(
                text = formatCallDuration(totalDuration),
                fontSize = 36.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.Bold,
                color = monetColors.onPrimaryContainer,
                fontFamily = GoogleSans
            )

            Text(
                text = "$callCount matching calls • Avg ${formatCallDuration(avgDuration)}",
                fontSize = 13.5.sp,
                color = monetColors.onPrimaryContainer.copy(alpha = 0.85f),
                fontFamily = GoogleSans
            )
        }
    }
}

/**
 * Row of 3 Metric Breakdown Cards (Incoming, Outgoing, Missed)
 */
@Composable
fun StatsMetricCards(
    inCount: Int,
    inDuration: Long,
    outCount: Int,
    outDuration: Long,
    missedCount: Int,
    monetColors: ColorScheme,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatsMetricMiniCard(
            title = "Incoming",
            count = inCount,
            duration = formatCallDuration(inDuration),
            icon = Icons.Filled.CallReceived,
            accentColor = Color(0xFF81C995),
            monetColors = monetColors,
            modifier = Modifier.weight(1f)
        )

        StatsMetricMiniCard(
            title = "Outgoing",
            count = outCount,
            duration = formatCallDuration(outDuration),
            icon = Icons.Filled.CallMade,
            accentColor = Color(0xFF8AB4F8),
            monetColors = monetColors,
            modifier = Modifier.weight(1f)
        )

        StatsMetricMiniCard(
            title = "Missed",
            count = missedCount,
            duration = "-",
            icon = Icons.Filled.CallMissed,
            accentColor = Color(0xFFF28B82),
            monetColors = monetColors,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatsMetricMiniCard(
    title: String,
    count: Int,
    duration: String,
    icon: ImageVector,
    accentColor: Color,
    monetColors: ColorScheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(monetColors.surfaceVariant.copy(alpha = 0.45f))
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    fontSize = 11.5.sp,
                    color = monetColors.onSurfaceVariant,
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }

            Text(
                text = "$count",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = monetColors.onSurface,
                fontFamily = GoogleSans
            )

            Text(
                text = duration,
                fontSize = 11.5.sp,
                color = monetColors.onSurfaceVariant.copy(alpha = 0.8f),
                fontFamily = GoogleSans,
                maxLines = 1
            )
        }
    }
}

/**
 * Top Contact Metric Card with duration bar and call info
 */
@Composable
fun ContactMetricCard(
    contact: ContactCallMetric,
    maxDuration: Long,
    maxCallCount: Int,
    monetColors: ColorScheme,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val progress = remember(contact.totalDuration, maxDuration, contact.callCount, maxCallCount) {
        if (maxDuration > 0) {
            (contact.totalDuration.toFloat() / maxDuration.toFloat()).coerceIn(0.04f, 1f)
        } else if (maxCallCount > 0) {
            (contact.callCount.toFloat() / maxCallCount.toFloat()).coerceIn(0.04f, 1f)
        } else {
            0.04f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(monetColors.surfaceVariant.copy(alpha = 0.35f))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val initial = remember(contact.name, contact.number) {
                    val displayName = contact.name.ifBlank { contact.number }
                    displayName.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "#"
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(monetColors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = monetColors.onPrimaryContainer,
                        fontFamily = GoogleSans
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = contact.name.ifBlank { contact.number },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        color = monetColors.onSurface,
                        fontFamily = GoogleSans
                    )
                    if (contact.name.isNotBlank() && contact.name != contact.number) {
                        Text(
                            text = contact.number,
                            fontSize = 12.sp,
                            color = monetColors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            fontFamily = GoogleSans
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatCallDuration(contact.totalDuration),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = monetColors.onSurface,
                        fontFamily = GoogleSans
                    )
                    Text(
                        text = "${contact.callCount} calls",
                        fontSize = 11.5.sp,
                        color = monetColors.onSurfaceVariant,
                        fontFamily = GoogleSans
                    )
                }
            }

            // Duration Progress Bar (Google Dialer style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(monetColors.surfaceVariant.copy(alpha = 0.7f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(monetColors.primary)
                )
            }
        }
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
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val view = LocalView.current
        SideEffect {
            var parent: Any? = view.parent
            var dialogWindow: android.view.Window? = null
            while (parent != null) {
                if (parent is DialogWindowProvider) {
                    dialogWindow = parent.window
                    break
                }
                parent = (parent as? android.view.View)?.parent
            }
            if (dialogWindow == null) {
                dialogWindow = (view.rootView.parent as? DialogWindowProvider)?.window
            }

            dialogWindow?.let { win ->
                win.setDimAmount(0f)
                win.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                win.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                win.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                win.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                win.setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.MATCH_PARENT
                )
                win.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                WindowCompat.setDecorFitsSystemWindows(win, false)
                win.statusBarColor = android.graphics.Color.TRANSPARENT
                win.navigationBarColor = android.graphics.Color.TRANSPARENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    win.isNavigationBarContrastEnforced = false
                    win.isStatusBarContrastEnforced = false
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    win.attributes = win.attributes.apply {
                        layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                }
                val insets = WindowCompat.getInsetsController(win, win.decorView)
                insets.isAppearanceLightStatusBars = false
                insets.isAppearanceLightNavigationBars = false
                win.decorView.fitsSystemWindows = false
                (win.decorView as? android.view.ViewGroup)?.clipToPadding = false
            }
            (view.parent as? android.view.View)?.fitsSystemWindows = false
            (view as? android.view.View)?.fitsSystemWindows = false
        }

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
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp),
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
                            itemsIndexed(recordings, key = { index, item -> "rec_${item.id}_$index" }) { _, item ->
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
