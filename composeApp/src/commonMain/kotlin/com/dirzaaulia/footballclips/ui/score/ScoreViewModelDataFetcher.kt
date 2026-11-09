package com.dirzaaulia.footballclips.ui.score

import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.StandingDto
import com.dirzaaulia.footballclips.data.model.remote.isMatchForFavoriteClubs
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.domain.model.toMatch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

fun ScoreViewModel.getStandings(
    competitionId: String? = selectedCompetitionId.value,
    season: Int = selectedSeason.value
) {
    val compId = competitionId ?: "PL"
    viewModelScope.launch {
        _standingsState.value = StandingsState.Loading

        val matchRes = oracleVmRepository.getMatches(
            competitionId = compId,
            limit = 300,
            ascending = true,
            season = season
        )
        
        val matches = if (matchRes is NetworkResult.Success) matchRes.data.map { it.toMatch() } else emptyList()
        val hasKnockouts = matches.any { it.roundName != null && !it.roundName.contains("Regular", ignoreCase = true) }
        val hasLeaguePhase = matches.any { it.matchday != null }

        val compDef = oracleVmRepository.competitions.value.firstOrNull { it.id.equals(compId, ignoreCase = true) }
        val isCup = if (compDef != null) {
            compDef.isCup
        } else {
            compId in setOf("FAC", "EFL", "CDR", "DFB", "CIT") || (hasKnockouts && !hasLeaguePhase)
        }
        val isHybrid = if (compDef != null) {
            compDef.isHybrid
        } else {
            compId in setOf("CL", "EL", "ECL") || (hasKnockouts && hasLeaguePhase)
        }

        if (isCup) {
            if (matchRes is NetworkResult.Success) {
                _standingsState.value = StandingsState.Success(
                    standings = emptyList(),
                    knockoutMatches = matches
                )
            } else if (matchRes is NetworkResult.Error) {
                _standingsState.value = StandingsState.Error("Error ${matchRes.code}: ${matchRes.message}")
            } else if (matchRes is NetworkResult.Exception) {
                _standingsState.value = StandingsState.Error(matchRes.e.message ?: "Failed to load cup matches")
            }
        } else if (isHybrid) {
            val standingsRes = oracleVmRepository.getStandings(compId, season)

            val standings = if (standingsRes is NetworkResult.Success) {
                deduplicateStandings(standingsRes.data)
            } else emptyList()

            if (standings.isEmpty() && matches.isEmpty() && standingsRes is NetworkResult.Error) {
                _standingsState.value = StandingsState.Error("Error ${standingsRes.code}: ${standingsRes.message}")
            } else {
                _standingsState.value = StandingsState.Success(
                    standings = standings,
                    knockoutMatches = matches
                )
            }
        } else {
            when (val res = oracleVmRepository.getStandings(compId, season)) {
                is NetworkResult.Success -> {
                    val deduplicated = deduplicateStandings(res.data)
                    _standingsState.value = StandingsState.Success(
                        standings = deduplicated,
                        knockoutMatches = emptyList()
                    )
                }
                is NetworkResult.Error -> _standingsState.value = StandingsState.Error("Error ${res.code}: ${res.message}")
                is NetworkResult.Exception -> _standingsState.value = StandingsState.Error(res.e.message ?: "Failed to load standings")
            }
        }
    }
}

fun ScoreViewModel.selectCompetition(competitionId: String?) {
    if (selectedCompetitionId.value == competitionId) return
    _selectedCompetitionId.value = competitionId
    getMatches()
    if (competitionId != null) getStandings(competitionId)
}

fun ScoreViewModel.selectDate(date: String?) {
    if (selectedDate.value == date) return
    _selectedDate.value = date
    getMatches()
}

