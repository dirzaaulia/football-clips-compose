package com.dirzaaulia.footballclips.monitor.data

import com.dirzaaulia.footballclips.monitor.domain.Backlog
import com.dirzaaulia.footballclips.monitor.domain.CountryCoverage
import com.dirzaaulia.footballclips.monitor.domain.CountryDirectory
import com.dirzaaulia.footballclips.monitor.domain.Dashboard
import com.dirzaaulia.footballclips.monitor.domain.Metric
import com.dirzaaulia.footballclips.monitor.domain.MonitorFilter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

class DashboardProvider(private val remote: MonitorRemoteClient) {

    private val postgrest = "https://edge.fc.dirzaaulia.com/rest/v1"

    suspend fun loadDashboard(filter: MonitorFilter): Dashboard = coroutineScope {
        val totalFinishedDeferred = async {
            remote.count("$postgrest/matches?status=eq.FINISHED&limit=0")
        }
        val withHighlightsDeferred = async {
            remote.count("$postgrest/matches?status=eq.FINISHED&highlight_video_id=not.is.null&limit=0")
        }
        val totalMatchesDeferred = async {
            remote.count("$postgrest/matches?limit=0")
        }
        val liveMatchesDeferred = async {
            remote.count("$postgrest/matches?status=in.(IN_PLAY,PAUSED,LIVE)&limit=0")
        }
        val staleLiveDeferred = async {
            remote.count("$postgrest/matches?status=in.(IN_PLAY,PAUSED,LIVE)&display_clock=is.null&limit=0")
        }
        val pendingHighlightsDeferred = async {
            remote.count("$postgrest/matches?status=eq.FINISHED&highlight_video_id=is.null&limit=0")
        }
        val blockedMatchesDeferred = async {
            remote.readArray(
                "$postgrest/matches?status=eq.FINISHED&youtube_region_info->blocked=not.is.null&select=id,youtube_region_info",
            )
        }
        val allowedMatchesDeferred = async {
            remote.readArray(
                "$postgrest/matches?status=eq.FINISHED&youtube_region_info->allowed=not.is.null&select=id,youtube_region_info",
            )
        }
        val opsDeferred = async {
            try {
                remote.read("monitor/dashboard?${filter.query()}").toDashboard()
            } catch (_: Exception) {
                null
            }
        }

        val totalFinished = totalFinishedDeferred.await().coerceAtLeast(0L)
        val withHighlights = withHighlightsDeferred.await().coerceAtLeast(0L)
        val totalMatches = totalMatchesDeferred.await().coerceAtLeast(0L)
        val liveMatches = liveMatchesDeferred.await().coerceAtLeast(0L)
        val staleLive = staleLiveDeferred.await().coerceAtLeast(0L)
        val pendingHighlights = pendingHighlightsDeferred.await().coerceAtLeast(0L)

        val blockedMatches = blockedMatchesDeferred.await()
        val allowedMatches = allowedMatchesDeferred.await()
        val restrictedMatches = (blockedMatches + allowedMatches).distinctBy { it.text("id") }
        val regionalGaps = restrictedMatches.size.toLong()

        val missing = (totalFinished - withHighlights).coerceAtLeast(0L)
        val missingPercent = if (totalFinished > 0L) (missing * 100L) / totalFinished else 0L

        // Worldwide videos = highlights without regional restrictions
        val geoWorldwide = (withHighlights - regionalGaps).coerceAtLeast(0L)

        // Classify restricted videos into above bare minimum (>=50 countries) vs below bare minimum (<50 countries)
        var geoAboveMin = 0L
        var geoBelowMin = 0L
        for (match in restrictedMatches) {
            val watchable = countWatchableCountries(match.obj("youtube_region_info"))
            if (watchable >= 50) {
                geoAboveMin++
            } else {
                geoBelowMin++
            }
        }

        val countryList = CountryDirectory.ALL.map { (code, _) ->
            val blockedInCode = restrictedMatches.count { match ->
                isBlockedInCountry(match.obj("youtube_region_info"), code)
            }
            val watchableInCode = (withHighlights - blockedInCode).coerceAtLeast(0L)
            CountryCoverage(
                code = code,
                matches = watchableInCode.toString(),
                blocked = blockedInCode.toString(),
                unknown = "0",
            )
        }.toImmutableList()

        val metrics = listOf(
            Metric("total_matches", totalMatches.toString(), "all"),
            Metric("total_finished", totalFinished.toString(), "all_finished"),
            Metric("finished", totalFinished.toString(), "finished"),
            Metric("with_highlights", withHighlights.toString(), "covered"),
            Metric("missing", missing.toString(), "missing"),
            Metric("missing_percent", missingPercent.toString(), "missing"),
            Metric("country_gaps", regionalGaps.toString(), "blocked"),
            Metric("live", liveMatches.toString(), "live"),
            Metric("stale_live", staleLive.toString(), "stale_live"),
            Metric("accepted", withHighlights.toString(), "video_accepted"),
            Metric("geo_worldwide", geoWorldwide.toString(), "geo_worldwide"),
            Metric("geo_above_min", geoAboveMin.toString(), "geo_above_min"),
            Metric("geo_below_min", geoBelowMin.toString(), "geo_below_min"),
        ).toImmutableList()

        val ops = opsDeferred.await()

        val fallbackQueues = listOf(
            Backlog(
                state = "Pending Highlights",
                count = pendingHighlights.toString(),
                oldest = "Worker Intake Queue",
            ),
            Backlog(
                state = "Live Fixture Clock Sync",
                count = liveMatches.toString(),
                oldest = "Semi-live 60s Polling Tick",
            ),
        ).toImmutableList()

        Dashboard(
            metrics = metrics,
            quota = ops?.quota ?: persistentListOf(),
            scope = if (filter.competition.isNotBlank()) filter.competition else "Global",
            generated = "Live PostgREST",
            reset = ops?.reset ?: "",
            rollout = ops?.rollout ?: "production",
            fraction = ops?.fraction ?: "1.0",
            series = ops?.series ?: persistentListOf(),
            countries = countryList,
            origins = ops?.origins ?: persistentListOf(),
            jobs = if (ops?.jobs?.isNotEmpty() == true) ops.jobs else fallbackQueues,
            delivery = if (ops?.delivery?.isNotEmpty() == true) ops.delivery else fallbackQueues,
            activationAt = ops?.activationAt ?: "",
        )
    }

    private fun countWatchableCountries(info: JsonObject): Int {
        val blockedArray = (info["blocked"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
        if (blockedArray != null) {
            return (249 - blockedArray.size).coerceAtLeast(0)
        }
        val allowedArray = (info["allowed"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
        if (allowedArray != null) {
            return allowedArray.size
        }
        return 249
    }

    private fun isBlockedInCountry(info: JsonObject, countryCode: String): Boolean {
        val blockedArray = (info["blocked"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
        if (blockedArray != null && blockedArray.contains(countryCode)) {
            return true
        }
        val allowedArray = (info["allowed"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
        if (allowedArray != null && allowedArray.isNotEmpty() && !allowedArray.contains(countryCode)) {
            return true
        }
        return false
    }
}
