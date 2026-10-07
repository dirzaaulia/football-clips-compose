package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import com.dirzaaulia.footballclips.ui.components.VerticalHighlightCard
import com.dirzaaulia.footballclips.ui.score.components.MatchCard

@Composable
internal fun HomeListContent(
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
    listState: LazyListState,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
    onLeagueSelected: (Int?, String?) -> Unit,
    onResetFilters: () -> Unit,
    onToggleExternalHighlights: () -> Unit,
    viewModel: HomeViewModel
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        if (featuredHighlight != null) {
            item {
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
                            if (!isJumping) {
                                viewModel.consumePendingInterstitial()
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
                )
            }
        }

        item {
            BigLeaguesQuickFilterBar(
                selectedLeagueId = selectedLeagueId,
                onLeagueSelected = onLeagueSelected
            )
        }

        if (items.isEmpty()) {
            item {
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
                    is HighlightUiItem.SupabaseMatch -> {
                        MatchCard(
                            match = item.match,
                            onWatchHighlightClick = {
                                onVideoClick(item) { isJumping ->
                                    viewModel.setPendingInterstitial(true)
                                    if (!isJumping) {
                                        viewModel.consumePendingInterstitial()
                                    }
                                }
                            },
                            showStatus = false,
                            isSpoilerFree = isSpoilerFreeMode,
                            isRevealed = isRevealed,
                            onRevealClick = { onRevealItem(itemUniqueId) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    is HighlightUiItem.Highlight -> {
                        VerticalHighlightCard(
                            highlight = item.highlight,
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
                            style = if (showExternalHighlights) NativeAdStyle.HIGHLIGHT else NativeAdStyle.MATCH
                        )
                    }
                }
            }
        }
        
        if (!showExternalHighlights) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
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

        val loadMoreError = (viewModel.uiState.value as? HomeState.Success)?.loadMoreError

        if (isLoadingMore && loadMoreError == null) {
            item {
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
            item {
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
