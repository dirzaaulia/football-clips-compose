package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dirzaaulia.footballclips.ui.score.getMatches
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
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.components.filter.SeasonDropdownSelector
import com.dirzaaulia.footballclips.ui.score.ArchiveSeasonIndicator
import com.dirzaaulia.footballclips.ui.score.DateOption
import com.dirzaaulia.footballclips.ui.score.MatchStatusFilter
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
    val selectedSeason by viewModel.selectedSeason.collectAsState()
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fixtures",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                SeasonDropdownSelector(
                    selectedSeason = selectedSeason,
                    onSeasonSelected = viewModel::setSeason
                )
            }

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
                    // Sub-Column 1: Calendar Dates 2-Column Vertical Grid or Archive Indicator
                    if (selectedSeason == 2026) {
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
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(0.52f)
                                .fillMaxHeight()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ArchiveSeasonIndicator(season = selectedSeason)
                            Text(
                                text = "Viewing archive fixtures and verified highlights.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        if (selectedSeason == 2026) {
                            item {
                                val selectedStatus by viewModel.selectedStatusFilter.collectAsState()
                                val isLiveOnly by viewModel.isLiveOnlyFilterActive.collectAsState()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        MatchStatusFilter.ALL to "All",
                                        MatchStatusFilter.FINISHED to "Fin",
                                        MatchStatusFilter.UPCOMING to "Upc"
                                    ).forEach { (status, label) ->
                                        val isSelected = selectedStatus == status && !isLiveOnly
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { viewModel.selectStatusFilter(status) },
                                            label = {
                                                Text(
                                                    text = label,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            },
                                            modifier = Modifier.weight(1f).height(28.dp),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }

                            item {
                                val isLiveOnly by viewModel.isLiveOnlyFilterActive.collectAsState()
                                val liveCount by viewModel.liveMatchCount.collectAsState()
                                FilterChip(
                                    selected = isLiveOnly,
                                    onClick = { viewModel.toggleLiveOnlyFilter() },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    if (isLiveOnly || liveCount > 0) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    CircleShape
                                                )
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = if (liveCount > 0) "Live ($liveCount)" else "Live",
                                            fontWeight = if (isLiveOnly) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = if (isLiveOnly) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

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

        FixturesLandscapeGrid(
            uiState = uiState,
            favoriteClubs = favoriteClubs,
            supportedClubs = supportedClubs,
            isSpoilerFreeMode = isSpoilerFreeMode,
            revealedMatchIds = revealedMatchIds,
            gridState = gridState,
            viewModel = viewModel,
            onRevealMatch = onRevealMatch,
            onVideoClick = onVideoClick,
            modifier = Modifier.weight(0.58f),
        )
    }
}
