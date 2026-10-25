package com.dirzaaulia.footballclips.monitor.data

import com.dirzaaulia.footballclips.monitor.domain.MonitorFilter
import com.dirzaaulia.footballclips.monitor.presentation.browserTime
import com.dirzaaulia.footballclips.monitor.presentation.sinceTime
import io.ktor.http.encodeURLParameter

internal fun MonitorFilter.query(ignoreSince: Boolean = false): String {
    val end = until.ifBlank { browserTime() }
    val start = if (ignoreSince) "" else since.ifBlank { sinceTime(minutes) }
    return listOf(
        "_window" to minutes.toString(),
        "_since" to since,
        "_until" to until,
        "since" to start,
        "until" to end,
        "team_id" to teamId,
        "opponent_id" to opponentId,
        "q" to query,
        "competition" to competition,
        "country" to country,
        "season" to season,
    ).filter { it.second.isNotBlank() }.joinToString("&") { (key, value) -> "$key=${value.encodeURLParameter()}" }
}

internal fun buildCatalogPostgrestUrl(filter: MonitorFilter, mode: String): String {
    val base = "https://edge.fc.dirzaaulia.com/rest/v1/matches"
    val params = mutableListOf("select=*")
    when (mode) {
        "missing" -> {
            params.add("status=eq.FINISHED")
            params.add("highlight_video_id=is.null")
        }

        "covered", "video_accepted", "accepted" -> {
            params.add("status=eq.FINISHED")
            params.add("highlight_video_id=not.is.null")
        }

        "blocked", "video_blocked" -> {
            params.add("status=eq.FINISHED")
            params.add("youtube_region_info->blocked=not.is.null")
        }

        "all_finished", "finished" -> {
            params.add("status=eq.FINISHED")
        }

        "live", "stale_live" -> {
            params.add("status=in.(IN_PLAY,PAUSED,LIVE)")
        }
    }
    if (filter.competition.isNotBlank()) {
        params.add("competition_id=eq.${filter.competition}")
    }
    if (filter.teamId.isNotBlank()) {
        params.add("or=(home_team_id.eq.${filter.teamId},away_team_id.eq.${filter.teamId})")
    }
    params.add("order=utc_date.desc")
    params.add("limit=50")
    return "$base?${params.joinToString("&")}"
}
