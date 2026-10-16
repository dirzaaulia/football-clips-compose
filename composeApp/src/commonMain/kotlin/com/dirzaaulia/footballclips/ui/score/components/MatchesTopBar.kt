package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.ui.score.DateOption

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
    selectedSeason: Int = 2026,
    onSeasonSelected: (Int) -> Unit = {},
    isFiltersExpanded: Boolean = true,
    onToggleFilterExpanded: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column {
            if (!isWeb) {
                TopAppBar(
                    title = {
                        Text(
                            "Fixtures",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            IconButton(
                                onClick = onToggleFilterExpanded,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFiltersExpanded) Icons.Default.FilterListOff else Icons.Default.FilterList,
                                    contentDescription = "Toggle Filters",
                                    tint = if (isFiltersExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onOpenMyClubsSheet,
                                modifier = Modifier.size(38.dp)
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (favoriteClubsCount > 0) {
                                            Badge(
                                                containerColor = Color(0xFFFFB300),
                                                contentColor = Color.Black
                                            ) {
                                                Text(favoriteClubsCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (favoriteClubsCount > 0) Icons.Default.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "My Clubs",
                                        tint = if (favoriteClubsCount > 0) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onToggleSpoilerFree,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpoilerFreeMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Spoiler-Free Mode",
                                    tint = if (isSpoilerFreeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            "Fixtures & Live Scores",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = onToggleSpoilerFree,
                            modifier = Modifier.padding(end = 24.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpoilerFreeMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Spoiler-Free Mode",
                                tint = if (isSpoilerFreeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
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
                selectedSeason = selectedSeason,
                onSeasonSelected = onSeasonSelected
            )
        }
    }
}
