package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

val StitchTextPrimary = Color(0xFFF1F5F9)
val StitchTextSecondary = Color(0xFF94A3B8)
val StitchTextMuted = Color(0xFF64748B)

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
        content = content
    )
}
