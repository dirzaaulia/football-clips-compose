package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.data.model.remote.SupportedClub
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.CatalogPageDto
import com.dirzaaulia.footballclips.data.model.remote.MatchDto
import com.dirzaaulia.footballclips.data.model.remote.StandingDto
import kotlinx.coroutines.flow.StateFlow

/**
 * Unified Repository interface consolidating all Oracle Cloud VM PostgREST data access
 * (https://edge.fc.dirzaaulia.com).
 */
interface OracleVmRepository {
    suspend fun getMatches(
        competitionId: String? = null,
        status: String? = null,
        excludeStatus: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        hasHighlight: Boolean = false,
        ascending: Boolean = false,
        limit: Int = 20,
        offset: Int = 0,
        season: Int? = null,
        query: String? = null,
    ): NetworkResult<List<MatchDto>>

    suspend fun getMatch(id: Long): NetworkResult<MatchDto>

    suspend fun getLiveMatches(): NetworkResult<List<MatchDto>>

    suspend fun getHighlightPage(
        competition: String? = null,
        season: Int = 2026,
        cursor: String? = null,
        query: String = "",
    ): NetworkResult<CatalogPageDto>

    suspend fun getStandings(
        competitionId: String,
        season: Int? = null,
    ): NetworkResult<List<StandingDto>>

    val competitions: StateFlow<List<BigLeague>>
    suspend fun refreshCompetitions()

    val supportedClubs: StateFlow<List<SupportedClub>>
    suspend fun refreshClubs()
    fun findClubById(id: String): SupportedClub?
}
