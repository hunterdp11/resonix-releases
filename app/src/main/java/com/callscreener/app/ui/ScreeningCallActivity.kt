package com.callscreener.app.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.callscreener.app.model.CallStatus
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

                // Close activity when call ends
                if (state.status == CallStatus.ENDED && state.messages.isEmpty()) {
                    finish()
                }

                ScreeningCallScreen(
                    state = state,
                    presets = presets,
                    onPlayPreset = { preset -> sessionManager.playAudioPreset(preset) },
                    onStartScreening = { sessionManager.startScreening() },
                    onAnswerDirectly = { sessionManager.answerCallDirectly() },
                    onSendReply = { replyText -> sessionManager.sendUserReply(replyText) },
                    onTakeOver = { sessionManager.takeOverCall() },
                    onHangUp = {
                        sessionManager.hangUp()
                        finish()
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
                    onDtmfDigit = { digit -> sessionManager.playDtmf(digit) },
                    presetManager = sessionManager.presetManager,
                    quickReplyManager = sessionManager.quickReplyManager
                )
            }
        }
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
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}

