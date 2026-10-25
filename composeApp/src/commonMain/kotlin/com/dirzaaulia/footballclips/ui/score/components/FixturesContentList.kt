package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.remote.SupportedClub
import com.dirzaaulia.footballclips.data.model.remote.isMatchForFavoriteClubs
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdItem

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
) {
    if (isGrid) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            state = gridState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                items = matches,
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

                    is HighlightUiItem.BannerAd -> BannerAdItem()
                    else -> {}
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
            items(
                items = matches,
                key = { it.uniqueId },
            ) { item ->
                when (item) {
                    is HighlightUiItem.OracleMatch -> {
                        val isFav = item.match.isMatchForFavoriteClubs(favoriteClubs, supportedClubs)
                        StandardMatchCard(
                            match = item.match,
                            isSpoilerFree = isSpoilerFreeMode,
                            isRevealed = revealedMatchIds.contains(item.match.id),
                            isFavorite = isFav,
                            onRevealClick = { onVideoClick(item) {} },
                            onWatchHighlightClick = { onVideoClick(item) {} },
                        )
                    }

                    is HighlightUiItem.BannerAd -> BannerAdItem()
                    else -> {}
                }
            }
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
