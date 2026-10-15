package com.dirzaaulia.footballclips.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.google.firebase.messaging.FirebaseMessaging
import com.dirzaaulia.footballclips.data.telemetry.DeviceTelemetrySync
import io.github.jan.supabase.auth.Auth
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "settings")

actual class PreferenceManager(
    private val context: Context,
    private val deviceTelemetrySync: DeviceTelemetrySync? = null,
    private val auth: Auth? = null
) {

    private val adsRemovedKey = booleanPreferencesKey("is_ads_removed")
    private val darkModeKey = booleanPreferencesKey("is_dark_mode")
    private val debugPremiumKey = booleanPreferencesKey("is_debug_premium")
    private val forceNonPremiumKey = booleanPreferencesKey("is_force_non_premium")
    private val spoilerFreeModeKey = booleanPreferencesKey("is_spoiler_free_mode")
    private val spoilerFreeConfiguredKey = booleanPreferencesKey("is_spoiler_free_configured")
    private val favoriteClubsKey = stringSetPreferencesKey("favorite_clubs")
    private val deviceIdKey = stringPreferencesKey("telemetry_device_id")
    private val anonUserIdKey = stringPreferencesKey("telemetry_anon_user_id")

    private val _inMemoryDarkMode = MutableStateFlow<Boolean?>(null)
    private val _inMemorySpoilerFree = MutableStateFlow<Boolean?>(null)
    private val _inMemorySpoilerConfigured = MutableStateFlow<Boolean?>(null)
    private val _inMemoryFavoriteClubs = MutableStateFlow<Set<String>?>(null)

    actual val isAdsRemoved: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[adsRemovedKey] ?: false
    }

    actual suspend fun setAdsRemoved(removed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[adsRemovedKey] = removed
        }
    }

    actual val isDarkMode: Flow<Boolean> = combine(
        context.dataStore.data.map { preferences -> preferences[darkModeKey] ?: true },
        _inMemoryDarkMode
    ) { fromDisk, inMem ->
        inMem ?: fromDisk
    }

    actual suspend fun setDarkMode(isDark: Boolean) {
        _inMemoryDarkMode.value = isDark
        context.dataStore.edit { preferences ->
            preferences[darkModeKey] = isDark
        }
    }

    actual val isDebugPremium: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[debugPremiumKey] ?: false
    }

    actual suspend fun setDebugPremium(isPremium: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[debugPremiumKey] = isPremium
        }
    }

    actual val isForceNonPremium: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[forceNonPremiumKey] ?: false
    }

    actual suspend fun setForceNonPremium(isForce: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[forceNonPremiumKey] = isForce
        }
    }

    actual val isSpoilerFreeMode: Flow<Boolean> = combine(
        context.dataStore.data.map { preferences -> preferences[spoilerFreeModeKey] ?: false },
        _inMemorySpoilerFree
    ) { fromDisk, inMem ->
        inMem ?: fromDisk
    }

    actual suspend fun setSpoilerFreeMode(enabled: Boolean) {
        _inMemorySpoilerFree.value = enabled
        context.dataStore.edit { preferences ->
            preferences[spoilerFreeModeKey] = enabled
        }
    }

    actual val isSpoilerFreeConfigured: Flow<Boolean?> = combine(
        context.dataStore.data.map { preferences -> preferences[spoilerFreeConfiguredKey] ?: false },
        _inMemorySpoilerConfigured
    ) { fromDisk, inMem ->
        inMem ?: fromDisk
    }

    actual suspend fun setSpoilerFreeConfigured(configured: Boolean, isSpoilerFree: Boolean) {
        _inMemorySpoilerConfigured.value = configured
        _inMemorySpoilerFree.value = isSpoilerFree
        context.dataStore.edit { preferences ->
            preferences[spoilerFreeConfiguredKey] = configured
            preferences[spoilerFreeModeKey] = isSpoilerFree
        }
    }

    actual val favoriteClubs: Flow<Set<String>> = combine(
        context.dataStore.data.map { preferences -> preferences[favoriteClubsKey] ?: emptySet() },
        _inMemoryFavoriteClubs
    ) { fromDisk, inMem ->
        inMem ?: fromDisk
    }

    actual suspend fun setFavoriteClubs(clubs: Set<String>) {
        _inMemoryFavoriteClubs.value = clubs
        context.dataStore.edit { preferences ->
            preferences[favoriteClubsKey] = clubs
        }
    }

    actual suspend fun toggleFavoriteClub(clubId: String) {
        val current = _inMemoryFavoriteClubs.value ?: context.dataStore.data.first()[favoriteClubsKey] ?: emptySet()
        val isAdding = !current.contains(clubId)
        val updated = if (isAdding) current + clubId else current - clubId
        setFavoriteClubs(updated)
        try {
            if (isAdding) {
                FirebaseMessaging.getInstance().subscribeToTopic("club_$clubId")
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("club_$clubId")
            }
        } catch (_: Exception) {
        }
        syncTelemetry(updated)
    }

    private suspend fun syncTelemetry(clubs: Set<String>) {
        if (deviceTelemetrySync == null) return
        try {
            val prefs = context.dataStore.data.first()
            var devId = prefs[deviceIdKey]
            if (devId.isNullOrBlank()) {
                devId = UUID.randomUUID().toString()
                context.dataStore.edit { it[deviceIdKey] = devId }
            }
            val userId = auth?.currentUserOrNull()?.id ?: run {
                var anonId = prefs[anonUserIdKey]
                if (anonId.isNullOrBlank()) {
                    anonId = "anon_" + UUID.randomUUID().toString().take(8)
                    context.dataStore.edit { it[anonUserIdKey] = anonId }
                }
                anonId
            }
            val clubIds = clubs.mapNotNull { it.toIntOrNull() }
            val devName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
            deviceTelemetrySync.syncDevice(
                deviceId = devId,
                userId = userId,
                clubs = clubIds,
                deviceName = devName
            )
        } catch (_: Exception) {
        }
    }
}
