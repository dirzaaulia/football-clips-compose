package com.dirzaaulia.footballclips.telemetry.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Kinetic Editorial - Dark Mode Observability Palette (Cohesive with Android App & Next.js)
val DarkBaseBackground = Color(0xFF101418)
val DarkSurfaceLow = Color(0xFF181C20)
val DarkSurfaceCard = Color(0xFF1E2227)
val DarkBorder = Color(0xFF334155)

val DarkTextHigh = Color(0xFFF1F5F9)
val DarkTextMuted = Color(0xFF94A3B8)

val DarkCobaltPrimary = Color(0xFF7098F8)
val DarkCobaltContainer = Color(0xFF1E3A8A)
val DarkOnCobaltContainer = Color(0xFFDBE5FF)
val DarkSlateSecondary = Color(0xFF8DA3C7)
val DarkIcyCyan = Color(0xFF4FC3F7)

// Backward compatibility mappings for Telemetry UI components
val StitchBg = DarkBaseBackground
val StitchSidebar = DarkSurfaceLow
val StitchSurface = DarkSurfaceCard
val StitchSurfaceContainer = Color(0xFF23272E)
val StitchSurfaceContainerHigh = Color(0xFF282D35)
val StitchTerminalBg = Color(0xFF0D1013)
val StitchBorder = DarkBorder
val StitchBorderSubtle = Color(0xFF242C38)

val StitchPrimary = DarkCobaltPrimary
val StitchSecondary = DarkSlateSecondary
val StitchSuccess = Color(0xFF22C55E)
val StitchWarning = Color(0xFFF59E0B)
val StitchError = Color(0xFFEF4444)
val StitchPurple = Color(0xFFA855F7)
val StitchCyan = DarkIcyCyan
val StitchTertiary = DarkIcyCyan

val StitchTextPrimary = DarkTextHigh
val StitchTextSecondary = Color(0xFFCBD5E1)
val StitchTextMuted = DarkTextMuted
val StitchTeal = Color(0xFF14B8A6)
val StitchGold = Color(0xFFFBBF24)

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
        letterSpacing = 0.sp,
    )
    val titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
    )
    val titleSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    )
    val bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.25.sp,
    )
    val bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.25.sp,
    )
    val bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.2.sp,
    )
    val labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
    )
    val labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.4.sp,
    )
    val codeStream = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
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
    labelSmall = TelemetryTypography.labelSmall,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkCobaltPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkCobaltContainer,
    onPrimaryContainer = DarkOnCobaltContainer,
    secondary = DarkSlateSecondary,
    onSecondary = DarkBaseBackground,
    secondaryContainer = DarkSurfaceCard,
    onSecondaryContainer = DarkTextHigh,
    tertiary = DarkIcyCyan,
    onTertiary = Color.Black,
    background = DarkBaseBackground,
    onBackground = DarkTextHigh,
    surface = DarkSurfaceLow,
    onSurface = DarkTextHigh,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = StitchError,
)

@Composable
fun TelemetryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MaterialTypography,
        content = content,
    )
}
