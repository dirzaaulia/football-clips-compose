package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.ui.components.filter.BigLeaguesQuickFilterBar
import com.dirzaaulia.footballclips.ui.score.DateOption
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
    selectedCompetitionId: String?,
    onLeagueSelected: (Int?, String?) -> Unit,
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        WebDateSelectorCard(
                            dates = dates,
                            selectedDate = selectedDate,
                            onDateSelected = onDateSelected,
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

                    BigLeaguesQuickFilterBar(
                        selectedCompetitionId = if (isMyClubsActive) null else selectedCompetitionId,
                        onLeagueSelected = { leagueId, competitionId ->
                            if (isMyClubsActive) onToggleMyClubs()
                            onLeagueSelected(leagueId, competitionId)
                        },
                        isCompact = true,
                        contentPadding = PaddingValues(0.dp),
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    WebDateSelectorCard(
                        dates = dates,
                        selectedDate = selectedDate,
                        onDateSelected = onDateSelected,
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

                Spacer(modifier = Modifier.height(4.dp))

                BigLeaguesQuickFilterBar(
                    selectedCompetitionId = if (isMyClubsActive) null else selectedCompetitionId,
                    onLeagueSelected = { leagueId, competitionId ->
                        if (isMyClubsActive) onToggleMyClubs()
                        onLeagueSelected(leagueId, competitionId)
                    },
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
