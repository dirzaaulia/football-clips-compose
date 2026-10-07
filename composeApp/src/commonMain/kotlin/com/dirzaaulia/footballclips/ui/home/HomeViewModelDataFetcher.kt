package com.dirzaaulia.footballclips.ui.home

import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.data.model.remote.toUiModel
import com.dirzaaulia.footballclips.data.repository.HighlightRepository
import com.dirzaaulia.footballclips.data.repository.ScoreRepository
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.domain.model.toMatch
import kotlinx.coroutines.delay

internal suspend fun fetchSupabaseMatches(
    scoreRepository: ScoreRepository,
    competitionId: String?,
    limit: Int,
    offset: Int
): Pair<List<Match>, Boolean> {
    var attempt = 0
    while (attempt < 3) {
        attempt++
        val res = scoreRepository.getMatches(
            competitionId = competitionId,
            hasHighlight = true,
            ascending = false,
            limit = limit,
            offset = offset
        )
        if (res is NetworkResult.Success) {
            val newM = res.data.map { it.toMatch() }
            val hasMore = newM.size >= limit
            return Pair(newM, hasMore)
        }
        if (attempt < 3) delay(500L * attempt)
    }
    return Pair(emptyList(), true)
}

internal suspend fun fetchExternalHighlightsData(
    highlightRepository: HighlightRepository,
    limit: Int,
    offset: Int,
    leagueId: Int?
): Pair<List<HighlightUiModel>, Int>? {
    val res = highlightRepository.getHighlights(
        limit = limit,
        offset = offset,
        leagueId = leagueId?.toString()
    )
    return if (res is NetworkResult.Success) {
        val total = res.data.pagination?.totalCount ?: 0
        val items = res.data.data?.mapNotNull { it.toUiModel() } ?: emptyList()
        Pair(items, total)
    } else {
        null
    }
}
