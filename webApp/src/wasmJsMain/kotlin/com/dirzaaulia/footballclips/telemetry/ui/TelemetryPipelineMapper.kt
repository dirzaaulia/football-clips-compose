package com.dirzaaulia.footballclips.telemetry.ui

import com.dirzaaulia.footballclips.telemetry.model.*

object TelemetryPipelineMapper {

    private val dateRegex = Regex("\\((\\d{4}-\\d{2}-\\d{2})\\)")
    private val leagueRegex = Regex("\\[([A-Za-z0-9\\s_-]+)\\]")

    fun extractPipelineSteps(logs: List<LogStreamItem>): List<PipelineStageStep> {
        val steps = mutableListOf<PipelineStageStep>()

        for (item in logs) {
            val msg = item.cleanMessage
            val stage = inferStage(item)
            val league = extractLeague(msg) ?: item.league ?: "European Football"
            val matchDate = extractDate(msg) ?: item.matchDate ?: item.timestamp.take(10)
            val fixture = extractFixture(msg) ?: item.fixture ?: "Active Match Cycle"

            steps.add(
                PipelineStageStep(
                    id = "step_${item.id}",
                    fixtureName = fixture,
                    league = league,
                    matchDate = matchDate,
                    stage = stage,
                    status = item.level,
                    detailMessage = msg,
                    channelOrSource = extractChannel(msg),
                    videoUrl = item.extractedUrl,
                    timestamp = item.timestamp,
                    rawLogItem = item
                )
            )
        }
        return steps
    }

    fun mapMatchesToPipelineSteps(matches: List<SupabaseMatchRecord>): List<PipelineStageStep> {
        return matches.map { match ->
            val fixture = "${match.homeTeamName} vs ${match.awayTeamName}"
            val league = extractLeague(match.competitionName) ?: match.competitionName
            val matchDate = match.utcDate.take(10)
            val scoreStr = if (match.homeScore != null && match.awayScore != null) {
                "${match.homeScore} - ${match.awayScore}"
            } else "vs"
            val clockStr = match.displayClock?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""
            val stage = when {
                match.highlightVideoId != null -> PipelineStepStage.LINKED
                match.status == "FINISHED" -> PipelineStepStage.MATCH_FINISHED
                match.status in listOf("IN_PLAY", "PAUSED", "LIVE") -> PipelineStepStage.LIVE_GOAL
                else -> PipelineStepStage.FIXTURE_INGESTED
            }
            val providerLabel = when (match.provider?.lowercase()) {
                "espn" -> "ESPN Live Engine"
                "football_data" -> "Legacy Archive"
                else -> match.provider ?: "ESPN Live Engine"
            }
            val detail = buildString {
                append("[$scoreStr$clockStr] Status: ${match.status}")
                if (match.highlightVideoId != null) {
                    append(" · Highlight linked")
                } else if (match.status == "FINISHED") {
                    append(" · Awaiting clip scrape")
                } else {
                    append(" · Synced from $providerLabel")
                }
            }
            PipelineStageStep(
                id = "match_${match.id}",
                fixtureName = fixture,
                league = league,
                matchDate = matchDate,
                stage = stage,
                status = if (match.highlightVideoId != null) "SUCCESS" else "INFO",
                detailMessage = detail,
                channelOrSource = if (match.highlightVideoId != null) "YouTube Highlight" else providerLabel,
                videoUrl = match.highlightVideoId?.let { "https://www.youtube.com/watch?v=$it" },
                timestamp = match.updatedAt ?: match.utcDate,
                rawLogItem = null
            )
        }
    }

    fun extractRetentionSummary(logs: List<LogStreamItem>): RetentionPurgeSummary {
        val purgeLog = logs.firstOrNull {
            it.subsystem == "MAINTENANCE" || it.cleanMessage.contains("[PURGE]", true) || it.cleanMessage.contains("retention", true)
        }
        val msg = purgeLog?.cleanMessage ?: ""
        val deletedCount = if (msg.contains("Cleaned up", true)) {
            Regex("Cleaned up (\\d+)").find(msg)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        } else 0

        val cutoff = if (msg.contains("cutoff:", true)) {
            msg.substringAfter("cutoff:").substringBefore(")").trim()
        } else "30-Day Window"

        return RetentionPurgeSummary(
            retentionDays = 30,
            cutoffDate = cutoff,
            staleMatchesDeleted = deletedCount,
            lastVerified = purgeLog?.timestamp ?: "Active Cycle",
            isClean = deletedCount == 0
        )
    }

