package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.constants.BigLeaguesConstants
import com.dirzaaulia.footballclips.data.constants.SupportedClub
import com.dirzaaulia.footballclips.data.constants.SupportedClubsConstants
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.EmptyState
import com.dirzaaulia.footballclips.ui.components.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.score.DateOption
import com.dirzaaulia.footballclips.ui.score.ScoreState
import com.dirzaaulia.footballclips.ui.score.ScoreViewModel

@Composable
fun FixturesLandscapeContent(
    uiState: ScoreState,
    dates: List<DateOption>,
    selectedDate: String?,
    onDateSelected: (String?) -> Unit,
    selectedCompetitionId: String?,
    onLeagueSelected: (Int?, String?) -> Unit,
    favoriteClubs: Set<String>,
    supportedClubs: List<SupportedClub>,
    isMyClubsActive: Boolean,
    onToggleMyClubs: () -> Unit,
    onOpenMyClubsSheet: () -> Unit,
    isSpoilerFreeMode: Boolean,
    revealedMatchIds: Set<Long>,
    onRevealMatch: (Long) -> Unit,
    gridState: LazyGridState,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
    viewModel: ScoreViewModel
) {
    val isFiltersExpanded by viewModel.isFiltersExpanded.collectAsState()
    val dateGridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()

    LaunchedEffect(selectedDate) {
        val selectedIndex = dates.indexOfFirst { it.date == selectedDate }
        if (selectedIndex >= 0) {
            val scrollIndex = (selectedIndex - 2).coerceAtLeast(0)
            dateGridState.animateScrollToItem(scrollIndex)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Left Column (~42% width): Title + 2 Sub-Columns (Calendar 2-col grid + League filter vertical list)
        Column(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Fixtures",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            AnimatedVisibility(
                visible = isFiltersExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sub-Column 1: Calendar Dates 2-Column Vertical Grid
                    LazyVerticalGrid(
                        state = dateGridState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .weight(0.52f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(dates) { dateOption ->
                            LandscapeDateChip(
                                option = dateOption,
                                isSelected = dateOption.date == selectedDate,
                                onClick = { onDateSelected(dateOption.date) }
                            )
                        }
                    }

                    // Sub-Column 2: League Filters Vertical List
                    LazyColumn(
                        modifier = Modifier
                            .weight(0.48f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = isMyClubsActive,
                                onClick = {
                                    if (favoriteClubs.isEmpty()) onOpenMyClubsSheet()
                                    else onToggleMyClubs()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isMyClubsActive || favoriteClubs.isNotEmpty()) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (favoriteClubs.isNotEmpty()) "My Clubs (${favoriteClubs.size})" else "My Clubs",
                                        fontWeight = if (isMyClubsActive) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        items(BigLeaguesConstants.leagues) { league ->
                            val isSelected = if (isMyClubsActive) false else league.competitionId == selectedCompetitionId
                            LandscapeLeagueChip(
                                name = league.name,
                                logo = league.logo,
                                isSelected = isSelected,
                                onClick = {
                                    if (isMyClubsActive) onToggleMyClubs()
                                    onLeagueSelected(league.leagueId, league.competitionId)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Right Column (~58% width): 2-Column Grid Match Fixtures List
        Box(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight()
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is ScoreState.Loading -> ExpressiveLoadingIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 5.dp
                )
                is ScoreState.Error -> {
                    EmptyState(
                        title = "Failed to Load Matches",
                        description = uiState.message,
                        onActionClick = { viewModel.getMatches() },
                        actionText = "Try Again"
                    )
                }
                is ScoreState.Success -> {
                    if (uiState.matches.isEmpty()) {
                        EmptyState(
                            title = "No Matches Scheduled",
                            description = "Try selecting a different date or competition."
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = gridState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 60.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = uiState.matches,
                                key = { it.uniqueId },
                                span = { item ->
                                    if (item is HighlightUiItem.BannerAd) GridItemSpan(maxLineSpan)
                                    else GridItemSpan(1)
                                }
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
                                            onWatchHighlightClick = { onVideoClick(item) { } }
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
}
