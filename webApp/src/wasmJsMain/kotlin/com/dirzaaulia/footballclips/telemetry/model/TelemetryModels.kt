package com.dirzaaulia.footballclips.telemetry.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkerCycle(
    val id: Long,
    val timestamp: String,
    val mode: String,
    @SerialName("next_sleep")
    val nextSleep: String? = null,
    @SerialName("cycle_index")
    val cycleIndex: Long? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
)

@Serializable
data class WorkerEvent(
    val id: Long,
    @SerialName("cycle_id")
    val cycleId: Long,
    @SerialName("step_title")
    val stepTitle: String,
    @SerialName("step_status")
    val stepStatus: String,
    @SerialName("event_text")
    val eventText: String,
    @SerialName("event_order")
    val eventOrder: Int,
    @SerialName("is_highlight")
    val isHighlight: Boolean = false,
    @SerialName("is_goal")
    val isGoal: Boolean = false,
    @SerialName("is_error")
    val isError: Boolean = false,
)

@Serializable
data class SupabaseMatchRecord(
    val id: Long,
    @SerialName("home_team_name")
    val homeTeamName: String,
    @SerialName("away_team_name")
    val awayTeamName: String,
    @SerialName("competition_name")
    val competitionName: String = "European Football",
    @SerialName("utc_date")
    val utcDate: String,
    val status: String,
    @SerialName("home_score")
    val homeScore: Int? = null,
    @SerialName("away_score")
    val awayScore: Int? = null,
    @SerialName("highlight_video_id")
    val highlightVideoId: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

enum class TelemetryFilterLevel(val label: String) {
    ALL("Semua Status & Logs"),
    HIGHLIGHT("Highlight / Extract"),
    SYNC("Supabase Sync & Schedules"),
    SCORE("Deteksi Skor & Match"),
    WARN("Warning / API Quota"),
    ERROR("Error")
}

enum class TelemetryViewMode(val label: String) {
    CONSOLE_STREAM("Console Stream"),
    PIPELINE_CYCLES("Pipeline Cycles")
}

enum class TelemetryNavSection(val label: String) {
    STREAM("Telemetry Stream"),
    NOTIFICATIONS("Club Alerts"),
    THREADS("Worker Services"),
    ALERTS("Failure Alerts"),
    CONFIG("Configuration")
}

data class LogStreamItem(
    val id: Long,
    val cycleId: Long,
    val cycleTimestamp: String,
    val timestamp: String,
    val threadId: String = "TH-0",
    val level: String,
    val subsystem: String,
    val action: String,
    val cleanMessage: String,
    val rawText: String,
    val isHighlight: Boolean = false,
    val isGoal: Boolean = false,
    val isError: Boolean = false,
    val extractedUrl: String? = null,
    val league: String? = null,
    val matchDate: String? = null,
    val fixture: String? = null,
    val stageCategory: String = "GENERAL"
)

data class DiscoveredHighlight(
    val id: String,
    val title: String,
    val league: String,
    val quality: String = "1080p60",
    val duration: String = "03:42",
    val channel: String,
    val videoUrl: String,
    val isEmbedReady: Boolean = true,
    val isSynced: Boolean = true,
)

data class ParsedMatch(
    val index: Int,
    val title: String,
    val statusType: String,
    val badgeText: String,
    val events: List<WorkerEvent>,
    val isExpanded: Boolean = false,
)

data class ParsedStep(
    val title: String,
    val status: String,
    val headerEvents: List<WorkerEvent> = emptyList(),
    val matches: List<ParsedMatch> = emptyList(),
    val footerEvents: List<WorkerEvent> = emptyList(),
    val regularEvents: List<WorkerEvent> = emptyList(),
    val isExpanded: Boolean = true,
)

data class CycleWithEvents(
    val cycle: WorkerCycle,
    val steps: List<ParsedStep> = emptyList(),
    val rawEvents: List<WorkerEvent> = emptyList(),
    val isExpanded: Boolean = false,
)

data class TelemetryMetrics(
    val totalCycles: Int = 17,
    val totalHighlights: Int = 17,
    val totalCandidates: Int = 358,
    val totalGoals: Int = 0,
    val totalErrors: Int = 0,
    val totalLogLines: Int = 0,
    val apiQuotaUsed: Int = 1420,
    val apiQuotaTotal: Int = 10000,
    val mappedClubs: Int = 96,
    val totalClubs: Int = 96,
    val nextSleepText: String = "Next sleep: 2 Jam (Jeda normal)",
    val cycleNumber: Long = 17,
)

data class CycleHistoryItem(
    val id: Long,
    val timestamp: String,
    val executionMode: String,
    val ingestedMatches: Int,
    val highlightsHarvested: Int,
    val apiQuotaCost: Int,
    val dbStatus: String,
    val exceptions: Int,
)

@Serializable
data class TelemetryQuotaInfo(
    @SerialName("hits_today")
    val hitsToday: Int = 0,
    val limit: Int = 10000,
    val remaining: Int = 10000,
    @SerialName("pt_date")
    val ptDate: String = "",
    @SerialName("hours_until_reset")
    val hoursUntilReset: Double = 0.0,
    @SerialName("is_exhausted")
    val isExhausted: Boolean = false,
)

@Serializable
data class TelemetryStreamPayload(
    @SerialName("updated_at")
    val updatedAt: String = "",
    @SerialName("cycle_count")
    val cycleCount: Int = 0,
    val quota: TelemetryQuotaInfo? = null,
    val cycles: List<WorkerCycle> = emptyList(),
    val events: List<WorkerEvent> = emptyList(),
)

