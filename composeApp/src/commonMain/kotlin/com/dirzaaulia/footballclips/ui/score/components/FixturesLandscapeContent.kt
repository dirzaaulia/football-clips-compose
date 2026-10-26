package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.remote.SupportedClub
import com.dirzaaulia.footballclips.ui.components.filter.LeagueDropdownSelector
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
    viewModel: ScoreViewModel,
) {
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val dynamicCompetitions by viewModel.competitions.collectAsState()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsState()
    val isLiveOnlyActive by viewModel.isLiveOnlyFilterActive.collectAsState()
    val liveMatchCount by viewModel.liveMatchCount.collectAsState()
    val dateRowState = rememberLazyListState()

    LaunchedEffect(selectedDate) {
        val selectedIndex = dates.indexOfFirst { it.date == selectedDate }
        if (selectedIndex >= 0) {
            val scrollIndex = (selectedIndex - 1).coerceAtLeast(0)
            dateRowState.animateScrollToItem(scrollIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = 8.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
    ) {
        FixturesLandscapeHeader(
            selectedSeason = selectedSeason,
            onSeasonSelected = viewModel::setSeason,
            selectedStatusFilter = selectedStatusFilter,
            onStatusFilterSelected = viewModel::selectStatusFilter,
            isLiveOnlyActive = isLiveOnlyActive,
            onToggleLiveOnly = viewModel::toggleLiveOnlyFilter,
            liveMatchCount = liveMatchCount,
            favoriteClubs = favoriteClubs,
            isMyClubsActive = isMyClubsActive,
            onToggleMyClubs = onToggleMyClubs,
            onOpenMyClubsSheet = onOpenMyClubsSheet,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LazyRow(
                state = dateRowState,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(dates) { dateOption ->
                    val isSelected = dateOption.date == selectedDate
                    Surface(
                        onClick = { onDateSelected(dateOption.date) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        ),
                        modifier = Modifier.height(36.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp),
                        ) {
                            Text(
                                text = "${dateOption.displayDay.uppercase()} ${dateOption.displayDate}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (dateOption.isToday) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            LeagueDropdownSelector(
                selectedCompetitionId = selectedCompetitionId ?: "ALL",
                dynamicCompetitions = dynamicCompetitions,
                onLeagueSelected = onLeagueSelected,
                showAll = true,
            )
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
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
    }
}
