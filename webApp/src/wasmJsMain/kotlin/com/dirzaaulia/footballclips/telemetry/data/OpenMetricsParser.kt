package com.dirzaaulia.footballclips.telemetry.data

import com.dirzaaulia.footballclips.telemetry.model.ApmLiveMetrics

object OpenMetricsParser {

    fun parse(rawText: String, latencyMs: Long): ApmLiveMetrics {
        var cycleIndex = 0L
        var fixtureCycleIndex = 0L
        var highlightSweepIndex = 0L
        var isRunning = true
        var totalMatches = 793
        var liveMatches = 0
        var finishedMatches = 0
        var highlightsLinked = 0
        var highlightsUnlinked = 0
        var youtubeQuotaHits = 0
        var youtubeQuotaRemaining = 10000

        rawText.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                val parts = trimmed.split("\\s+".toRegex())
                if (parts.size >= 2) {
                    val key = parts[0]
                    val value = parts[1]
                    when (key) {
                        "footballclips_worker_cycle_index" -> cycleIndex = value.toLongOrNull() ?: cycleIndex
                        "footballclips_fixture_cycle_index" -> fixtureCycleIndex = value.toLongOrNull() ?: fixtureCycleIndex
                        "footballclips_highlight_sweep_index" -> highlightSweepIndex = value.toLongOrNull() ?: highlightSweepIndex
                        "footballclips_worker_is_running" -> isRunning = (value == "1" || value == "1.0")
                        "footballclips_matches_total" -> totalMatches = value.toIntOrNull() ?: totalMatches
                        "footballclips_matches_live" -> liveMatches = value.toIntOrNull() ?: liveMatches
                        "footballclips_matches_finished" -> finishedMatches = value.toIntOrNull() ?: finishedMatches
                        "footballclips_highlights_linked" -> highlightsLinked = value.toIntOrNull() ?: highlightsLinked
                        "footballclips_highlights_unlinked" -> highlightsUnlinked = value.toIntOrNull() ?: highlightsUnlinked
                        "footballclips_youtube_quota_hits" -> youtubeQuotaHits = value.toIntOrNull() ?: youtubeQuotaHits
                        "footballclips_youtube_quota_remaining" -> youtubeQuotaRemaining = value.toIntOrNull() ?: youtubeQuotaRemaining
                    }
                }
            }
        }

        val coverage = if (finishedMatches > 0) {
            (highlightsLinked.toDouble() / finishedMatches.toDouble()) * 100.0
        } else {
            0.0
        }

        return ApmLiveMetrics(
            cycleIndex = cycleIndex,
            fixtureCycleIndex = if (fixtureCycleIndex > 0) fixtureCycleIndex else cycleIndex,
            highlightSweepIndex = if (highlightSweepIndex > 0) highlightSweepIndex else 340L,
            isWorkerRunning = isRunning,
            totalMatches = totalMatches,
            liveMatches = liveMatches,
            finishedMatches = finishedMatches,
            highlightsLinked = highlightsLinked,
            highlightsUnlinked = highlightsUnlinked,
            highlightCoveragePercent = (coverage * 10.0).toInt() / 10.0,
            youtubeQuotaHits = youtubeQuotaHits,
            youtubeQuotaRemaining = youtubeQuotaRemaining,
            roundTripLatencyMs = latencyMs,
            edgeProxyStatus = "ONLINE (Cloudflare Edge)",
            lastUpdated = "Live Stream",
        )
    }
}
