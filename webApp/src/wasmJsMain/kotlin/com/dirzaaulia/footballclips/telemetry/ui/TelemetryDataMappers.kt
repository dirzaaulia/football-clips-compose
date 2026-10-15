package com.dirzaaulia.footballclips.telemetry.ui

import com.dirzaaulia.footballclips.telemetry.model.*

object TelemetryDataMappers {

    fun extractDiscoveredHighlights(
        events: List<WorkerEvent>,
        logStream: List<LogStreamItem>
    ): List<DiscoveredHighlight> {
        val list = mutableListOf<DiscoveredHighlight>()
        val seenUrls = mutableSetOf<String>()

        for (item in logStream) {
            val url = item.extractedUrl
            if (url != null && !seenUrls.contains(url)) {
                seenUrls.add(url)
                val msg = item.cleanMessage
                val title = extractTitleFromMessage(msg)
                val channel = extractChannelFromMessage(msg)
                val league = extractLeagueFromMessage(msg)
                list.add(
                    DiscoveredHighlight(
                        id = item.id.toString(),
                        title = title,
                        league = league,
                        quality = "1080p60",
                        duration = "03:42",
                        channel = channel,
                        videoUrl = url,
                        isEmbedReady = true,
                        isSynced = true
                    )
                )
            }
        }

        return list.take(15)
    }

    private fun extractTitleFromMessage(msg: String): String {
        // Extract quoted title from python logs: ... 'Title Here' (@handle) ...
        if (msg.contains("'")) {
            val candidate = msg.substringAfter("'").substringBefore("'").trim()
            if (candidate.isNotBlank() && candidate.length > 3) {
                return candidate
            }
        }
        return when {
            msg.contains("SELECTED") -> msg.substringAfter("SELECTED").substringBefore("(").trim()
            msg.contains("AI MATCH") -> msg.substringAfter("AI MATCH").substringBefore("(").trim()
            msg.contains("LINKED") -> msg.substringAfter("LINKED").substringBefore("->").trim()
            msg.contains("LINK:") -> msg.substringBefore("LINK:").substringAfter("]").trim()
            msg.contains("Inserted upcoming fixture:") -> msg.substringAfter("Inserted upcoming fixture:").substringBefore("(").trim()
            msg.contains("vs") -> msg.substringBefore("http").substringBefore("(").trim()
            else -> msg.take(45)
        }
    }

    private fun extractChannelFromMessage(msg: String): String {
        // In python: [MATCHED] [Comp] (Date) 'Title' (@channel_handle) - https://youtu.be/...
        if (msg.contains(" - http")) {
            val beforeUrl = msg.substringBefore(" - http").trim()
            if (beforeUrl.contains("(") && beforeUrl.contains(")")) {
                val candidate = beforeUrl.substringAfterLast("(").substringBefore(")").trim()
                if (candidate.isNotBlank() && !candidate.contains("-")) {
                    return candidate
                }
            }
        }

        // Direct handle detection in message (e.g. @arsenal, @realmadrid)
        val handleMatch = Regex("(@[A-Za-z0-9_.-]+)").find(msg)
        if (handleMatch != null) {
            return handleMatch.groupValues[1]
        }

        return "Official Club Broadcaster"
    }

    private fun extractLeagueFromMessage(msg: String): String {
        return when {
            msg.contains("LaLiga", true) || msg.contains("PD", true) -> "LaLiga"
            msg.contains("Premier", true) || msg.contains("PL", true) -> "Premier League"
            msg.contains("Serie A", true) || msg.contains("SA", true) -> "Serie A"
            msg.contains("Bundesliga", true) || msg.contains("BL1", true) -> "Bundesliga"
            msg.contains("Ligue 1", true) || msg.contains("FL1", true) -> "Ligue 1"
            msg.contains("Champions League", true) || msg.contains("CL", true) -> "UEFA Champions League"
            else -> "European Football"
        }
    }

    fun extractCycleHistory(
        cycles: List<WorkerCycle>,
        eventsByCycle: Map<Long, List<WorkerEvent>>
    ): List<CycleHistoryItem> {
        return cycles.take(15).map { cycle ->
            val events = eventsByCycle[cycle.id] ?: emptyList()
            val highlightsCount = events.count { it.isHighlight || it.eventText.contains("MATCHED") || it.eventText.contains("BERHASIL") }
            val matchesCount = events.count { it.eventText.contains("INGESTED") || it.eventText.contains("FIXTURE") || it.eventText.contains("Ditemukan") }
            val quotaCost = events.count { it.eventText.contains("CANDIDATES", true) || it.eventText.contains("MATCHED", true) || it.eventText.contains("BERHASIL", true) } * 100
            val errCount = events.count { it.isError || it.stepStatus == "ERROR" }

            CycleHistoryItem(
                id = cycle.id,
                timestamp = cycle.timestamp,
                executionMode = cycle.mode.ifBlank { "SCHEDULED SYNC" },
                ingestedMatches = matchesCount,
                highlightsHarvested = highlightsCount,
                apiQuotaCost = quotaCost,
                dbStatus = if (highlightsCount > 0) "$highlightsCount highlights linked" else "Cycle synchronized",
                exceptions = errCount
            )
        }
    }
}
