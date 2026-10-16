package com.dirzaaulia.footballclips.ui.score

import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.remote.StandingDto

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

enum class MatchStatusFilter(val label: String) {
    ALL("All"),
    LIVE("Live"),
    FINISHED("Finished"),
    UPCOMING("Upcoming")
}
