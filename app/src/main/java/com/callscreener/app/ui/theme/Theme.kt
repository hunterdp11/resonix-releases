package com.callscreener.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(22.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val DarkColorScheme = darkColorScheme(
    primary = VibrantBlue,
    onPrimary = TextPrimary,
    primaryContainer = GlassSurface,
    onPrimaryContainer = TextPrimary,
    secondary = VibrantGreen,
    onSecondary = TextPrimary,
    error = VibrantRed,
    onError = TextPrimary,
    background = GlassBackground,
    onBackground = TextPrimary,
    surface = GlassSurface,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurfaceElevated,
    onSurfaceVariant = TextSecondary
)

@Composable
fun CallScreenerTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val insets = WindowCompat.getInsetsController(window, view)
                insets.isAppearanceLightStatusBars = false
                insets.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        shapes = AppShapes,
        typography = AppTypography
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle(fontFamily = AppFont)
        ) {
            content()
        }
    }
}
