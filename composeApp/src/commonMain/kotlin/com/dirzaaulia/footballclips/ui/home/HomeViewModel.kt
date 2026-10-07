package com.dirzaaulia.footballclips.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.data.admob.AdMobManager
import com.dirzaaulia.footballclips.data.billing.BillingManager
import com.dirzaaulia.footballclips.data.constants.BigLeaguesConstants
import com.dirzaaulia.footballclips.data.local.PreferenceManager
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.data.model.remote.Profile
import com.dirzaaulia.footballclips.data.repository.HighlightRepository
import com.dirzaaulia.footballclips.data.repository.ProfilesRepository
import com.dirzaaulia.footballclips.data.repository.ScoreRepository
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.domain.model.toMatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val highlightRepository: HighlightRepository,
    private val scoreRepository: ScoreRepository,
    private val preferenceManager: PreferenceManager,
    private val billingManager: BillingManager,
    private val adMobManager: AdMobManager,
    private val profilesRepository: ProfilesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    val offerings: StateFlow<Any?> = billingManager.offerings
    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _selectedLeagueId = MutableStateFlow<Int?>(null)
    val selectedLeagueId: StateFlow<Int?> = _selectedLeagueId.asStateFlow()
    private val _selectedCompetitionId = MutableStateFlow<String?>(null)

    private val _selectedItem = MutableStateFlow<HighlightUiItem?>(null)
    val selectedItem = _selectedItem.asStateFlow()
    private val _relatedHighlights = MutableStateFlow<List<HighlightUiItem>>(emptyList())
    val relatedHighlights = _relatedHighlights.asStateFlow()
    private val _relatedFixturesFinished = MutableStateFlow<List<HighlightUiItem>>(emptyList())
    val relatedFixturesFinished = _relatedFixturesFinished.asStateFlow()
    private val _relatedFixturesUpcoming = MutableStateFlow<List<HighlightUiItem>>(emptyList())
    val relatedFixturesUpcoming = _relatedFixturesUpcoming.asStateFlow()
    private val _showExternalHighlights = MutableStateFlow(false)
    val showExternalHighlights = _showExternalHighlights.asStateFlow()
    private val _showScrollToTop = MutableStateFlow(false)
    val showScrollToTop = _showScrollToTop.asStateFlow()
    private val _scrollToTopEvent = MutableSharedFlow<Unit>()
    val scrollToTopEvent = _scrollToTopEvent.asSharedFlow()
    private val _showFilterSheet = MutableStateFlow(false)
    val showFilterSheet = _showFilterSheet.asStateFlow()
    private val _showMyClubsSheet = MutableStateFlow(false)
    val showMyClubsSheet = _showMyClubsSheet.asStateFlow()
    fun setScrollToTopVisible(v: Boolean) { _showScrollToTop.value = v }
    fun triggerScrollToTop() { viewModelScope.launch { _scrollToTopEvent.emit(Unit) } }
    fun setFilterSheetVisible(v: Boolean) { _showFilterSheet.value = v }
    fun setMyClubsSheetVisible(v: Boolean) { _showMyClubsSheet.value = v }

    private var currentProfile: Profile? = null
    private val _isPendingInterstitial = MutableStateFlow(false)

    val isDebugPremium = preferenceManager.isDebugPremium.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    fun setDebugPremium(isPremium: Boolean) = viewModelScope.launch { preferenceManager.setDebugPremium(isPremium) }
    val isDarkMode = preferenceManager.isDarkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    fun setDarkMode(isDark: Boolean) = viewModelScope.launch { preferenceManager.setDarkMode(isDark) }
    val isSpoilerFreeMode = preferenceManager.isSpoilerFreeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    fun toggleSpoilerFreeMode() = viewModelScope.launch { preferenceManager.setSpoilerFreeMode(!isSpoilerFreeMode.value) }
    val isSpoilerFreeConfigured = preferenceManager.isSpoilerFreeConfigured.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    fun setSpoilerFreePreference(v: Boolean) = viewModelScope.launch { preferenceManager.setSpoilerFreeConfigured(true, v) }
    val isForceNonPremium = preferenceManager.isForceNonPremium.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    fun setForceNonPremium(isForce: Boolean) = viewModelScope.launch { preferenceManager.setForceNonPremium(isForce) }

    val isPremium: StateFlow<Boolean> = combine(billingManager.isPremium, profilesRepository.profile, preferenceManager.isDebugPremium, preferenceManager.isForceNonPremium) { p, sp, dp, fnp ->
        if (fnp) false else p || sp?.isPremium == true || dp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isAdsRemoved: StateFlow<Boolean> = combine(preferenceManager.isAdsRemoved, isPremium, preferenceManager.isForceNonPremium) { local, premium, fnp ->
        if (fnp) false else local || premium
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentUserProfile = profilesRepository.profile
    val customerInfo = billingManager.customerInfo
    val favoriteClubs: StateFlow<Set<String>> = preferenceManager.favoriteClubs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    fun toggleFavoriteClub(clubId: String) = viewModelScope.launch { preferenceManager.toggleFavoriteClub(clubId) }

    private val _isVerifyingAuth = MutableStateFlow(false)
    val isBillingLoading: StateFlow<Boolean> = combine(billingManager.isLoading, _isVerifyingAuth) { b, a -> b || a }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    fun setVerifyingAuth(verifying: Boolean) { _isVerifyingAuth.value = verifying }

    private val limit = 40; private var currentOffset = 0; private var totalCount = 0; private var isFetching = false
    private val supabaseLimit = 20; private var supabaseOffset = 0; private var hasMoreSupabase = true

    private val supabaseMatches = mutableListOf<Match>(); private val allHighlights = mutableListOf<HighlightUiModel>()
    private var selectedCountries = mutableSetOf<String>(); private var selectedLeagues = mutableSetOf<String>(); private var searchQuery = ""

    init {
        observeBillingErrors(); observeAdsRemoved(); observeProfileForBilling(); refreshData()
    }

    fun onSignInSuccess() { viewModelScope.launch { profilesRepository.refreshProfile() } }

    private fun observeProfileForBilling() {
        viewModelScope.launch {
            profilesRepository.profile.collectLatest { profile ->
                currentProfile = profile; _isVerifyingAuth.value = false
                if (profile != null) billingManager.identify(profile.id) else billingManager.logOut()
            }
        }
    }

    private fun observeAdsRemoved() {
        viewModelScope.launch {
            isAdsRemoved.collectLatest { removed ->
                if (_uiState.value is HomeState.Success && supabaseMatches.isNotEmpty()) updateUiState(removed = removed)
            }
        }
    }

    private fun observeBillingErrors() {
        merge(billingManager.errorEvent, billingManager.messageEvent).onEach { _errorMessage.emit(it) }.launchIn(viewModelScope)
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = HomeState.Loading
            getSupabaseHighlights()
            if (_showExternalHighlights.value || supabaseMatches.isEmpty()) getExternalHighlights(isRefresh = true)
            else updateUiState()
        }
    }

    fun selectLeague(leagueId: Int?, competitionId: String?) {
        _selectedLeagueId.value = leagueId; _selectedCompetitionId.value = competitionId
        selectedCountries.clear(); selectedLeagues.clear(); searchQuery = ""; refreshData()
    }

    fun selectVideoById(itemId: String) = selectVideo(findVideoById(supabaseMatches, allHighlights, itemId))

    fun selectVideo(item: HighlightUiItem?) {
        _selectedItem.value = item; _relatedFixturesFinished.value = emptyList(); _relatedFixturesUpcoming.value = emptyList()
        if (item == null) { _relatedHighlights.value = emptyList(); return }
        val (related, compId) = calculateRelatedHighlights(item, supabaseMatches, allHighlights)
        _relatedHighlights.value = related
        if (compId != null) viewModelScope.launch { fetchLeagueFixtures(compId) }
    }

    private suspend fun fetchLeagueFixtures(compId: String) {
        val f = scoreRepository.getMatches(competitionId = compId, status = "FINISHED", ascending = false)
        if (f is NetworkResult.Success) _relatedFixturesFinished.value = f.data.map { HighlightUiItem.SupabaseMatch(it.toMatch()) }
        val u = scoreRepository.getMatches(competitionId = compId, excludeStatus = "FINISHED", ascending = true)
        if (u is NetworkResult.Success) _relatedFixturesUpcoming.value = u.data.map { it.toMatch() }.filter { it.isScheduled }.map { HighlightUiItem.SupabaseMatch(it) }
    }

    private suspend fun getSupabaseHighlights(isRefresh: Boolean = true) {
        if (isRefresh) { supabaseOffset = 0; supabaseMatches.clear(); hasMoreSupabase = true }
        if (!hasMoreSupabase) return
        val (newM, hasMore) = fetchSupabaseMatches(scoreRepository, _selectedCompetitionId.value, supabaseLimit, supabaseOffset)
        val exIds = supabaseMatches.map { it.id }.toSet()
        supabaseMatches.addAll(newM.filter { it.id !in exIds })
        hasMoreSupabase = hasMore
        supabaseOffset += supabaseLimit
    }

    private suspend fun getExternalHighlightsInternal(isRefresh: Boolean = false) {
        val s = _uiState.value
        if (s is HomeState.Success) _uiState.value = s.copy(isLoadingMore = true, loadMoreError = null)
        if (isRefresh) { currentOffset = 0; allHighlights.clear() }
        val res = fetchExternalHighlightsData(highlightRepository, limit, currentOffset, _selectedLeagueId.value)
        if (res != null) {
            totalCount = res.second; allHighlights.addAll(res.first); currentOffset += limit; updateUiState()
        } else handleNetworkError(isRefresh, "Failed to load external highlights")
    }

    fun loadMore() {
        if (isFetching || (_uiState.value as? HomeState.Success)?.loadMoreError != null) return
        viewModelScope.launch {
            isFetching = true
            try {
                if (hasMoreSupabase) {
                    val cs = _uiState.value
                    if (cs is HomeState.Success) _uiState.value = cs.copy(isLoadingMore = true, loadMoreError = null)
                    getSupabaseHighlights(false); updateUiState()
                } else if (_showExternalHighlights.value) getExternalHighlightsInternal(false)
            } catch (e: Exception) {
                val cs = _uiState.value
                if (cs is HomeState.Success) _uiState.value = cs.copy(isLoadingMore = false, loadMoreError = "Network error. Please try again.")
            } finally { isFetching = false }
        }
    }
    
    fun retryLoadMore() {
        val cs = _uiState.value
        if (cs is HomeState.Success) { _uiState.value = cs.copy(loadMoreError = null); loadMore() }
    }

    fun toggleExternalHighlights() {
        _showExternalHighlights.value = !_showExternalHighlights.value
        if (_showExternalHighlights.value) {
            if (allHighlights.isEmpty()) getExternalHighlights(isRefresh = true) else loadMore()
        } else updateUiState()
    }

    fun getExternalHighlights(isRefresh: Boolean = false) {
        if (isFetching) return
        viewModelScope.launch {
            isFetching = true
            try { getExternalHighlightsInternal(isRefresh) }
            catch (e: Exception) { handleNetworkError(isRefresh, "Network error. Please try again.") }
            finally { isFetching = false }
        }
    }

    private fun handleNetworkError(isRefresh: Boolean, message: String) {
        val s = _uiState.value
        if (s is HomeState.Success) { _uiState.value = s.copy(isLoadingMore = false, loadMoreError = message); viewModelScope.launch { _errorMessage.emit(message) } }
        else if (isRefresh) _uiState.value = HomeState.Error(message)
        else viewModelScope.launch { _errorMessage.emit(message) }
    }

    private fun updateUiState(removed: Boolean? = null) {
        viewModelScope.launch(Dispatchers.Default) {
            val (items, itemCount) = buildHomeUiItems(supabaseMatches, allHighlights, searchQuery, selectedLeagues, selectedCountries, _selectedLeagueId.value, _selectedCompetitionId.value, _showExternalHighlights.value)
            val builtItems = buildListWithAds(items, removed ?: isAdsRemoved.value)
            _uiState.value = HomeState.Success(items = builtItems, isAdsRemoved = removed ?: isAdsRemoved.value, canLoadMore = hasMoreSupabase || (_showExternalHighlights.value && currentOffset < totalCount))
            _filterState.value = extractAllFilters(supabaseMatches, allHighlights, searchQuery, selectedCountries, selectedLeagues, itemCount)
        }
    }

    fun toggleCountry(country: String) { if (country in selectedCountries) selectedCountries.remove(country) else selectedCountries.add(country); updateUiState() }
    fun toggleLeague(league: String) { if (league in selectedLeagues) selectedLeagues.remove(league) else selectedLeagues.add(league); updateUiState() }
    fun onSearchQueryChanged(query: String) { searchQuery = query; updateUiState() }
    fun resetFilters() { selectedCountries.clear(); selectedLeagues.clear(); searchQuery = ""; _selectedLeagueId.value = null; _selectedCompetitionId.value = null; refreshData() }
    fun applyFilters() { updateUiState() }
    fun signIn() = viewModelScope.launch { profilesRepository.signInWithGoogle() }
    fun purchasePackage(pkg: Any) = handlePurchasePackage(viewModelScope, pkg, billingManager, profilesRepository)
    fun restorePurchases() = handleRestorePurchases(viewModelScope, billingManager, profilesRepository) { _errorMessage.emit(it) }
    fun showInterstitial(onDismiss: () -> Unit) = if (isAdsRemoved.value) onDismiss() else adMobManager.showInterstitial(onDismiss)
    fun openAdInspector() = adMobManager.openAdInspector()
    fun setPendingInterstitial(p: Boolean) { _isPendingInterstitial.value = p }
    fun consumePendingInterstitial() { if (_isPendingInterstitial.value) { _isPendingInterstitial.value = false; showInterstitial { } } }
}
