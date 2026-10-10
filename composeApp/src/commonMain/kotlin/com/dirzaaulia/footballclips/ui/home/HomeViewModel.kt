package com.dirzaaulia.footballclips.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.data.admob.AdMobManager
import com.dirzaaulia.footballclips.data.billing.BillingManager
import com.dirzaaulia.footballclips.data.local.PreferenceManager
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.data.model.remote.Profile
import com.dirzaaulia.footballclips.data.repository.HighlightRepository
import com.dirzaaulia.footballclips.data.repository.ProfilesRepository
import com.dirzaaulia.footballclips.data.repository.ScoreRepository
import com.dirzaaulia.footballclips.domain.model.Match
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    internal val highlightRepository: HighlightRepository,
    internal val scoreRepository: ScoreRepository,
    internal val preferenceManager: PreferenceManager,
    internal val billingManager: BillingManager,
    internal val adMobManager: AdMobManager,
    internal val profilesRepository: ProfilesRepository,
) : ViewModel() {

    internal val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    internal val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    val offerings: StateFlow<Any?> = billingManager.offerings
    internal val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    internal val _selectedLeagueId = MutableStateFlow<Int?>(null)
    val selectedLeagueId: StateFlow<Int?> = _selectedLeagueId.asStateFlow()
    internal val _selectedCompetitionId = MutableStateFlow<String?>(null)

    internal val _selectedItem = MutableStateFlow<HighlightUiItem?>(null)
    val selectedItem = _selectedItem.asStateFlow()
    internal val _relatedHighlights = MutableStateFlow<List<HighlightUiItem>>(emptyList())
    val relatedHighlights = _relatedHighlights.asStateFlow()
    internal val _relatedFixturesFinished = MutableStateFlow<List<HighlightUiItem>>(emptyList())
    val relatedFixturesFinished = _relatedFixturesFinished.asStateFlow()
    internal val _relatedFixturesUpcoming = MutableStateFlow<List<HighlightUiItem>>(emptyList())
    val relatedFixturesUpcoming = _relatedFixturesUpcoming.asStateFlow()
    internal val _showExternalHighlights = MutableStateFlow(false)
    val showExternalHighlights = _showExternalHighlights.asStateFlow()
    internal val _showScrollToTop = MutableStateFlow(false)
    val showScrollToTop = _showScrollToTop.asStateFlow()
    internal val _scrollToTopEvent = MutableSharedFlow<Unit>()
    val scrollToTopEvent = _scrollToTopEvent.asSharedFlow()
    internal val _showFilterSheet = MutableStateFlow(false)
    val showFilterSheet = _showFilterSheet.asStateFlow()
    internal val _showMyClubsSheet = MutableStateFlow(false)
    val showMyClubsSheet = _showMyClubsSheet.asStateFlow()

    fun setScrollToTopVisible(v: Boolean) { _showScrollToTop.value = v }
    fun triggerScrollToTop() { viewModelScope.launch { _scrollToTopEvent.emit(Unit) } }
    fun setFilterSheetVisible(v: Boolean) { _showFilterSheet.value = v }
    fun setMyClubsSheetVisible(v: Boolean) { _showMyClubsSheet.value = v }

    internal var currentProfile: Profile? = null
    internal val _isPendingInterstitial = MutableStateFlow(false)

    val isDebugPremium = preferenceManager.isDebugPremium.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val isDarkMode = preferenceManager.isDarkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val isSpoilerFreeMode = preferenceManager.isSpoilerFreeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val isSpoilerFreeConfigured = preferenceManager.isSpoilerFreeConfigured.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val isForceNonPremium = preferenceManager.isForceNonPremium.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isPremium: StateFlow<Boolean> = combine(
        billingManager.isPremium,
        billingManager.customerInfo,
        profilesRepository.profile,
        preferenceManager.isDebugPremium,
        preferenceManager.isForceNonPremium,
    ) { p, cInfo, sp, dp, fnp ->
        if (fnp) {
            false
        } else if (dp) {
            true
        } else if (cInfo != null && !cInfo.isAnonymous && !cInfo.isActive) {
            false
        } else {
            p || sp?.isPremium == true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isAdsRemoved: StateFlow<Boolean> = combine(
        preferenceManager.isAdsRemoved,
        isPremium,
        preferenceManager.isForceNonPremium,
    ) { local, premium, fnp ->
        if (fnp) false else local || premium
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val currentUserProfile = profilesRepository.profile
    val customerInfo = billingManager.customerInfo
    val favoriteClubs: StateFlow<Set<String>> = preferenceManager.favoriteClubs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    internal val _isVerifyingAuth = MutableStateFlow(false)
    val isBillingLoading: StateFlow<Boolean> = combine(billingManager.isLoading, _isVerifyingAuth) { b, a -> b || a }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    internal val limit = 40
    internal var currentOffset = 0
    internal var totalCount = 0
    internal var isFetching = false
    internal val supabaseLimit = 20
    internal var supabaseOffset = 0
    internal var hasMoreSupabase = true

    internal val supabaseMatches = mutableListOf<Match>()
    internal val allHighlights = mutableListOf<HighlightUiModel>()
    internal var selectedCountries = mutableSetOf<String>()
    internal var selectedLeagues = mutableSetOf<String>()
    internal var searchQuery = ""

    init {
        observeBillingErrors()
        observeAdsRemoved()
        observeProfileForBilling()
        refreshData()
    }
}
