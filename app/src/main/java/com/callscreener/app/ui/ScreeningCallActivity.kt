package com.callscreener.app.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import com.callscreener.app.data.DialerRepository
import com.callscreener.app.model.CallStatus
import com.callscreener.app.service.CallScreeningInCallService
import com.callscreener.app.session.CallSessionManager
import com.callscreener.app.ui.theme.CallScreenerTheme

class ScreeningCallActivity : ComponentActivity() {

    private lateinit var sessionManager: CallSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        val insets = WindowCompat.getInsetsController(window, window.decorView)
        insets.isAppearanceLightStatusBars = false
        insets.isAppearanceLightNavigationBars = false

        sessionManager = CallSessionManager.getInstance(this)
        com.callscreener.app.ui.overlay.DynamicCallCapsuleManager.dismissCapsule()

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val state = sessionManager.sessionState.value
                if (state.status != CallStatus.ENDED) {
                    moveTaskToBack(true)
                } else {
                    finish()
                }
            }
        })

        configureLockScreenDisplay()

        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        setContent {
            CallScreenerTheme {
                val state by sessionManager.sessionState.collectAsState()
                val presets by sessionManager.presetManager.presets.collectAsState()

                if (state.status == CallStatus.ENDED) {
                    val inCallService = CallScreeningInCallService.currentInstance
                    val hasAliveCalls = inCallService?.calls?.any {
                        it.state != Call.STATE_DISCONNECTED
                    } == true
                    if (!hasAliveCalls) {
                        LaunchedEffect(Unit) {
                            delay(600)
                            finish()
                        }
                    }
                }

                ScreeningCallScreen(
                    state = state,
                    presets = presets,
                    onPlayPreset = { preset -> sessionManager.playAudioPreset(preset) },
                    onPauseCustomAudio = { sessionManager.pauseCustomAudio() },
                    onResumeCustomAudio = { sessionManager.resumeCustomAudio() },
                    onStopCustomAudio = { sessionManager.stopCustomAudio() },
                    onStartScreening = { sessionManager.startScreening() },
                    onAnswerDirectly = { sessionManager.answerCallDirectly() },
                    onSendReply = { replyText -> sessionManager.sendUserReply(replyText) },
                    onTakeOver = { sessionManager.takeOverCall() },
                    onHangUp = {
                        if (sessionManager.hasSecondaryCall()) {
                            sessionManager.hangUpCurrentCall()
                        } else {
                            sessionManager.hangUp()
                            finish()
                        }
                    },
                    onToggleMute = { sessionManager.toggleMute() },
                    onToggleSpeaker = { sessionManager.toggleSpeaker() },
                    onSelectPersona = { persona -> sessionManager.setVoicePersona(persona) },
                    onSwitchToAiTypeMode = { sessionManager.switchToAiTypeMode() },
                    onSwitchToDirectMicMode = { sessionManager.switchToDirectMicMode() },
                    onSimulateCallerReply = { simReply -> sessionManager.simulateCallerResponse(simReply) },
                    onToggleAiVoiceListening = { sessionManager.toggleAiVoiceListening() },
                    onToggleAutoRelay = { sessionManager.setAutoRelayEnabled(it) },
                    onToggleRecording = { sessionManager.toggleRecording() },
                    onToggleHold = { sessionManager.toggleHold() },
                    onToggleSpeechJammer = { sessionManager.toggleSpeechJammer() },
                    onToggleEchoUserVoice = { sessionManager.toggleEchoUserVoice() },
                    onDtmfDigit = { digit -> sessionManager.playDtmf(digit) },
                    onAddCall = { number -> sessionManager.addCall(number) },
                    onMergeCalls = { sessionManager.mergeConferenceCalls() },
                    onSwapCalls = { sessionManager.swapCalls() },
                    presetManager = sessionManager.presetManager,
                    quickReplyManager = sessionManager.quickReplyManager
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val inCallService = CallScreeningInCallService.currentInstance
        if (inCallService != null) {
            val aliveCalls = inCallService.calls.filter { it.state != Call.STATE_DISCONNECTED }
            if (aliveCalls.isNotEmpty() && !sessionManager.hasActiveCall()) {
                val primary = aliveCalls.first()
                val num = CallScreeningInCallService.extractPhoneNumber(primary)
                val name = DialerRepository.lookupContactName(this, num) ?: primary.details?.callerDisplayName
                Log.i("ScreeningCallActivity", "onResume: Reattaching active Telecom call ($num, $name)")
                sessionManager.attachCall(primary, num, name, isIncoming = false)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun configureLockScreenDisplay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }
}
