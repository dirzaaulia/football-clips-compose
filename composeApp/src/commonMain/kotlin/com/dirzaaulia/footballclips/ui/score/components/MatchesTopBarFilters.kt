package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import com.dirzaaulia.footballclips.ui.components.filter.LeagueDropdownSelector
import com.dirzaaulia.footballclips.ui.score.ArchiveSeasonIndicator
import com.dirzaaulia.footballclips.ui.score.CompactDateNavigator
import com.dirzaaulia.footballclips.ui.score.DateOption
import com.dirzaaulia.footballclips.ui.score.MatchStatusFilter
import com.dirzaaulia.footballclips.ui.score.WebDateSelectorCard

@OptIn(ExperimentalMaterial3Api::class)
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
    dynamicCompetitions: List<com.dirzaaulia.footballclips.data.constants.BigLeague>? = null,
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
                Row(
                    modifier = Modifier
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
                                MatchStatusFilter.UPCOMING to "Upcoming"
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
                                                CircleShape
                                            )
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (liveMatchCount > 0) "Live ($liveMatchCount)" else "Live",
                                        fontWeight = if (isLiveOnlyActive) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isLiveOnlyActive) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Row 1: Date Navigator & League Dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedSeason == 2026) {
                            CompactDateNavigator(
                                dates = dates,
                                selectedDate = selectedDate,
                                onDateSelected = onDateSelected
                            )
                        } else {
                            ArchiveSeasonIndicator(season = selectedSeason)
                        }

                        LeagueDropdownSelector(
                            selectedCompetitionId = if (isMyClubsActive) null else selectedCompetitionId,
                            dynamicCompetitions = dynamicCompetitions,
                            onLeagueSelected = { leagueId, competitionId ->
                                if (isMyClubsActive) onToggleMyClubs()
                                onLeagueSelected(leagueId, competitionId)
                            }
                        )
                    }

                    // Row 2: Status Filter Chips (All, Finished, Upcoming) on left & Live Match Switch on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Status Filter Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
                                MatchStatusFilter.ALL to "All",
                                MatchStatusFilter.FINISHED to "Finished",
                                MatchStatusFilter.UPCOMING to "Upcoming"
                            ).forEach { (status, label) ->
                                val isSelected = selectedStatusFilter == status && !isLiveOnlyActive
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onStatusFilterSelected(status) },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    modifier = Modifier.height(28.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        selectedBorderColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        // Right: Live Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onToggleLiveOnly() }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isLiveOnlyActive || liveMatchCount > 0) Color(0xFFEF4444) 
                                        else MaterialTheme.colorScheme.outline
                                    )
                            )
                            Text(
                                text = if (liveMatchCount > 0) "Live ($liveMatchCount)" else "Live",
                                fontSize = 11.sp,
                                fontWeight = if (isLiveOnlyActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isLiveOnlyActive) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Compact Custom Switch: 34dp x 18dp, thumb 12dp
                            val thumbOffset by animateDpAsState(
                                targetValue = if (isLiveOnlyActive) 17.dp else 2.dp,
                                label = "liveSwitchThumb"
                            )
                            Box(
                                modifier = Modifier
                                    .width(34.dp)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(
                                        if (isLiveOnlyActive) Color(0xFFEF4444)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isLiveOnlyActive) Color(0xFFDC2626) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(9.dp)
                                    ),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = thumbOffset)
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (isLiveOnlyActive) Color.White else MaterialTheme.colorScheme.outline)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
