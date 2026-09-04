package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.MatchDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

class ScoreRepositoryImpl(private val client: HttpClient) : ScoreRepository {

    private val baseUrl = "https://eiomktvavndorazreyba.supabase.co/rest/v1/matches"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVpb21rdHZhdm5kb3JhenJleWJhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1OTQ4ODcsImV4cCI6MjEwMzE3MDg4N30.GhP7TJXMeAEpG8F-9pB_SLNwaXLjIrOJ2LAiPC1kCk4"

    override suspend fun getMatches(
        competitionId: String?,
        status: String?,
        excludeStatus: String?,
        startDate: String?,
        endDate: String?,
        hasHighlight: Boolean,
        ascending: Boolean,
        limit: Int,
        offset: Int
    ): NetworkResult<List<MatchDto>> {
        return try {
            val order = if (ascending) "utc_date.asc" else "utc_date.desc"
            val response = client.get(baseUrl) {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                
                url {
                    parameters.append("select", "*")
                    parameters.append("order", order)
                    parameters.append("limit", limit.toString())
                    parameters.append("offset", offset.toString())
                    
                    competitionId?.let { 
                        if (it.isNotBlank()) {
                            parameters.append("competition_id", "ilike.$it")
                        }
                    }
                    status?.let { 
                        if (it.isNotBlank()) {
                            parameters.append("status", "ilike.$it")
                        }
                    }
                    excludeStatus?.let { 
                        if (it.isNotBlank()) {
                            parameters.append("status", "not.ilike.$it")
                        }
                    }
                    
                    startDate?.let { 
                        if (it.isNotBlank()) {
                            parameters.append("utc_date", "gte.${it}T00:00:00Z")
                        }
                    }
                    endDate?.let { 
                        if (it.isNotBlank()) {
                            parameters.append("utc_date", "lte.${it}T23:59:59Z")
                        }
                    }
                    
                    if (hasHighlight) {
                        parameters.append("highlight_video_id", "not.is.null")
                    }
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
