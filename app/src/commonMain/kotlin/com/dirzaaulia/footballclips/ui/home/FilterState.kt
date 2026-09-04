package com.dirzaaulia.footballclips.ui.home

import com.dirzaaulia.footballclips.data.constants.BigLeaguesConstants
import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.domain.model.Match

data class FilterState(
    val searchQuery: String = "",
    val selectedCountries: Set<String> = emptySet(),
    val selectedLeagues: Set<String> = emptySet(),
    val availableCountries: List<FilterOption> = emptyList(),
    val availableLeagues: List<FilterOption> = emptyList(),
    val matchesFound: Int = 0,
    val totalMatchesLoaded: Int = 0
)

data class FilterOption(
    val name: String,
    val count: Int,
    val logo: String = ""
)

fun extractAllFilters(
    supabaseMatches: List<Match>,
    externalHighlights: List<HighlightUiModel>,
    searchQuery: String,
    selectedCountries: Set<String>,
    selectedLeagues: Set<String>
): FilterState {
    val countries = externalHighlights.groupBy { it.countryName }
        .map { (name, list) ->
            FilterOption(name, list.size, list.first().countryLogo)
        }
        .sortedByDescending { it.count }

    val supabaseLeagues = supabaseMatches.groupBy { it.competitionName }
    val externalLeagues = externalHighlights.groupBy { it.leagueName }

    val allLeagueNames = (supabaseLeagues.keys + externalLeagues.keys).toMutableSet()

    // Ensure all big leagues are present
    BigLeaguesConstants.leagues.forEach {
        if (it.name != "All Leagues") allLeagueNames.add(it.name)
    }

    val leagues = allLeagueNames.map { name ->
        val supabaseList = supabaseLeagues[name] ?: emptyList()
        val externalList = externalLeagues[name] ?: emptyList()
        val totalCount = supabaseList.size + externalList.size

        val bigLeague = BigLeaguesConstants.leagues.find {
            val bigLeagueName = it.name.replace(" ", "").lowercase()
            val currentName = name.replace(" ", "").lowercase()
            bigLeagueName.contains(currentName) || currentName.contains(bigLeagueName)
        }
        val logo = bigLeague?.logo
            ?: externalList.firstOrNull()?.leagueLogo
            ?: ""

        FilterOption(name, totalCount, logo)
    }.sortedByDescending { it.count }

    val filteredSupabaseCount = supabaseMatches.filter { match ->
        val matchSearch = searchQuery.isEmpty() ||
                match.homeTeamName.contains(searchQuery, ignoreCase = true) ||
                match.awayTeamName.contains(searchQuery, ignoreCase = true)

        val matchesSelectedFilters = (selectedCountries.isEmpty()) &&
                (selectedLeagues.isEmpty() || match.competitionName in selectedLeagues)

        matchSearch && matchesSelectedFilters
    }.size

    val filteredExternalCount = externalHighlights.filter { highlight ->
        val matchSearch = searchQuery.isEmpty() ||
                highlight.homeTeam.contains(searchQuery, ignoreCase = true) ||
                highlight.awayTeam.contains(searchQuery, ignoreCase = true) ||
                highlight.title.contains(searchQuery, ignoreCase = true)

        val matchesSelectedFilters = (selectedCountries.isEmpty() || highlight.countryName in selectedCountries) &&
                (selectedLeagues.isEmpty() || highlight.leagueName in selectedLeagues)

        matchSearch && matchesSelectedFilters
    }.size

    return FilterState(
        searchQuery = searchQuery,
        selectedCountries = selectedCountries,
        selectedLeagues = selectedLeagues,
        availableCountries = countries,
        availableLeagues = leagues,
        matchesFound = filteredSupabaseCount + filteredExternalCount,
        totalMatchesLoaded = supabaseMatches.size + externalHighlights.size
    )
}
