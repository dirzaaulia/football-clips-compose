package com.dirzaaulia.footballclips.telemetry.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkerCycle(
    val id: Long,
    val timestamp: String,
    val mode: String,
    @SerialName("next_sleep") val nextSleep: String? = null,
    @SerialName("cycle_index") val cycleIndex: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val pipeline: String? = null,
    @SerialName("pipeline_cycle_index") val pipelineCycleIndex: Long? = null,
)

@Serializable
data class WorkerEvent(
    val id: Long,
    @SerialName("cycle_id") val cycleId: Long,
    @SerialName("step_title") val stepTitle: String,
    @SerialName("step_status") val stepStatus: String,
    @SerialName("event_text") val eventText: String,
    @SerialName("event_order") val eventOrder: Int,
    @SerialName("is_highlight") val isHighlight: Boolean = false,
    @SerialName("is_goal") val isGoal: Boolean = false,
    @SerialName("is_error") val isError: Boolean = false,
)

@Serializable
data class TelemetryStreamPayload(
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("cycle_count") val cycleCount: Int = 0,
    val quota: TelemetryQuotaInfo? = null,
    val cycles: List<WorkerCycle> = emptyList(),
    val events: List<WorkerEvent> = emptyList(),
)

@Serializable
data class CloudRunHealth(
    val status: String = "healthy",
    val service: String = "football-clips-worker",
    val timestamp: String = "",
    @SerialName("cycle_index") val cycleIndex: Long = 0,
    @SerialName("is_running") val isRunning: Boolean = false,
    val provider: String = "espn",
    @SerialName("target_competitions") val targetCompetitions: List<String> = emptyList(),
)

@Serializable
data class CloudRunStatus(
    @SerialName("cycle_index") val cycleIndex: Long = 0,
    val provider: String = "espn",
    @SerialName("last_youtube_sync_timestamp") val lastYoutubeSyncTimestamp: Double? = null,
    @SerialName("last_websub_renewal_timestamp") val lastWebsubRenewalTimestamp: Double? = null,
    val timestamp: String = "",
)

@Serializable
data class DatabaseMatchRecord(
    val id: Long,
    @SerialName("home_team_name") val homeTeamName: String,
    @SerialName("away_team_name") val awayTeamName: String,
    @SerialName("home_team_crest") val homeTeamCrest: String? = null,
    @SerialName("away_team_crest") val awayTeamCrest: String? = null,
    @SerialName("competition_name") val competitionName: String = "European Football",
    @SerialName("competition_id") val competitionId: String? = null,
    @SerialName("utc_date") val utcDate: String,
    val status: String,
    @SerialName("home_score") val homeScore: Int? = null,
    @SerialName("away_score") val awayScore: Int? = null,
    @SerialName("highlight_video_id") val highlightVideoId: String? = null,
    val provider: String? = "espn",
    @SerialName("display_clock") val displayClock: String? = null,
    @SerialName("round_name") val roundName: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class TelemetryStandingRecord(
    val id: String? = null,
    @SerialName("competition_code") val competitionCode: String = "",
    @SerialName("competition_name") val competitionName: String = "",
    val position: Int = 0,
    @SerialName("team_id") val teamId: Long? = null,
    @SerialName("season") val season: Int? = null,
    @SerialName("team_name") val teamName: String = "",
    @SerialName("team_short_name") val teamShortName: String? = null,
    @SerialName("crest_url") val crestUrl: String? = null,
    @SerialName("played_games") val playedGames: Int = 0,
    val won: Int = 0,
    val draw: Int = 0,
    val lost: Int = 0,
    val points: Int = 0,
    @SerialName("goals_for") val goalsFor: Int = 0,
    @SerialName("goals_against") val goalsAgainst: Int = 0,
    @SerialName("goal_difference") val goalDifference: Int = 0,
    val form: String? = null,
)

data class ApmLiveMetrics(
    val cycleIndex: Long = 0,
    val fixtureCycleIndex: Long = 0,
    val highlightSweepIndex: Long = 0,
    val isWorkerRunning: Boolean = true,
    val totalMatches: Int = 793,
    val liveMatches: Int = 0,
    val finishedMatches: Int = 0,
    val highlightsLinked: Int = 0,
    val highlightsUnlinked: Int = 0,
    val highlightCoveragePercent: Double = 0.0,
    val youtubeQuotaHits: Int = 0,
    val youtubeQuotaRemaining: Int = 10000,
    val roundTripLatencyMs: Long = 0,
    val edgeProxyStatus: String = "ONLINE (Cloudflare)",
    val lastUpdated: String = "",
)
