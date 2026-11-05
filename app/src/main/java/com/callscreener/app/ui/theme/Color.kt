package com.callscreener.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Authentic Apple OLED Pitch Black & Liquid Glass Palette
val GlassBackground = Color(0xFF000000)
val GlassBackgroundGradient = Brush.verticalGradient(
    listOf(
        Color(0xFF000000),
        Color(0xFF030509),
        Color(0xFF000000)
    )
)

val GlassSurface = Color(0x18FFFFFF) // 9% translucent liquid
val GlassSurfaceSubtle = Color(0x0CFFFFFF) // 5% translucent liquid
val GlassSurfaceElevated = Color(0x26FFFFFF) // 15% elevated liquid
val GlassBorder = Color(0x30FFFFFF) // 19% crisp specular edge
val GlassBorderSubtle = Color(0x18FFFFFF) // 9% subtle border

// Apple iOS Accent Tones
val AppleBlue = Color(0xFF0A84FF)
val AppleIndigo = Color(0xFF5E5CE6)
val ApplePurple = Color(0xFFBF5AF2)
val AppleTeal = Color(0xFF64D2FF)
val AppleGreen = Color(0xFF30D158)
val AppleYellow = Color(0xFFFFD60A)
val AppleAmber = Color(0xFFFF9F0A)
val AppleRed = Color(0xFFFF453A)

// Typography & Hierarchy
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xCCEBEBF5) // 80% opacity white
val TextTertiary = Color(0x80EBEBF5) // 50% opacity white
val TextMuted = Color(0x48EBEBF5) // 28% opacity white

// Chat & Audio Bubbles
val CallerGlassBubble = Color(0x28FFFFFF)
val UserGlassBubble = Color(0xFF0A84FF)
val SystemGlassBubble = Color(0x14FFFFFF)

// Legacy compatibility aliases
val BgDark = GlassBackground
val SurfaceDark = Color(0xFF0B101C)
val SurfaceVariantDark = Color(0x18FFFFFF)
val PrimaryIndigo = AppleBlue
val PrimaryIndigoLight = AppleTeal
val GreenAccept = AppleGreen
val RedHangup = AppleRed
val AmberScreening = AppleAmber

// Specular Liquid Glass Materials (iOS / VisionOS Style)
val LiquidGlassFill = Brush.linearGradient(
    listOf(
        Color(0x28FFFFFF), // Specular light dispersion top-left
        Color(0x14182234),
        Color(0x0A0D1520)
    )
)

val LiquidGlassFillElevated = Brush.linearGradient(
    listOf(
        Color(0x34FFFFFF),
        Color(0x1A203046),
        Color(0x10152232)
    )
)

val LiquidGlassFillSubtle = Brush.linearGradient(
    listOf(
        Color(0x16FFFFFF),
        Color(0x0A121C28),
        Color(0x05090F16)
    )
)

val LiquidGlassDockFill = Brush.verticalGradient(
    listOf(
        Color(0x32FFFFFF), // Crystalline top refraction
        Color(0x10FFFFFF), // Ultra-clear optical transmission body (wallpaper shines through vividly)
        Color(0x1CFFFFFF)  // Curved base caustic return
    )
)

val LiquidGlassBorder = Brush.verticalGradient(
    listOf(
        Color(0x85FFFFFF), // Razor-sharp specular light catching top beveled rim
        Color(0x30FFFFFF), // Translucent light refraction on sides
        Color(0x12FFFFFF)  // Subtle bottom edge
    )
)

val LiquidGlassBorderSubtle = Brush.verticalGradient(
    listOf(
        Color(0x40FFFFFF),
        Color(0x18FFFFFF),
        Color(0x08FFFFFF)
    )
)

val LiquidGlassDockBorder = Brush.verticalGradient(
    listOf(
        Color(0xE5FFFFFF), // Razor-sharp brilliant specular top highlight
        Color(0x50FFFFFF), // Translucent prismatic refraction on sides
        Color(0x20FFFFFF), // Mid transmission
        Color(0x65FFFFFF)  // Bottom caustic refraction return rim
    )
)

val AppleBlueGlassFill = Brush.linearGradient(
    listOf(
        Color(0xF00A84FF),
        Color(0xD00055D6)
    )
)

val AppleGreenGlassFill = Brush.linearGradient(
    listOf(
        Color(0xF030D158),
        Color(0xD0189B37)
    )
)

val AppleAmberGlassFill = Brush.linearGradient(
    listOf(
        Color(0xF0FF9F0A),
        Color(0xD0C87300)
    )
)

val AppleRedGlassFill = Brush.linearGradient(
    listOf(
        Color(0xF0FF453A),
        Color(0xD0B8251C)
    )
)
