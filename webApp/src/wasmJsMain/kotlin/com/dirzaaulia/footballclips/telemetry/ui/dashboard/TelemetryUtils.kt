package com.dirzaaulia.footballclips.telemetry.ui.dashboard

import androidx.compose.ui.graphics.Color
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchGold
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPurple
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainerHigh
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTeal
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchWarning
import kotlin.js.ExperimentalWasmJsInterop
import kotlinx.browser.window

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
            "MATCH_SYNC" -> StitchSecondary
            "YOUTUBE" -> StitchPurple
            "AI_GEMINI" -> StitchPurple
            "STANDINGS" -> StitchGold
            "MAINTENANCE" -> StitchTeal
            "NOTIFICATIONS" -> Color(0xFFFB7185)
            else -> StitchTextPrimary
        }
    }

    fun getEventLineColor(text: String): Color {
        return when {
            text.contains("CLEANUP") || text.contains("PURGE") || text.contains("RETENTION") || text.contains("MAINTENANCE") -> StitchTeal
            text.contains("LIVE SCORE") || text.contains("GOAL") -> StitchSuccess
            text.contains("FULL TIME") || text.contains("KICKOFF") -> StitchWarning
            text.contains("NEW FIXTURE") || text.contains("INSERTED") || text.contains("NEW_MATCH") || text.contains("MATCH_SYNC") -> StitchSecondary
            text.contains("YOUTUBE") || text.contains("PROCESS") || text.contains("SEARCH") || text.contains("CANDIDATES") || text.contains("CHANNEL") -> StitchPurple
            text.contains("AI") || text.contains("GEMINI") -> StitchPurple
            text.contains("STANDINGS") -> StitchGold
            text.contains("429") || text.contains("Quota") || text.contains("Error") || text.contains("ERR") || text.contains("CRITICAL") -> StitchError
            text.contains("WARN") || text.contains("NOT FOUND") || text.contains("SKIP") || text.contains("STANDBY") -> StitchWarning
            text.contains("SUCCESS") || text.contains("LINKED") || text.contains("SELECTED") -> StitchSuccess
            else -> StitchTextPrimary
        }
    }

    fun getBadgeColorForMatch(statusType: String): Pair<Color, Color> {
        return when (statusType) {
            "success" -> Pair(StitchSuccess.copy(alpha = 0.15f), StitchSuccess)
            "fail" -> Pair(StitchWarning.copy(alpha = 0.15f), StitchWarning)
            "error", "quota" -> Pair(StitchError.copy(alpha = 0.15f), StitchError)
            "force", "opp" -> Pair(StitchPrimary.copy(alpha = 0.15f), StitchPrimary)
            "skip" -> Pair(StitchSurfaceContainerHigh, StitchTextSecondary)
            else -> Pair(StitchSecondary.copy(alpha = 0.15f), StitchSecondary)
        }
    }
}
