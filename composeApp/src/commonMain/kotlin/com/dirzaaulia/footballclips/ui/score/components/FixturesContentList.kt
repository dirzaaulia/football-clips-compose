package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.matchesLeague
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.data.model.remote.SupportedClub
import com.dirzaaulia.footballclips.data.model.remote.isMatchForFavoriteClubs
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.ad.NativeAdStyle
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.theme.LocalThemeIsDark

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FixturesContentList(
    isGrid: Boolean,
    matches: List<HighlightUiItem>,
    gridState: LazyGridState,
    listState: LazyListState,
    favoriteClubs: Set<String>,
    supportedClubs: List<SupportedClub>,
    isSpoilerFreeMode: Boolean,
    revealedMatchIds: Set<Long>,
    onRevealMatch: (Long) -> Unit,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    isAdsRemoved: Boolean = false,
    dynamicCompetitions: List<BigLeague> = emptyList(),
    selectedCompetitionId: String? = null,
) {
    val isDark = LocalThemeIsDark.current
    var collapsedCompIds by remember { mutableStateOf(emptySet<String>()) }

    val groupedItems = remember(matches, dynamicCompetitions, selectedCompetitionId, isAdsRemoved) {
        val rawMatches = matches.filter { it !is HighlightUiItem.BannerAd }
        if (!selectedCompetitionId.isNullOrEmpty()) {
            val listWithAds = if (isAdsRemoved) rawMatches else injectFixtureAds(rawMatches, selectedCompetitionId)
            listOf(null to listWithAds)
        } else {
            val remaining = rawMatches.toMutableList()
            val groups = mutableListOf<Pair<BigLeague?, List<HighlightUiItem>>>()

            dynamicCompetitions.forEach { comp ->
                val compMatches = remaining.filter { it.matchesLeague(comp) }
                if (compMatches.isNotEmpty()) {
                    val listWithAds = if (isAdsRemoved) compMatches else injectFixtureAds(compMatches, comp.id)
                    groups.add(comp to listWithAds)
                    remaining.removeAll(compMatches)
                }
            }
            if (remaining.isNotEmpty()) {
                val listWithAds = if (isAdsRemoved) remaining else injectFixtureAds(remaining, "other")
                groups.add(null to listWithAds)
            }
            groups
        }
    }

    if (isGrid) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            state = gridState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!isAdsRemoved) {
                item(key = "fixtures-top-admob-banner", span = { GridItemSpan(maxLineSpan) }) {
                    BannerAdItem(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        style = NativeAdStyle.MATCH,
                    )
                }
            }
            if (matches.isEmpty()) {
                item(key = "empty-day-fixtures-grid", span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        title = "No Matches Scheduled",
                        description = "Try selecting a different date or competition.",
                    )
                }
            }
            groupedItems.forEachIndexed { groupIndex, (comp, compMatches) ->
                val isExpanded = comp == null || !collapsedCompIds.contains(comp.id)
                if (comp != null) {
                    item(key = "header-${comp.id}", span = { GridItemSpan(maxLineSpan) }) {
                        LeagueHeader(
                            competitionName = comp.name,
                            competitionEmblemUrl = comp.getEmblemUrl(isDark),
                            competitionId = comp.competitionId,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                collapsedCompIds = if (isExpanded) {
                                    collapsedCompIds + comp.id
                                } else {
                                    collapsedCompIds - comp.id
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                        )
                    }
                }
                if (isExpanded) {
                    items(
                        items = compMatches,
                        key = { it.uniqueId },
                        span = { item ->
                            if (item is HighlightUiItem.BannerAd) {
                                GridItemSpan(maxLineSpan)
                            } else {
                                GridItemSpan(1)
                            }
                        },
                    ) { item ->
                        when (item) {
                            is HighlightUiItem.OracleMatch -> {
                                val isFav = item.match.isMatchForFavoriteClubs(favoriteClubs, supportedClubs)
                                WebFixtureCard(
                                    match = item.match,
                                    isSpoilerFree = isSpoilerFreeMode,
                                    isRevealed = revealedMatchIds.contains(item.match.id),
                                    isFavorite = isFav,
                                    onRevealClick = { onVideoClick(item) {} },
                                    onClick = { onVideoClick(item) {} },
                                )
                            }
                            is HighlightUiItem.BannerAd -> {
                                if (!isAdsRemoved) BannerAdItem(style = NativeAdStyle.MATCH)
                            }
                            else -> {}
                        }
                    }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!isAdsRemoved) {
                item(key = "fixtures-top-admob-banner") {
                    BannerAdItem(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        style = NativeAdStyle.MATCH,
                    )
                }
            }
            if (matches.isEmpty()) {
                item(key = "empty-day-fixtures-list") {
                    EmptyState(
                        title = "No Matches Scheduled",
                        description = "Try selecting a different date or competition.",
                    )
                }
            }
            groupedItems.forEachIndexed { groupIndex, (comp, compMatches) ->
                val isExpanded = comp == null || !collapsedCompIds.contains(comp.id)
                if (comp != null) {
                    stickyHeader(key = "header-${comp.id}") {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            LeagueHeader(
                                competitionName = comp.name,
                                competitionEmblemUrl = comp.getEmblemUrl(isDark),
                                competitionId = comp.competitionId,
                                isExpanded = isExpanded,
                                onToggleExpand = {
                                    collapsedCompIds = if (isExpanded) {
                                        collapsedCompIds + comp.id
                                    } else {
                                        collapsedCompIds - comp.id
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                            )
                        }
                    }
                }
                if (isExpanded) {
                    items(
                        items = compMatches,
                        key = { it.uniqueId },
                    ) { item ->
                        when (item) {
                            is HighlightUiItem.OracleMatch -> {
                                val isFav = item.match.isMatchForFavoriteClubs(favoriteClubs, supportedClubs)
                                StandardMatchCard(
                                    match = item.match,
                                    showCompetitionHeader = (comp == null),
                                    isSpoilerFree = isSpoilerFreeMode,
                                    isRevealed = revealedMatchIds.contains(item.match.id),
                                    isFavorite = isFav,
                                    onRevealClick = { onVideoClick(item) {} },
                                    onWatchHighlightClick = { onVideoClick(item) {} },
                                )
                            }
                            is HighlightUiItem.BannerAd -> {
                                if (!isAdsRemoved) BannerAdItem(style = NativeAdStyle.MATCH)
                            }
                            else -> {}
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

private fun injectFixtureAds(items: List<HighlightUiItem>, prefix: String = "feed"): List<HighlightUiItem> {
    if (items.size < 6) return items
    val result = mutableListOf<HighlightUiItem>()
    for (i in items.indices) {
        result.add(items[i])
        if ((i + 1) % 5 == 0 && i < items.size - 1) {
            result.add(HighlightUiItem.BannerAd("fixture-$prefix-ad-${i / 5}"))
        }
    }
    return result
}
