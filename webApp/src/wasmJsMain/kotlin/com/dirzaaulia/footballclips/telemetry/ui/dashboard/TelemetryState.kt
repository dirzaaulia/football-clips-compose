package com.dirzaaulia.footballclips.telemetry.ui.dashboard

import com.dirzaaulia.footballclips.telemetry.model.ApmLiveMetrics
import com.dirzaaulia.footballclips.telemetry.model.AudienceStatePayload
import com.dirzaaulia.footballclips.telemetry.model.CycleHistoryItem
import com.dirzaaulia.footballclips.telemetry.model.DatabaseMatchRecord
import com.dirzaaulia.footballclips.telemetry.model.DiscoveredHighlight
import com.dirzaaulia.footballclips.telemetry.model.LogStreamItem
import com.dirzaaulia.footballclips.telemetry.model.NotificationLogItem
import com.dirzaaulia.footballclips.telemetry.model.NotificationTelemetryPayload
import com.dirzaaulia.footballclips.telemetry.model.PipelineScopeFilter
import com.dirzaaulia.footballclips.telemetry.model.TelemetryFilterLevel
import com.dirzaaulia.footballclips.telemetry.model.TelemetryMetrics
import com.dirzaaulia.footballclips.telemetry.model.WorkerCycle
import com.dirzaaulia.footballclips.telemetry.model.WorkerEvent

