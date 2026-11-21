package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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

@Composable
internal fun HomeGridContent(
    items: List<HighlightUiItem>,
    gridItems: List<HighlightUiItem>,
    featuredHighlight: HighlightUiItem?,
    isAdsRemoved: Boolean,
    showExternalHighlights: Boolean,
    selectedLeagueId: Int?,
    isLoadingMore: Boolean = false,
    isSpoilerFreeMode: Boolean = false,
    revealedItemIds: Set<String> = emptySet(),
    onRevealItem: (String) -> Unit = {},
    onSeeAllLiveMatches: () -> Unit = {},
    gridState: LazyGridState,
    onLeagueSelected: (Int?, String?) -> Unit,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
    onResetFilters: () -> Unit,
    onToggleExternalHighlights: () -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val competitions by viewModel.competitions.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 280.dp),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp, start = 16.dp, end = 16.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (featuredHighlight != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeroCarousel(
                    items = items,
                    isAdsRemoved = isAdsRemoved,
                    isSpoilerFreeMode = isSpoilerFreeMode,
                    revealedItemIds = revealedItemIds,
                    onRevealItem = onRevealItem,
                    onVideoClick = { selectedItem ->
                        onVideoClick(selectedItem) { isJumping ->
                            viewModel.setPendingInterstitial(true)
                            if (!isJumping) {
                                viewModel.consumePendingInterstitial()
                            }
                        }
                    },
                )
            }
        }

        if (competitions.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HomeGridFilterBar(
                    isAdsRemoved = isAdsRemoved,
                    selectedLeagueId = selectedLeagueId,
                    competitions = competitions,
                    onLeagueSelected = onLeagueSelected,
                )
            }
        }

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
            ) { item ->
                HighlightGridItem(
                    item = item,
                    revealedItemIds = revealedItemIds,
                    isSpoilerFreeMode = isSpoilerFreeMode,
                    showExternalHighlights = showExternalHighlights,
                    viewModel = viewModel,
                    onVideoClick = onVideoClick,
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