    private fun inferStage(item: LogStreamItem): PipelineStepStage {
        val msg = item.cleanMessage.uppercase()
        val action = item.action.uppercase()
        return when {
            msg.contains("SEARCH") || action == "SEARCH_START" -> PipelineStepStage.SEARCHING
            msg.contains("CANDIDATES") -> PipelineStepStage.CANDIDATES
            msg.contains("AI RANKED") || item.subsystem == "AI_GEMINI" -> PipelineStepStage.AI_EVALUATION
            msg.contains("MATCHED") -> PipelineStepStage.MATCHED
            msg.contains("LINKED") || action == "DB_LINKED" -> PipelineStepStage.LINKED
            msg.contains("NOT_FOUND") || action == "NOT_FOUND" -> PipelineStepStage.NOT_FOUND
            item.isError || msg.contains("QUOTA") || msg.contains("429") -> PipelineStepStage.QUOTA_ERROR
            msg.contains("NEW FIXTURE") || action == "NEW_MATCH" -> PipelineStepStage.NEW_FIXTURE
            msg.contains("GOAL") || action == "GOAL_SCORE" -> PipelineStepStage.LIVE_GOAL
            msg.contains("FULL TIME") || action == "FULL_TIME" -> PipelineStepStage.MATCH_FINISHED
            msg.contains("PURGE") && msg.contains("CLEANED") -> PipelineStepStage.RETENTION_PURGED
            msg.contains("PURGE") || item.subsystem == "MAINTENANCE" -> PipelineStepStage.RETENTION_CLEAN
            msg.contains("INGESTED") -> PipelineStepStage.FIXTURE_INGESTED
            else -> PipelineStepStage.FIXTURE_INGESTED
        }
    }

    fun extractLeague(msg: String): String? {
        val match = leagueRegex.find(msg)
        if (match != null) {
            val valStr = match.groupValues[1].trim()
            if (valStr in listOf("Premier League", "LaLiga", "Serie A", "Bundesliga", "Ligue 1", "Champions League", "PL", "PD", "SA", "BL1", "FL1", "CL")) {
                return normalizeLeague(valStr)
            }
        }
        return when {
            msg.contains("Premier League", true) || msg.contains("PL", true) -> "Premier League"
            msg.contains("LaLiga", true) || msg.contains("Primera Division", true) -> "LaLiga"
            msg.contains("Serie A", true) -> "Serie A"
            msg.contains("Bundesliga", true) -> "Bundesliga"
            msg.contains("Ligue 1", true) -> "Ligue 1"
            msg.contains("Champions League", true) -> "Champions League"
            else -> null
        }
    }

    fun extractDate(msg: String): String? {
        return dateRegex.find(msg)?.groupValues?.get(1)
    }

    fun extractFixture(msg: String): String? {
        return when {
            msg.contains("for: ") -> msg.substringAfter("for: ").substringBefore("-").trim()
            msg.contains("fixture: ") -> msg.substringAfter("fixture: ").trim()
            msg.contains("underway!") -> msg.substringAfter("]").substringBefore("underway!").trim()
            msg.contains("vs") -> {
                val cleaned = msg.substringAfter("]").trim()
                if (cleaned.contains("vs")) cleaned.substringBefore("(").substringBefore("-").trim() else null
            }
            else -> null
        }
    }

    private fun extractChannel(msg: String): String? {
        if (msg.contains("(") && msg.contains(")")) {
            val paren = msg.substringAfter("(").substringBefore(")")
            if (paren.contains("Official", true) || paren.contains("Channel", true) || paren.contains("Sports", true)) {
                return paren
            }
        }
        return null
    }

    private fun normalizeLeague(raw: String): String {
        return when (raw.uppercase()) {
            "PL" -> "Premier League"
            "PD" -> "LaLiga"
            "SA" -> "Serie A"
            "BL1" -> "Bundesliga"
            "FL1" -> "Ligue 1"
            "CL" -> "Champions League"
            else -> raw
        }
    }
}