data class TelemetryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
    val isInitialLoad: Boolean = true,
    val rawCycles: List<WorkerCycle> = emptyList(),
    val allRawEvents: List<WorkerEvent> = emptyList(),
    val logStream: List<LogStreamItem> = emptyList(),
    val discoveredHighlights: List<DiscoveredHighlight> = emptyList(),
    val cycleHistory: List<CycleHistoryItem> = emptyList(),
    val notificationsPayload: NotificationTelemetryPayload = NotificationTelemetryPayload(),
    val audienceState: AudienceStatePayload = AudienceStatePayload(),
    val apmLiveMetrics: ApmLiveMetrics? = null,
    val syncedMatches: List<DatabaseMatchRecord> = emptyList(),
    val searchQuery: String = "",
    val selectedLevel: TelemetryFilterLevel = TelemetryFilterLevel.ALL,
    val selectedDate: String? = null,
    val selectedPipelineScope: PipelineScopeFilter = PipelineScopeFilter.ALL,
    val selectedInspectorItem: LogStreamItem? = null,
    val selectedCycleItem: CycleHistoryItem? = null,
    val selectedNotificationItem: NotificationLogItem? = null,
) {
    val filteredStream: List<LogStreamItem>
        get() = logStream.filter { item ->
            if (selectedDate != null && !item.cycleTimestamp.startsWith(selectedDate)) return@filter false

            val matchesPipeline = when (selectedPipelineScope) {
                PipelineScopeFilter.ALL -> true
                PipelineScopeFilter.YOUTUBE -> {
                    item.subsystem.contains("YOUTUBE", true) ||
                        item.subsystem.contains("AI_GEMINI", true) ||
                        item.subsystem.contains("QUOTA", true) ||
                        item.isHighlight ||
                        item.action in listOf("SEARCH_START", "CHANNEL_SCAN", "RSS_DISCOVERY", "AI_EVAL", "AI_MATCH", "LINKED", "REJECTED", "CANDIDATES", "CANDIDATE_DETAIL", "CHANNEL_FOUND", "CHANNEL_EMPTY", "FILTER_TITLE", "FILTER_DURATION", "FILTER_SHORT", "FILTER_GEO", "QUOTA_EXCEEDED", "COOLDOWN_PROTECTED", "SKIP_COOLDOWN", "DUPLICATE_CLAIM", "NOT_FOUND", "DB_LINKED", "QUOTA_GUARD", "API_FALLBACK", "SEARCH_ERROR") ||
                        item.cleanMessage.contains("YouTube", true) ||
                        item.cleanMessage.contains("highlight", true)
                }
                PipelineScopeFilter.FIXTURES -> {
                    item.subsystem.contains("MATCH", true) ||
                        item.subsystem.contains("FIXTURE", true) ||
                        item.subsystem.contains("SCORE", true) ||
                        item.subsystem.contains("LIVE", true) ||
                        item.subsystem.contains("ESPN", true) ||
                        item.subsystem.contains("STANDINGS", true) ||
                        item.subsystem.contains("MAINTENANCE", true) ||
                        item.subsystem.contains("DATABASE", true) ||
                        item.isGoal ||
                        item.action == "SCORE" ||
                        item.cleanMessage.contains("fixture", true) ||
                        item.cleanMessage.contains("score", true) ||
                        item.cleanMessage.contains("standings", true) ||
                        item.cleanMessage.contains("kickoff", true) ||
                        item.cleanMessage.contains("full time", true)
                }
            }
            if (!matchesPipeline) return@filter false

            val matchesLevel = when (selectedLevel) {
                TelemetryFilterLevel.ALL -> true
                TelemetryFilterLevel.HIGHLIGHT -> item.isHighlight || item.level == "SUCCESS"
                TelemetryFilterLevel.SYNC -> item.subsystem.contains("SYNC", true) || item.cleanMessage.contains("fixture", true)
                TelemetryFilterLevel.SCORE -> item.isGoal || item.action == "SCORE" || item.cleanMessage.contains("goal", true)
                TelemetryFilterLevel.WARN -> item.level == "WARN" || item.cleanMessage.contains("Quota", true)
                TelemetryFilterLevel.ERROR -> item.isError || item.level == "ERROR"
            }
            if (!matchesLevel) return@filter false

            if (searchQuery.isBlank()) return@filter true
            val q = searchQuery.trim().lowercase()
            item.cleanMessage.lowercase().contains(q) ||
                item.subsystem.lowercase().contains(q) ||
                item.action.lowercase().contains(q) ||
                item.timestamp.lowercase().contains(q)
        }

    val metrics: TelemetryMetrics
        get() {
            val errs = logStream.count { it.isError || it.level == "ERROR" }
            val highlights = apmLiveMetrics?.highlightsLinked ?: syncedMatches.count { it.highlightVideoId != null }
            val espnCount = syncedMatches.count { it.provider?.lowercase() == "espn" }
            val legacyCount = syncedMatches.count { it.provider?.lowercase() == "football_data" }
            val totalMatches = apmLiveMetrics?.totalMatches ?: (if (syncedMatches.isNotEmpty()) syncedMatches.size else 788)
            val cycleNum = apmLiveMetrics?.cycleIndex ?: (rawCycles.firstOrNull()?.cycleIndex ?: (rawCycles.firstOrNull()?.id ?: 281L))

            val fixtureCycle = apmLiveMetrics?.fixtureCycleIndex
                ?: rawCycles.firstOrNull { it.pipeline == "FIXTURE_SYNC" }?.let { it.pipelineCycleIndex ?: it.cycleIndex }
                ?: cycleNum

            val highlightSweep = apmLiveMetrics?.highlightSweepIndex
                ?: rawCycles.firstOrNull { it.pipeline == "YOUTUBE_SCRAPER" }?.let { it.pipelineCycleIndex ?: it.cycleIndex }
                ?: 340L

            val quotaUsed = apmLiveMetrics?.youtubeQuotaHits ?: 0
            val quotaRem = apmLiveMetrics?.youtubeQuotaRemaining ?: 10000

            val lastFixtureCycle = rawCycles.firstOrNull { it.pipeline == "FIXTURE_SYNC" } ?: rawCycles.firstOrNull()
            val lastHighlightCycle = rawCycles.firstOrNull { it.pipeline == "YOUTUBE_SCRAPER" }

            val fixtureCadence = lastFixtureCycle?.nextSleep ?: "60s Live / 2h Idle"
            val highlightCadence = lastHighlightCycle?.nextSleep ?: "Dynamic Tier Cooldown Pacing"

            return TelemetryMetrics(
                totalCycles = cycleNum.toInt(),
                totalHighlights = highlights,
                totalCandidates = totalMatches,
                totalGoals = syncedMatches.sumOf { (it.homeScore ?: 0) + (it.awayScore ?: 0) },
                totalErrors = errs,
                totalLogLines = logStream.size,
                apiQuotaUsed = quotaUsed,
                apiQuotaTotal = if (quotaUsed + quotaRem > 0) quotaUsed + quotaRem else 10000,
                mappedClubs = 96,
                totalClubs = 96,
                totalCompetitions = 17,
                espnMatchesCount = espnCount,
                legacyMatchesCount = legacyCount,
                nextSleepText = lastFixtureCycle?.nextSleep?.let { "Next Run: $it" } ?: "Next Run: Dynamic 1m/2h Scheduler",
                cycleNumber = cycleNum,
                fixtureCycleIndex = fixtureCycle,
                highlightSweepIndex = highlightSweep,
                fixtureCadence = fixtureCadence,
                highlightCadence = highlightCadence,
                fixtureStatus = if (fixtureCadence.contains("60s", true)) "LIVE ACTIVE" else "IDLE NOMINAL",
                highlightStatus = if (quotaUsed >= 10000) "QUOTA SLEEP" else "AUTONOMOUS SWEEP",
            )
        }

    val latestCycleTimestamp: String
        get() = rawCycles.firstOrNull()?.timestamp ?: "15 Oct 2026, 19:05:00 UTC"
}

sealed interface TelemetryUiAction {
    data object RefreshData : TelemetryUiAction
    data object TriggerCycle : TelemetryUiAction
    data object PurgeCache : TelemetryUiAction
    data object RenewWebSub : TelemetryUiAction
    data class SetSearchQuery(val query: String) : TelemetryUiAction
    data class SelectFilterLevel(val level: TelemetryFilterLevel) : TelemetryUiAction
    data class SelectDate(val date: String?) : TelemetryUiAction
    data class SelectPipelineScope(val scope: PipelineScopeFilter) : TelemetryUiAction
    data class InspectLog(val item: LogStreamItem?) : TelemetryUiAction
    data class InspectCycle(val item: CycleHistoryItem?) : TelemetryUiAction
    data class InspectNotification(val item: NotificationLogItem?) : TelemetryUiAction
    data object DismissInspector : TelemetryUiAction
}

sealed interface TelemetryUiEffect {
    data class ShowToast(val message: String) : TelemetryUiEffect
    data class OpenUrl(val url: String) : TelemetryUiEffect
}
