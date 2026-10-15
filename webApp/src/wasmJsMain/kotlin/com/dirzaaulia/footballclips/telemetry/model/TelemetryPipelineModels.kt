package com.dirzaaulia.footballclips.telemetry.model

enum class PipelineCategory(val label: String) {
    ALL("All Activity"),
    HIGHLIGHTS("Highlight Pipeline"),
    FIXTURES("Fixture & Match Sync"),
    RETENTION("Retention & Purge")
}

data class PipelineStageStep(
    val id: String,
    val fixtureName: String,
    val league: String,
    val matchDate: String,
    val stage: PipelineStepStage,
    val status: String,
    val detailMessage: String,
    val channelOrSource: String? = null,
    val videoUrl: String? = null,
    val timestamp: String,
    val rawLogItem: LogStreamItem? = null
)

enum class PipelineStepStage(val title: String, val badgeColor: String) {
    SEARCHING("YouTube Search", "INFO"),
    CANDIDATES("Candidates Found", "INFO"),
    AI_EVALUATION("Gemini AI Rerank", "INFO"),
    MATCHED("Official Clip Matched", "SUCCESS"),
    LINKED("Saved & Ready", "SUCCESS"),
    NOT_FOUND("Not Found / No Clip", "WARN"),
    QUOTA_ERROR("Quota Limit / Error", "ERROR"),
    FIXTURE_INGESTED("Schedule Ingested", "INFO"),
    NEW_FIXTURE("New Fixture Added", "INFO"),
    LIVE_GOAL("Goal Scored", "SUCCESS"),
    MATCH_FINISHED("Full Time", "INFO"),
    RETENTION_CLEAN("Retention Clean", "SUCCESS"),
    RETENTION_PURGED("Stale Purged", "SUCCESS")
}

data class RetentionPurgeSummary(
    val retentionDays: Int = 30,
    val cutoffDate: String,
    val staleMatchesDeleted: Int,
    val lastVerified: String,
    val isClean: Boolean = true
)
