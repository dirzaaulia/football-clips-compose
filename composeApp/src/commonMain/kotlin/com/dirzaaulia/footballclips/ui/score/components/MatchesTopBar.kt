package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    selectedSeason: Int = 2026,
    onSeasonSelected: (Int) -> Unit = {},
    selectedStatusFilter: MatchStatusFilter = MatchStatusFilter.ALL,
    onStatusFilterSelected: (MatchStatusFilter) -> Unit = {},
    isFiltersExpanded: Boolean = true,
    onToggleFilterExpanded: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMoreMenu by remember { mutableStateOf(false) }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Fixtures",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )

                            SeasonDropdownSelector(
                                selectedSeason = selectedSeason,
                                onSeasonSelected = onSeasonSelected,
                                isCompact = true
                            )
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            // 1. Prominent Live Pill (if any matches are live)
                            if (liveMatchCount > 0) {
                                Surface(
                                    onClick = onToggleLiveOnly,
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isLiveOnlyActive) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.15f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isLiveOnlyActive) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isLiveOnlyActive) Color.White else Color(0xFFEF4444))
                                        )
                                        Text(
                                            text = "LIVE ($liveMatchCount)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLiveOnlyActive) Color.White else Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }

                            // 2. My Clubs Button
                            IconButton(
                                onClick = {
                                    if (favoriteClubsCount == 0) {
                                        onOpenMyClubsSheet()
                                    } else {
                                        onToggleMyClubs()
                                    }
                                },
                                modifier = Modifier.size(36.dp)
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
                                        imageVector = if (isMyClubsActive || favoriteClubsCount > 0) Icons.Default.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "My Clubs",
                                        tint = if (isMyClubsActive || favoriteClubsCount > 0) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // 3. Unified More Options Menu (Spoiler-Free, Filters, Live Only)
                            Box {
                                IconButton(
                                    onClick = { showMoreMenu = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false },
                                    modifier = Modifier
                                        .width(220.dp)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    // Spoiler Free Toggle
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (isSpoilerFreeMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = if (isSpoilerFreeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        text = {
                                            Text(
                                                text = "Spoiler-Free Mode",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSpoilerFreeMode) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSpoilerFreeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        trailingIcon = {
                                            Switch(
                                                checked = isSpoilerFreeMode,
                                                onCheckedChange = null,
                                                modifier = Modifier.height(24.dp)
                                            )
                                        },
                                        onClick = {
                                            onToggleSpoilerFree()
                                            showMoreMenu = false
                                        },
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    )

                                    // Toggle Filter Bar Visibility
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (isFiltersExpanded) Icons.Default.FilterListOff else Icons.Default.FilterList,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        text = {
                                            Text(
                                                text = if (isFiltersExpanded) "Hide Filters" else "Show Filters",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            onToggleFilterExpanded()
                                            showMoreMenu = false
                                        },
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Fixtures & Live Scores",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )

                            SeasonDropdownSelector(
                                selectedSeason = selectedSeason,
                                onSeasonSelected = onSeasonSelected
                            )
                        }
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
                onSeasonSelected = onSeasonSelected,
                selectedStatusFilter = selectedStatusFilter,
                onStatusFilterSelected = onStatusFilterSelected
            )
        }
    }
}
