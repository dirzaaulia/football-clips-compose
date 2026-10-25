package com.dirzaaulia.footballclips.ui.home

import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import kotlinx.coroutines.CancellationException

internal suspend fun HomeViewModel.loadCatalogPage(reset: Boolean): Boolean {
    try {
        val (items, more) = catalogPaging.loadNextBatch(_selectedCompetitionId.value, selectedSeason.value, searchQuery)
        if (reset) { oracleMatches.clear(); allHighlights.clear() }
        val own = items.filterIsInstance<HighlightUiItem.OracleMatch>().map { it.match }
        val external = items.filterIsInstance<HighlightUiItem.Highlight>().map { it.highlight }
        val ownIds = oracleMatches.map { it.id }.toSet()
        val externalIds = allHighlights.map { it.id }.toSet()
        oracleMatches.addAll(own.filter { it.id !in ownIds })
        allHighlights.addAll(external.filter { it.id !in externalIds })
        hasMoreOracle = more
        if (external.isNotEmpty()) _showExternalHighlights.value = true
        return true
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        handleNetworkError(reset, error.message ?: "Highlights unavailable; retry this page.")
        return false
    }
}
