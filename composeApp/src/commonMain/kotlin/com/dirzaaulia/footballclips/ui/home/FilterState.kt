package com.dirzaaulia.footballclips.ui.home

import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.domain.model.Match

data class FilterState(
    val searchQuery: String = "",
    val selectedLeagues: Set<String> = emptySet(),
    val availableLeagues: List<LeagueFilterItem> = emptyList(),
    val totalMatchesLoaded: Int = 0,
)

data class LeagueFilterItem(
    val name: String,
    val count: Int,
    val logo: String = "",
)

fun calculateFilterOptions(
    databaseMatches: List<Match>,
    externalHighlights: List<HighlightUiModel>,
    currentQuery: String,
    selectedLeagues: Set<String>,
): FilterState {
    val databaseLeagues = databaseMatches.groupBy { it.competitionName }
    val externalLeagues = externalHighlights.groupBy { it.leagueName }

    val allLeagueNames = (databaseLeagues.keys + externalLeagues.keys).toMutableSet()
    if (databaseMatches.isEmpty() && externalHighlights.isEmpty()) {
        allLeagueNames.clear()
    }

    val leagueItems = allLeagueNames.map { name ->
        val databaseList = databaseLeagues[name] ?: emptyList()
        val externalList = externalLeagues[name] ?: emptyList()
        val totalCount = databaseList.size + externalList.size
        val logo = databaseList.firstOrNull()?.leagueEmblemUrl ?: externalList.firstOrNull()?.leagueLogo ?: ""
        LeagueFilterItem(name = name, count = totalCount, logo = logo)
    }.sortedByDescending { it.count }

    return FilterState(
        searchQuery = currentQuery,
        selectedLeagues = selectedLeagues,
        availableLeagues = leagueItems,
        totalMatchesLoaded = databaseMatches.size + externalHighlights.size,
    )
}
