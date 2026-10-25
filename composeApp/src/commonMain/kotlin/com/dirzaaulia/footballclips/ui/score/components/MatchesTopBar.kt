package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.ui.components.filter.SeasonDropdownSelector
import com.dirzaaulia.footballclips.ui.score.DateOption
import com.dirzaaulia.footballclips.ui.score.MatchStatusFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesTopBar(
    isWeb: Boolean,
    isSpoilerFreeMode: Boolean,
    onToggleSpoilerFree: () -> Unit,
    favoriteClubsCount: Int,
    isMyClubsActive: Boolean,
    onToggleMyClubs: () -> Unit,
    onOpenMyClubsSheet: () -> Unit,
    isLiveOnlyActive: Boolean = false,
    onToggleLiveOnly: () -> Unit = {},
    liveMatchCount: Int = 0,
    dates: List<DateOption>,
    selectedDate: String?,
    onDateSelected: (String?) -> Unit,
    selectedCompetitionId: String?,
    onLeagueSelected: (Int?, String?) -> Unit,
    dynamicCompetitions: List<BigLeague>? = null,
    selectedSeason: Int = 2026,
    onSeasonSelected: (Int) -> Unit = {},
    selectedStatusFilter: MatchStatusFilter = MatchStatusFilter.ALL,
    onStatusFilterSelected: (MatchStatusFilter) -> Unit = {},
    isFiltersExpanded: Boolean = true,
    onToggleFilterExpanded: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        modifier = modifier,
    ) {
        Column {
            if (!isWeb) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                "Fixtures",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                            )

                            SeasonDropdownSelector(
                                selectedSeason = selectedSeason,
                                onSeasonSelected = onSeasonSelected,
                                isCompact = true,
                            )
                        }
                    },
                    actions = {
                        MatchesTopBarMobileActions(
                            liveMatchCount = liveMatchCount,
                            isLiveOnlyActive = isLiveOnlyActive,
                            onToggleLiveOnly = onToggleLiveOnly,
                            favoriteClubsCount = favoriteClubsCount,
                            isMyClubsActive = isMyClubsActive,
                            onToggleMyClubs = onToggleMyClubs,
                            onOpenMyClubsSheet = onOpenMyClubsSheet,
                            isSpoilerFreeMode = isSpoilerFreeMode,
                            onToggleSpoilerFree = onToggleSpoilerFree,
                            isFiltersExpanded = isFiltersExpanded,
                            onToggleFilterExpanded = onToggleFilterExpanded,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    ),
                )
            } else {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                "Fixtures",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                            )

                            SeasonDropdownSelector(
                                selectedSeason = selectedSeason,
                                onSeasonSelected = onSeasonSelected,
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onToggleSpoilerFree,
                            modifier = Modifier.padding(end = 24.dp),
                        ) {
                            Icon(
                                imageVector = if (isSpoilerFreeMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Spoiler-Free Mode",
                                tint = if (isSpoilerFreeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    ),
                )
            }

            val horizontalPadding = if (isWeb) 32.dp else 16.dp

            MatchesTopBarFilters(
                isWeb = isWeb,
                isFiltersExpanded = isFiltersExpanded,
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
                onSeasonSelected = onSeasonSelected,
                selectedStatusFilter = selectedStatusFilter,
                onStatusFilterSelected = onStatusFilterSelected,
            )
        }
    }
}
