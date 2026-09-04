package com.dirzaaulia.footballclips.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

actual class PreferenceManager(private val context: Context) {

    private val adsRemovedKey = booleanPreferencesKey("is_ads_removed")
    private val darkModeKey = booleanPreferencesKey("is_dark_mode")
    private val debugPremiumKey = booleanPreferencesKey("is_debug_premium")

    actual val isAdsRemoved: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[adsRemovedKey] ?: false
    }

    actual suspend fun setAdsRemoved(removed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[adsRemovedKey] = removed
        }
    }

    actual val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[darkModeKey] ?: true
    }

    actual suspend fun setDarkMode(isDark: Boolean) {
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
}
