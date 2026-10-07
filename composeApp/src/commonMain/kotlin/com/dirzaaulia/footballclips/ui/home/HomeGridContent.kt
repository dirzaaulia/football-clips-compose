package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.BigLeaguesQuickFilterBar
import com.dirzaaulia.footballclips.ui.components.EmptyState
import com.dirzaaulia.footballclips.ui.components.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.components.HeroCarousel
import com.dirzaaulia.footballclips.ui.components.NativeAdStyle
import com.dirzaaulia.footballclips.ui.components.WebHighlightCard

@Composable
internal fun HomeGridContent(
    items: List<HighlightUiItem>,
    gridItems: List<HighlightUiItem>,
    featuredHighlight: HighlightUiItem?,
    isAdsRemoved: Boolean,
    showExternalHighlights: Boolean,
    selectedLeagueId: Int?,
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
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (featuredHighlight != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeroCarousel(
                    items = items,
                    isAdsRemoved = isAdsRemoved,
                    isCinematic = true,
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
                    }
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            if (isAdsRemoved) {
                BigLeaguesQuickFilterBar(
                    selectedLeagueId = selectedLeagueId,
                    onLeagueSelected = onLeagueSelected
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(0.7f)) {
                        BigLeaguesQuickFilterBar(
                            selectedLeagueId = selectedLeagueId,
                            onLeagueSelected = onLeagueSelected
                        )
                    }
                    Box(modifier = Modifier.weight(0.3f)) {
                        BannerAdItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                    }
                }
            }
        }

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
                key = { item -> item.uniqueId }
            ) { item ->
                val itemUniqueId = item.uniqueId
                val isRevealed = revealedItemIds.contains(itemUniqueId)

                when (item) {
                    is HighlightUiItem.SupabaseMatch, is HighlightUiItem.Highlight -> {
                        WebHighlightCard(
                            item = item,
                            isSpoilerFree = isSpoilerFreeMode,
                            isRevealed = isRevealed,
                            onRevealClick = { onRevealItem(itemUniqueId) },
                            onClick = {
                                onVideoClick(item) { isJumping ->
                                    viewModel.setPendingInterstitial(true)
                                    if (!isJumping) {
                                        viewModel.consumePendingInterstitial()
                                    }
                                }
                            }
                        )
                    }
                    is HighlightUiItem.BannerAd -> {
                        BannerAdItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16 / 9f),
                            style = if (showExternalHighlights) NativeAdStyle.HIGHLIGHT else NativeAdStyle.MATCH
                        )
                    }
                }
            }
        }

        if (!showExternalHighlights) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = onToggleExternalHighlights) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Explore More Highlights",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
        
        val isLoadingMore = (viewModel.uiState.value as? HomeState.Success)?.isLoadingMore == true
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
        
        item(span = { GridItemSpan(maxLineSpan) }) { 
            Spacer(modifier = Modifier.height(100.dp)) 
        }
    }
}
