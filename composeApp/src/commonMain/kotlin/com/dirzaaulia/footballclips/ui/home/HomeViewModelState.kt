package com.dirzaaulia.footballclips.ui.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal fun HomeViewModel.observeProfileForBilling() {
    viewModelScope.launch {
        profilesRepository.profile.collectLatest { profile ->
            currentProfile = profile
            _isVerifyingAuth.value = false
            if (profile != null) {
                billingManager.identify(profile.id)
            } else {
                billingManager.logOut()
            }
        }
    }

    viewModelScope.launch {
        combine(
            profilesRepository.profile,
            billingManager.customerInfo
        ) { profile, customerInfo ->
            Pair(profile, customerInfo)
        }.collectLatest { (profile, customerInfo) ->
            if (profile != null && customerInfo != null) {
                if (!customerInfo.isAnonymous && customerInfo.userId == profile.id) {
                    if (customerInfo.isActive != profile.isPremium) {
                        profilesRepository.updatePremiumStatus(customerInfo.isActive)
                    }
                }
            }
        }
    }
}

internal fun HomeViewModel.observeAdsRemoved() {
    viewModelScope.launch {
        isAdsRemoved.collectLatest { removed ->
            if (_uiState.value is HomeState.Success && supabaseMatches.isNotEmpty()) updateUiState(removed = removed)
        }
    }
}

internal fun HomeViewModel.observeBillingErrors() {
    merge(billingManager.errorEvent, billingManager.messageEvent).onEach { _errorMessage.emit(it) }.launchIn(viewModelScope)
}

internal fun HomeViewModel.updateUiState(removed: Boolean? = null) {
    viewModelScope.launch(Dispatchers.Default) {
        val (items, itemCount) = buildHomeUiItems(
            supabaseMatches,
            allHighlights,
            searchQuery,
            selectedLeagues,
            selectedCountries,
            _selectedLeagueId.value,
            _selectedCompetitionId.value,
            _showExternalHighlights.value,
        )
        val builtItems = buildListWithAds(items, removed ?: isAdsRemoved.value)
        _uiState.value = HomeState.Success(
            items = builtItems,
            isAdsRemoved = removed ?: isAdsRemoved.value,
            canLoadMore = hasMoreSupabase || (_showExternalHighlights.value && currentOffset < totalCount),
        )
        _filterState.value = extractAllFilters(
            supabaseMatches,
            allHighlights,
            searchQuery,
            selectedCountries,
            selectedLeagues,
            itemCount,
        )
    }
}

fun HomeViewModel.setDebugPremium(isPremium: Boolean) {
    viewModelScope.launch { preferenceManager.setDebugPremium(isPremium) }
}

fun HomeViewModel.setDarkMode(isDark: Boolean) {
    viewModelScope.launch { preferenceManager.setDarkMode(isDark) }
}

fun HomeViewModel.toggleSpoilerFreeMode() {
    viewModelScope.launch { preferenceManager.setSpoilerFreeMode(!isSpoilerFreeMode.value) }
}

fun HomeViewModel.setSpoilerFreePreference(v: Boolean) {
    viewModelScope.launch { preferenceManager.setSpoilerFreeConfigured(true, v) }
}

fun HomeViewModel.setForceNonPremium(isForce: Boolean) {
    viewModelScope.launch { preferenceManager.setForceNonPremium(isForce) }
}

fun HomeViewModel.toggleFavoriteClub(clubId: String) {
    viewModelScope.launch { preferenceManager.toggleFavoriteClub(clubId) }
}

fun HomeViewModel.setVerifyingAuth(verifying: Boolean) {
    _isVerifyingAuth.value = verifying
}

fun HomeViewModel.onSignInSuccess() {
    viewModelScope.launch {
        profilesRepository.refreshProfile()
        currentProfile?.id?.let { uid ->
            billingManager.identify(uid)
            billingManager.syncPurchases()
        }
    }
}
