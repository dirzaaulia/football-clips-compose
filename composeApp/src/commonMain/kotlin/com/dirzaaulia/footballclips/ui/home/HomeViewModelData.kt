package com.dirzaaulia.footballclips.ui.home

import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.data.constants.BigLeaguesConstants
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.data.model.remote.toUiModel
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.data.repository.HighlightRepository
import com.dirzaaulia.footballclips.data.repository.ScoreRepository
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.domain.model.toMatch
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal suspend fun fetchSupabaseMatches(
    scoreRepository: ScoreRepository,
    competitionId: String?,
    limit: Int,
    offset: Int,
): Pair<List<Match>, Boolean> {
    var attempt = 0
    while (attempt < 3) {
        attempt++
        val res = scoreRepository.getMatches(
            competitionId = competitionId,
            hasHighlight = true,
            ascending = false,
            limit = limit,
            offset = offset,
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
    highlightRepository: HighlightRepository,
    limit: Int,
    offset: Int,
    leagueId: Int?,
): Pair<List<HighlightUiModel>, Int>? {
    val res = highlightRepository.getHighlights(
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
    val f = scoreRepository.getMatches(competitionId = compId, status = "FINISHED", ascending = false)
    if (f is NetworkResult.Success) {
        _relatedFixturesFinished.value = f.data.map { HighlightUiItem.SupabaseMatch(it.toMatch()) }
    }
    val u = scoreRepository.getMatches(competitionId = compId, excludeStatus = "FINISHED", ascending = true)
    if (u is NetworkResult.Success) {
        _relatedFixturesUpcoming.value = u.data.map { it.toMatch() }.filter { it.isScheduled }.map { HighlightUiItem.SupabaseMatch(it) }
    }
}

internal suspend fun HomeViewModel.getSupabaseHighlights(isRefresh: Boolean = true) {
    if (isRefresh) {
        supabaseOffset = 0
        supabaseMatches.clear()
        hasMoreSupabase = true
    }
    if (!hasMoreSupabase) return
    val (newM, hasMore) = fetchSupabaseMatches(scoreRepository, _selectedCompetitionId.value, supabaseLimit, supabaseOffset)
    val exIds = supabaseMatches.map { it.id }.toSet()
    val exVideos = supabaseMatches.mapNotNull { it.highlightVideoId }.toSet()
    val deduplicated = newM.filter { it.id !in exIds && (it.highlightVideoId == null || it.highlightVideoId !in exVideos) }
    supabaseMatches.addAll(deduplicated)
    hasMoreSupabase = hasMore && newM.isNotEmpty()
    supabaseOffset += supabaseLimit
}

internal suspend fun HomeViewModel.getExternalHighlightsInternal(isRefresh: Boolean = false) {
    val s = _uiState.value
    if (s is HomeState.Success) _uiState.value = s.copy(isLoadingMore = true, loadMoreError = null)
    if (isRefresh) {
        currentOffset = 0
        allHighlights.clear()
    }
    val res = fetchExternalHighlightsData(highlightRepository, limit, currentOffset, _selectedLeagueId.value)
    if (res != null) {
        totalCount = res.second
        allHighlights.addAll(res.first)
        currentOffset += limit
        updateUiState()
    } else {
        handleNetworkError(isRefresh, "Failed to load external highlights")
    }
}

internal fun HomeViewModel.handleNetworkError(isRefresh: Boolean, message: String) {
    val s = _uiState.value
    if (s is HomeState.Success) {
        _uiState.value = s.copy(isLoadingMore = false, loadMoreError = message)
        viewModelScope.launch { _errorMessage.emit(message) }
    } else if (isRefresh) {
        _uiState.value = HomeState.Error(message)
    } else {
        viewModelScope.launch { _errorMessage.emit(message) }
    }
}

internal fun findVideoById(
    supabaseMatches: List<Match>,
    allHighlights: List<HighlightUiModel>,
    itemId: String,
): HighlightUiItem? {
    val match = supabaseMatches.firstOrNull { it.id.toString() == itemId }
    if (match != null) return HighlightUiItem.SupabaseMatch(match)
    val highlight = allHighlights.firstOrNull { it.id.toString() == itemId }
    if (highlight != null) return HighlightUiItem.Highlight(highlight)
    return null
}

internal fun calculateRelatedHighlights(
    item: HighlightUiItem,
    supabaseMatches: List<Match>,
    allHighlights: List<HighlightUiModel>,
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

internal fun buildHomeUiItems(
    supabaseMatches: List<Match>,
    allHighlights: List<HighlightUiModel>,
    searchQuery: String,
    selectedLeagues: Set<String>,
    selectedCountries: Set<String>,
    selectedLeagueId: Int?,
    selectedCompetitionId: String?,
    showExternalHighlights: Boolean,
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

internal fun buildListWithAds(items: List<HighlightUiItem>, isAdsRemoved: Boolean): List<HighlightUiItem> {
    if (items.isEmpty()) return emptyList()
    if (isAdsRemoved) return items

    val result = mutableListOf<HighlightUiItem>()
    for (i in items.indices) {
        result.add(items[i])
        if ((i + 1) % 6 == 0 && i < items.size - 1) {
            result.add(HighlightUiItem.BannerAd("ad-$i"))
        }
    }
    return result
}

internal fun isLeagueMatch(target: String?, candidate: String?): Boolean {
    if (target == null || candidate == null) return false
    val t = target.lowercase().trim()
    val c = candidate.lowercase().trim()
    if (t == c) return true

    val plRegex = Regex(".*\\b(pl|epl|premier league)\\b.*", RegexOption.IGNORE_CASE)
    if (plRegex.matches(t) && plRegex.matches(c)) return true

    return t.contains(c) || c.contains(t)
}
