package com.callscreener.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val GlassBackground = Color(0xFF000000)
val GlassBackgroundGradient = Brush.verticalGradient(
    listOf(
        Color(0xFF000000),
        Color(0xFF030509),
        Color(0xFF000000)
    )
)

val GlassSurface = Color(0x18FFFFFF)
val GlassSurfaceSubtle = Color(0x0CFFFFFF)
val GlassSurfaceElevated = Color(0x26FFFFFF)
val GlassBorder = Color(0x30FFFFFF)
val GlassBorderSubtle = Color(0x18FFFFFF)

val AppleBlue = Color(0xFF0A84FF)
val AppleIndigo = Color(0xFF5E5CE6)
val ApplePurple = Color(0xFFBF5AF2)
val AppleTeal = Color(0xFF64D2FF)
val AppleGreen = Color(0xFF30D158)
val AppleYellow = Color(0xFFFFD60A)
val AppleAmber = Color(0xFFFF9F0A)
val AppleRed = Color(0xFFFF453A)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xCCEBEBF5)
val TextTertiary = Color(0x80EBEBF5)
val TextMuted = Color(0x48EBEBF5)

val CallerGlassBubble = Color(0x28FFFFFF)
val UserGlassBubble = Color(0xFF0A84FF)
val SystemGlassBubble = Color(0x14FFFFFF)

val BgDark = GlassBackground
val SurfaceDark = Color(0xFF0B101C)
val SurfaceVariantDark = Color(0x18FFFFFF)
val PrimaryIndigo = AppleBlue
val PrimaryIndigoLight = AppleTeal
val GreenAccept = AppleGreen
val RedHangup = AppleRed
val AmberScreening = AppleAmber

val LiquidGlassFill = Brush.linearGradient(
    listOf(
        Color(0x28FFFFFF),
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
        Color(0x32FFFFFF),
        Color(0x10FFFFFF),
        Color(0x1CFFFFFF)
    )
)

val LiquidGlassBorder = Brush.verticalGradient(
    listOf(
        Color(0x85FFFFFF),
        Color(0x30FFFFFF),
        Color(0x12FFFFFF)
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
        Color(0xE5FFFFFF),
        Color(0x50FFFFFF),
        Color(0x20FFFFFF),
        Color(0x65FFFFFF)
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
