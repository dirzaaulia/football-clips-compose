package com.dirzaaulia.footballclips.ui.home

import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.data.billing.BillingManager
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.data.repository.ProfilesRepository
import com.dirzaaulia.footballclips.domain.model.toMatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

fun HomeViewModel.refreshData() {
    viewModelScope.launch {
        _uiState.value = HomeState.Loading
        getOracleHighlights(isRefresh = true)
        updateUiState()
        val isArchiveSeason = selectedSeason.value != 2026
        if (!isArchiveSeason && (_showExternalHighlights.value || oracleMatches.isEmpty())) {
            getExternalHighlights(isRefresh = true)
        }
    }
}

fun HomeViewModel.selectLeague(leagueId: Int?, competitionId: String?) {
    _selectedLeagueId.value = leagueId
    _selectedCompetitionId.value = competitionId
    selectedLeagues.clear()
    searchQuery = ""
    refreshData()
}

fun HomeViewModel.selectVideoById(itemId: String) {
    val cached = findVideoById(oracleMatches, allHighlights, itemId)
    if (cached != null) {
        selectVideo(cached)
    } else {
        val matchId = itemId.toLongOrNull() ?: return
        viewModelScope.launch {
            when (val res = oracleVmRepository.getMatch(matchId)) {
                is com.dirzaaulia.footballclips.data.model.NetworkResult.Success -> {
                    selectVideo(com.dirzaaulia.footballclips.data.model.HighlightUiItem.OracleMatch(res.data.toMatch()))
                }
                else -> {}
            }
        }
    }
}

fun HomeViewModel.selectVideo(item: HighlightUiItem?) {
    _selectedItem.value = item
    _relatedFixturesFinished.value = emptyList()
    _relatedFixturesUpcoming.value = emptyList()
    if (item == null) {
        _relatedHighlights.value = emptyList()
        return
    }
    val (related, compId) = calculateRelatedHighlights(item, oracleMatches, allHighlights, competitions.value)
    _relatedHighlights.value = related
    viewModelScope.launch {
        fetchRelatedHighlights(compId, item.uniqueId)
        if (compId != null) {
            fetchLeagueFixtures(compId)
        }
    }
}

fun HomeViewModel.loadMore() {
    if (isFetching || (_uiState.value as? HomeState.Success)?.loadMoreError != null) return
    viewModelScope.launch {
        isFetching = true
        try {
            if (hasMoreOracle) {
                val cs = _uiState.value
                if (cs is HomeState.Success) _uiState.value = cs.copy(isLoadingMore = true, loadMoreError = null)
                getOracleHighlights(false)
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

fun HomeViewModel.toggleLeague(league: String) {
    if (league in selectedLeagues) selectedLeagues.remove(league) else selectedLeagues.add(league)
    updateUiState()
}

fun HomeViewModel.onSearchQueryChanged(query: String) {
    searchQuery = query
    updateUiState()
}

fun HomeViewModel.resetFilters() {
    selectedLeagues.clear()
    searchQuery = ""
    _selectedLeagueId.value = null
    _selectedCompetitionId.value = null
    refreshData()
}

fun HomeViewModel.applyFilters() {
    updateUiState()
    triggerScrollToTop()
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

fun HomeViewModel.fetchLiveMatches() {
    viewModelScope.launch {
        when (val res = oracleVmRepository.getLiveMatches()) {
            is com.dirzaaulia.footballclips.data.model.NetworkResult.Success -> {
                _liveMatches.value = res.data.map { it.toMatch() }
            }
            else -> {}
        }
    }
}
