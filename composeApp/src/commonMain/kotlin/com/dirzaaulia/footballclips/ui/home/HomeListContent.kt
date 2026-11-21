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
import com.dirzaaulia.footballclips.ui.components.card.HeroCarousel
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.components.filter.BigLeaguesQuickFilterBar

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
    val competitions by viewModel.competitions.collectAsState()
    val selectedCompetitionId by viewModel.selectedCompetitionId.collectAsState()
    val selectedLeagueId by viewModel.selectedLeagueId.collectAsState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp, start = 16.dp, end = 16.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (liveMatches.isNotEmpty()) {
            item(key = "home-live-matches-strip") {
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
            item(key = "home-hero-carousel") {
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

        if (competitions.isNotEmpty()) {
            item(key = "competition-horizontal-scroll") {
                BigLeaguesQuickFilterBar(
                    selectedLeagueId = selectedLeagueId,
                    selectedCompetitionId = selectedCompetitionId,
                    onLeagueSelected = { leagueId, compId ->
                        viewModel.selectLeague(leagueId, compId)
                    },
                    dynamicCompetitions = competitions,
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 2.dp),
                )
            }
        }

        if (items.isEmpty()) {
            item(key = "empty-highlights-state") {
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
                HighlightItemRow(
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
            item(key = "loading-more-indicator") {
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
            item(key = "load-more-error-retry") {
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
