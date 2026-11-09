package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.cache.InMemorySwrCache
import com.dirzaaulia.footballclips.data.model.remote.FormationsDto
import com.dirzaaulia.footballclips.data.model.remote.espn.EspnSummaryResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.serialization.json.Json

interface EspnSummaryRepository {
    suspend fun getTacticalSummary(
        matchId: Long,
        competitionCode: String,
        competitionSlug: String? = null,
    ): Pair<FormationsDto?, Map<String, Map<String, String>>?>
}

class EspnSummaryRepositoryImpl(
    private val client: HttpClient,
) : EspnSummaryRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val summaryCache = InMemorySwrCache<Long, Pair<FormationsDto?, Map<String, Map<String, String>>?>>(
        ttlMillis = 300_000L,
    )

    override suspend fun getTacticalSummary(
        matchId: Long,
        competitionCode: String,
        competitionSlug: String?,
    ): Pair<FormationsDto?, Map<String, Map<String, String>>?> {
        val cached = summaryCache.get(matchId)
        if (cached != null) return cached

        val slug = competitionSlug?.takeIf { it.isNotBlank() } ?: resolveDefaultSlug(competitionCode)
        val url = "https://site.api.espn.com/apis/site/v2/sports/soccer/$slug/summary?event=$matchId"

        return try {
            val responseText: String = client.get(url) {
                header("User-Agent", "Mozilla/5.0")
            }.body()

            val summaryDto = json.decodeFromString<EspnSummaryResponseDto>(responseText)
            val formations = summaryDto.toFormationsDto()
            val statistics = summaryDto.toStatisticsMap()

            val result = Pair(formations, statistics)
            summaryCache.put(matchId, result)
            result
        } catch (_: Exception) {
            summaryCache.getStale(matchId) ?: Pair(null, null)
        }
    }

    private fun resolveDefaultSlug(code: String): String {
        return code.lowercase().trim()
    }
}
