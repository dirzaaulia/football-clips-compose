package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.HighlightResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode

class HighlightRepositoryImpl(private val client: HttpClient) : HighlightRepository {

    private val baseUrl = "https://drzfootball.netlify.app/api/highlights"

    override suspend fun getHighlights(
        limit: Int,
        offset: Int,
        season: Int?,
        leagueId: String?,
        country: String?
    ): NetworkResult<HighlightResponse> {
        return try {
            val response = client.get(baseUrl) {
                parameter("limit", limit)
                parameter("offset", offset)
                season?.let { parameter("season", it) }
                leagueId?.let { parameter("leagueId", it) }
                country?.let { parameter("country", it) }
            }
            if (response.status == HttpStatusCode.OK) {
                NetworkResult.Success(response.body())
            } else {
                NetworkResult.Error(response.status.value, response.status.description)
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}
