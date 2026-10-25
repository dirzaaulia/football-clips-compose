package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.remote.SupportedClub
import com.dirzaaulia.footballclips.data.model.remote.isMatchForFavoriteClubs
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.util.toProxyUrl
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.score.ScoreState
import com.dirzaaulia.footballclips.ui.score.ScoreViewModel
import com.dirzaaulia.footballclips.ui.score.getMatches
import com.dirzaaulia.footballclips.ui.score.selectCompetition

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
        modifier = modifier.fillMaxSize(),
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
                    val liveItems = uiState.matches.filter {
                        (it as? HighlightUiItem.OracleMatch)?.match?.isLive == true
                    }
                    val regularItems = uiState.matches.filter {
                        (it as? HighlightUiItem.OracleMatch)?.match?.isLive != true
                    }

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Left Pane: Live matches or League grid
                        Column(
                            modifier = Modifier
                                .weight(0.48f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Top,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (liveItems.isNotEmpty()) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary),
                                    )
                                    Text(
                                        text = if (liveItems.isNotEmpty()) "LIVE MATCH CENTRE" else "COMPETITIONS & LEAGUES",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp,
                                        color = if (liveItems.isNotEmpty()) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                                    )
                                }
                                if (liveItems.isNotEmpty()) {
                                    Text(
                                        text = "${liveItems.size} In-Play",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            if (liveItems.isNotEmpty()) {
                                val liveGrouped = liveItems
                                    .filterIsInstance<HighlightUiItem.OracleMatch>()
                                    .groupBy { it.match.competitionName.ifBlank { "Other Competitions" } }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(bottom = 60.dp),
                                ) {
                                    liveGrouped.forEach { (compName, items) ->
                                        item {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                AsyncImage(
                                                    model = items.first().match.leagueEmblemUrl.toProxyUrl(),
                                                    contentDescription = compName,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                                Text(
                                                    text = compName,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                            }
                                        }
                                        items(items, key = { it.uniqueId }) { item ->
                                            val isFav = item.match.isMatchForFavoriteClubs(favoriteClubs, supportedClubs)
                                            HeroLiveMatchCard(
                                                match = item.match,
                                                isFavorite = isFav,
                                                onWatchClick = { onVideoClick(item) { } },
                                            )
                                        }
                                    }
                                }
                            } else {
                                val groupedByLeague = regularItems
                                    .filterIsInstance<HighlightUiItem.OracleMatch>()
                                    .groupBy { it.match.competitionName }

                                LandscapeLeagueGrid(
                                    groupedByLeague = groupedByLeague,
                                    onCompetitionSelected = { compId -> viewModel.selectCompetition(compId) },
                                )
                            }
                        }

                        // Right Pane: Fixtures Feed with Filter FAB
                        Box(
                            modifier = Modifier
                                .weight(0.52f)
                                .fillMaxHeight(),
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "FIXTURES FEED",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(bottom = 70.dp),
                                ) {
                                    items(regularItems, key = { it.uniqueId }) { item ->
                                        when (item) {
                                            is HighlightUiItem.OracleMatch -> {
                                                val isFav = item.match.isMatchForFavoriteClubs(favoriteClubs, supportedClubs)
                                                StandardMatchCard(
                                                    match = item.match,
                                                    isSpoilerFree = isSpoilerFreeMode,
                                                    isRevealed = revealedMatchIds.contains(item.match.id),
                                                    isFavorite = isFav,
                                                    onRevealClick = { onVideoClick(item) { } },
                                                    onWatchHighlightClick = { onVideoClick(item) { } },
                                                )
                                            }
                                            is HighlightUiItem.BannerAd -> BannerAdItem()
                                            else -> {}
                                        }
                                    }
                                }
                            }

                            FloatingActionButton(
                                onClick = { viewModel.toggleFiltersExpanded() },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 12.dp, end = 4.dp),
                                shape = RoundedCornerShape(24.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter Matches",
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "Filter Matches",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
