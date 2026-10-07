package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.BigLeaguesQuickFilterBar
import com.dirzaaulia.footballclips.ui.components.EmptyState
import com.dirzaaulia.footballclips.ui.components.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.components.HeroCarousel

@Composable
internal fun HomeLandscapeContent(
    items: List<HighlightUiItem>,
    gridItems: List<HighlightUiItem>,
    featuredHighlight: HighlightUiItem?,
    isAdsRemoved: Boolean,
    showExternalHighlights: Boolean,
    selectedLeagueId: Int?,
    isLoadingMore: Boolean,
    isSpoilerFreeMode: Boolean = false,
    revealedItemIds: Set<String> = emptySet(),
    onRevealItem: (String) -> Unit = {},
    gridState: LazyGridState,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
    onLeagueSelected: (Int?, String?) -> Unit,
    onResetFilters: () -> Unit,
    onToggleExternalHighlights: () -> Unit,
    viewModel: HomeViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Left Column: Hero Carousel (Centered vertically in the middle of left column)
        Column(
            modifier = Modifier
                .weight(0.40f)
                .fillMaxHeight()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (featuredHighlight != null) {
                HeroCarousel(
                    items = items,
                    isAdsRemoved = isAdsRemoved,
                    isCinematic = false,
                    isSpoilerFreeMode = isSpoilerFreeMode,
                    revealedItemIds = revealedItemIds,
                    onRevealItem = onRevealItem,
                    onVideoClick = { selectedItem ->
                        onVideoClick(selectedItem) { isJumping ->
                            viewModel.setPendingInterstitial(true)
                            if (!isJumping) viewModel.consumePendingInterstitial()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Right Column: League Filter + 2-Column Grid Highlight List
        Column(
            modifier = Modifier
                .weight(0.60f)
                .fillMaxHeight()
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
        ) {
            BigLeaguesQuickFilterBar(
                selectedLeagueId = selectedLeagueId,
                onLeagueSelected = onLeagueSelected,
                contentPadding = PaddingValues(0.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 60.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (items.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(
                            title = "No Highlights Found",
                            description = "Try selecting another league or check back later.",
                            onActionClick = onResetFilters
                        )
                    }
                } else {
                    items(
                        items = gridItems,
                        key = { item -> item.uniqueId },
                        span = { item ->
                            if (item is HighlightUiItem.BannerAd) GridItemSpan(maxLineSpan)
                            else GridItemSpan(1)
                        }
                    ) { item ->
                        LandscapeHighlightItem(
                            item = item,
                            isSpoilerFreeMode = isSpoilerFreeMode,
                            revealedItemIds = revealedItemIds,
                            showExternalHighlights = showExternalHighlights,
                            onRevealItem = onRevealItem,
                            onVideoClick = onVideoClick,
                            viewModel = viewModel
                        )
                    }
                }

                if (!showExternalHighlights) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LandscapeExploreMoreButton(onToggleExternalHighlights = onToggleExternalHighlights)
                    }
                }

                val loadMoreError = (viewModel.uiState.value as? HomeState.Success)?.loadMoreError

                if (isLoadingMore && loadMoreError == null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ExpressiveLoadingIndicator(
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 4.dp
                            )
                        }
                    }
                } else if (loadMoreError != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = loadMoreError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { viewModel.retryLoadMore() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}
