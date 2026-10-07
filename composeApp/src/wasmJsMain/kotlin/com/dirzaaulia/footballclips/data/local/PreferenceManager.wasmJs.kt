package com.dirzaaulia.footballclips.data.local

import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

actual class PreferenceManager {
    actual val isAdsRemoved: Flow<Boolean> = flowOf(false)
    actual suspend fun setAdsRemoved(removed: Boolean) {}

    private val _darkModeFlow = MutableStateFlow(
        runCatching { window.localStorage.getItem("is_dark_mode") != "false" }.getOrDefault(true)
    )
    actual val isDarkMode: Flow<Boolean> = _darkModeFlow

    actual suspend fun setDarkMode(isDark: Boolean) {
        runCatching { window.localStorage.setItem("is_dark_mode", isDark.toString()) }
        _darkModeFlow.value = isDark
    }

    actual val isDebugPremium: Flow<Boolean> = flowOf(false)
    actual suspend fun setDebugPremium(isPremium: Boolean) {}

    actual val isForceNonPremium: Flow<Boolean> = flowOf(false)
    actual suspend fun setForceNonPremium(isForce: Boolean) {}

    private val _spoilerFreeFlow = MutableStateFlow(
        runCatching { window.localStorage.getItem("is_spoiler_free_mode") == "true" }.getOrDefault(false)
    )
    actual val isSpoilerFreeMode: Flow<Boolean> = _spoilerFreeFlow

    actual suspend fun setSpoilerFreeMode(enabled: Boolean) {
        runCatching {
            window.localStorage.setItem("is_spoiler_free_mode", enabled.toString())
            window.localStorage.setItem("is_spoiler_free_configured", "true")
        }
        _spoilerFreeFlow.value = enabled
        _spoilerConfiguredFlow.value = true
    }

    private val _spoilerConfiguredFlow = MutableStateFlow<Boolean?>(
        runCatching {
            val item = window.localStorage.getItem("is_spoiler_free_configured")
            if (item != null) item == "true" else false
        }.getOrDefault(false)
    )
    actual val isSpoilerFreeConfigured: Flow<Boolean?> = _spoilerConfiguredFlow

    actual suspend fun setSpoilerFreeConfigured(configured: Boolean, isSpoilerFree: Boolean) {
        runCatching {
            window.localStorage.setItem("is_spoiler_free_configured", configured.toString())
            window.localStorage.setItem("is_spoiler_free_mode", isSpoilerFree.toString())
        }
        _spoilerConfiguredFlow.value = configured
        _spoilerFreeFlow.value = isSpoilerFree
    }

    private val _favoriteClubsFlow = MutableStateFlow<Set<String>>(
        runCatching {
            window.localStorage.getItem("favorite_clubs")?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        }.getOrDefault(emptySet())
    )
    actual val favoriteClubs: Flow<Set<String>> = _favoriteClubsFlow

    actual suspend fun setFavoriteClubs(clubs: Set<String>) {
        runCatching {
            window.localStorage.setItem("favorite_clubs", clubs.joinToString(","))
        }
        _favoriteClubsFlow.value = clubs
    }

    actual suspend fun toggleFavoriteClub(clubId: String) {
        val current = _favoriteClubsFlow.value
        val updated = if (current.contains(clubId)) current - clubId else current + clubId
        setFavoriteClubs(updated)
    }
}
