package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.data.constants.DatabaseConstants
import com.dirzaaulia.footballclips.data.model.remote.SupportedClub
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.CatalogPageDto
import com.dirzaaulia.footballclips.data.model.remote.MatchDto
import com.dirzaaulia.footballclips.data.model.remote.StandingDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OracleVmRepositoryImpl(
    private val client: HttpClient,
) : OracleVmRepository {

    private val databaseUrl = DatabaseConstants.DATABASE_URL
    private val databaseApiKey = DatabaseConstants.DATABASE_ANON_KEY

    private val _competitions = MutableStateFlow<List<BigLeague>>(emptyList())
    override val competitions: StateFlow<List<BigLeague>> = _competitions.asStateFlow()

    private val _supportedClubs = MutableStateFlow<List<SupportedClub>>(emptyList())
    override val supportedClubs: StateFlow<List<SupportedClub>> = _supportedClubs.asStateFlow()

    override suspend fun getMatches(
        competitionId: String?,
        status: String?,
        excludeStatus: String?,
        startDate: String?,
        endDate: String?,
        hasHighlight: Boolean,
        ascending: Boolean,
        limit: Int,
        offset: Int,
        season: Int?,
        query: String?,
    ): NetworkResult<List<MatchDto>> {
        return try {
            val order = if (ascending) "utc_date.asc,id.asc" else "utc_date.desc,id.desc"
            val effectiveLimit = if (limit > 0) limit else 20
            val response = client.get("$databaseUrl/rest/v1/matches") {
                header("apikey", databaseApiKey)
                header("Authorization", "Bearer $databaseApiKey")

                url {
                    parameters.append("select", "id,competition_id,competition_name,utc_date,status,matchday,home_team_id,home_team_name,home_team_crest,away_team_id,away_team_name,away_team_crest,home_score,away_score,highlight_video_id,backup_highlight_video_id,season,round_name,display_clock,youtube_region_info,backup_youtube_region_info")
                    parameters.append("order", order)
                    parameters.append("limit", effectiveLimit.toString())
                    parameters.append("offset", offset.toString())

                    season?.let {
                        parameters.append("season", "eq.$it")
                    }

                    competitionId?.let {
                        if (it.isNotBlank()) {
                            parameters.append("competition_id", "ilike.$it")
                        }
                    }

                    status?.let {
                        if (it.isNotBlank()) {
                            parameters.append("status", "eq.$it")
                        }
                    }

                    excludeStatus?.let {
                        if (it.isNotBlank()) {
                            parameters.append("status", "neq.$it")
                        }
                    }

                    startDate?.let {
                        parameters.append("utc_date", "gte.$it")
                    }

                    endDate?.let {
                        parameters.append("utc_date", "lte.$it")
                    }

                    if (hasHighlight) {
                        parameters.append("highlight_video_id", "not.is.null")
                    }

                    query?.let { q ->
                        if (q.isNotBlank()) {
                            val cleanQuery = q.trim()
                            parameters.append(
                                "or",
                                "(home_team_name.ilike.*$cleanQuery*,away_team_name.ilike.*$cleanQuery*,competition_name.ilike.*$cleanQuery*)",
                            )
                        }
                    }
                }
            }
            if (response.status.value == 200) {
                NetworkResult.Success(response.body())
            } else {
                NetworkResult.Error(response.status.value, "Error fetching matches from Oracle VM")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    override suspend fun getMatch(id: Long): NetworkResult<MatchDto> {
        return try {
            val response = client.get("$databaseUrl/rest/v1/matches") {
                header("apikey", databaseApiKey)
                header("Authorization", "Bearer $databaseApiKey")

                url {
                    parameters.append("select", "*")
                    parameters.append("id", "eq.$id")
                }
            }
            if (response.status.value == 200) {
                val list: List<MatchDto> = response.body()
                val match = list.firstOrNull()
                if (match != null) {
                    NetworkResult.Success(match)
                } else {
                    NetworkResult.Error(404, "Match $id not found")
                }
            } else {
                NetworkResult.Error(response.status.value, "Failed to fetch match $id")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    override suspend fun getHighlightPage(
        competition: String?,
        season: Int,
        cursor: String?,
        query: String,
    ): NetworkResult<CatalogPageDto> {
        return try {
            val response = client.get("https://fc-edge-proxy.dirzaaulia11.workers.dev/api/v2/feed") {
                parameter("limit", 20)
                if (query.isNotBlank()) parameter("q", query)
                parameter("season", season)
                competition?.let { parameter("competition", it) }
                cursor?.let { parameter("cursor", it) }
                parameter("include_unavailable", "true")
            }
            if (response.status.value == 200) {
                NetworkResult.Success(response.body())
            } else {
                NetworkResult.Error(response.status.value, "Highlights unavailable; retry this page.")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    override suspend fun getStandings(
        competitionId: String,
        season: Int?,
    ): NetworkResult<List<StandingDto>> {
        return try {
            val effSeason = season ?: 2026
            val response = client.get("$databaseUrl/rest/v1/standings") {
                header("apikey", databaseApiKey)
                header("Authorization", "Bearer $databaseApiKey")

                url {
                    parameters.append("select", "*")
                    parameters.append("competition_id", "ilike.$competitionId")
                    parameters.append("season", "eq.$effSeason")
                    parameters.append("order", "position.asc")
                }
            }
            if (response.status.value == 200) {
                NetworkResult.Success(response.body())
            } else {
                NetworkResult.Error(response.status.value, "Error fetching standings")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    override suspend fun refreshCompetitions() {
        try {
            val response = client.get("$databaseUrl/rest/v1/competitions") {
                header("apikey", databaseApiKey)
                header("Authorization", "Bearer $databaseApiKey")

                url {
                    parameters.append("select", "id,name,short_name,country_code,emblem_url,league_id,is_active,display_order,type")
                    parameters.append("order", "display_order.asc")
                }
            }
            if (response.status.value == 200) {
                val list: List<BigLeague> = response.body()
                if (list.isNotEmpty()) {
                    _competitions.value = list
                }
            }
        } catch (_: Exception) {
        }
    }

    override suspend fun getLiveMatches(): NetworkResult<List<MatchDto>> {
        return try {
            val response = client.get("$databaseUrl/rest/v1/matches") {
                header("apikey", databaseApiKey)
                header("Authorization", "Bearer $databaseApiKey")
                url {
                    parameters.append("select", "id,competition_id,competition_name,utc_date,status,matchday,home_team_id,home_team_name,home_team_crest,away_team_id,away_team_name,away_team_crest,home_score,away_score,highlight_video_id,backup_highlight_video_id,season,round_name,display_clock,youtube_region_info,backup_youtube_region_info")
                    parameters.append("or", "(status.eq.IN_PLAY,status.eq.PAUSED,status.eq.LIVE)")
                    parameters.append("order", "utc_date.asc,id.asc")
                    parameters.append("limit", "50")
                }
            }
            if (response.status.value == 200) {
                NetworkResult.Success(response.body())
            } else {
                NetworkResult.Error(response.status.value, "Error fetching live matches")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    override suspend fun refreshClubs() {
        try {
            val response = client.get("$databaseUrl/rest/v1/supported_clubs") {
                header("apikey", databaseApiKey)
                header("Authorization", "Bearer $databaseApiKey")

                url {
                    parameters.append("select", "id,name,short_name,competition_code,competition_name,crest_url,aliases")
                    parameters.append("order", "name.asc")
                }
            }
            if (response.status.value == 200) {
                val list: List<SupportedClub> = response.body()
                if (list.isNotEmpty()) {
                    _supportedClubs.value = list
                }
            }
        } catch (_: Exception) {
        }
    }

    override fun findClubById(id: String): SupportedClub? {
        return _supportedClubs.value.firstOrNull { it.id == id }
    }
}
