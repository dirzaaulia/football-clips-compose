package com.dirzaaulia.footballclips.ui.home

import com.dirzaaulia.footballclips.data.constants.BigLeaguesConstants
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.domain.model.Match

internal fun calculateRelatedHighlights(
    item: HighlightUiItem,
    supabaseMatches: List<Match>,
    allHighlights: List<HighlightUiModel>
): Pair<List<HighlightUiItem>, String?> {
    val originalLeagueName = when (item) {
        is HighlightUiItem.SupabaseMatch -> item.match.competitionName
        is HighlightUiItem.Highlight -> item.highlight.leagueName
        else -> null
    }

    val bigLeague = BigLeaguesConstants.leagues.find { 
        originalLeagueName != null && (
            it.name.equals(originalLeagueName, ignoreCase = true) || 
            it.competitionId?.equals(originalLeagueName, ignoreCase = true) == true ||
            isLeagueMatch(it.name, originalLeagueName)
        )
    }

    val leagueName = bigLeague?.name ?: originalLeagueName
    val competitionId = if (item is HighlightUiItem.SupabaseMatch) item.match.competitionId else bigLeague?.competitionId

    if (leagueName == null) return Pair(emptyList(), null)

    val fromExternal = allHighlights
        .filter { isLeagueMatch(it.leagueName, leagueName) }
        .map { HighlightUiItem.Highlight(it) }
    
    val fromSupabase = supabaseMatches
        .filter { 
            isLeagueMatch(it.competitionName, leagueName) && 
            !it.highlightVideoId.isNullOrEmpty() 
        }
        .map { HighlightUiItem.SupabaseMatch(it) }

    val related = (fromSupabase + fromExternal)
        .filter { it.uniqueId != item.uniqueId }
        .distinctBy { it.uniqueId }
        .take(15)

    return Pair(related, competitionId)
}
