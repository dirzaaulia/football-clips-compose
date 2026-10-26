package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.ui.components.filter.LeagueDropdownSelector
import com.dirzaaulia.footballclips.ui.score.ArchiveSeasonIndicator
import com.dirzaaulia.footballclips.ui.score.CompactDateNavigator
import com.dirzaaulia.footballclips.ui.score.DateOption
import com.dirzaaulia.footballclips.ui.score.MatchStatusFilter

@Composable
internal fun MatchesTopBarFilters(
    isWeb: Boolean,
    isFiltersExpanded: Boolean,
    horizontalPadding: Dp,
    dates: List<DateOption>,
    selectedDate: String?,
    onDateSelected: (String?) -> Unit,
    isMyClubsActive: Boolean,
    favoriteClubsCount: Int,
    onOpenMyClubsSheet: () -> Unit,
    onToggleMyClubs: () -> Unit,
    isLiveOnlyActive: Boolean = false,
    onToggleLiveOnly: () -> Unit = {},
    liveMatchCount: Int = 0,
    selectedCompetitionId: String?,
    onLeagueSelected: (Int?, String?) -> Unit,
    dynamicCompetitions: List<BigLeague>? = null,
    selectedSeason: Int = 2026,
    onSeasonSelected: (Int) -> Unit = {},
    selectedStatusFilter: MatchStatusFilter = MatchStatusFilter.ALL,
    onStatusFilterSelected: (MatchStatusFilter) -> Unit = {},
) {
    AnimatedVisibility(
        visible = isFiltersExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column {
            if (isWeb) {
                MatchesTopBarWebFilters(
                    horizontalPadding = horizontalPadding,
                    dates = dates,
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected,
                    isMyClubsActive = isMyClubsActive,
                    favoriteClubsCount = favoriteClubsCount,
                    onOpenMyClubsSheet = onOpenMyClubsSheet,
                    onToggleMyClubs = onToggleMyClubs,
                    isLiveOnlyActive = isLiveOnlyActive,
                    onToggleLiveOnly = onToggleLiveOnly,
                    liveMatchCount = liveMatchCount,
                    selectedCompetitionId = selectedCompetitionId,
                    onLeagueSelected = onLeagueSelected,
                    dynamicCompetitions = dynamicCompetitions,
                    selectedSeason = selectedSeason,
                    selectedStatusFilter = selectedStatusFilter,
                    onStatusFilterSelected = onStatusFilterSelected,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (selectedSeason == 2026) {
                            CompactDateNavigator(
                                dates = dates,
                                selectedDate = selectedDate,
                                onDateSelected = onDateSelected,
                            )
                        } else {
                            ArchiveSeasonIndicator(season = selectedSeason)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onToggleLiveOnly() }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isLiveOnlyActive || liveMatchCount > 0) Color(0xFFEF4444)
                                        else MaterialTheme.colorScheme.outline,
                                    ),
                            )
                            Text(
                                text = if (liveMatchCount > 0) "Live ($liveMatchCount)" else "Live",
                                fontSize = 11.sp,
                                fontWeight = if (isLiveOnlyActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isLiveOnlyActive) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            val thumbOffset by animateDpAsState(
                                targetValue = if (isLiveOnlyActive) 17.dp else 2.dp,
                                label = "liveSwitchThumb",
                            )
                            Box(
                                modifier = Modifier
                                    .width(34.dp)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(
                                        if (isLiveOnlyActive) Color(0xFFEF4444)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isLiveOnlyActive) Color(0xFFDC2626) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(9.dp),
                                    ),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = thumbOffset)
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (isLiveOnlyActive) Color.White else MaterialTheme.colorScheme.outline),
                                )
                            }
                        }
                    }

                    LeagueDropdownSelector(
                        selectedCompetitionId = if (isMyClubsActive) null else selectedCompetitionId,
                        dynamicCompetitions = dynamicCompetitions,
                        onLeagueSelected = { leagueId, competitionId ->
                            if (isMyClubsActive) onToggleMyClubs()
                            onLeagueSelected(leagueId, competitionId)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
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
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        )
                                    },
                                    modifier = Modifier.height(28.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
