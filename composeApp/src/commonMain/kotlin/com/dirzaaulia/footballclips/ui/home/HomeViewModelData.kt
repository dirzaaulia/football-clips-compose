package com.dirzaaulia.footballclips.ui.home

import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.data.model.remote.toUiModel
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.data.repository.MiddlewareRepository
import com.dirzaaulia.footballclips.data.repository.OracleVmRepository
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.domain.model.toMatch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

private val PRIORITY_COMPETITION_IDS = listOf(
    "CL", "EL", "ECL", "PL", "PD", "SA", "BL1", "FL1",
    "FAC", "CDR", "CIT", "DFB", "EFL", "DED", "PPL", "SPL", "MLS", "BRA", "ARG",
)

internal suspend fun fetchGroupedOracleHighlights(
    oracleVmRepository: OracleVmRepository,
    season: Int? = null,
): List<Match> {
    return coroutineScope {
        PRIORITY_COMPETITION_IDS.map { compId ->
            async {
                val res = oracleVmRepository.getMatches(
                    competitionId = compId,
                    hasHighlight = true,
                    ascending = false,
                    limit = 20,
                    season = season,
                )
                if (res is NetworkResult.Success) {
                    res.data.map { it.toMatch() }
                } else {
                    emptyList()
                }
            }
        }.awaitAll().flatten()
    }
}

internal suspend fun fetchOracleMatches(
    oracleVmRepository: OracleVmRepository,
    competitionId: String?,
    limit: Int,
    offset: Int,
    season: Int? = null,
    query: String? = null,
): Pair<List<Match>, Boolean> {
    var attempt = 0
    while (attempt < 3) {
        attempt++
        val res = oracleVmRepository.getMatches(
            competitionId = competitionId,
            hasHighlight = true,
            ascending = false,
            limit = limit,
            offset = offset,
            season = season,
            query = query,
        )
        if (res is NetworkResult.Success) {
            val newM = res.data.map { it.toMatch() }
            val hasMore = newM.size >= limit
            return Pair(newM, hasMore)
        }
        if (attempt < 3) delay(500L * attempt)
    }
    return Pair(emptyList(), false)
}

internal suspend fun fetchExternalHighlightsData(
    middlewareRepository: MiddlewareRepository,
    limit: Int,
    offset: Int,
    leagueId: Int?,
): Pair<List<HighlightUiModel>, Int>? {
    val res = middlewareRepository.getMiddlewareHighlights(
        limit = limit,
        offset = offset,
        leagueId = leagueId?.toString(),
    )
    return if (res is NetworkResult.Success) {
        val total = res.data.pagination?.totalCount ?: 0
        val items = res.data.data?.mapNotNull { it.toUiModel() } ?: emptyList()
        Pair(items, total)
    } else {
        null
    }
}

internal suspend fun HomeViewModel.fetchLeagueFixtures(compId: String) {
    val f = oracleVmRepository.getMatches(
        competitionId = compId,
        status = "FINISHED",
        ascending = false,
        limit = 20,
        season = selectedSeason.value,
    )
    if (f is NetworkResult.Success) {
        _relatedFixturesFinished.value = f.data.map { HighlightUiItem.OracleMatch(it.toMatch()) }
    }
    val u = oracleVmRepository.getMatches(
        competitionId = compId,
        excludeStatus = "FINISHED",
        ascending = true,
        limit = 20,
        season = selectedSeason.value,
    )
    if (u is NetworkResult.Success) {
        _relatedFixturesUpcoming.value = u.data.map { it.toMatch() }.filter { it.isScheduled }.map { HighlightUiItem.OracleMatch(it) }
    }
}

internal suspend fun HomeViewModel.fetchRelatedHighlights(compId: String?, currentItemUniqueId: String) {
    val h = oracleVmRepository.getMatches(
        competitionId = compId,
        hasHighlight = true,
        ascending = false,
        limit = 20,
        season = selectedSeason.value,
    )
    if (h is NetworkResult.Success) {
        val fetched = h.data.map { HighlightUiItem.OracleMatch(it.toMatch()) }
            .filter { it.uniqueId != currentItemUniqueId }
        val merged = (_relatedHighlights.value + fetched).distinctBy { it.uniqueId }.take(20)
        _relatedHighlights.value = merged
    }
}