fun ScoreViewModel.getMatches() {
    viewModelScope.launch {
        _uiState.value = ScoreState.Loading
        try {
            val currSeason = selectedSeason.value
            val isArchiveSeason = currSeason != 2026
            val selectedDateStr = selectedDate.value
            val statusFilter = selectedStatusFilter.value
            val isLiveOnly = isLiveOnlyFilterActive.value || statusFilter == MatchStatusFilter.LIVE
            val (startDate, endDate) = if (!isArchiveSeason && selectedDateStr != null && !isLiveOnly) {
                val date = LocalDate.parse(selectedDateStr)
                date.plus(-1, DateTimeUnit.DAY).toString() to date.plus(1, DateTimeUnit.DAY).toString()
            } else null to null

            val isToday = selectedDateStr == availableDates.firstOrNull { it.isToday }?.date
            val result = if (isLiveOnly && !isArchiveSeason) {
                oracleVmRepository.getLiveMatches()
            } else {
                oracleVmRepository.getMatches(
                    competitionId = selectedCompetitionId.value,
                    excludeStatus = null,
                    startDate = startDate,
                    endDate = endDate,
                    ascending = if (isArchiveSeason) false else true,
                    limit = if (isArchiveSeason) 50 else 60,
                    season = currSeason,
                )
            }

            when (result) {
                is NetworkResult.Success -> {
                    val selDate = selectedDate.value
                    val favIds = favoriteClubs.value
                    val isMyClubsOnly = isMyClubsFilterActive.value
                    val clubs = supportedClubs.value

                    val baseMatches = result.data.map { it.toMatch() }
                    val liveMatchesList = if (!isArchiveSeason && !isLiveOnly && (isToday || selDate == null)) {
                        val liveRes = oracleVmRepository.getLiveMatches()
                        if (liveRes is NetworkResult.Success) liveRes.data.map { it.toMatch() } else emptyList()
                    } else {
                        emptyList()
                    }

                    val liveMap = liveMatchesList.associateBy { it.id }
                    val merged = if (liveMap.isNotEmpty()) {
                        baseMatches.filter { it.id !in liveMap } + liveMatchesList
                    } else {
                        baseMatches
                    }

                    val filteredMatches = merged
                        .filter { match ->
                            if (!isArchiveSeason) {
                                if (isLiveOnly) {
                                    if (!match.isLive) return@filter false
                                    val selComp = selectedCompetitionId.value
                                    if (selComp != null && !match.competitionId.equals(selComp, ignoreCase = true)) {
                                        return@filter false
                                    }
                                } else {
                                    val isSelectedDate = match.dateOnlyISO == selDate
                                    val isLiveNow = match.isLive
                                    when (statusFilter) {
                                        MatchStatusFilter.FINISHED -> {
                                            if (!match.isFinished || !isSelectedDate) return@filter false
                                        }
                                        MatchStatusFilter.UPCOMING -> {
                                            if (!match.isScheduled || !isSelectedDate) return@filter false
                                        }
                                        MatchStatusFilter.LIVE -> {
                                            if (!match.isLive) return@filter false
                                        }
                                        MatchStatusFilter.ALL -> {
                                            val dateMatches = isSelectedDate || isLiveNow
                                            if (!dateMatches) return@filter false
                                        }
                                    }
                                }
                            }
                            if (isMyClubsOnly) {
                                match.isMatchForFavoriteClubs(favIds, clubs)
                            } else {
                                true
                            }
                        }

                    // Deduplicate matches to eliminate stale duplicates between providers
                    val deduplicatedMatches = deduplicateMatches(filteredMatches)

                    // Prioritize LIVE matches at the top, then favorite clubs, then kickoff time
                    val matches = deduplicatedMatches
                        .sortedWith(
                            compareByDescending<Match> { it.isLive }
                                .thenByDescending { it.isMatchForFavoriteClubs(favIds, clubs) }
                                .thenBy { it.utcDate }
                        )
                        .map { HighlightUiItem.OracleMatch(it) }

                    val adsRemoved = isAdsRemoved.value
                    _uiState.value = ScoreState.Success(
                        matches = buildFixtureListWithAds(matches, adsRemoved),
                        isAdsRemoved = adsRemoved,
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = ScoreState.Error("Error: ${result.code} - ${result.message}")
                }
                is NetworkResult.Exception -> {
                    _uiState.value = ScoreState.Error("Exception: ${result.e.message}")
                }
            }
        } catch (t: Throwable) {
            _uiState.value = ScoreState.Error("Critical Error: ${t.message}")
        }
    }
}

fun ScoreViewModel.buildFixtureListWithAds(
    items: List<HighlightUiItem>,
    isAdsRemoved: Boolean,
): List<HighlightUiItem> {
    if (items.isEmpty()) return emptyList()
    if (isAdsRemoved) return items

    val result = mutableListOf<HighlightUiItem>()
    for (i in items.indices) {
        result.add(items[i])
        if ((i + 1) % 6 == 0 && i < items.size - 1) {
            result.add(HighlightUiItem.BannerAd)
        }
    }
    return result
}

fun ScoreViewModel.observeAdsRemoved() {
    viewModelScope.launch {
        isAdsRemoved.collectLatest { removed ->
            val current = _uiState.value
            if (current is ScoreState.Success) {
                val nonAds = current.matches.filter { it !is HighlightUiItem.BannerAd }
                _uiState.value = current.copy(
                    matches = buildFixtureListWithAds(nonAds, removed),
                    isAdsRemoved = removed,
                )
            }
        }
    }
}

fun ScoreViewModel.startLiveSyncTicker() {
    viewModelScope.launch {
        while (isActive) {
            delay(30_000L)
            val isToday = selectedDate.value == availableDates.firstOrNull { it.isToday }?.date
            if (isToday || isLiveOnlyFilterActive.value) {
                getMatches()
            }
        }
    }
}
