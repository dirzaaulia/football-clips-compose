package com.dirzaaulia.footballclips.ui.home

import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.data.billing.BillingManager
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.repository.ProfilesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

fun HomeViewModel.refreshData() {
    viewModelScope.launch {
        _uiState.value = HomeState.Loading
        getSupabaseHighlights()
        if (_showExternalHighlights.value || supabaseMatches.isEmpty()) {
            getExternalHighlights(isRefresh = true)
        } else {
            updateUiState()
        }
    }
}

fun HomeViewModel.selectLeague(leagueId: Int?, competitionId: String?) {
    _selectedLeagueId.value = leagueId
    _selectedCompetitionId.value = competitionId
    selectedCountries.clear()
    selectedLeagues.clear()
    searchQuery = ""
    refreshData()
}

fun HomeViewModel.selectVideoById(itemId: String) = selectVideo(findVideoById(supabaseMatches, allHighlights, itemId))

fun HomeViewModel.selectVideo(item: HighlightUiItem?) {
    _selectedItem.value = item
    _relatedFixturesFinished.value = emptyList()
    _relatedFixturesUpcoming.value = emptyList()
    if (item == null) {
        _relatedHighlights.value = emptyList()
        return
    }
    val (related, compId) = calculateRelatedHighlights(item, supabaseMatches, allHighlights)
    _relatedHighlights.value = related
    if (compId != null) viewModelScope.launch { fetchLeagueFixtures(compId) }
}

fun HomeViewModel.loadMore() {
    if (isFetching || (_uiState.value as? HomeState.Success)?.loadMoreError != null) return
    viewModelScope.launch {
        isFetching = true
        try {
            if (hasMoreSupabase) {
                val cs = _uiState.value
                if (cs is HomeState.Success) _uiState.value = cs.copy(isLoadingMore = true, loadMoreError = null)
                getSupabaseHighlights(false)
                updateUiState()
            } else {
                getExternalHighlightsInternal(false)
            }
        } catch (e: Exception) {
            val cs = _uiState.value
            if (cs is HomeState.Success) {
                _uiState.value = cs.copy(isLoadingMore = false, loadMoreError = "Network error. Please try again.")
            }
        } finally {
            isFetching = false
        }
    }
}

fun HomeViewModel.retryLoadMore() {
    val cs = _uiState.value
    if (cs is HomeState.Success) {
        _uiState.value = cs.copy(loadMoreError = null)
        loadMore()
    }
}

fun HomeViewModel.toggleExternalHighlights() {
    _showExternalHighlights.value = !_showExternalHighlights.value
    if (_showExternalHighlights.value) {
        if (allHighlights.isEmpty()) getExternalHighlights(isRefresh = true) else loadMore()
    } else {
        updateUiState()
    }
}

fun HomeViewModel.getExternalHighlights(isRefresh: Boolean = false) {
    if (isFetching) return
    viewModelScope.launch {
        isFetching = true
        try {
            getExternalHighlightsInternal(isRefresh)
        } catch (e: Exception) {
            handleNetworkError(isRefresh, "Network error. Please try again.")
        } finally {
            isFetching = false
        }
    }
}

fun HomeViewModel.toggleCountry(country: String) {
    if (country in selectedCountries) selectedCountries.remove(country) else selectedCountries.add(country)
    updateUiState()
}

fun HomeViewModel.toggleLeague(league: String) {
    if (league in selectedLeagues) selectedLeagues.remove(league) else selectedLeagues.add(league)
    updateUiState()
}

fun HomeViewModel.onSearchQueryChanged(query: String) {
    searchQuery = query
    updateUiState()
}

fun HomeViewModel.resetFilters() {
    selectedCountries.clear()
    selectedLeagues.clear()
    searchQuery = ""
    _selectedLeagueId.value = null
    _selectedCompetitionId.value = null
    refreshData()
}

fun HomeViewModel.applyFilters() {
    updateUiState()
}

fun HomeViewModel.signIn() = viewModelScope.launch { profilesRepository.signInWithGoogle() }

fun HomeViewModel.signOut() = viewModelScope.launch { profilesRepository.signOut() }

fun HomeViewModel.purchasePackage(pkg: Any) = handlePurchasePackage(viewModelScope, pkg, billingManager, profilesRepository)

fun HomeViewModel.restorePurchases() = handleRestorePurchases(viewModelScope, billingManager, profilesRepository) { viewModelScope.launch { _errorMessage.emit(it) } }

fun HomeViewModel.showInterstitial(onDismiss: () -> Unit) =
    if (isAdsRemoved.value) onDismiss() else adMobManager.showInterstitial(onDismiss)

fun HomeViewModel.openAdInspector() = adMobManager.openAdInspector()

fun HomeViewModel.setPendingInterstitial(p: Boolean) { _isPendingInterstitial.value = p }

fun HomeViewModel.consumePendingInterstitial() {
    if (_isPendingInterstitial.value) {
        _isPendingInterstitial.value = false
        showInterstitial {}
    }
}

internal fun handlePurchasePackage(
    scope: CoroutineScope,
    packageToPurchase: Any,
    billingManager: BillingManager,
    profilesRepository: ProfilesRepository,
) {
    billingManager.purchasePackage(packageToPurchase) { isSuccess ->
        if (isSuccess) {
            scope.launch {
                val profile = profilesRepository.profile.firstOrNull()
                if (profile != null) {
                    profilesRepository.updatePremiumStatus(true)
                }
            }
        }
    }
}

internal fun handleRestorePurchases(
    scope: CoroutineScope,
    billingManager: BillingManager,
    profilesRepository: ProfilesRepository,
    onError: suspend (String) -> Unit,
) {
    billingManager.restorePurchases { isRestoredPremium ->
        if (isRestoredPremium) {
            scope.launch {
                val profile = profilesRepository.profile.firstOrNull()
                if (profile != null) {
                    profilesRepository.updatePremiumStatus(true)
                } else {
                    onError("Purchases restored on Google Play! Sign in with Google to sync your Premium status across all platforms.")
                }
            }
        }
    }
}
