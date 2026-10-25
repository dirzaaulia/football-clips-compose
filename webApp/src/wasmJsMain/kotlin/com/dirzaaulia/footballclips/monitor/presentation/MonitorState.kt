package com.dirzaaulia.footballclips.monitor.presentation

import com.dirzaaulia.footballclips.monitor.domain.Competition
import com.dirzaaulia.footballclips.monitor.domain.CoverageSummary
import com.dirzaaulia.footballclips.monitor.domain.Cycle
import com.dirzaaulia.footballclips.monitor.domain.Dashboard
import com.dirzaaulia.footballclips.monitor.domain.Evidence
import com.dirzaaulia.footballclips.monitor.domain.LogLine
import com.dirzaaulia.footballclips.monitor.domain.MatchRun
import com.dirzaaulia.footballclips.monitor.domain.MonitorFilter
import com.dirzaaulia.footballclips.monitor.domain.Page
import com.dirzaaulia.footballclips.monitor.domain.SourceChannel
import com.dirzaaulia.footballclips.monitor.domain.TeamOption
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf

data class MonitorState(
    val filter: MonitorFilter = MonitorFilter(),
    val page: String = "dashboard",
    val dashboard: Dashboard = Dashboard(),
    val cycles: Page<Cycle> = Page(),
    val matches: Page<MatchRun> = Page(),
    val cycle: Cycle? = null,
    val match: MatchRun? = null,
    val evidence: Evidence = Evidence(),
    val teams: ImmutableList<TeamOption> = persistentListOf(),
    val competitions: ImmutableList<Competition> = persistentListOf(),
    val expanded: ImmutableSet<String> = persistentSetOf(),
    val loading: Boolean = false,
    val live: Boolean = true,
    val error: String = "",
    val updatedAt: String = "",
    val catalogMode: String = "",
    val logs: Page<LogLine> = Page(),
    val correction: Pair<String, String>? = null,
    val coverages: ImmutableMap<String, CoverageSummary> = persistentMapOf(),
    val sourceChannels: ImmutableMap<String, ImmutableList<SourceChannel>> = persistentMapOf(),
    val branches: ImmutableMap<String, Page<MatchRun>> = persistentMapOf(),
    val details: ImmutableMap<String, Evidence> = persistentMapOf(),
    val branchErrors: ImmutableMap<String, String> = persistentMapOf(),
    val pending: ImmutableSet<String> = persistentSetOf(),
)

sealed interface MonitorAction {
    data class Filter(val value: MonitorFilter) : MonitorAction
    data class Navigate(val page: String, val recordHistory: Boolean = true) : MonitorAction
    data class TeamSearch(val value: String) : MonitorAction
    data class SelectTeam(val team: TeamOption, val opponent: Boolean = false) : MonitorAction
    data class SelectCycle(val cycle: Cycle) : MonitorAction
    data class SelectMatch(val match: MatchRun) : MonitorAction
    data class Expand(val id: String) : MonitorAction
    data class Metric(val mode: String) : MonitorAction
    data class Correct(val videoId: String, val undo: Boolean = false, val matchId: String? = null) : MonitorAction
    data class MoreBranch(val id: String) : MonitorAction
    data object CycleLogs : MonitorAction
    data object Refresh : MonitorAction
    data object Older : MonitorAction
    data object ToggleLive : MonitorAction
}
