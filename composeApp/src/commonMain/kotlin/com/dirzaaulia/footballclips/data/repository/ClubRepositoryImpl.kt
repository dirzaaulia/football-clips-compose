package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.constants.SupabaseConstants
import com.dirzaaulia.footballclips.data.constants.SupportedClub
import com.dirzaaulia.footballclips.data.constants.SupportedClubsConstants
import com.dirzaaulia.footballclips.data.model.remote.ClubDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClubRepositoryImpl(private val client: HttpClient) : ClubRepository {

    private val baseUrl = "${SupabaseConstants.SUPABASE_URL}/rest/v1/supported_clubs"
    private val supabaseKey = SupabaseConstants.SUPABASE_ANON_KEY

    private val _supportedClubs = MutableStateFlow<List<SupportedClub>>(SupportedClubsConstants.allClubs)
    override val supportedClubs: StateFlow<List<SupportedClub>> = _supportedClubs.asStateFlow()

    override suspend fun refreshClubs() {
        try {
            val response = client.get(baseUrl) {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                url {
                    parameters.append("is_active", "eq.true")
                    parameters.append("order", "name.asc")
                }
            }
            val dtoList: List<ClubDto> = response.body()
            if (dtoList.isNotEmpty()) {
                val mapped = dtoList.map { dto ->
                    SupportedClub(
                        id = dto.id,
                        name = dto.name,
                        shortName = dto.shortName ?: dto.name.take(15),
                        leagueCode = dto.competitionCode,
                        leagueName = dto.competitionName,
                        crestUrl = dto.crestUrl ?: "https://crests.football-data.org/${dto.teamId}.png",
                        aliases = dto.aliases
                    )
                }
                _supportedClubs.value = mapped
            }
        } catch (_: Exception) {
            // Gracefully keep current/fallback clubs
        }
    }

    override fun findClubById(id: String): SupportedClub? {
        return _supportedClubs.value.find { it.id == id } ?: SupportedClubsConstants.findClubById(id)
    }
}
