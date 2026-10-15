package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Stitch Dark / Cyberpunk Observability Theme
val StitchBg = Color(0xFF070A0F)
val StitchSidebar = Color(0xFF090D15)
val StitchSurface = Color(0xFF0D131D)
val StitchSurfaceContainer = Color(0xFF101724)
val StitchSurfaceContainerHigh = Color(0xFF141C2B)
val StitchTerminalBg = Color(0xFF05080C)
val StitchBorder = Color(0xFF1A2333)
val StitchBorderSubtle = Color(0xFF131A26)

val StitchPrimary = Color(0xFF00E5FF) // Neon Cyan
val StitchSecondary = Color(0xFF38BDF8) // Sky Blue
val StitchSuccess = Color(0xFF22C55E) // Emerald Green
val StitchWarning = Color(0xFFF59E0B) // Amber
val StitchError = Color(0xFFEF4444) // Red
val StitchPurple = Color(0xFFA855F7) // Purple
val StitchCyan = Color(0xFF00E5FF)
val StitchTertiary = StitchCyan

val StitchTextPrimary = Color(0xFFF8FAFC) // Bright Crisp White
val StitchTextSecondary = Color(0xFFCBD5E1) // High-contrast Light Slate
val StitchTextMuted = Color(0xFF94A3B8) // Clearly Legible Muted Slate
val StitchTeal = Color(0xFF14B8A6) // Clean Mint/Teal for Cleanup/Maintenance
val StitchGold = Color(0xFFFBBF24) // Gold for Standings & Match Events

// Backwards compatibility aliases
val TelemetryBg = StitchBg
val TelemetrySurface = StitchSurface
val TelemetrySurfaceVariant = StitchSurfaceContainer
val TelemetryBorder = StitchBorder
val TelemetryPrimary = StitchPrimary
val TelemetryAccent = StitchSuccess
val TelemetryError = StitchError
val TelemetryWarning = StitchWarning
val TelemetryTextPrimary = StitchTextPrimary
val TelemetryTextSecondary = StitchTextSecondary

object TelemetryTypography {
    val titleLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )
    val titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    )
    val titleSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
    val bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.25.sp
    )
    val bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.25.sp
    )
    val bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.2.sp
    )
    val labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    )
    val labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.4.sp
    )
    val codeStream = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )
}

private val MaterialTypography = Typography(
    titleLarge = TelemetryTypography.titleLarge,
    titleMedium = TelemetryTypography.titleMedium,
    titleSmall = TelemetryTypography.titleSmall,
    bodyLarge = TelemetryTypography.bodyLarge,
    bodyMedium = TelemetryTypography.bodyMedium,
    bodySmall = TelemetryTypography.bodySmall,
    labelLarge = TelemetryTypography.bodyMedium,
    labelMedium = TelemetryTypography.labelMedium,
    labelSmall = TelemetryTypography.labelSmall
)

private val DarkColorScheme = darkColorScheme(
    primary = StitchPrimary,
    background = StitchBg,
    surface = StitchSurface,
    surfaceVariant = StitchSurfaceContainer,
    onPrimary = Color(0xFF002D6B),
    onBackground = StitchTextPrimary,
    onSurface = StitchTextPrimary,
    outline = StitchBorder,
    error = StitchError,
)

@Composable
fun TelemetryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MaterialTypography,
        content = content
    )
}
