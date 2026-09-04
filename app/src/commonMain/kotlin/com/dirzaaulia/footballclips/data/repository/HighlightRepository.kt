package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.HighlightResponse

interface HighlightRepository {
    suspend fun getHighlights(
        limit: Int,
        offset: Int,
        season: Int? = null,
        leagueId: String? = null,
        country: String? = null
    ): NetworkResult<HighlightResponse>
}
