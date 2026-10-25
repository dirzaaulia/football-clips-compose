package com.dirzaaulia.footballclips.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator

@Composable
internal fun HomeScreenBody(
    uiState: HomeState,
    items: List<HighlightUiItem>,
    gridItems: List<HighlightUiItem>,
    featuredHighlight: HighlightUiItem?,
    isAdsRemoved: Boolean,
    showExternalHighlights: Boolean,
    selectedLeagueId: Int?,
    isLandscape: Boolean,
    isGrid: Boolean,
    isSpoilerFreeMode: Boolean = false,
    revealedItemIds: Set<String> = emptySet(),
    onRevealItem: (String) -> Unit = {},
    onSeeAllLiveMatches: () -> Unit = {},
    listState: LazyListState,
    gridState: LazyGridState,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val stateKey = when (uiState) {
        is HomeState.Loading -> 0
        is HomeState.Success -> 1
        is HomeState.Error -> 2
    }

    AnimatedContent(
        targetState = stateKey,
        transitionSpec = {
            fadeIn(animationSpec = tween(300, easing = EaseOutCubic)) togetherWith
                fadeOut(animationSpec = tween(200, easing = EaseInCubic))
        },
        modifier = modifier,
        label = "HomeStateTransition",
    ) { targetKey ->
        when (targetKey) {
            0 -> ExpressiveLoadingIndicator()
            1 -> {
                val state = uiState as? HomeState.Success
                if (state != null) {
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
                            viewModel = viewModel,
                        )
                    } else if (isGrid) {
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
                            onSeeAllLiveMatches = onSeeAllLiveMatches,
                            gridState = gridState,
                            onVideoClick = onVideoClick,
                            onLeagueSelected = viewModel::selectLeague,
                            onResetFilters = viewModel::resetFilters,
                            onToggleExternalHighlights = viewModel::toggleExternalHighlights,
                            viewModel = viewModel,
                        )
                    } else {
                        HomeListContent(
                            items = items,
                            featuredHighlight = featuredHighlight,
                            isAdsRemoved = isAdsRemoved,
                            showExternalHighlights = showExternalHighlights,
                            isLoadingMore = state.isLoadingMore,
                            isSpoilerFreeMode = isSpoilerFreeMode,
                            revealedItemIds = revealedItemIds,
                            onRevealItem = onRevealItem,
                            onSeeAllLiveMatches = onSeeAllLiveMatches,
                            listState = listState,
                            onVideoClick = onVideoClick,
                            onResetFilters = viewModel::resetFilters,
                            onToggleExternalHighlights = viewModel::toggleExternalHighlights,
                            viewModel = viewModel,
                        )
                    }
                }
            }

            else -> {
                val state = uiState as? HomeState.Error
                EmptyState(
                    title = "Failed to Load Highlights",
                    description = state?.message ?: "An error occurred",
                    onActionClick = { viewModel.refreshData() },
                    actionText = "Try Again",
                )
            }
        }
    }
}
