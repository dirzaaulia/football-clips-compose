package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.constants.BigLeague
import com.dirzaaulia.footballclips.data.constants.BigLeaguesConstants
import com.dirzaaulia.footballclips.data.constants.DatabaseConstants
import com.dirzaaulia.footballclips.data.model.remote.CompetitionDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CompetitionRepositoryImpl(private val client: HttpClient) : CompetitionRepository {

    private val baseUrl = "${DatabaseConstants.DATABASE_URL}/rest/v1/competitions"
    private val databaseApiKey = DatabaseConstants.DATABASE_ANON_KEY

    private val _competitions = MutableStateFlow<List<BigLeague>>(BigLeaguesConstants.leagues)
    override val competitions: StateFlow<List<BigLeague>> = _competitions.asStateFlow()

    override suspend fun refreshCompetitions() {
        try {
            val response = client.get(baseUrl) {
                header("apikey", databaseApiKey)
                header("Authorization", "Bearer $databaseApiKey")
                url {
                    parameters.append("is_active", "eq.true")
                    parameters.append("order", "display_order.asc")
                }
            }
            val dtoList: List<CompetitionDto> = response.body()
            if (dtoList.isNotEmpty()) {
                val allLeagues = BigLeague(
                    name = "All Leagues",
                    leagueId = null,
                    countryCode = null,
                    competitionId = null,
                    logo = ""
                )
                val mapped = listOf(allLeagues) + dtoList.map { dto ->
                    BigLeague(
                        name = dto.name,
                        leagueId = dto.leagueId,
                        countryCode = dto.countryCode,
                        competitionId = dto.id,
                        logo = dto.emblemUrl ?: ""
                    )
                }
                _competitions.value = mapped
            }
        } catch (_: Exception) {
            // Gracefully keep fallback BigLeaguesConstants.leagues
        }
    }

    override fun findLeagueByCompetitionId(competitionId: String): BigLeague? {
        return _competitions.value.find { it.competitionId.equals(competitionId, ignoreCase = true) }
    }
}
