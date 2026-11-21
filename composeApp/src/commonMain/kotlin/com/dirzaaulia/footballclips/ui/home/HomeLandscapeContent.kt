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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.card.HeroCarousel
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import kotlinx.coroutines.launch

@Composable
internal fun HomeLandscapeContent(
    items: List<HighlightUiItem>,
    featuredHighlight: HighlightUiItem?,
    isAdsRemoved: Boolean,
    showExternalHighlights: Boolean,
    selectedLeagueId: Int?,
    isLoadingMore: Boolean,
    isSpoilerFreeMode: Boolean = false,
    revealedItemIds: Set<String> = emptySet(),
    onRevealItem: (String) -> Unit = {},
    onSeeAllLiveMatches: () -> Unit = {},
    listState: LazyListState,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
    onLeagueSelected: (Int?, String?) -> Unit,
    onResetFilters: () -> Unit,
    onToggleExternalHighlights: () -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val competitions by viewModel.competitions.collectAsState()
    val liveMatches by viewModel.liveMatches.collectAsState()
    val favoriteClubs by viewModel.favoriteClubs.collectAsState()
    val supportedClubs by viewModel.supportedClubs.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val selectedCompetitionId by viewModel.selectedCompetitionId.collectAsState()

    Row(
        modifier = modifier.fillMaxSize(),
    ) {
        CompetitionSideRail(
            competitions = competitions,
            selectedCompetitionId = selectedCompetitionId ?: viewModel.selectedLeagueId.value?.toString(),
            onAllSelected = {
                onResetFilters()
                coroutineScope.launch { listState.animateScrollToItem(0) }
            },
            onCompetitionClick = { league, _ ->
                onLeagueSelected(league.leagueId, league.competitionId)
                coroutineScope.launch { listState.animateScrollToItem(0) }
            },
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (liveMatches.isNotEmpty()) {
                item(key = "landscape-live-matches") {
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
                item(key = "landscape-hero-carousel") {
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
                    )
                }
            }

            if (items.isEmpty()) {
                item(key = "landscape-empty-state") {
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
                item(key = "landscape-loading-more") {
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
                item(key = "landscape-load-more-error") {
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
