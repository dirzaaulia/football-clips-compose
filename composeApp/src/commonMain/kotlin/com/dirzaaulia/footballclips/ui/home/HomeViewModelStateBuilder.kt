package com.dirzaaulia.footballclips.ui.home

import com.dirzaaulia.footballclips.data.constants.BigLeaguesConstants
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.domain.model.Match

internal fun buildHomeUiItems(
    supabaseMatches: List<Match>,
    allHighlights: List<HighlightUiModel>,
    searchQuery: String,
    selectedLeagues: Set<String>,
    selectedCountries: Set<String>,
    selectedLeagueId: Int?,
    selectedCompetitionId: String?,
    showExternalHighlights: Boolean
): Pair<List<HighlightUiItem>, Int> {
    val items = mutableListOf<HighlightUiItem>()
    
    val filteredSupabase = supabaseMatches.filter { match ->
        val matchSearch = searchQuery.isEmpty() || 
                match.homeTeamName.contains(searchQuery, ignoreCase = true) || 
                match.awayTeamName.contains(searchQuery, ignoreCase = true)

        val matchesSelectedFilters = selectedLeagues.isEmpty() || 
                selectedLeagues.any { it.equals(match.competitionName, ignoreCase = true) }
            
        matchSearch && matchesSelectedFilters
    }
    items.addAll(filteredSupabase.map { HighlightUiItem.SupabaseMatch(it) })
    
    if (showExternalHighlights || supabaseMatches.isEmpty()) {
        val topLeague = BigLeaguesConstants.leagues.find { 
            (it.leagueId != null && it.leagueId == selectedLeagueId) || 
            (it.competitionId != null && it.competitionId == selectedCompetitionId)
        }

        val filteredExternal = allHighlights.filter { highlight ->
            val matchSearch = searchQuery.isEmpty() || 
                    highlight.homeTeam.contains(searchQuery, ignoreCase = true) || 
                    highlight.awayTeam.contains(searchQuery, ignoreCase = true) ||
                    highlight.title.contains(searchQuery, ignoreCase = true)

            val matchesSelectedFilters = (selectedCountries.isEmpty() || highlight.countryName in selectedCountries) &&
            (selectedLeagues.isEmpty() || selectedLeagues.any { it.equals(highlight.leagueName, ignoreCase = true) })
            
            val matchesTopLeague = topLeague == null || topLeague.leagueId == null ||
                                   highlight.leagueName.contains(topLeague.name, ignoreCase = true) ||
                                   (topLeague.name == "La Liga" && highlight.countryName.contains("Spain", ignoreCase = true))

            matchSearch && matchesSelectedFilters && matchesTopLeague
        }
        
        items.addAll(filteredExternal.map { HighlightUiItem.Highlight(it) })
    }

    return Pair(items, items.size)
}
