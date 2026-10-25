package com.dirzaaulia.footballclips.ui.home


import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.data.model.remote.toUiModel
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.data.repository.MiddlewareRepository
import com.dirzaaulia.footballclips.data.repository.OracleVmRepository
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.domain.model.toMatch
import com.dirzaaulia.footballclips.util.extractVideoId
import kotlinx.coroutines.delay

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
    val (newM, hasMore) = fetchOracleMatches(
        oracleVmRepository = oracleVmRepository,
        competitionId = _selectedCompetitionId.value,
        limit = oracleLimit,
        offset = oracleOffset,
        season = selectedSeason.value,
        query = searchQuery,
    )
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
    val competitionName = if (item is HighlightUiItem.OracleMatch) item.match.competitionName else (item as? HighlightUiItem.Highlight)?.highlight?.leagueName

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
