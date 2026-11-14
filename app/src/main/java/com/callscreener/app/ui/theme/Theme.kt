package com.callscreener.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

fun getPixelMonetLightColorScheme(): ColorScheme {
    return lightColorScheme(
        primary = Color(0xFF0B57D0),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD3E3FD),
        onPrimaryContainer = Color(0xFF041E49),
        secondary = Color(0xFF00639B),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFC2E7FF),
        onSecondaryContainer = Color(0xFF001D35),
        tertiary = Color(0xFF6750A4),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE8DEF8),
        onTertiaryContainer = Color(0xFF21005D),
        background = Color(0xFFFDFBFF),
        onBackground = Color(0xFF1A1C1E),
        surface = Color(0xFFFFFFFF),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF7F9FC),
        surfaceContainer = Color(0xFFF0F4F9),
        surfaceContainerHigh = Color(0xFFE9EEF6),
        surfaceContainerHighest = Color(0xFFE1E3E8),
        onSurface = Color(0xFF1F1F1F),
        onSurfaceVariant = Color(0xFF444746),
        outline = Color(0xFF747775),
        outlineVariant = Color(0xFFC4C7C5),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002)
    )
}

fun getPixelMonetDarkColorScheme(): ColorScheme {
    return darkColorScheme(
        primary = Color(0xFFA8C7FA),
        onPrimary = Color(0xFF042E69),
        primaryContainer = Color(0xFF234479),
        onPrimaryContainer = Color(0xFFD3E3FD),
        secondary = Color(0xFFC2E7FF),
        onSecondary = Color(0xFF003355),
        secondaryContainer = Color(0xFF2A4257),
        onSecondaryContainer = Color(0xFFD4E4F7),
        tertiary = Color(0xFFD0BCFF),
        onTertiary = Color(0xFF381E72),
        tertiaryContainer = Color(0xFF4A4458),
        onTertiaryContainer = Color(0xFFE8DEF8),
        background = Color(0xFF111318),
        onBackground = Color(0xFFE2E2E6),
        surface = Color(0xFF111318),
        surfaceContainerLowest = Color(0xFF0C0E13),
        surfaceContainerLow = Color(0xFF191B20),
        surfaceContainer = Color(0xFF1E2025),
        surfaceContainerHigh = Color(0xFF282A2F),
        surfaceContainerHighest = Color(0xFF33353A),
        onSurface = Color(0xFFE2E2E6),
        onSurfaceVariant = Color(0xFFC4C6D0),
        outline = Color(0xFF8E9099),
        outlineVariant = Color(0xFF44474F),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6)
    )
}

@Composable
fun rememberPixelMonetColorScheme(isDark: Boolean = ThemeManager.isDarkTheme()): ColorScheme {
    val context = LocalContext.current
    return remember(context, isDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } catch (_: Throwable) {
                if (isDark) getPixelMonetDarkColorScheme() else getPixelMonetLightColorScheme()
            }
        } else {
            if (isDark) getPixelMonetDarkColorScheme() else getPixelMonetLightColorScheme()
        }
    }
}

@Composable
fun CallScreenerTheme(
    darkTheme: Boolean = ThemeManager.isDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = rememberPixelMonetColorScheme(darkTheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val insets = WindowCompat.getInsetsController(window, view)
                insets.isAppearanceLightStatusBars = !darkTheme
                insets.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
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
