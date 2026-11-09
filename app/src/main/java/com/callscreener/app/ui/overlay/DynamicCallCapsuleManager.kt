package com.callscreener.app.ui.overlay

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.callscreener.app.root.RootPrivilegeManager
import com.callscreener.app.session.CallSessionManager
import com.callscreener.app.telephony.RingtonePlayer
import com.callscreener.app.ui.ScreeningCallActivity
import com.callscreener.app.ui.theme.CallScreenerTheme

object DynamicCallCapsuleManager {

    private const val TAG = "DynamicCapsuleManager"
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentView: ComposeView? = null
    private var currentLifecycleOwner: OverlayLifecycleOwner? = null
    private var windowManager: WindowManager? = null

    /**
     * Checks if overlay can be drawn. If rooted, automatically attempts to grant SYSTEM_ALERT_WINDOW.
     */
    fun canShowCapsule(context: Context): Boolean {
        if (Settings.canDrawOverlays(context)) {
            return true
        }

        // Auto-grant via root if privileged or rooted
        try {
            if (RootPrivilegeManager.isRootAvailableCached() || RootPrivilegeManager.isPrivilegedAppCached(context)) {
                val grantCmd = "cmd appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow; appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow; pm grant ${context.packageName} android.permission.SYSTEM_ALERT_WINDOW"
                val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", grantCmd))
                proc.waitFor()
                if (Settings.canDrawOverlays(context)) {
                    Log.i(TAG, "SYSTEM_ALERT_WINDOW granted via root privilege")
                    return true
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed attempting root auto-grant of SYSTEM_ALERT_WINDOW", t)
        }

        return Settings.canDrawOverlays(context)
    }

    /**
     * Prompts the user to grant Overlay Permission (Display over other apps)
     * so the sleek dynamic call capsule header can display above any active app.
     */
    fun requestOverlayPermission(context: Context) {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Cannot open overlay permission settings", e)
            }
        }
    }

    /**
     * Returns true if the dynamic call capsule is currently visible on screen.
     */
    fun isCapsuleShowing(): Boolean = currentView != null

    /**
     * Display the compact floating Dynamic Capsule header at the top of the screen.
     */
    fun showCapsule(
        context: Context,
        callerNumber: String,
        callerName: String?
    ) {
        mainHandler.post {
            try {
                if (!canShowCapsule(context)) {
                    Log.w(TAG, "Cannot show capsule: SYSTEM_ALERT_WINDOW permission missing")
                    return@post
                }

                // If already showing, dismiss previous view cleanly first
                dismissCapsule()

                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                windowManager = wm

                val displayMetrics = context.resources.displayMetrics
                val density = displayMetrics.density
                val maxWidthPx = (410 * density).toInt()
                val capsuleWidth = (displayMetrics.widthPixels * 0.94f).toInt().coerceAtMost(maxWidthPx)

                val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
                val statusBarHeight = if (resId > 0) context.resources.getDimensionPixelSize(resId) else (28 * density).toInt()
                val topMargin = statusBarHeight + (8 * density).toInt()

                val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                val params = WindowManager.LayoutParams(
                    capsuleWidth,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layoutFlag,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    x = 0
                    y = topMargin
                    windowAnimations = 0
                }

                val lifecycleOwner = OverlayLifecycleOwner()
                currentLifecycleOwner = lifecycleOwner
                lifecycleOwner.onCreate()
                lifecycleOwner.onResume()

                val composeView = ComposeView(context).apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                }

                composeView.setViewTreeLifecycleOwner(lifecycleOwner)
                composeView.setViewTreeViewModelStoreOwner(lifecycleOwner)
                composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)

                composeView.setContent {
                    CallScreenerTheme {
                        DynamicIncomingCallCapsule(
                            callerNumber = callerNumber,
                            callerName = callerName,
                            onAnswer = {
                                Log.i(TAG, "Dynamic Capsule: User tapped Answer")
                                dismissCapsule()
                                val sessionManager = CallSessionManager.getInstance(context.applicationContext)
                                sessionManager.answerCallDirectly()
                                val intent = Intent(context, ScreeningCallActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                context.startActivity(intent)
                            },
                            onDecline = {
                                Log.i(TAG, "Dynamic Capsule: User tapped Decline")
                                dismissCapsule()
                                val sessionManager = CallSessionManager.getInstance(context.applicationContext)
                                sessionManager.hangUp()
                            },
                            onScreen = {
                                Log.i(TAG, "Dynamic Capsule: User tapped Screen Call with AI")
                                dismissCapsule()
                                val sessionManager = CallSessionManager.getInstance(context.applicationContext)
                                sessionManager.startScreening()
                                val intent = Intent(context, ScreeningCallActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                context.startActivity(intent)
                            },
                            onExpand = {
                                Log.i(TAG, "Dynamic Capsule: User tapped capsule body to expand to full screen")
                                dismissCapsule()
                                val intent = Intent(context, ScreeningCallActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                context.startActivity(intent)
                            },
                            onSilenceOrDismiss = {
                                Log.i(TAG, "Dynamic Capsule: User swiped up to silence ringer and dismiss")
                                RingtonePlayer.stop(context.applicationContext)
                                dismissCapsule()
                            }
                        )
                    }
                }

                wm.addView(composeView, params)
                currentView = composeView
                Log.i(TAG, "Dynamic call capsule successfully attached to WindowManager")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed showing dynamic call capsule overlay", t)
                dismissCapsule()
            }
        }
    }

    /**
     * Dismiss and remove the capsule from the screen.
     */
    fun dismissCapsule() {
        mainHandler.post {
            try {
                currentView?.let { v ->
                    try {
                        windowManager?.removeViewImmediate(v)
                    } catch (e: Exception) {
                        try {
                            windowManager?.removeView(v)
                        } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error removing dynamic capsule view", e)
            } finally {
                currentLifecycleOwner?.onDestroy()
                currentLifecycleOwner = null
                currentView = null
            }
        }
    }
}
