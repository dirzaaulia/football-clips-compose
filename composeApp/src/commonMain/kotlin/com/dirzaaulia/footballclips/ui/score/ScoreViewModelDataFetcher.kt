package com.dirzaaulia.footballclips.ui.score

import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.data.constants.SupportedClubsConstants
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.domain.model.toMatch
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

fun ScoreViewModel.getStandings(competitionId: String? = selectedCompetitionId.value) {
    val compId = competitionId ?: "PL"
    viewModelScope.launch {
        _standingsState.value = StandingsState.Loading
        when (val res = standingRepository.getStandings(compId)) {
            is NetworkResult.Success -> _standingsState.value = StandingsState.Success(res.data)
            is NetworkResult.Error -> _standingsState.value = StandingsState.Error("Error ${res.code}: ${res.message}")
            is NetworkResult.Exception -> _standingsState.value = StandingsState.Error(res.e.message ?: "Failed to load standings")
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
            val selectedDateStr = selectedDate.value
            val isLiveOnly = isLiveOnlyFilterActive.value
            val (startDate, endDate) = if (selectedDateStr != null && !isLiveOnly) {
                val date = LocalDate.parse(selectedDateStr)
                date.plus(-1, DateTimeUnit.DAY).toString() to date.plus(1, DateTimeUnit.DAY).toString()
            } else null to null

            val result = repository.getMatches(
                competitionId = selectedCompetitionId.value,
                excludeStatus = null,
                startDate = startDate,
                endDate = endDate,
                ascending = true,
            )

            when (result) {
                is NetworkResult.Success -> {
                    val selDate = selectedDate.value
                    val todayDate = availableDates.firstOrNull { it.isToday }?.date
                    val favIds = favoriteClubs.value
                    val isMyClubsOnly = isMyClubsFilterActive.value
                    val clubs = supportedClubs.value

                    val matches = result.data
                        .map { it.toMatch() }
                        .filter { match ->
                            if (isLiveOnly) {
                                if (!match.isLive) return@filter false
                            } else {
                                val isSelectedDate = match.dateOnlyISO == selDate
                                val isLiveNow = match.isLive
                                val dateMatches = isSelectedDate || (isLiveNow && selDate == todayDate)
                                if (!dateMatches) return@filter false
                            }
                            if (isMyClubsOnly) {
                                SupportedClubsConstants.isMatchForFavoriteClubs(match, favIds, clubs)
                            } else {
                                true
                            }
                        }
                        .sortedWith(
                            compareByDescending<Match> { match ->
                                SupportedClubsConstants.isMatchForFavoriteClubs(match, favIds, clubs)
                            }.thenBy { it.utcDate },
                        )
                        .map { HighlightUiItem.SupabaseMatch(it) }

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
            result.add(HighlightUiItem.BannerAd("ad-$i"))
        }
    }
    return result
}
