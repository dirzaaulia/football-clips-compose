package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dirzaaulia.footballclips.ui.components.filter.FilterBottomSheet
import com.dirzaaulia.footballclips.ui.components.common.MyClubsBottomSheet
import com.dirzaaulia.footballclips.ui.home.FilterState
import com.dirzaaulia.footballclips.ui.home.HomeState
import com.dirzaaulia.footballclips.ui.home.HomeViewModel
import com.dirzaaulia.footballclips.ui.home.applyFilters
import com.dirzaaulia.footballclips.ui.home.loadMore
import com.dirzaaulia.footballclips.ui.home.onSearchQueryChanged
import com.dirzaaulia.footballclips.ui.home.resetFilters
import com.dirzaaulia.footballclips.ui.home.toggleCountry
import com.dirzaaulia.footballclips.ui.home.toggleLeague
import com.dirzaaulia.footballclips.ui.score.ScoreViewModel

@Composable
internal fun WebMatchesSheets(
    showFilterSheet: Boolean,
    isMyClubsSheetVisible: Boolean,
    filterState: FilterState,
    uiState: HomeState,
    showExternalHighlights: Boolean,
    viewModel: HomeViewModel,
    scoreViewModel: ScoreViewModel,
    onDismissFilterSheet: () -> Unit,
) {
    if (showFilterSheet) {
        FilterBottomSheet(
            state = filterState,
            isLoadingMore = (uiState as? HomeState.Success)?.isLoadingMore == true,
            showLoadMore = showExternalHighlights,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onCountryToggle = viewModel::toggleCountry,
            onLeagueToggle = viewModel::toggleLeague,
            onReset = viewModel::resetFilters,
            onLoadMore = viewModel::loadMore,
            onApply = {
                viewModel.applyFilters()
                onDismissFilterSheet()
            },
            onDismiss = onDismissFilterSheet,
        )
    }

    if (isMyClubsSheetVisible) {
        val supportedClubs by scoreViewModel.supportedClubs.collectAsState()
        val favoriteClubs by scoreViewModel.favoriteClubs.collectAsState()
        MyClubsBottomSheet(
            selectedClubIds = favoriteClubs,
            onToggleClub = { scoreViewModel.toggleFavoriteClub(it) },
            onDismissRequest = {
                viewModel.setMyClubsSheetVisible(false)
                scoreViewModel.setMyClubsSheetVisible(false)
            },
            clubs = supportedClubs,
        )
    }
}
