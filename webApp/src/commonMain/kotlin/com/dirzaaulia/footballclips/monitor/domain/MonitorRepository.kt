package com.dirzaaulia.footballclips.monitor.domain

import kotlinx.collections.immutable.ImmutableList

interface MonitorRepository {
    suspend fun cachedCycles(filter: MonitorFilter): Page<Cycle> = Page()
    suspend fun cachedMatches(cycleId: String, filter: MonitorFilter): Page<MatchRun> = Page()
    suspend fun coverage(matchId: String): CoverageSummary
    suspend fun sources(code: String, season: String): ImmutableList<SourceChannel>
    suspend fun dashboard(filter: MonitorFilter): Dashboard
    suspend fun teams(query: String): ImmutableList<TeamOption>
    suspend fun cycles(filter: MonitorFilter, cursor: String = "", limit: Int = 50): Page<Cycle>
    suspend fun matches(cycleId: String, filter: MonitorFilter, cursor: String = ""): Page<MatchRun>
    suspend fun evidence(evaluationId: String, cursor: String = ""): Evidence
    suspend fun catalog(filter: MonitorFilter, mode: String, cursor: String = ""): Page<MatchRun>
    suspend fun competitions(): ImmutableList<Competition>
    suspend fun logs(filter: MonitorFilter, before: String = "", after: String = "", cycle: String = ""): Page<LogLine>
    suspend fun correct(matchId: String, videoId: String, undo: Boolean)
}

class LoadDashboard(private val repository: MonitorRepository) {
    suspend operator fun invoke(filter: MonitorFilter): Dashboard = repository.dashboard(filter)
}

class SearchTeams(private val repository: MonitorRepository) {
    suspend operator fun invoke(query: String): ImmutableList<TeamOption> = repository.teams(query)
}
