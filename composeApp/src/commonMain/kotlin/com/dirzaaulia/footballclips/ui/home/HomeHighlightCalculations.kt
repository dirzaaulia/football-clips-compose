package com.dirzaaulia.footballclips.ui.home

import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.util.extractVideoId

internal fun findVideoById(
    oracleMatches: List<Match>,
    allHighlights: List<HighlightUiModel>,
    itemId: String,
): HighlightUiItem? {
    val match = oracleMatches.firstOrNull { it.id.toString() == itemId }
    if (match != null) return HighlightUiItem.OracleMatch(match)
    val hl = allHighlights.firstOrNull { it.id.toString() == itemId }
    if (hl != null) return HighlightUiItem.Highlight(hl)
    return null
}

internal fun calculateRelatedHighlights(
    item: HighlightUiItem,
    oracleMatches: List<Match>,
    allHighlights: List<HighlightUiModel>,
    competitions: List<BigLeague> = emptyList(),
): Pair<List<HighlightUiItem>, String?> {
    val selectedItemUniqueId = item.uniqueId
    val competitionName = if (item is HighlightUiItem.OracleMatch) {
        item.match.competitionName
    } else {
        (item as? HighlightUiItem.Highlight)?.highlight?.leagueName
    }

    val selectedComp = competitions.firstOrNull { it.name.equals(competitionName, ignoreCase = true) }
    val competitionId = if (item is HighlightUiItem.OracleMatch) {
        item.match.competitionId
    } else {
        selectedComp?.competitionId
    }

    val fromOracle = oracleMatches
        .filter { m ->
            m.id.toString() != selectedItemUniqueId && (
                competitionId == null || m.competitionId == competitionId || m.competitionName == competitionName
            )
        }
        .map { HighlightUiItem.OracleMatch(it) }

    val fromExternal = allHighlights
        .filter { h ->
            h.id.toString() != selectedItemUniqueId && (
                competitionName == null || h.leagueName == competitionName
            )
        }
        .map { HighlightUiItem.Highlight(it) }

    val combined = (fromOracle + fromExternal).distinctBy { it.uniqueId }.take(10)
    return Pair(combined, competitionId)
}

internal fun filterHighlights(
    oracleMatches: List<Match>,
    allHighlights: List<HighlightUiModel>,
    selectedLeagues: Set<String>,
    searchQuery: String,
    showExternalHighlights: Boolean,
): List<HighlightUiItem> {
    val queryLower = searchQuery.trim().lowercase()

    val filteredOracle = oracleMatches.filter { match ->
        val matchesLeague = selectedLeagues.isEmpty() || match.competitionName in selectedLeagues
        val matchesQuery = queryLower.isEmpty() ||
            match.homeTeamName.lowercase().contains(queryLower) ||
            match.awayTeamName.lowercase().contains(queryLower) ||
            match.competitionName.lowercase().contains(queryLower)
        matchesLeague && matchesQuery
    }

    val filteredExternal = allHighlights.filter { highlight ->
        val matchesLeague = selectedLeagues.isEmpty() || highlight.leagueName in selectedLeagues
        val matchesQuery = queryLower.isEmpty() ||
            highlight.title.lowercase().contains(queryLower) ||
            (highlight.leagueName.lowercase().contains(queryLower))
        matchesLeague && matchesQuery
    }

    val items = mutableListOf<HighlightUiItem>()
    items.addAll(filteredOracle.map { HighlightUiItem.OracleMatch(it) })

    if (showExternalHighlights || allHighlights.isNotEmpty() || oracleMatches.isEmpty()) {
        val existingVideoIds = filteredOracle.mapNotNull { it.highlightVideoId }.toSet()
        val deduplicatedExternal = filteredExternal.filter { hl ->
            val videoId = extractVideoId(hl.videoUrl)
            videoId == null || videoId !in existingVideoIds
        }
        items.addAll(deduplicatedExternal.map { HighlightUiItem.Highlight(it) })
    }

    return items
}