internal suspend fun HomeViewModel.getOracleHighlights(isRefresh: Boolean = true) {
    if (isRefresh) {
        oracleOffset = 0
        oracleMatches.clear()
        hasMoreOracle = true
    }
    if (!hasMoreOracle) return

    val (newM, hasMore) = if (_selectedCompetitionId.value == null && searchQuery.isBlank() && oracleOffset == 0) {
        val grouped = fetchGroupedOracleHighlights(oracleVmRepository, selectedSeason.value)
        Pair(grouped, false)
    } else {
        fetchOracleMatches(
            oracleVmRepository = oracleVmRepository,
            competitionId = _selectedCompetitionId.value,
            limit = oracleLimit,
            offset = oracleOffset,
            season = selectedSeason.value,
            query = searchQuery,
        )
    }

    val exIds = oracleMatches.map { it.id }.toSet()
    val exVideos = oracleMatches.mapNotNull { it.highlightVideoId }.toSet()
    val deduplicated = newM.filter { it.id !in exIds && (it.highlightVideoId == null || it.highlightVideoId !in exVideos) }
    oracleMatches.addAll(deduplicated)
    hasMoreOracle = hasMore && newM.isNotEmpty()
    oracleOffset += oracleLimit
}

internal suspend fun HomeViewModel.getExternalHighlightsInternal(isRefresh: Boolean) {
    if (isRefresh) {
        currentOffset = 0
        allHighlights.clear()
    }
    val result = fetchExternalHighlightsData(
        middlewareRepository = middlewareRepository,
        limit = limit,
        offset = currentOffset,
        leagueId = _selectedLeagueId.value,
    )
    if (result != null) {
        val (newItems, total) = result
        totalCount = total
        val existingIds = allHighlights.map { it.id }.toSet()
        val deduplicated = newItems.filter { it.id !in existingIds }
        allHighlights.addAll(deduplicated)
        currentOffset += limit
        updateUiState()
    } else {
        handleNetworkError(isRefresh, "Failed to load external highlights.")
    }
}

internal fun HomeViewModel.handleNetworkError(isRefresh: Boolean, message: String) {
    val cs = _uiState.value
    if (isRefresh && oracleMatches.isEmpty() && allHighlights.isEmpty()) {
        _uiState.value = HomeState.Error(message)
    } else if (cs is HomeState.Success) {
        _uiState.value = cs.copy(isLoadingMore = false, loadMoreError = message)
    }
}

internal fun buildHomeUiItems(
    oracleMatches: List<Match>,
    allHighlights: List<HighlightUiModel>,
    searchQuery: String,
    selectedLeagues: Set<String>,
    selectedLeagueId: Int?,
    selectedCompetitionId: String?,
    showExternalHighlights: Boolean,
): Pair<List<HighlightUiItem>, Int> {
    val filteredItems = filterHighlights(
        oracleMatches = oracleMatches,
        allHighlights = allHighlights,
        selectedLeagues = selectedLeagues,
        searchQuery = searchQuery,
        showExternalHighlights = showExternalHighlights,
    )
    return Pair(filteredItems, filteredItems.size)
}

internal fun buildListWithAds(
    items: List<HighlightUiItem>,
    isAdsRemoved: Boolean,
): List<HighlightUiItem> {
    if (items.isEmpty()) return emptyList()
    if (isAdsRemoved) return items

    val result = mutableListOf<HighlightUiItem>()
    for (i in items.indices) {
        result.add(items[i])
        if ((i + 1) % 5 == 0 && i < items.size - 1) {
            result.add(HighlightUiItem.BannerAd)
        }
    }
    return result
}

internal fun extractAllFilters(
    oracleMatches: List<Match>,
    allHighlights: List<HighlightUiModel>,
    searchQuery: String,
    selectedLeagues: Set<String>,
    totalMatchesLoaded: Int,
): FilterState {
    return calculateFilterOptions(
        databaseMatches = oracleMatches,
        externalHighlights = allHighlights,
        currentQuery = searchQuery,
        selectedLeagues = selectedLeagues,
    ).copy(totalMatchesLoaded = totalMatchesLoaded)
}
