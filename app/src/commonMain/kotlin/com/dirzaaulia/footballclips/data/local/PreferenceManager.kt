package com.dirzaaulia.footballclips.data.local

import kotlinx.coroutines.flow.Flow

expect class PreferenceManager {
    val isAdsRemoved: Flow<Boolean>
    suspend fun setAdsRemoved(removed: Boolean)

    val isDarkMode: Flow<Boolean>
    suspend fun setDarkMode(isDark: Boolean)

    val isDebugPremium: Flow<Boolean>
    suspend fun setDebugPremium(isPremium: Boolean)
}
