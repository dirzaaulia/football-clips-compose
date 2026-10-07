package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.StandingDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

class StandingRepositoryImpl(private val client: HttpClient) : StandingRepository {

    private val baseUrl = "https://eiomktvavndorazreyba.supabase.co/rest/v1/standings"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVpb21rdHZhdm5kb3JhenJleWJhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1OTQ4ODcsImV4cCI6MjEwMzE3MDg4N30.GhP7TJXMeAEpG8F-9pB_SLNwaXLjIrOJ2LAiPC1kCk4"

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
