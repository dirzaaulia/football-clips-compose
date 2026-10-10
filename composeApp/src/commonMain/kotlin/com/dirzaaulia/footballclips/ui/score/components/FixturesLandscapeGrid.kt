package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.constants.SupportedClub
import com.dirzaaulia.footballclips.data.constants.SupportedClubsConstants
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.score.ScoreState
import com.dirzaaulia.footballclips.ui.score.ScoreViewModel
import com.dirzaaulia.footballclips.ui.score.getMatches

@Composable
internal fun FixturesLandscapeGrid(
    uiState: ScoreState,
    favoriteClubs: Set<String>,
    supportedClubs: List<SupportedClub>,
    isSpoilerFreeMode: Boolean,
    revealedMatchIds: Set<Long>,
    gridState: LazyGridState,
    viewModel: ScoreViewModel,
    onRevealMatch: (Long) -> Unit,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (uiState) {
            is ScoreState.Loading -> ExpressiveLoadingIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 5.dp,
            )
            is ScoreState.Error -> {
                EmptyState(
                    title = "Failed to Load Matches",
                    description = uiState.message,
                    onActionClick = { viewModel.getMatches() },
                    actionText = "Try Again",
                )
            }
            is ScoreState.Success -> {
                if (uiState.matches.isEmpty()) {
                    EmptyState(
                        title = "No Matches Scheduled",
                        description = "Try selecting a different date or competition.",
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 60.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(
                            items = uiState.matches,
                            key = { it.uniqueId },
                            span = { item ->
                                if (item is HighlightUiItem.BannerAd) GridItemSpan(maxLineSpan)
                                else GridItemSpan(1)
                            },
                        ) { item ->
                            when (item) {
                                is HighlightUiItem.SupabaseMatch -> {
                                    val isFav = SupportedClubsConstants.isMatchForFavoriteClubs(item.match, favoriteClubs, supportedClubs)
                                    MatchCard(
                                        match = item.match,
                                        isSpoilerFree = isSpoilerFreeMode,
                                        isRevealed = revealedMatchIds.contains(item.match.id),
                                        isFavorite = isFav,
                                        showTeamName = false,
                                        onRevealClick = { onRevealMatch(item.match.id) },
                                        onWatchHighlightClick = { onVideoClick(item) { } },
                                    )
                                }
                                is HighlightUiItem.BannerAd -> BannerAdItem()
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}
