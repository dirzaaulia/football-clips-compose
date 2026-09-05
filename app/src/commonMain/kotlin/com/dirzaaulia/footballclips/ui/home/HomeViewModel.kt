package com.dirzaaulia.footballclips.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.data.admob.AdMobManager
import com.dirzaaulia.footballclips.data.billing.BillingManager
import com.dirzaaulia.footballclips.data.constants.BigLeaguesConstants
import com.dirzaaulia.footballclips.data.local.PreferenceManager
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.data.model.remote.toUiModel
import com.dirzaaulia.footballclips.data.repository.HighlightRepository
import com.dirzaaulia.footballclips.data.repository.ProfilesRepository
import com.dirzaaulia.footballclips.data.repository.ScoreRepository
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.domain.model.toMatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    val selectedItem: StateFlow<HighlightUiItem?> = _selectedItem.asStateFlow()

    private val _relatedHighlights = MutableStateFlow<List<HighlightUiItem>>(emptyList())
    val relatedHighlights: StateFlow<List<HighlightUiItem>> = _relatedHighlights.asStateFlow()

    private val _relatedFixturesFinished = MutableStateFlow<List<HighlightUiItem>>(emptyList())
    val relatedFixturesFinished: StateFlow<List<HighlightUiItem>> = _relatedFixturesFinished.asStateFlow()

    private val _relatedFixturesUpcoming = MutableStateFlow<List<HighlightUiItem>>(emptyList())
    val relatedFixturesUpcoming: StateFlow<List<HighlightUiItem>> = _relatedFixturesUpcoming.asStateFlow()

    private val _showExternalHighlights = MutableStateFlow(false)
    val showExternalHighlights: StateFlow<Boolean> = _showExternalHighlights.asStateFlow()

    private val _isPendingInterstitial = MutableStateFlow(false)

    val isDebugPremium: StateFlow<Boolean> = preferenceManager.isDebugPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDebugPremium(isPremium: Boolean) {
        viewModelScope.launch {
            preferenceManager.setDebugPremium(isPremium)
        }
    }

    val isDarkMode: StateFlow<Boolean> = preferenceManager.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            preferenceManager.setDarkMode(isDark)
        }
    }

    val isPremium: StateFlow<Boolean> = combine(
        billingManager.isPremium,
        profilesRepository.profile,
        preferenceManager.isDebugPremium
    ) { premium, supabaseProfile, debugPremium ->
        premium || supabaseProfile?.isPremium == true || debugPremium
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isAdsRemoved: StateFlow<Boolean> = combine(
        preferenceManager.isAdsRemoved,
        isPremium
    ) { local, premium ->
        local || premium
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentUserProfile = profilesRepository.profile
    
    val customerInfo = billingManager.customerInfo

    private val limit = 40
    private var currentOffset = 0
    private var totalCount = 0
    private var isFetching = false
    
    private val supabaseMatches = mutableListOf<Match>()
    private val allHighlights = mutableListOf<HighlightUiModel>()
    private var selectedCountries = mutableSetOf<String>()
    private var selectedLeagues = mutableSetOf<String>()
    private var searchQuery = ""

    init {
        observeBillingErrors()
        observeAdsRemoved()
        observeProfileForBilling()
        refreshData()
    }

    private fun observeProfileForBilling() {
        viewModelScope.launch {
            profilesRepository.profile.collectLatest { profile ->
                if (profile != null) {
                    billingManager.identify(profile.id)
                } else {
                    billingManager.logOut()
                }
            }
        }
    }

    private fun observeAdsRemoved() {
        viewModelScope.launch {
            isAdsRemoved.collectLatest { removed ->
                if (_uiState.value is HomeState.Success && supabaseMatches.isNotEmpty()) {
                    updateUiState(removed = removed)
                }
            }
        }
    }

    private fun observeBillingErrors() {
        viewModelScope.launch {
            billingManager.errorEvent.collect {
                _errorMessage.emit(it)
            }
        }
        viewModelScope.launch {
            billingManager.messageEvent.collect {
                _errorMessage.emit(it)
            }
        }
    }

    fun refreshData() {
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

    fun selectLeague(leagueId: Int?, competitionId: String?) {
        _selectedLeagueId.value = leagueId
        _selectedCompetitionId.value = competitionId
        
        // Reset local filters
        selectedCountries.clear()
        selectedLeagues.clear()
        searchQuery = ""
        
        refreshData()
    }

    fun selectVideoById(itemId: String) {
        val item = (supabaseMatches.map { HighlightUiItem.SupabaseMatch(it) } + 
                    allHighlights.map { HighlightUiItem.Highlight(it) })
            .find { it.uniqueId == itemId }
        
        selectVideo(item)
    }

    fun selectVideo(item: HighlightUiItem?) {
        _selectedItem.value = item
        _relatedFixturesFinished.value = emptyList()
        _relatedFixturesUpcoming.value = emptyList()

        if (item == null) {
            _relatedHighlights.value = emptyList()
            return
        }

        val originalLeagueName = when (item) {
            is HighlightUiItem.SupabaseMatch -> item.match.competitionName
            is HighlightUiItem.Highlight -> item.highlight.leagueName
            else -> null
        }

        val bigLeague = BigLeaguesConstants.leagues.find { 
            originalLeagueName != null && (
                it.name.equals(originalLeagueName, ignoreCase = true) || 
                it.competitionId?.equals(originalLeagueName, ignoreCase = true) == true ||
                isLeagueMatch(it.name, originalLeagueName)
            )
        }

        val leagueName = bigLeague?.name ?: originalLeagueName
        val competitionId = if (item is HighlightUiItem.SupabaseMatch) item.match.competitionId else bigLeague?.competitionId

        if (leagueName != null) {
            // Filter highlights from both sources (External and Supabase)
            val fromExternal = allHighlights
                .filter { isLeagueMatch(it.leagueName, leagueName) }
                .map { HighlightUiItem.Highlight(it) }
            
            val fromSupabase = supabaseMatches
                .filter { 
                    isLeagueMatch(it.competitionName, leagueName) && 
                    !it.highlightVideoId.isNullOrEmpty() 
                }
                .map { HighlightUiItem.SupabaseMatch(it) }

            _relatedHighlights.value = (fromSupabase + fromExternal)
                .filter { it.uniqueId != item.uniqueId } // Don't show current video
                .distinctBy { it.uniqueId }
                .take(15)
            
            // Related fixtures (Matches regardless of highlight status)
            if (competitionId != null) {
                viewModelScope.launch {
                    fetchLeagueFixtures(competitionId)
                }
            } else {
                val filtered = supabaseMatches.filter { isLeagueMatch(it.competitionName, leagueName) }
                _relatedFixturesFinished.value = filtered.filter { it.isFinished }.map { HighlightUiItem.SupabaseMatch(it) }
                _relatedFixturesUpcoming.value = filtered.filter { it.isScheduled }.map { HighlightUiItem.SupabaseMatch(it) }
            }
        } else {
            _relatedHighlights.value = emptyList()
            _relatedFixturesFinished.value = emptyList()
            _relatedFixturesUpcoming.value = emptyList()
        }
    }

    private suspend fun fetchLeagueFixtures(competitionId: String) {
        // Fetch Finished
        val finishedResult = scoreRepository.getMatches(
            competitionId = competitionId,
            status = "FINISHED",
            ascending = false
        )
        if (finishedResult is NetworkResult.Success) {
            _relatedFixturesFinished.value = finishedResult.data.map { HighlightUiItem.SupabaseMatch(it.toMatch()) }
        }

        // Fetch Upcoming
        val upcomingResult = scoreRepository.getMatches(
            competitionId = competitionId,
            excludeStatus = "FINISHED",
            ascending = true
        )
        if (upcomingResult is NetworkResult.Success) {
            _relatedFixturesUpcoming.value = upcomingResult.data
                .map { it.toMatch() }
                .filter { it.isScheduled }
                .map { HighlightUiItem.SupabaseMatch(it) }
        }
    }

    private val supabaseLimit = 20
    private var supabaseOffset = 0
    private var hasMoreSupabase = true

    private suspend fun getSupabaseHighlights(isRefresh: Boolean = true) {
        if (isRefresh) {
            supabaseOffset = 0
            supabaseMatches.clear()
            hasMoreSupabase = true
        }

        if (!hasMoreSupabase) return

        var attempt = 0
        var success = false

        while (attempt < 3 && !success) {
            attempt++
            val result = scoreRepository.getMatches(
                competitionId = _selectedCompetitionId.value,
                hasHighlight = true,
                ascending = false,
                limit = supabaseLimit,
                offset = supabaseOffset
            )

            when (result) {
                is NetworkResult.Success -> {
                    val newMatches = result.data.map { it.toMatch() }
                    val existingIds = supabaseMatches.map { it.id }.toSet()
                    val uniqueNew = newMatches.filter { it.id !in existingIds }
                    supabaseMatches.addAll(uniqueNew)
                    if (newMatches.size < supabaseLimit) {
                        hasMoreSupabase = false
                    }
                    supabaseOffset += supabaseLimit
                    success = true
                }
                else -> {
                    if (attempt < 3) {
                        delay(500L * attempt)
                    } else {
                        hasMoreSupabase = true
                    }
                }
            }
        }
    }

    fun loadMore() {
        if (isFetching) return

        viewModelScope.launch {
            isFetching = true
            try {
                if (hasMoreSupabase) {
                    val currentState = _uiState.value
                    if (currentState is HomeState.Success) {
                        _uiState.value = currentState.copy(isLoadingMore = true)
                    }
                    getSupabaseHighlights(isRefresh = false)
                    updateUiState()
                } else if (_showExternalHighlights.value) {
                    getExternalHighlights(isRefresh = false)
                }
            } finally {
                isFetching = false
            }
        }
    }

    fun toggleExternalHighlights() {
        _showExternalHighlights.value = !_showExternalHighlights.value
        if (_showExternalHighlights.value && allHighlights.isEmpty()) {
            getExternalHighlights(isRefresh = true)
        } else {
            updateUiState()
        }
    }

    fun getExternalHighlights(isRefresh: Boolean = false) {
        if (isFetching) return
        
        val currentState = _uiState.value
        if (currentState is HomeState.Success) {
            _uiState.value = currentState.copy(isLoadingMore = true)
        }

        if (isRefresh) {
            currentOffset = 0
            allHighlights.clear()
        }

        isFetching = true
        viewModelScope.launch {
            try {
                val result = highlightRepository.getHighlights(
                    limit = limit,
                    offset = currentOffset,
                    leagueId = _selectedLeagueId.value?.toString()
                )
                
                when (result) {
                    is NetworkResult.Success -> {
                        totalCount = result.data.pagination?.totalCount ?: 0
                        val newHighlights = result.data.data?.mapNotNull { it.toUiModel() } ?: emptyList()
                        
                        allHighlights.addAll(newHighlights)
                        currentOffset += limit
                        
                        updateUiState()
                    }
                    is NetworkResult.Error -> {
                        handleNetworkError(isRefresh, "Error: ${result.code} - ${result.message}")
                    }
                    is NetworkResult.Exception -> {
                        handleNetworkError(isRefresh, "Exception: ${result.e.message}")
                    }
                }
            } finally {
                isFetching = false
            }
        }
    }

    private fun handleNetworkError(isRefresh: Boolean, message: String) {
        val currentState = _uiState.value
        if (currentState is HomeState.Success) {
            _uiState.value = currentState.copy(isLoadingMore = false)
            viewModelScope.launch { _errorMessage.emit(message) }
        } else if (isRefresh) {
            _uiState.value = HomeState.Error(message)
        } else {
            viewModelScope.launch { _errorMessage.emit(message) }
        }
    }

    private fun updateUiState(removed: Boolean? = null) {
        viewModelScope.launch(Dispatchers.Default) {
            val currentRemoved = removed ?: isAdsRemoved.value
            
            val items = mutableListOf<HighlightUiItem>()
            
            // 1. Add Supabase Matches with filtering
            val filteredSupabase = supabaseMatches.filter { match ->
                val matchSearch = searchQuery.isEmpty() || 
                        match.homeTeamName.contains(searchQuery, ignoreCase = true) || 
                        match.awayTeamName.contains(searchQuery, ignoreCase = true)

                val matchesSelectedFilters = selectedLeagues.isEmpty() || 
                        selectedLeagues.any { it.equals(match.competitionName, ignoreCase = true) }
                    
                matchSearch && matchesSelectedFilters
            }
            items.addAll(filteredSupabase.map { HighlightUiItem.SupabaseMatch(it) })
            
            // 2. Add External Highlights if enabled OR if Supabase has 0 matches
            if (_showExternalHighlights.value || supabaseMatches.isEmpty()) {
                val topLeague = BigLeaguesConstants.leagues.find { 
                    (it.leagueId != null && it.leagueId == _selectedLeagueId.value) || 
                    (it.competitionId != null && it.competitionId == _selectedCompetitionId.value)
                }

                val filteredExternal = allHighlights.filter { highlight ->
                    val matchSearch = searchQuery.isEmpty() || 
                            highlight.homeTeam.contains(searchQuery, ignoreCase = true) || 
                            highlight.awayTeam.contains(searchQuery, ignoreCase = true) ||
                            highlight.title.contains(searchQuery, ignoreCase = true)

                    val matchesSelectedFilters = (selectedCountries.isEmpty() || highlight.countryName in selectedCountries) &&
                    (selectedLeagues.isEmpty() || selectedLeagues.any { it.equals(highlight.leagueName, ignoreCase = true) })
                    
                    val matchesTopLeague = topLeague == null || topLeague.leagueId == null ||
                                           highlight.leagueName.contains(topLeague.name, ignoreCase = true) ||
                                           (topLeague.name == "La Liga" && highlight.countryName.contains("Spain", ignoreCase = true))

                    matchSearch && matchesSelectedFilters && matchesTopLeague
                }
                
                items.addAll(filteredExternal.map { HighlightUiItem.Highlight(it) })
            }

            val finalCanLoadMore = hasMoreSupabase || (_showExternalHighlights.value && currentOffset < totalCount)

            val builtItems = buildListWithAds(items, currentRemoved)

            val filterStateCalculated = extractAllFilters(
                supabaseMatches = supabaseMatches,
                externalHighlights = allHighlights,
                searchQuery = searchQuery,
                selectedCountries = selectedCountries,
                selectedLeagues = selectedLeagues,
                filteredCount = items.size
            )

            _uiState.value = HomeState.Success(
                items = builtItems,
                isAdsRemoved = currentRemoved,
                canLoadMore = finalCanLoadMore,
                isLoadingMore = false
            )
            
            _filterState.value = filterStateCalculated
        }
    }

    private fun buildListWithAds(items: List<HighlightUiItem>, isAdsRemoved: Boolean): List<HighlightUiItem> {
        if (items.isEmpty()) return emptyList()
        if (isAdsRemoved) return items

        val result = mutableListOf<HighlightUiItem>()
        result.add(items[0])
        result.add(HighlightUiItem.BannerAd("ad-0"))
        
        for (i in 1 until items.size) {
            result.add(items[i])
            if (i % 5 == 0 && i < items.size - 1) {
                result.add(HighlightUiItem.BannerAd("ad-$i"))
            }
        }
        return result
    }

    fun toggleCountry(country: String) {
        if (country in selectedCountries) selectedCountries.remove(country)
        else selectedCountries.add(country)
        updateUiState()
    }

    fun toggleLeague(league: String) {
        if (league in selectedLeagues) selectedLeagues.remove(league)
        else selectedLeagues.add(league)
        updateUiState()
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        updateUiState()
    }

    fun resetFilters() {
        selectedCountries.clear()
        selectedLeagues.clear()
        searchQuery = ""
        _selectedLeagueId.value = null
        _selectedCompetitionId.value = null
        refreshData()
    }

    fun applyFilters() {
        updateUiState()
    }

    fun signIn() {
        viewModelScope.launch {
            profilesRepository.signInWithGoogle()
        }
    }

    fun purchasePackage(packageToPurchase: Any) {
        billingManager.purchasePackage(packageToPurchase)
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }

    fun showInterstitial(onAdDismissed: () -> Unit) {
        if (isAdsRemoved.value) {
            onAdDismissed()
        } else {
            adMobManager.showInterstitial(onAdDismissed)
        }
    }

    fun setPendingInterstitial(pending: Boolean) {
        _isPendingInterstitial.value = pending
    }

    fun consumePendingInterstitial() {
        if (_isPendingInterstitial.value) {
            _isPendingInterstitial.value = false
            showInterstitial { }
        }
    }

    private fun isLeagueMatch(target: String?, candidate: String?): Boolean {
        if (target == null || candidate == null) return false
        val t = target.lowercase().trim()
        val c = candidate.lowercase().trim()
        if (t == c) return true

        // Regex for Premier League / PL / EPL
        val plRegex = Regex(".*\\b(pl|epl|premier league)\\b.*", RegexOption.IGNORE_CASE)
        if (plRegex.matches(t) && plRegex.matches(c)) return true

        return t.contains(c) || c.contains(t)
    }
}
