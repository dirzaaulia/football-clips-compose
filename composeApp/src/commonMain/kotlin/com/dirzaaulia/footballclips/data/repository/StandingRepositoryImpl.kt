package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.constants.SupabaseConstants
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.StandingDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

class StandingRepositoryImpl(private val client: HttpClient) : StandingRepository {

    private val baseUrl = "${SupabaseConstants.SUPABASE_URL}/rest/v1/standings"
    private val supabaseKey = SupabaseConstants.SUPABASE_ANON_KEY

    override suspend fun getStandings(competitionId: String): NetworkResult<List<StandingDto>> {
        return try {
            val response = client.get(baseUrl) {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                url {
                    parameters.append("select", "*")
                    if (competitionId.isNotBlank()) {
                        parameters.append("competition_code", "eq.$competitionId")
                    }
                    parameters.append("order", "position.asc")
                }
            }
            if (response.status.value in 200..299) {
                NetworkResult.Success(response.body())
            } else {
                NetworkResult.Error(response.status.value, response.status.description)
            }
        } catch (t: Throwable) {
            NetworkResult.Exception(Exception(t))
        }
    }
}
