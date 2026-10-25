package com.dirzaaulia.footballclips.data.paging

import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.toUiModel
import com.dirzaaulia.footballclips.data.repository.MiddlewareRepository
import com.dirzaaulia.footballclips.data.repository.OracleVmRepository
import com.dirzaaulia.footballclips.domain.model.toMatch

class HighlightPagingSource(
    private val oracleVmRepository: OracleVmRepository,
    private val middlewareRepository: MiddlewareRepository,
    private val competitionId: String? = null,
    private val selectedLeagueId: Int? = null,
    private val searchQuery: String = "",
    private val season: Int = 2026,
) {
    private var databaseOffset = 0
    private var middlewareOffset = 0
    private var hasMoreDatabase = true
    private var hasMoreMiddleware = season == 2026
    private val seenIds = mutableSetOf<String>()

    suspend fun loadNextPage(pageSize: Int = 20): Pair<List<HighlightUiItem>, Boolean> {
        val resultItems = mutableListOf<HighlightUiItem>()

        // Phase 1: Fetch priority database matches from Oracle Cloud VM PostgREST with server-side query search
        if (hasMoreDatabase) {
            val res = oracleVmRepository.getMatches(
                competitionId = competitionId,
                hasHighlight = true,
                ascending = false,
                limit = pageSize,
                offset = databaseOffset,
                season = season,
                query = searchQuery,
            )

            if (res is NetworkResult.Success) {
                val matches = res.data.map { it.toMatch() }
                val uiMatches = matches
                    .map { HighlightUiItem.OracleMatch(it) }
                    .filter { item ->
                        val key = item.match.id.toString()
                        val notSeen = key !in seenIds
                        if (notSeen) seenIds.add(key)
                        notSeen
                    }

                resultItems.addAll(uiMatches)
                // Always increment offset by the limit exactly so the next page starts after this chunk,
                // regardless of whether `uiMatches` were filtered out.
                databaseOffset += pageSize

                if (matches.size < pageSize) {
                    hasMoreDatabase = false
                }
            } else {
                hasMoreDatabase = false
            }
        }

        // Phase 2: If Oracle database matches yielded a deficit or are exhausted, chain to Middleware Worker highlights
        if (resultItems.size < pageSize && hasMoreMiddleware && season == 2026) {
            val deficit = pageSize - resultItems.size
            val mwRes = middlewareRepository.getMiddlewareHighlights(
                limit = deficit,
                offset = middlewareOffset,
                season = season,
                leagueId = selectedLeagueId?.toString(),
            )

            if (mwRes is NetworkResult.Success) {
                val rawItems = mwRes.data.data?.mapNotNull { it.toUiModel() } ?: emptyList()
                val totalCount = mwRes.data.pagination?.totalCount ?: 0

                val uiHighlights = rawItems
                    .map { HighlightUiItem.Highlight(it) }
                    .filter { item ->
                        val key = item.highlight.id.toString()
                        val notSeen = key !in seenIds
                        if (notSeen) seenIds.add(key)
                        notSeen
                    }

                resultItems.addAll(uiHighlights)
                // Consume either the full deficit or exactly the items returned
                middlewareOffset += deficit

                if (middlewareOffset >= totalCount || rawItems.size < deficit) {
                    hasMoreMiddleware = false
                }
            } else {
                hasMoreMiddleware = false
            }
        }

        val overallHasMore = hasMoreDatabase || hasMoreMiddleware
        return Pair(resultItems, overallHasMore)
    }
}
