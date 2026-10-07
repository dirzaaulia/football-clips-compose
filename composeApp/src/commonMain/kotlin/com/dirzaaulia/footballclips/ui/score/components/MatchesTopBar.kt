package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.ui.components.BigLeaguesQuickFilterBar
import com.dirzaaulia.footballclips.ui.score.DateOption
import com.dirzaaulia.footballclips.ui.score.WebDateSelectorCard

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
    dates: List<DateOption>,
    selectedDate: String?,
    onDateSelected: (String?) -> Unit,
    selectedCompetitionId: String?,
    onLeagueSelected: (Int?, String?) -> Unit,
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
                        IconButton(
                            onClick = onToggleSpoilerFree,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpoilerFreeMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Spoiler-Free Mode",
                                tint = if (isSpoilerFreeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
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

            AnimatedVisibility(
                visible = isFiltersExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    if (isWeb) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = horizontalPadding, end = horizontalPadding, bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                WebDateSelectorCard(
                                    dates = dates,
                                    selectedDate = selectedDate,
                                    onDateSelected = onDateSelected
                                )

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
                                            tint = if (isMyClubsActive || favoriteClubsCount > 0) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = if (favoriteClubsCount > 0) "My Clubs ($favoriteClubsCount)" else "My Clubs",
                                            fontWeight = if (isMyClubsActive) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    },
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                            
                            BigLeaguesQuickFilterBar(
                                selectedCompetitionId = if (isMyClubsActive) null else selectedCompetitionId,
                                onLeagueSelected = { leagueId, competitionId ->
                                    if (isMyClubsActive) onToggleMyClubs()
                                    onLeagueSelected(leagueId, competitionId)
                                },
                                isCompact = true,
                                contentPadding = PaddingValues(0.dp)
                            )
                        }
                    } else {
                        // Android Mobile Layout with Calendar Card Style Date Filter
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            WebDateSelectorCard(
                                dates = dates,
                                selectedDate = selectedDate,
                                onDateSelected = onDateSelected
                            )

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
                                        tint = if (isMyClubsActive || favoriteClubsCount > 0) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (favoriteClubsCount > 0) "My Clubs ($favoriteClubsCount)" else "My Clubs",
                                        fontWeight = if (isMyClubsActive) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                },
                                shape = RoundedCornerShape(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // League Bar
                        BigLeaguesQuickFilterBar(
                            selectedCompetitionId = if (isMyClubsActive) null else selectedCompetitionId,
                            onLeagueSelected = { leagueId, competitionId ->
                                if (isMyClubsActive) onToggleMyClubs()
                                onLeagueSelected(leagueId, competitionId)
                            },
                            contentPadding = PaddingValues(horizontal = horizontalPadding)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
