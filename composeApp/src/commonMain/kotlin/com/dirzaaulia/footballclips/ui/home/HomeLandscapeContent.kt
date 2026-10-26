package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.card.HeroCarousel
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.components.filter.BigLeaguesQuickFilterBar

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
    viewModel: HomeViewModel,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Left Column: PitchScope Featured Hero Card
        Column(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight()
                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (featuredHighlight != null) {
                HeroCarousel(
                    items = items,
                    isAdsRemoved = isAdsRemoved,
                    isCinematic = false,
                    isLandscape = true,
                    isSpoilerFreeMode = isSpoilerFreeMode,
                    revealedItemIds = revealedItemIds,
                    onRevealItem = onRevealItem,
                    onVideoClick = { selectedItem ->
                        onVideoClick(selectedItem) { isJumping ->
                            viewModel.setPendingInterstitial(true)
                            if (!isJumping) viewModel.consumePendingInterstitial()
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // Right Column: Quick Filters + Stream Feed List
        Column(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight()
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
        ) {
            val competitions by viewModel.competitions.collectAsState()
            BigLeaguesQuickFilterBar(
                selectedLeagueId = selectedLeagueId,
                dynamicCompetitions = competitions,
                onLeagueSelected = onLeagueSelected,
                contentPadding = PaddingValues(0.dp),
                isCompact = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (items.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(
                            title = "No Highlights Found",
                            description = "Try selecting another league or check back later.",
                            onActionClick = onResetFilters,
                        )
                    }
                } else {
                    items(
                        items = gridItems,
                        key = { item -> item.uniqueId },
                        span = { item ->
                            if (item is HighlightUiItem.BannerAd) GridItemSpan(maxLineSpan)
                            else GridItemSpan(1)
                        },
                    ) { item ->
                        LandscapeHighlightItem(
                            item = item,
                            isSpoilerFreeMode = isSpoilerFreeMode,
                            revealedItemIds = revealedItemIds,
                            showExternalHighlights = showExternalHighlights,
                            onRevealItem = onRevealItem,
                            onVideoClick = onVideoClick,
                            viewModel = viewModel,
                        )
                    }
                }

                val loadMoreError = (viewModel.uiState.value as? HomeState.Success)?.loadMoreError

                if (isLoadingMore && loadMoreError == null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            ExpressiveLoadingIndicator(
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 4.dp,
                            )
                        }
                    }
                } else if (loadMoreError != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = loadMoreError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
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
