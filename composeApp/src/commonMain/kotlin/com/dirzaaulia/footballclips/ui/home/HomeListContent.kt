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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.ad.NativeAdStyle
import com.dirzaaulia.footballclips.ui.components.card.HeroCarousel
import com.dirzaaulia.footballclips.ui.components.card.WebHighlightCard
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.score.components.MatchCard

@Composable
internal fun HomeListContent(
    items: List<HighlightUiItem>,
    featuredHighlight: HighlightUiItem?,
    isAdsRemoved: Boolean,
    showExternalHighlights: Boolean,
    isLoadingMore: Boolean,
    isSpoilerFreeMode: Boolean = false,
    revealedItemIds: Set<String> = emptySet(),
    onRevealItem: (String) -> Unit = {},
    onSeeAllLiveMatches: () -> Unit = {},
    listState: LazyListState,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
    onResetFilters: () -> Unit,
    onToggleExternalHighlights: () -> Unit,
    viewModel: HomeViewModel,
) {
    val liveMatches by viewModel.liveMatches.collectAsState()
    val favoriteClubs by viewModel.favoriteClubs.collectAsState()
    val supportedClubs by viewModel.supportedClubs.collectAsState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp, start = 16.dp, end = 16.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (liveMatches.isNotEmpty()) {
            item {
                HomeLiveMatchSection(
                    liveMatches = liveMatches,
                    favoriteClubs = favoriteClubs,
                    supportedClubs = supportedClubs,
                    onMatchClick = { selectedItem ->
                        onVideoClick(selectedItem) { isJumping ->
                            viewModel.setPendingInterstitial(true)
                            if (!isJumping) viewModel.consumePendingInterstitial()
                        }
                    },
                    onSeeAllClick = onSeeAllLiveMatches,
                )
            }
        }

        if (featuredHighlight != null) {
            item {
                HeroCarousel(
                    items = items,
                    isAdsRemoved = isAdsRemoved,
                    isSpoilerFreeMode = isSpoilerFreeMode,
                    revealedItemIds = revealedItemIds,
                    onRevealItem = onRevealItem,
                    onVideoClick = { selectedItem ->
                        onVideoClick(selectedItem) { isJumping ->
                            viewModel.setPendingInterstitial(true)
                            if (!isJumping) viewModel.consumePendingInterstitial()
                        }
                    },
                )
            }
        }

        if (items.isEmpty()) {
            item {
                EmptyState(
                    title = "No Highlights Found",
                    description = "Try selecting another league or check back later.",
                    onActionClick = onResetFilters,
                )
            }
        } else {
            items(
                items = items,
                key = { item -> item.uniqueId },
            ) { item ->
                val itemUniqueId = item.uniqueId
                val isRevealed = revealedItemIds.contains(itemUniqueId)

                val handleVideoClick: () -> Unit = {
                    onVideoClick(item) { isJumping ->
                        viewModel.setPendingInterstitial(true)
                        if (!isJumping) viewModel.consumePendingInterstitial()
                    }
                }

                when (item) {
                    is HighlightUiItem.OracleMatch -> {
                        MatchCard(
                            match = item.match,
                            isSpoilerFree = isSpoilerFreeMode,
                            isRevealed = isRevealed,
                            onRevealClick = handleVideoClick,
                            onWatchHighlightClick = { handleVideoClick() },
                        )
                    }

                    is HighlightUiItem.Highlight -> {
                        WebHighlightCard(
                            item = item,
                            isSpoilerFree = isSpoilerFreeMode,
                            isRevealed = isRevealed,
                            onRevealClick = handleVideoClick,
                            onClick = { handleVideoClick() },
                        )
                    }

                    is HighlightUiItem.BannerAd -> {
                        BannerAdItem(
                            style = if (showExternalHighlights) NativeAdStyle.HIGHLIGHT else NativeAdStyle.MATCH,
                        )
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
                    contentAlignment = Alignment.Center,
                ) {
                    ExpressiveLoadingIndicator(
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 4.dp,
                    )
                }
            }
        } else if (loadMoreError != null) {
            item {
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
