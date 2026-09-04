package com.dirzaaulia.footballclips.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual class PreferenceManager {
    actual val isAdsRemoved: Flow<Boolean> = flowOf(false)
    actual suspend fun setAdsRemoved(removed: Boolean) {}

    actual val isDarkMode: Flow<Boolean> = flowOf(true)
    actual suspend fun setDarkMode(isDark: Boolean) {}

    actual val isDebugPremium: Flow<Boolean> = flowOf(false)
    actual suspend fun setDebugPremium(isPremium: Boolean) {}
}
