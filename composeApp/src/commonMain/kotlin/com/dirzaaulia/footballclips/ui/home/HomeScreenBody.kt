package com.dirzaaulia.footballclips.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.components.EmptyState
import com.dirzaaulia.footballclips.ui.components.ExpressiveLoadingIndicator

@Composable
internal fun HomeScreenBody(
    uiState: HomeState,
    isWeb: Boolean,
    isLandscape: Boolean = false,
    selectedLeagueId: Int?,
    filterState: FilterState,
    showExternalHighlights: Boolean,
    isAdsRemoved: Boolean,
    isSpoilerFreeMode: Boolean,
    revealedItemIds: Set<String>,
    onRevealItem: (String) -> Unit,
    gridState: LazyGridState,
    listState: LazyListState,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
    viewModel: HomeViewModel
) {
    AnimatedContent(
        targetState = uiState,
        transitionSpec = {
            (fadeIn(animationSpec = tween(400, easing = EaseOutCubic)) +
                    slideInVertically(animationSpec = tween(400, easing = EaseOutCubic)) { it / 10 })
                .togetherWith(
                    fadeOut(animationSpec = tween(200, easing = EaseInCubic))
                )
        },
        label = "HomeScreenStateTransition",
        modifier = Modifier.fillMaxSize()
    ) { state ->
        when (state) {
            is HomeState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveLoadingIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 5.dp
                    )
                }
            }
            is HomeState.Success -> {
                val items = state.items
                val isFiltering = selectedLeagueId != null || filterState.searchQuery.isNotEmpty() || 
                                 filterState.selectedCountries.isNotEmpty() || filterState.selectedLeagues.isNotEmpty()
                
                val featuredHighlight = if (!isFiltering) {
                    items.firstOrNull { it !is HighlightUiItem.BannerAd }
                } else null

                val gridItems = if (featuredHighlight != null) {
                    items.filterIndexed { index, _ -> index != items.indexOf(featuredHighlight) }
                } else {
                    items
                }

                if (isLandscape) {
                    HomeLandscapeContent(
                        items = items,
                        gridItems = gridItems,
                        featuredHighlight = featuredHighlight,
                        isAdsRemoved = isAdsRemoved,
                        showExternalHighlights = showExternalHighlights,
                        selectedLeagueId = selectedLeagueId,
                        isLoadingMore = state.isLoadingMore,
                        isSpoilerFreeMode = isSpoilerFreeMode,
                        revealedItemIds = revealedItemIds,
                        onRevealItem = onRevealItem,
                        gridState = gridState,
                        onVideoClick = onVideoClick,
                        onLeagueSelected = viewModel::selectLeague,
                        onResetFilters = viewModel::resetFilters,
                        onToggleExternalHighlights = viewModel::toggleExternalHighlights,
                        viewModel = viewModel
                    )
                } else if (isWeb) {
                    HomeGridContent(
                        items = items,
                        gridItems = gridItems,
                        featuredHighlight = featuredHighlight,
                        isAdsRemoved = isAdsRemoved,
                        showExternalHighlights = showExternalHighlights,
                        selectedLeagueId = selectedLeagueId,
                        isSpoilerFreeMode = isSpoilerFreeMode,
                        revealedItemIds = revealedItemIds,
                        onRevealItem = onRevealItem,
                        gridState = gridState,
                        onVideoClick = onVideoClick,
                        onLeagueSelected = viewModel::selectLeague,
                        onResetFilters = viewModel::resetFilters,
                        onToggleExternalHighlights = viewModel::toggleExternalHighlights,
                        viewModel = viewModel
                    )
                } else {
                    HomeListContent(
                        items = items,
                        gridItems = gridItems,
                        featuredHighlight = featuredHighlight,
                        isAdsRemoved = isAdsRemoved,
                        showExternalHighlights = showExternalHighlights,
                        selectedLeagueId = selectedLeagueId,
                        isLoadingMore = state.isLoadingMore,
                        isSpoilerFreeMode = isSpoilerFreeMode,
                        revealedItemIds = revealedItemIds,
                        onRevealItem = onRevealItem,
                        listState = listState,
                        onVideoClick = onVideoClick,
                        onLeagueSelected = viewModel::selectLeague,
                        onResetFilters = viewModel::resetFilters,
                        onToggleExternalHighlights = viewModel::toggleExternalHighlights,
                        viewModel = viewModel
                    )
                }
            }
            is HomeState.Error -> {
                EmptyState(
                    title = "Failed to Load Highlights",
                    description = state.message,
                    onActionClick = { viewModel.refreshData() },
                    actionText = "Try Again"
                )
            }
        }
    }
}
