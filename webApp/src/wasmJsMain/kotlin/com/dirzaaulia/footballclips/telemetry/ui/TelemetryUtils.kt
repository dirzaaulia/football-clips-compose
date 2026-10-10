package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.ui.graphics.Color
import kotlinx.browser.window
import kotlin.js.ExperimentalWasmJsInterop

object TelemetryUtils {

    @OptIn(ExperimentalWasmJsInterop::class)
    fun copyToClipboard(text: String) {
        runCatching {
            window.navigator.clipboard.writeText(text)
        }
    }

    fun openUrlInNewTab(url: String) {
        runCatching {
            window.open(url, "_blank")
        }
    }

    fun getCurrentTimestampWib(): String {
        return "10 Oct 2026, 00:35:51 WIB"
    }

    private val urlRegex = Regex("(https?://(?:www\\.)?(?:youtube\\.com|youtu\\.be)/[^\\s)]+)")

    fun extractYouTubeUrl(text: String): String? {
        val match = urlRegex.find(text)
        return match?.value
    }

    fun getLevelBadgeColors(level: String): Triple<Color, Color, Color> {
        return when (level.uppercase()) {
            "SUCCESS", "OK" -> Triple(StitchSuccess.copy(alpha = 0.15f), StitchSuccess, StitchSuccess.copy(alpha = 0.4f))
            "WARN", "WARNING" -> Triple(StitchWarning.copy(alpha = 0.15f), StitchWarning, StitchWarning.copy(alpha = 0.4f))
            "ERROR", "CRITICAL", "ERR" -> Triple(StitchError.copy(alpha = 0.15f), StitchError, StitchError.copy(alpha = 0.4f))
            else -> Triple(StitchPrimary.copy(alpha = 0.15f), StitchPrimary, StitchPrimary.copy(alpha = 0.4f))
        }
    }

    fun getSubsystemBadgeColor(subsystem: String): Color {
        return when (subsystem.uppercase()) {
            "MATCH_SYNC" -> StitchPrimary
            "YOUTUBE" -> StitchError
            "AI_GEMINI" -> StitchSecondary
            "STANDINGS" -> StitchTertiary
            "MAINTENANCE" -> StitchTextSecondary
            else -> StitchTextPrimary
        }
    }

    fun getEventLineColor(text: String): Color {
        return when {
            text.contains("SUCCESS") || text.contains("BERHASIL") || text.contains("DAPAT") -> StitchSuccess
            text.contains("SCORE") || text.contains("SKOR") || text.contains("FULL TIME") -> StitchWarning
            text.contains("STATUS") || text.contains("NEW_MATCH") -> StitchTertiary
            text.contains("429") || text.contains("Quota") || text.contains("Error") || text.contains("ERR") -> StitchError
            text.contains("WARN") || text.contains("Limit") -> StitchWarning
            text.contains("AI") || text.contains("GEMINI") -> StitchSecondary
            text.contains("FETCH") || text.contains("Meminta") -> StitchPrimary
            else -> StitchTextSecondary
        }
    }

    fun getBadgeColorForMatch(statusType: String): Pair<Color, Color> {
        return when (statusType) {
            "success" -> Pair(StitchSuccess.copy(alpha = 0.15f), StitchSuccess)
            "fail" -> Pair(StitchWarning.copy(alpha = 0.15f), StitchWarning)
            "force", "opp" -> Pair(StitchPrimary.copy(alpha = 0.15f), StitchPrimary)
            "skip" -> Pair(StitchSurfaceContainerHigh, StitchTextSecondary)
            else -> Pair(StitchSecondary.copy(alpha = 0.15f), StitchSecondary)
        }
    }
}
