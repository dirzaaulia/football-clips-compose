package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.constants.SupportedClub
import kotlinx.coroutines.flow.StateFlow

interface ClubRepository {
    val supportedClubs: StateFlow<List<SupportedClub>>
    suspend fun refreshClubs()
    fun findClubById(id: String): SupportedClub?
}
