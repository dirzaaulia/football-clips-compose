package com.dirzaaulia.footballclips.data.local

import kotlinx.coroutines.flow.Flow

expect class PreferenceManager {
    val isAdsRemoved: Flow<Boolean>
    suspend fun setAdsRemoved(removed: Boolean)

    val isDarkMode: Flow<Boolean>
    suspend fun setDarkMode(isDark: Boolean)

    val isDebugPremium: Flow<Boolean>
    suspend fun setDebugPremium(isPremium: Boolean)

    val isForceNonPremium: Flow<Boolean>
    suspend fun setForceNonPremium(isForce: Boolean)

    val isSpoilerFreeMode: Flow<Boolean>
    suspend fun setSpoilerFreeMode(enabled: Boolean)

    val isSpoilerFreeConfigured: Flow<Boolean?>
    suspend fun setSpoilerFreeConfigured(configured: Boolean, isSpoilerFree: Boolean)

    val favoriteClubs: Flow<Set<String>>
    suspend fun setFavoriteClubs(clubs: Set<String>)
    suspend fun toggleFavoriteClub(clubId: String)
}
