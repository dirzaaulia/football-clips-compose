package com.dirzaaulia.footballclips.ui.score

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.data.billing.BillingManager
import com.dirzaaulia.footballclips.data.local.PreferenceManager
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.domain.model.toMatch
import com.dirzaaulia.footballclips.data.repository.ScoreRepository
import com.dirzaaulia.footballclips.data.repository.ProfilesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.dirzaaulia.footballclips.util.DateTimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

sealed interface ScoreState {
    data object Loading : ScoreState
    data class Success(
        val matches: List<HighlightUiItem>,
        val isAdsRemoved: Boolean
    ) : ScoreState
    data class Error(val message: String) : ScoreState
}

class ScoreViewModel(
    private val repository: ScoreRepository,
    private val preferenceManager: PreferenceManager,
    private val billingManager: BillingManager,
    private val profilesRepository: ProfilesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScoreState>(ScoreState.Loading)
    val uiState: StateFlow<ScoreState> = _uiState.asStateFlow()

    private val _selectedCompetitionId = MutableStateFlow<String?>(null)
    val selectedCompetitionId: StateFlow<String?> = _selectedCompetitionId.asStateFlow()

    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate.asStateFlow()

    val availableDates: List<DateOption> = generateDateOptions()

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

    init {
        _selectedDate.value = availableDates.firstOrNull { it.isToday }?.date
        getMatches()
    }

    fun selectCompetition(competitionId: String?) {
        if (_selectedCompetitionId.value == competitionId) return
        _selectedCompetitionId.value = competitionId
        getMatches()
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
                val startDate: String?
                val endDate: String?

                if (selectedDateStr != null) {
                    val date = kotlinx.datetime.LocalDate.parse(selectedDateStr)
                    startDate = date.plus(-1, DateTimeUnit.DAY).toString()
                    endDate = date.plus(1, DateTimeUnit.DAY).toString()
                } else {
                    startDate = null
                    endDate = null
                }

                val result = repository.getMatches(
                    competitionId = _selectedCompetitionId.value,
                    excludeStatus = "FINISHED",
                    startDate = startDate,
                    endDate = endDate,
                    ascending = true
                )
                
                when (result) {
                    is NetworkResult.Success -> {
                        val selectedDate = _selectedDate.value
                        val todayDate = availableDates.firstOrNull { it.isToday }?.date
                        
                        val matches = result.data
                            .map { it.toMatch() }
                            .filter { match ->
                                val isSelectedDate = match.dateOnlyISO == selectedDate
                                val isLiveNow = match.isLive
                                // Include if it matches the selected date OR 
                                // if we're looking at "Today" and the match is currently LIVE
                                isSelectedDate || (isLiveNow && selectedDate == todayDate)
                            }
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

    private fun generateDateOptions(): List<DateOption> {
        val now = Clock.System.now()
        val timeZone = DateTimeUtils.safeTimeZone

        return (0..14).map { daysOffset ->
            val date = try {
                now.plus(daysOffset, DateTimeUnit.DAY, timeZone)
                    .toLocalDateTime(timeZone)
            } catch (t: Throwable) {
                // Final fallback to UTC if everything else fails
                try {
                    now.plus(daysOffset, DateTimeUnit.DAY, TimeZone.UTC)
                        .toLocalDateTime(TimeZone.UTC)
                } catch (t2: Throwable) {
                    // This should theoretically never happen, but for absolute safety
                    Clock.System.now().toLocalDateTime(TimeZone.UTC)
                }
            }
            
            val dayName = when (daysOffset) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
            }

            DateOption(
                displayDay = dayName,
                displayDate = "${date.dayOfMonth} ${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }}",
                date = "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}",
                isToday = daysOffset == 0
            )
        }
    }
}

data class DateOption(
    val displayDay: String,
    val displayDate: String,
    val date: String,
    val isToday: Boolean
)
