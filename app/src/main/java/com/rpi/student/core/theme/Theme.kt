package com.rpi.student.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ---------- Glass token system (all values Remote Config-able per blueprint §12) ----------
data class GlassColors(
    val fill: Color,
    val border: Color,
    val highlight: Color,
    val blurRadius: Float = 24f,       // px — full effect API 31+
    val cornerRadius: Float = 24f,     // dp
)

// API 31+ = full Liquid Glass; API 26–30 = flat fallback (blueprint §12 lock)
val isFullGlassSupported: Boolean = android.os.Build.VERSION.SDK_INT >= 31

data class RpiColors(
    val text: Color,
    val textDim: Color,
    val accent: Color,
    val accent2: Color,
    val alert: Color,
    val glass: GlassColors,
)

val LocalRpiColors = staticCompositionLocalOf {
    RpiColors(
        text = LText, textDim = LTextDim, accent = RpiBlue, accent2 = RpiCyan, alert = RpiRed,
        glass = GlassColors(GlassFillLight, GlassBorderLight, GlassHighlightLight),
    )
}

private val LightScheme = lightColorScheme(
    primary = RpiBlue,
    onPrimary = Color.White,
    secondary = RpiCyan,
    tertiary = RpiAmber,
    background = LBackground,
    surface = LSurface,
    onBackground = LText,
    onSurface = LText,
)

private val DarkScheme = darkColorScheme(
    primary = RpiBlue,
    onPrimary = Color.White,
    secondary = RpiCyan,
    tertiary = RpiAmber,
    background = DBackground,
    surface = DSurface,
    onBackground = DText,
    onSurface = DText,
)

@Composable
fun LiquidGlassTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        RpiColors(DText, DTextDim, RpiBlue, RpiCyan, RpiRed, GlassColors(GlassFillDark, GlassBorderDark, GlassHighlightDark))
    } else {
        RpiColors(LText, LTextDim, RpiBlue, RpiCyan, RpiRed, GlassColors(GlassFillLight, GlassBorderLight, GlassHighlightLight))
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalRpiColors provides colors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = RpiTypography,
            content = content,
        )
    }
}
