package com.dirzaaulia.footballclips.monitor.presentation

import com.dirzaaulia.footballclips.monitor.domain.Evidence
import com.dirzaaulia.footballclips.monitor.domain.MonitorFilter
import com.dirzaaulia.footballclips.monitor.domain.Page

internal fun reduceMonitor(state: MonitorState, action: MonitorAction): MonitorState = when (action) {
    is MonitorAction.Filter -> state.withFilter(action.value)

    is MonitorAction.Navigate -> state.navigation(action.page)

    is MonitorAction.SelectTeam -> state.withFilter(selectedTeamFilter(state.filter, action))

    is MonitorAction.SelectCycle -> state.navigation("pipelines").copy(cycle = action.cycle)

    is MonitorAction.SelectMatch -> state.copy(
        match = action.match.takeUnless { it.matchId == state.match?.matchId },
        evidence = Evidence(),
    )

    is MonitorAction.Metric -> {
        val targetPipeline = when (action.mode) {
            "live", "stale_live" -> "fixtures"
            "missing", "blocked", "country", "covered", "all_finished" -> "youtube"
            else -> if (action.mode.startsWith("video_")) "youtube" else state.filter.pipeline
        }
        state.navigation("pipelines").copy(
            catalogMode = action.mode,
            filter = state.filter.copy(pipeline = targetPipeline),
        )
    }

    else -> state
}

private fun MonitorState.withFilter(value: MonitorFilter): MonitorState = copy(
    filter = value,
    cycle = null,
    match = null,
    matches = Page(),
    evidence = Evidence(),
    logs = Page(),
    cycles = Page(),
    branches = kotlinx.collections.immutable.persistentMapOf(),
    details = kotlinx.collections.immutable.persistentMapOf(),
)

private fun MonitorState.navigation(value: String): MonitorState = copy(page = value, catalogMode = "")

private fun selectedTeamFilter(filter: MonitorFilter, action: MonitorAction.SelectTeam): MonitorFilter =
    if (action.opponent) {
        filter.copy(opponentId = action.team.id, opponentName = action.team.name)
    } else {
        filter.copy(teamId = action.team.id, teamName = action.team.name)
    }
