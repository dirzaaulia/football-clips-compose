package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.StandingDto

interface StandingRepository {
    suspend fun getStandings(competitionId: String): NetworkResult<List<StandingDto>>
}
