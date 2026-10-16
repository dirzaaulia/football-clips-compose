package com.dirzaaulia.footballclips.ui.score

import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.data.constants.SupportedClubsConstants
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.StandingDto
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.domain.model.toMatch
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
        when (val res = standingRepository.getStandings(compId, season)) {
            is NetworkResult.Success -> {
                val deduplicated = deduplicateStandings(res.data)
                _standingsState.value = StandingsState.Success(deduplicated)
            }
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
            val currSeason = selectedSeason.value
            val isArchiveSeason = currSeason != 2026
            val selectedDateStr = selectedDate.value
            val statusFilter = selectedStatusFilter.value
            val isLiveOnly = isLiveOnlyFilterActive.value || statusFilter == MatchStatusFilter.LIVE
            val (startDate, endDate) = if (!isArchiveSeason && selectedDateStr != null && !isLiveOnly) {
                val date = LocalDate.parse(selectedDateStr)
                date.plus(-1, DateTimeUnit.DAY).toString() to date.plus(1, DateTimeUnit.DAY).toString()
            } else null to null

            val result = repository.getMatches(
                competitionId = selectedCompetitionId.value,
                excludeStatus = null,
                startDate = startDate,
                endDate = endDate,
                ascending = if (isArchiveSeason) false else true,
                limit = if (isArchiveSeason) 50 else 60,
                season = currSeason
            )

            when (result) {
                is NetworkResult.Success -> {
                    val selDate = selectedDate.value
                    val favIds = favoriteClubs.value
                    val isMyClubsOnly = isMyClubsFilterActive.value
                    val clubs = supportedClubs.value

                    val filteredMatches = result.data
                        .map { it.toMatch() }
                        .filter { match ->
                            if (!isArchiveSeason) {
                                if (isLiveOnly) {
                                    if (!match.isLive) return@filter false
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
                                SupportedClubsConstants.isMatchForFavoriteClubs(match, favIds, clubs)
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
                                .thenByDescending { SupportedClubsConstants.isMatchForFavoriteClubs(it, favIds, clubs) }
                                .thenBy { it.utcDate }
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

internal fun deduplicateMatches(matches: List<Match>): List<Match> {
    if (matches.size <= 1) return matches

    val result = mutableListOf<Match>()
    for (match in matches) {
        val existingIndex = result.indexOfFirst { areMatchesDuplicate(it, match) }
        if (existingIndex < 0) {
            result.add(match)
        } else {
            val existing = result[existingIndex]
            val existingScore = scoreMatchQuality(existing)
            val newScore = scoreMatchQuality(match)
            if (newScore > existingScore) {
                result[existingIndex] = match
            }
        }
    }
    return result
}

private fun areMatchesDuplicate(m1: Match, m2: Match): Boolean {
    val h1 = normalizeTeamNameForDeduplication(m1.homeTeamName)
    val h2 = normalizeTeamNameForDeduplication(m2.homeTeamName)
    val a1 = normalizeTeamNameForDeduplication(m1.awayTeamName)
    val a2 = normalizeTeamNameForDeduplication(m2.awayTeamName)

    val homeMatches = h1 == h2 || h1.contains(h2) || h2.contains(h1)
    val awayMatches = a1 == a2 || a1.contains(a2) || a2.contains(a1)
    if (!homeMatches || !awayMatches) return false

    val d1 = m1.dateOnlyISO.ifEmpty { m1.utcDate.take(10) }
    val d2 = m2.dateOnlyISO.ifEmpty { m2.utcDate.take(10) }
    return d1 == d2 || m1.isLive || m2.isLive
}

private fun scoreMatchQuality(match: Match): Int {
    var score = 0
    // Live matches strongly prioritized over upcoming / scheduled
    if (match.isLive) score += 1000
    else if (match.isFinished) score += 500
    else if (match.isScheduled) score += 100

    // ESPN IDs are 9 digits (> 100_000_000), football-data are 6 digits
    if (match.id > 100_000_000L) score += 100
    if (!match.highlightVideoId.isNullOrBlank()) score += 50
    if (match.homeScore != null && match.awayScore != null) score += 25
    if (match.formations != null) score += 10
    if (match.statistics != null) score += 10
    return score
}

internal fun normalizeTeamNameForDeduplication(name: String): String {
    return name.lowercase()
        .replace("fc", "")
        .replace("bc", "")
        .replace("cf", "")
        .replace("afc", "")
        .replace("sc", "")
        .replace("us", "")
        .replace("ac", "")
        .replace("1.", "")
        .replace("balompié", "")
        .replace("balompie", "")
        .replace("calcio", "")
        .replace("de barcelona", "")
        .replace("münchen", "munich")
        .replace("manchester", "man")
        .replace("tottenham hotspur", "spurs")
        .replace("tottenham", "spurs")
        .replace("wolverhampton wanderers", "wolves")
        .replace("wolverhampton", "wolves")
        .replace("saint-germain", "sg")
        .replace("saint germain", "sg")
        .replace("atletico de madrid", "atletico")
        .replace("atlético de madrid", "atletico")
        .replace("atletico madrid", "atletico")
        .replace("atlético madrid", "atletico")
        .replace("athletic club", "bilbao")
        .replace("athletic bilbao", "bilbao")
        .replace("internazionale", "inter")
        .replace("inter milan", "inter")
        .filter { it.isLetterOrDigit() }
        .trim()
}

internal fun deduplicateStandings(standings: List<StandingDto>): List<StandingDto> {
    if (standings.size <= 1) return standings

    // 1. Group by normalized team name first (so the same club cannot appear twice at different ranks)
    val byTeam = standings
        .groupBy { normalizeTeamNameForDeduplication(it.teamName) }
        .values
        .map { duplicates ->
            if (duplicates.size == 1) {
                duplicates.first()
            } else {
                duplicates.maxByOrNull { item ->
                    var score = 0
                    if (item.playedGames > 0) score += 50
                    if (!item.teamCrest.isNullOrBlank()) score += 20
                    if (item.points > 0) score += 10
                    score
                } ?: duplicates.first()
            }
        }

    // 2. Ensure each position rank is unique and sorted
    return byTeam
        .groupBy { it.position }
        .values
        .map { duplicates ->
            if (duplicates.size == 1) {
                duplicates.first()
            } else {
                duplicates.maxByOrNull { item ->
                    var score = 0
                    if (item.playedGames > 0) score += 50
                    if (!item.teamCrest.isNullOrBlank()) score += 20
                    score
                } ?: duplicates.first()
            }
        }
        .sortedBy { it.position }
}
