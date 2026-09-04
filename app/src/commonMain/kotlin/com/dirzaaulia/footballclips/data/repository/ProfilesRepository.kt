package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.model.remote.Profile
import kotlinx.coroutines.flow.Flow

interface ProfilesRepository {
    val profile: Flow<Profile?>
    suspend fun signInWithSupabase()
    suspend fun signInWithGoogle()
    suspend fun signOut()
}
