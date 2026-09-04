package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.MatchDto

interface ScoreRepository {
    suspend fun getMatches(
        competitionId: String? = null,
        status: String? = null,
        excludeStatus: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        hasHighlight: Boolean = false,
        ascending: Boolean = false,
        limit: Int = 20,
        offset: Int = 0
    ): NetworkResult<List<MatchDto>>
}
