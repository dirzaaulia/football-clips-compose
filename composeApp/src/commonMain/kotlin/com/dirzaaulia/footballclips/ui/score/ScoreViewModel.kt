package com.dirzaaulia.footballclips.ui.score

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.data.billing.BillingManager
import com.dirzaaulia.footballclips.data.local.PreferenceManager
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.repository.ClubRepository
import com.dirzaaulia.footballclips.data.repository.ProfilesRepository
import com.dirzaaulia.footballclips.data.repository.ScoreRepository
import com.dirzaaulia.footballclips.data.repository.StandingRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScoreViewModel(
    internal val repository: ScoreRepository,
    private val preferenceManager: PreferenceManager,
    private val billingManager: BillingManager,
    private val profilesRepository: ProfilesRepository,
    private val clubRepository: ClubRepository,
    internal val standingRepository: StandingRepository,
) : ViewModel() {

    val supportedClubs = clubRepository.supportedClubs

    internal val _uiState = MutableStateFlow<ScoreState>(ScoreState.Loading)
    val uiState: StateFlow<ScoreState> = _uiState.asStateFlow()

    internal val _selectedCompetitionId = MutableStateFlow<String?>(null)
    val selectedCompetitionId: StateFlow<String?> = _selectedCompetitionId.asStateFlow()

    internal val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate.asStateFlow()

    val availableDates: List<DateOption> = generateDateOptions()

    val hasLiveMatch: StateFlow<Boolean> = uiState.map { state ->
        if (state is ScoreState.Success) {
            state.matches.any { item ->
                (item as? HighlightUiItem.SupabaseMatch)?.match?.isLive == true
            }
        } else {
            false
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isPremium: StateFlow<Boolean> = combine(
        billingManager.isPremium,
        profilesRepository.profile,
    ) { premium, supabaseProfile ->
        premium || supabaseProfile?.isPremium == true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isAdsRemoved: StateFlow<Boolean> = combine(
        preferenceManager.isAdsRemoved,
        isPremium,
    ) { local, premium ->
        local || premium
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isSpoilerFreeMode: StateFlow<Boolean> = preferenceManager.isSpoilerFreeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val favoriteClubs: StateFlow<Set<String>> = preferenceManager.favoriteClubs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _selectedStatusFilter = MutableStateFlow(MatchStatusFilter.ALL)
    val selectedStatusFilter: StateFlow<MatchStatusFilter> = _selectedStatusFilter.asStateFlow()

    fun selectStatusFilter(filter: MatchStatusFilter) {
        if (_selectedStatusFilter.value == filter) return
        _selectedStatusFilter.value = filter
        _isLiveOnlyFilterActive.value = (filter == MatchStatusFilter.LIVE)
        getMatches()
    }

    private val _isMyClubsFilterActive = MutableStateFlow(false)
    val isMyClubsFilterActive: StateFlow<Boolean> = _isMyClubsFilterActive.asStateFlow()

    private val _isLiveOnlyFilterActive = MutableStateFlow(false)
    val isLiveOnlyFilterActive: StateFlow<Boolean> = _isLiveOnlyFilterActive.asStateFlow()

    val liveMatchCount: StateFlow<Int> = uiState.map { state ->
        if (state is ScoreState.Success) {
            state.matches.count { item ->
                (item as? HighlightUiItem.SupabaseMatch)?.match?.isLive == true
            }
        } else 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _showScrollToTop = MutableStateFlow(false)
    val showScrollToTop: StateFlow<Boolean> = _showScrollToTop.asStateFlow()
    fun setScrollToTopVisible(v: Boolean) { _showScrollToTop.value = v }

    private val _scrollToTopEvent = MutableSharedFlow<Unit>()
    val scrollToTopEvent: SharedFlow<Unit> = _scrollToTopEvent.asSharedFlow()
    fun triggerScrollToTop() { viewModelScope.launch { _scrollToTopEvent.emit(Unit) } }

    fun toggleSpoilerFreeMode() {
        viewModelScope.launch {
            preferenceManager.setSpoilerFreeMode(!isSpoilerFreeMode.value)
        }
    }

    fun toggleMyClubsFilter() {
        _isMyClubsFilterActive.value = !_isMyClubsFilterActive.value
        getMatches()
    }

    fun toggleLiveOnlyFilter() {
        val next = !_isLiveOnlyFilterActive.value
        _isLiveOnlyFilterActive.value = next
        _selectedStatusFilter.value = if (next) MatchStatusFilter.LIVE else MatchStatusFilter.ALL
        getMatches()
    }

    fun toggleFavoriteClub(clubId: String) {
        viewModelScope.launch {
            preferenceManager.toggleFavoriteClub(clubId)
            if (_isMyClubsFilterActive.value) getMatches()
        }
    }

    private val _isFiltersExpanded = MutableStateFlow(true)
    val isFiltersExpanded: StateFlow<Boolean> = _isFiltersExpanded.asStateFlow()
    fun toggleFiltersExpanded() { _isFiltersExpanded.value = !_isFiltersExpanded.value }

    private val _showMyClubsSheet = MutableStateFlow(false)
    val showMyClubsSheet: StateFlow<Boolean> = _showMyClubsSheet.asStateFlow()
    fun setMyClubsSheetVisible(v: Boolean) { _showMyClubsSheet.value = v }

    internal val _standingsState = MutableStateFlow<StandingsState>(StandingsState.Loading)
    val standingsState: StateFlow<StandingsState> = _standingsState.asStateFlow()

    val selectedSeason: StateFlow<Int> = preferenceManager.selectedSeason
        .stateIn(viewModelScope, SharingStarted.Eagerly, 2026)

    fun setSeason(season: Int) {
        if (selectedSeason.value == season) return
        viewModelScope.launch {
            preferenceManager.setSelectedSeason(season)
        }
    }

    init {
        viewModelScope.launch { clubRepository.refreshClubs() }
        _selectedDate.value = availableDates.firstOrNull { it.isToday }?.date
        viewModelScope.launch {
            preferenceManager.selectedSeason.collect {
                getMatches()
                getStandings()
            }
        }
    }
}
