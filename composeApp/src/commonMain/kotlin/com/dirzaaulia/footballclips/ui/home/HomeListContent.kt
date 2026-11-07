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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
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
import com.dirzaaulia.footballclips.data.model.matchesLeague
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.card.HeroCarousel
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.score.components.getLeagueEmblemUrl

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
    val filterState by viewModel.filterState.collectAsState()
    val competitions by viewModel.competitions.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val isAllLeagues = filterState.selectedLeagues.isEmpty()

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
        } else if (isAllLeagues) {
            val nonAds = items.filter { it !is HighlightUiItem.BannerAd }
            competitions.forEach { league ->
                val compName = league.name
                val emblem = league.logo.ifBlank { league.emblemUrl ?: getLeagueEmblemUrl(compName) }
                val displayMatches = nonAds.filter { it.matchesLeague(league) }.take(20)

                item(key = "section-${league.id}") {
                    HomeCompetitionSectionHeader(
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
                item(key = "row-${league.id}") {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    ) {
                        if (displayMatches.isNotEmpty()) {
                            items(
                                items = displayMatches,
                                key = { it.uniqueId },
                            ) { item ->
                                Box(modifier = Modifier.width(280.dp)) {
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
                            item(key = "see-all-${league.id}") {
                                Box(modifier = Modifier.width(200.dp)) {
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

