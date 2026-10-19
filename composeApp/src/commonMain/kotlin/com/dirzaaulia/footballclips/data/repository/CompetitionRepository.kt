package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.constants.BigLeague
import kotlinx.coroutines.flow.StateFlow

interface CompetitionRepository {
    val competitions: StateFlow<List<BigLeague>>
    suspend fun refreshCompetitions()
    fun findLeagueByCompetitionId(competitionId: String): BigLeague?
}
