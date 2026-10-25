package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.ui.components.filter.LeagueDropdownSelector
import com.dirzaaulia.footballclips.ui.score.ArchiveSeasonIndicator
import com.dirzaaulia.footballclips.ui.score.DateOption
import com.dirzaaulia.footballclips.ui.score.MatchStatusFilter
import com.dirzaaulia.footballclips.ui.score.WebDateSelectorCard

@Composable
internal fun MatchesTopBarWebFilters(
    horizontalPadding: Dp,
    dates: List<DateOption>,
    selectedDate: String?,
    onDateSelected: (String?) -> Unit,
    isMyClubsActive: Boolean,
    favoriteClubsCount: Int,
    onOpenMyClubsSheet: () -> Unit,
    onToggleMyClubs: () -> Unit,
    isLiveOnlyActive: Boolean,
    onToggleLiveOnly: () -> Unit,
    liveMatchCount: Int,
    selectedCompetitionId: String?,
    onLeagueSelected: (Int?, String?) -> Unit,
    dynamicCompetitions: List<BigLeague>?,
    selectedSeason: Int,
    selectedStatusFilter: MatchStatusFilter,
    onStatusFilterSelected: (MatchStatusFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = horizontalPadding, end = horizontalPadding, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (selectedSeason == 2026) {
                WebDateSelectorCard(
                    dates = dates,
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected,
                )

                listOf(
                    MatchStatusFilter.ALL to "All",
                    MatchStatusFilter.FINISHED to "Finished",
                    MatchStatusFilter.UPCOMING to "Upcoming",
                ).forEach { (status, label) ->
                    val isSelected = selectedStatusFilter == status && !isLiveOnlyActive
                    FilterChip(
                        selected = isSelected,
                        onClick = { onStatusFilterSelected(status) },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                    )
                }

                FilterChip(
                    selected = isLiveOnlyActive,
                    onClick = onToggleLiveOnly,
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (isLiveOnlyActive || liveMatchCount > 0) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    CircleShape,
                                ),
                        )
                    },
                    label = {
                        Text(
                            text = if (liveMatchCount > 0) "Live ($liveMatchCount)" else "Live",
                            fontWeight = if (isLiveOnlyActive) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (isLiveOnlyActive) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                )
            } else {
                ArchiveSeasonIndicator(season = selectedSeason)
            }

            FilterChip(
                selected = isMyClubsActive,
                onClick = {
                    if (favoriteClubsCount == 0) {
                        onOpenMyClubsSheet()
                    } else {
                        onToggleMyClubs()
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isMyClubsActive || favoriteClubsCount > 0) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                label = {
                    Text(
                        text = if (favoriteClubsCount > 0) "My Clubs ($favoriteClubsCount)" else "My Clubs",
                        fontWeight = if (isMyClubsActive) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                    )
                },
                shape = RoundedCornerShape(20.dp),
            )
        }

        LeagueDropdownSelector(
            selectedCompetitionId = if (isMyClubsActive) null else selectedCompetitionId,
            dynamicCompetitions = dynamicCompetitions,
            onLeagueSelected = { leagueId, competitionId ->
                if (isMyClubsActive) onToggleMyClubs()
                onLeagueSelected(leagueId, competitionId)
            },
        )
    }
}
