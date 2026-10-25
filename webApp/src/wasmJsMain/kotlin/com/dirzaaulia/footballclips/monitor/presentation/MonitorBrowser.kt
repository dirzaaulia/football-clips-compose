@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.dirzaaulia.footballclips.monitor.presentation

import com.dirzaaulia.footballclips.monitor.domain.MonitorFilter
import io.ktor.http.encodeURLParameter

internal fun initialFilter(): MonitorFilter = MonitorFilter(
    teamId = filterParameter("team_id"),
    teamName = filterParameter("team_name"),
    opponentId = filterParameter("opponent_id"),
    opponentName = filterParameter("opponent_name"),
    query = filterParameter("q"),
    competition = filterParameter("competition"),
    season = filterParameter("season"),
    since = filterParameter("since"),
    until = filterParameter("until"),
    country = filterParameter("country").ifBlank { "ID" },
    minutes = filterParameter("minutes").toIntOrNull()?.coerceIn(
        MonitorPolicy.MIN_MINUTES,
        MonitorPolicy.MAX_MINUTES,
    ) ?: MonitorPolicy.DAY_MINUTES,
    pipeline = filterParameter("pipeline").ifBlank { "youtube" },
)

internal fun saveFilter(filter: MonitorFilter) {
    val query = listOf(
        "team_id" to filter.teamId,
        "team_name" to filter.teamName,
        "opponent_id" to filter.opponentId,
        "opponent_name" to filter.opponentName,
        "q" to filter.query,
        "since" to filter.since,
        "until" to filter.until,
        "competition" to filter.competition,
        "season" to filter.season,
        "country" to filter.country,
        "minutes" to filter.minutes.toString(),
        "pipeline" to filter.pipeline,
    ).filter { it.second.isNotBlank() }.joinToString("&") {
        "${it.first}=${it.second.encodeURLParameter()}"
    }
    replaceFilter(query)
}

@JsFun(
    """(query) => {
        const u = new URL(location.href);
        const view = u.searchParams.get('view');
        u.search = query;
        if (view) u.searchParams.set('view', view);
        history.replaceState(null, '', u);
    }""",
)
private external fun replaceFilter(query: String)

internal fun initialPage(): String = filterParameter("view").takeIf {
    it in setOf("dashboard", "pipelines", "competitions")
}.orEmpty().ifBlank { "dashboard" }

@JsFun(
    """(page) => {
        const u = new URL(location.href);
        if (u.searchParams.get('view') === page) return;
        u.searchParams.set('view', page);
        history.pushState(null, '', u);
    }""",
)
internal external fun pushNavigation(page: String)
