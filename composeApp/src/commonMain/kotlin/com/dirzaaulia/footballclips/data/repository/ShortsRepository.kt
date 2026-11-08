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
                    parameters.append("select", "*")
                    parameters.append("order", "published_at.desc,id.desc")
                    parameters.append("limit", limit.toString())
                    parameters.append("offset", offset.toString())
                    if (!category.isNullOrBlank()) {
                        parameters.append("category", "eq.$category")
                    }
                }
            }
            val dtos: List<FootballShortDto> = response.body()
            NetworkResult.Success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}

