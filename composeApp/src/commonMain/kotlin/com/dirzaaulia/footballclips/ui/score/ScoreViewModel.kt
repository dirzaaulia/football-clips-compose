package com.dirzaaulia.footballclips.ui.score

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.data.billing.BillingManager
import com.dirzaaulia.footballclips.data.local.PreferenceManager
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.StandingDto
import com.dirzaaulia.footballclips.data.repository.ClubRepository
import com.dirzaaulia.footballclips.domain.model.toMatch
import com.dirzaaulia.footballclips.data.repository.ScoreRepository
import com.dirzaaulia.footballclips.data.repository.ProfilesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.dirzaaulia.footballclips.util.DateTimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

sealed interface ScoreState {
    data object Loading : ScoreState
    data class Success(
        val matches: List<HighlightUiItem>,
        val isAdsRemoved: Boolean
    ) : ScoreState
    data class Error(val message: String) : ScoreState
}

sealed interface StandingsState {
    data object Loading : StandingsState
    data class Success(val standings: List<StandingDto>) : StandingsState
    data class Error(val message: String) : StandingsState
}

class ScoreViewModel(
    private val repository: ScoreRepository,
    private val preferenceManager: PreferenceManager,
    private val billingManager: BillingManager,
    private val profilesRepository: ProfilesRepository,
    private val clubRepository: ClubRepository,
    private val standingRepository: com.dirzaaulia.footballclips.data.repository.StandingRepository
) : ViewModel() {

    val supportedClubs = clubRepository.supportedClubs

    private val _uiState = MutableStateFlow<ScoreState>(ScoreState.Loading)
    val uiState: StateFlow<ScoreState> = _uiState.asStateFlow()

    private val _selectedCompetitionId = MutableStateFlow<String?>(null)
    val selectedCompetitionId: StateFlow<String?> = _selectedCompetitionId.asStateFlow()

    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate.asStateFlow()

    val availableDates: List<DateOption> = generateDateOptions()

    val hasLiveMatch: StateFlow<Boolean> = uiState.map { state ->
        if (state is ScoreState.Success) {
            state.matches.any { item ->
                (item as? HighlightUiItem.SupabaseMatch)?.match?.isLive == true
            }
        } else false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isPremium: StateFlow<Boolean> = combine(
        billingManager.isPremium,
        profilesRepository.profile
    ) { premium, supabaseProfile ->
        premium || supabaseProfile?.isPremium == true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isAdsRemoved: StateFlow<Boolean> = combine(
        preferenceManager.isAdsRemoved,
        isPremium
    ) { local, premium ->
        local || premium
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isSpoilerFreeMode: StateFlow<Boolean> = preferenceManager.isSpoilerFreeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val favoriteClubs: StateFlow<Set<String>> = preferenceManager.favoriteClubs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _isMyClubsFilterActive = MutableStateFlow(false)
    val isMyClubsFilterActive: StateFlow<Boolean> = _isMyClubsFilterActive.asStateFlow()

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

     fun toggleFavoriteClub(clubId: String) {
        viewModelScope.launch { preferenceManager.toggleFavoriteClub(clubId); if (_isMyClubsFilterActive.value) getMatches() }
    }

    private val _isFiltersExpanded = MutableStateFlow(true)
    val isFiltersExpanded: StateFlow<Boolean> = _isFiltersExpanded.asStateFlow()
    fun toggleFiltersExpanded() { _isFiltersExpanded.value = !_isFiltersExpanded.value }

    private val _showMyClubsSheet = MutableStateFlow(false)
    val showMyClubsSheet: StateFlow<Boolean> = _showMyClubsSheet.asStateFlow()
    fun setMyClubsSheetVisible(v: Boolean) { _showMyClubsSheet.value = v }

    private val _standingsState = MutableStateFlow<StandingsState>(StandingsState.Loading)
    val standingsState: StateFlow<StandingsState> = _standingsState.asStateFlow()

    fun getStandings(competitionId: String? = _selectedCompetitionId.value) {
        val compId = competitionId ?: "PL"
        viewModelScope.launch {
            _standingsState.value = StandingsState.Loading
            when (val res = standingRepository.getStandings(compId)) {
                is NetworkResult.Success -> _standingsState.value = StandingsState.Success(res.data)
                is NetworkResult.Error -> _standingsState.value = StandingsState.Error("Error ${res.code}: ${res.message}")
                is NetworkResult.Exception -> _standingsState.value = StandingsState.Error(res.e.message ?: "Failed to load standings")
            }
        }
    }

    init {
        viewModelScope.launch { clubRepository.refreshClubs() }
        _selectedDate.value = availableDates.firstOrNull { it.isToday }?.date
        getMatches()
        getStandings("PL")
    }

    fun selectCompetition(competitionId: String?) {
        if (_selectedCompetitionId.value == competitionId) return
        _selectedCompetitionId.value = competitionId
        getMatches()
        if (competitionId != null) getStandings(competitionId)
    }

    fun selectDate(date: String?) {
        if (_selectedDate.value == date) return
        _selectedDate.value = date
        getMatches()
    }

    fun getMatches() {
        viewModelScope.launch {
            _uiState.value = ScoreState.Loading
            
            try {
                val selectedDateStr = _selectedDate.value
                val (startDate, endDate) = if (selectedDateStr != null) {
                    val date = LocalDate.parse(selectedDateStr)
                    date.plus(-1, DateTimeUnit.DAY).toString() to date.plus(1, DateTimeUnit.DAY).toString()
                } else null to null

                val result = repository.getMatches(
                    competitionId = _selectedCompetitionId.value,
                    excludeStatus = null,
                    startDate = startDate,
                    endDate = endDate,
                    ascending = true
                )
                
                when (result) {
                    is NetworkResult.Success -> {
                        val selectedDate = _selectedDate.value
                        val todayDate = availableDates.firstOrNull { it.isToday }?.date
                        val favIds = favoriteClubs.value
                        val isMyClubsOnly = _isMyClubsFilterActive.value
                        val clubs = supportedClubs.value
                        
                        val matches = result.data
                            .map { it.toMatch() }
                            .filter { match ->
                                val isSelectedDate = match.dateOnlyISO == selectedDate
                                val isLiveNow = match.isLive
                                val dateMatches = isSelectedDate || (isLiveNow && selectedDate == todayDate)
                                if (!dateMatches) return@filter false
                                if (isMyClubsOnly) {
                                    com.dirzaaulia.footballclips.data.constants.SupportedClubsConstants.isMatchForFavoriteClubs(match, favIds, clubs)
                                } else {
                                    true
                                }
                            }
                            .sortedWith(
                                compareByDescending<com.dirzaaulia.footballclips.domain.model.Match> { match ->
                                    com.dirzaaulia.footballclips.data.constants.SupportedClubsConstants.isMatchForFavoriteClubs(match, favIds, clubs)
                                }.thenBy { it.utcDate }
                            )
                            .map { HighlightUiItem.SupabaseMatch(it) }

                        val adsRemoved = isAdsRemoved.value
                        _uiState.value = ScoreState.Success(
                            matches = buildFixtureListWithAds(matches, adsRemoved),
                            isAdsRemoved = adsRemoved
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = ScoreState.Error("Error: ${result.code} - ${result.message}")
                    }
                    is NetworkResult.Exception -> {
                        _uiState.value = ScoreState.Error("Exception: ${result.e.message}")
                    }
                }
            } catch (t: Throwable) {
                _uiState.value = ScoreState.Error("Critical Error: ${t.message}")
            }
        }
    }

    private fun buildFixtureListWithAds(items: List<HighlightUiItem>, isAdsRemoved: Boolean): List<HighlightUiItem> {
        if (items.isEmpty()) return emptyList()
        if (isAdsRemoved) return items

        val result = mutableListOf<HighlightUiItem>()
        for (i in items.indices) {
            result.add(items[i])
            if ((i + 1) % 6 == 0 && i < items.size - 1) {
                result.add(HighlightUiItem.BannerAd("ad-$i"))
            }
        }
        return result
    }
}
