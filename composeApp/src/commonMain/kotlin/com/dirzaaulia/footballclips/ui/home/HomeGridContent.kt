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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as rowItems
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.matchesLeague
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.card.HeroCarousel
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.ad.NativeAdStyle
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.score.components.getLeagueEmblemUrl

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
    onSeeAllLiveMatches: () -> Unit = {},
    gridState: LazyGridState,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
    onLeagueSelected: (Int?, String?) -> Unit,
    onResetFilters: () -> Unit,
    onToggleExternalHighlights: () -> Unit,
    viewModel: HomeViewModel,
) {
    val liveMatches by viewModel.liveMatches.collectAsState()
    val favoriteClubs by viewModel.favoriteClubs.collectAsState()
    val supportedClubs by viewModel.supportedClubs.collectAsState()
    val competitions by viewModel.competitions.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    var collapsedCompIds by remember { mutableStateOf(emptySet<String>()) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 220.dp),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        if (liveMatches.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
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
                    },
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            HomeGridFilterBar(
                isAdsRemoved = isAdsRemoved,
                selectedLeagueId = selectedLeagueId,
                competitions = competitions,
                onLeagueSelected = onLeagueSelected,
            )
        }

        if (items.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(
                    title = "No Highlights Found",
                    description = "Try selecting another league or check back later.",
                    onActionClick = onResetFilters,
                )
            }
        } else if (selectedLeagueId == null && viewModel.selectedLeagues.isEmpty()) {
            val nonAds = gridItems.filter { it !is HighlightUiItem.BannerAd }
            competitions.forEachIndexed { index, league ->
                val compName = league.name
                val emblem = league.logo.ifBlank { league.emblemUrl ?: getLeagueEmblemUrl(compName) }
                val displayMatches = nonAds.filter { it.matchesLeague(league) }.take(20)

                val isExpanded = !collapsedCompIds.contains(league.id)
                item(
                    key = "sec-${league.id}",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    HomeCompetitionSectionHeader(
                        competitionName = compName,
                        competitionEmblemUrl = emblem,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            collapsedCompIds = if (isExpanded) {
                                collapsedCompIds + league.id
                            } else {
                                collapsedCompIds - league.id
                            }
                        },
                        onSeeAll = {
                            if (league.leagueId != null) {
                                viewModel.selectLeague(league.leagueId, league.competitionId)
                            } else {
                                viewModel.selectCompetitionByName(compName)
                            }
                        },
                    )
                }
                if (isExpanded) {
                    item(
                        key = "row-${league.id}",
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        ) {
                            if (displayMatches.isNotEmpty()) {
                                rowItems(
                                    items = displayMatches,
                                    key = { it.uniqueId },
                                ) { item ->
                                    Box(modifier = Modifier.width(320.dp)) {
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
                                item(key = "see-all-${league.id}") {
                                    Box(modifier = Modifier.width(220.dp)) {
                                        HomeSeeAllCompetitionCard(
                                            competitionName = compName,
                                            competitionEmblemUrl = emblem,
                                            onSeeAll = {
                                                if (league.leagueId != null) {
                                                    viewModel.selectLeague(league.leagueId, league.competitionId)
                                                } else {
                                                    viewModel.selectCompetitionByName(compName)
                                                }
                                            },
                                        )
                                    }
                                }
                            } else {
                                item(key = "empty-${league.id}") {
                                    UpcomingCompetitionCard(
                                        league = league,
                                        selectedSeason = selectedSeason,
                                        onSeeAll = {
                                            if (league.leagueId != null) {
                                                viewModel.selectLeague(league.leagueId, league.competitionId)
                                            } else {
                                                viewModel.selectCompetitionByName(compName)
                                            }
                                        },
                                        onSwitchSeason = { viewModel.setSeason(it) },
                                    )
                                }
                            }
                        }
                    }
                }

                if (!isAdsRemoved && (index + 1) % 3 == 0 && index < competitions.size - 1) {
                    item(
                        key = "ad-between-${league.id}",
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        BannerAdItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            style = NativeAdStyle.MATCH,
                        )
                    }
                }
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

        val isLoadingMore = (viewModel.uiState.value as? HomeState.Success)?.isLoadingMore == true
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

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

