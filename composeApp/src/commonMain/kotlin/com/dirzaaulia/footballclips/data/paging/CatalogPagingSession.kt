package com.dirzaaulia.footballclips.data.paging

import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.HighlightItemResponse
import com.dirzaaulia.footballclips.data.model.remote.toUiModel
import com.dirzaaulia.footballclips.data.repository.OracleVmRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class CatalogPagingSession(
    private val oracleVmRepository: OracleVmRepository,
) {
    private var currentCursor: String? = null
    private var hasMorePages = true
    private val seenIds = mutableSetOf<String>()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadNextBatch(
        competition: String? = null,
        season: Int = 2026,
        query: String = "",
    ): Pair<List<HighlightUiItem>, Boolean> {
        if (!hasMorePages) return Pair(emptyList(), false)

        val result = oracleVmRepository.getHighlightPage(
            competition = competition,
            season = season,
            cursor = currentCursor,
            query = query,
        )

        return when (result) {
            is NetworkResult.Success -> {
                val pageDto = result.data
                val rawItems = pageDto.items

                val uiItems = rawItems
                    .mapNotNull { element ->
                        try {
                            json.decodeFromJsonElement<HighlightItemResponse>(element).toUiModel()
                        } catch (_: Exception) {
                            null
                        }
                    }
                    .map { HighlightUiItem.Highlight(it) }
                    .filter { item ->
                        val key = item.highlight.id.toString()
                        val notSeen = key !in seenIds
                        if (notSeen) seenIds.add(key)
                        notSeen
                    }

                currentCursor = pageDto.nextCursor
                hasMorePages = pageDto.hasMore && rawItems.isNotEmpty()

                Pair(uiItems, hasMorePages)
            }

            else -> Pair(emptyList(), false)
        }
    }
}
