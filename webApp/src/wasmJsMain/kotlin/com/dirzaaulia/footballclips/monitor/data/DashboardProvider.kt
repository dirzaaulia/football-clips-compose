package com.dirzaaulia.footballclips.monitor.data

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
            remote.count("$postgrest/matches?status=in.(IN_PLAY,PAUSED,LIVE)&clock=is.null&limit=0")
        }
        val blockedMatchesDeferred = async {
            remote.readArray(
                "$postgrest/matches?status=eq.FINISHED&youtube_region_info->blocked=not.is.null&select=id,youtube_region_info",
            )
        }
        val opsDeferred = async {
            try {
                remote.read("monitor/dashboard?${filter.query()}").toDashboard()
            } catch (_: Exception) {
                null
            }
        }
        val allowedMatchesDeferred = async {
            remote.readArray(
                "$postgrest/matches?status=eq.FINISHED&youtube_region_info->allowed=not.is.null&select=id,youtube_region_info",
            )
        }

        val totalFinished = totalFinishedDeferred.await().coerceAtLeast(0L)
        val withHighlights = withHighlightsDeferred.await().coerceAtLeast(0L)
        val totalMatches = totalMatchesDeferred.await().coerceAtLeast(0L)
        val liveMatches = liveMatchesDeferred.await().coerceAtLeast(0L)
        val staleLive = staleLiveDeferred.await().coerceAtLeast(0L)

        val blockedMatches = blockedMatchesDeferred.await()
        val allowedMatches = allowedMatchesDeferred.await()
        val restrictedMatches = (blockedMatches + allowedMatches).distinctBy { it.text("id") }
        val regionalGaps = restrictedMatches.size.toLong()

        val missing = (totalFinished - withHighlights).coerceAtLeast(0L)
        val missingPercent = if (totalFinished > 0L) (missing * 100L) / totalFinished else 0L

        val selectedCountry = filter.country.ifBlank { "ID" }.uppercase()
        val blockedInSelected = restrictedMatches.count { match ->
            isBlockedInCountry(match.obj("youtube_region_info"), selectedCountry)
        }
        val countryWatchable = (withHighlights - blockedInSelected).coerceAtLeast(0L)

        val countryList = CountryDirectory.ALL.map { (code, _) ->
            val blockedInCode = restrictedMatches.count { match ->
                isBlockedInCountry(match.obj("youtube_region_info"), code)
            }
            CountryCoverage(
                code = code,
                matches = withHighlights.toString(),
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
            Metric("country_watchable", countryWatchable.toString(), "country"),
            Metric("country_gaps", regionalGaps.toString(), "blocked"),
            Metric("live", liveMatches.toString(), "live"),
            Metric("stale_live", staleLive.toString(), "stale_live"),
            Metric("accepted", withHighlights.toString(), "video_accepted"),
            Metric("watchable", countryWatchable.toString(), "video_watchable"),
            Metric("blocked", blockedInSelected.toString(), "video_blocked"),
            Metric("unknown", "0", "video_unknown"),
        ).toImmutableList()

        val ops = opsDeferred.await()

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
            jobs = ops?.jobs ?: persistentListOf(),
            delivery = ops?.delivery ?: persistentListOf(),
            activationAt = ops?.activationAt ?: "",
        )
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
