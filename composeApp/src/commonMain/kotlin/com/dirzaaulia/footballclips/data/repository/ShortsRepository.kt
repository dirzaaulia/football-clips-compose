package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.constants.DatabaseConstants
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.FootballShortDto
import com.dirzaaulia.footballclips.data.model.remote.toDomain
import com.dirzaaulia.footballclips.domain.model.FootballShort
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

interface ShortsRepository {
    suspend fun getShorts(
        category: String? = null,
        limit: Int = 20,
        offset: Int = 0,
    ): NetworkResult<List<FootballShort>>
}

class ShortsRepositoryImpl(
    private val client: HttpClient,
) : ShortsRepository {

    private val databaseUrl = DatabaseConstants.DATABASE_URL
    private val databaseApiKey = DatabaseConstants.DATABASE_ANON_KEY

    override suspend fun getShorts(
        category: String?,
        limit: Int,
        offset: Int,
    ): NetworkResult<List<FootballShort>> {
        return try {
            val response = client.get("$databaseUrl/rest/v1/football_shorts") {
                header("apikey", databaseApiKey)
                header("Authorization", "Bearer $databaseApiKey")

                url {
                    val fetchLimit = (limit * 3).coerceAtMost(120)
                    parameters.append("select", "*")
                    parameters.append("order", "published_at.desc,hype_score.desc")
                    parameters.append("limit", fetchLimit.toString())
                    parameters.append("offset", offset.toString())
                    if (!category.isNullOrBlank()) {
                        parameters.append("category", "eq.$category")
                    }
                }
            }
            val dtos: List<FootballShortDto> = response.body()
            val diversified = diversifyShortsFeed(dtos.map { it.toDomain() }, limit = limit, cooldown = 4)
            NetworkResult.Success(diversified)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}

