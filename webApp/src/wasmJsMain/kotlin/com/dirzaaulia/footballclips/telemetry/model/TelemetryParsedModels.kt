package com.dirzaaulia.footballclips.telemetry.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val stageCategory: String = "GENERAL",
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
    val totalCycles: Int = 0,
    val totalHighlights: Int = 0,
    val totalCandidates: Int = 0,
    val totalGoals: Int = 0,
    val totalErrors: Int = 0,
    val totalLogLines: Int = 0,
    val apiQuotaUsed: Int = 0,
    val apiQuotaTotal: Int = 10000,
    val mappedClubs: Int = 96,
    val totalClubs: Int = 96,
    val totalCompetitions: Int = 17,
    val espnMatchesCount: Int = 0,
    val legacyMatchesCount: Int = 0,
    val nextSleepText: String = "Next Run: Dynamic Scheduler Active",
    val cycleNumber: Long = 0,
    val fixtureCycleIndex: Long = 0,
    val highlightSweepIndex: Long = 0,
    val fixtureCadence: String = "60s Live / 2h Idle",
    val highlightCadence: String = "Dynamic Tier Cooldown Pacing",
    val fixtureStatus: String = "ESPN LIVE",
    val highlightStatus: String = "AUTONOMOUS SWEEP",
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
    @SerialName("hits_today") val hitsToday: Int = 0,
    val limit: Int = 10000,
    val remaining: Int = 10000,
    @SerialName("pt_date") val ptDate: String = "",
    @SerialName("hours_until_reset") val hoursUntilReset: Double = 0.0,
    @SerialName("is_exhausted") val isExhausted: Boolean = false,
)

@Serializable
data class PipelineEngineInfo(
    @SerialName("cycle_index") val cycleIndex: Long? = null,
    @SerialName("sweep_index") val sweepIndex: Long? = null,
    val cadence: String = "",
    val status: String = "ACTIVE",
    val mode: String = "",
    @SerialName("last_sync") val lastSync: String? = null,
    @SerialName("last_sweep") val lastSweep: String? = null,
    @SerialName("quota_hits") val quotaHits: Int? = null,
    @SerialName("quota_target") val quotaTarget: Int? = null,
)
