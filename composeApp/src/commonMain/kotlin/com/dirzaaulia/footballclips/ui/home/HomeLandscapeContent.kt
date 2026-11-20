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
import androidx.compose.foundation.layout.width
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
import com.dirzaaulia.footballclips.data.model.matchesLeague
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.card.HeroCarousel
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.theme.LocalThemeIsDark
import com.dirzaaulia.footballclips.ui.score.components.RecentMatchesSection
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
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val liveMatches by viewModel.liveMatches.collectAsState()
    val favoriteClubs by viewModel.favoriteClubs.collectAsState()
    val supportedClubs by viewModel.supportedClubs.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val selectedCompetitionId by viewModel.selectedCompetitionId.collectAsState()
    val isAllLeagues = filterState.selectedLeagues.isEmpty() &&
        selectedLeagueId == null &&
        selectedCompetitionId.isNullOrEmpty()
    val isDark = LocalThemeIsDark.current

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
            onCompetitionClick = { league, index ->
                if (isAllLeagues) {
                    val targetIndex = (if (liveMatches.isNotEmpty()) 1 else 0) +
                        (if (featuredHighlight != null) 1 else 0) + (index * 2)
                    coroutineScope.launch { listState.animateScrollToItem(targetIndex) }
                } else {
                    onLeagueSelected(league.leagueId, league.competitionId)
                }
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
                item {
                    EmptyState(
                        title = "No Highlights Found",
                        description = "Try selecting another league or check back later.",
                        onActionClick = onResetFilters,
                    )
                }
            } else if (isAllLeagues) {
                val (intlItems, regularItems) = partitionRecentHighlights(items, competitions)
                if (intlItems.isNotEmpty()) {
                    item(key = "recent-intl-matches-landscape") {
                        RecentMatchesSection(
                            matches = intlItems,
                            title = "Recent International Highlights",
                            onMatchClick = { selectedItem ->
                                onVideoClick(selectedItem) { isJumping ->
                                    viewModel.setPendingInterstitial(true)
                                    if (!isJumping) viewModel.consumePendingInterstitial()
                                }
                            },
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                }
                item(key = "recent-matches-section") {
                    RecentMatchesSection(
                        matches = regularItems.ifEmpty { items },
                        title = "Recent Highlights",
                        onMatchClick = { selectedItem ->
                            onVideoClick(selectedItem) { isJumping ->
                                viewModel.setPendingInterstitial(true)
                                if (!isJumping) viewModel.consumePendingInterstitial()
                            }
                        },
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                val nonAds = items.filter { it !is HighlightUiItem.BannerAd }
                val sortedCompetitions = sortCompetitionsWithIntlPriority(competitions, items)
                sortedCompetitions.forEach { league ->
                    renderLandscapeCompetitionSection(
                        league = league,
                        nonAds = nonAds,
                        revealedItemIds = revealedItemIds,
                        isSpoilerFreeMode = isSpoilerFreeMode,
                        showExternalHighlights = showExternalHighlights,
                        isAdsRemoved = isAdsRemoved,
                        selectedSeason = selectedSeason,
                        viewModel = viewModel,
                        isDark = isDark,
                        onVideoClick = onVideoClick,
                    )
                }
            } else {
                item(key = "filtered-landscape-recent-matches") {
                    RecentMatchesSection(
                        matches = items,
                        onMatchClick = { selectedItem ->
                            onVideoClick(selectedItem) { isJumping ->
                                viewModel.setPendingInterstitial(true)
                                if (!isJumping) viewModel.consumePendingInterstitial()
                            }
                        },
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
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
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.renderLandscapeCompetitionSection(
    league: BigLeague,
    nonAds: List<HighlightUiItem>,
    revealedItemIds: Set<String>,
    isSpoilerFreeMode: Boolean,
    showExternalHighlights: Boolean,
    isAdsRemoved: Boolean,
    selectedSeason: Int,
    viewModel: HomeViewModel,
    isDark: Boolean,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
) {
    val compName = league.name
    val emblem = league.getEmblemUrl(isDark)
    val displayMatches = nonAds.filter { it.matchesLeague(league) }.take(20)

    if (displayMatches.isEmpty()) return

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
        CompetitionHighlightsRow(
            displayMatches = displayMatches,
            league = league,
            compName = compName,
            emblem = emblem,
            isAdsRemoved = isAdsRemoved,
            selectedSeason = selectedSeason,
            viewModel = viewModel,
            cardWidth = 280.dp,
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
}
