package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.ui.components.filter.SeasonDropdownSelector
import com.dirzaaulia.footballclips.ui.score.MatchStatusFilter

@Composable
internal fun FixturesLandscapeHeader(
    selectedSeason: Int,
    onSeasonSelected: (Int) -> Unit,
    selectedStatusFilter: MatchStatusFilter,
    onStatusFilterSelected: (MatchStatusFilter) -> Unit,
    isLiveOnlyActive: Boolean,
    onToggleLiveOnly: () -> Unit,
    liveMatchCount: Int,
    favoriteClubs: Set<String>,
    isMyClubsActive: Boolean,
    onToggleMyClubs: () -> Unit,
    onOpenMyClubsSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Fixtures",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
            )
            SeasonDropdownSelector(
                selectedSeason = selectedSeason,
                onSeasonSelected = onSeasonSelected,
                isCompact = true,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            StatusPill(
                text = "All",
                isSelected = selectedStatusFilter == MatchStatusFilter.ALL && !isLiveOnlyActive,
                onClick = {
                    if (isLiveOnlyActive) onToggleLiveOnly()
                    onStatusFilterSelected(MatchStatusFilter.ALL)
                },
            )

            // LIVE Pill
            Surface(
                onClick = onToggleLiveOnly,
                shape = RoundedCornerShape(20.dp),
                color = if (isLiveOnlyActive) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, if (isLiveOnlyActive) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.4f)),
                modifier = Modifier.height(30.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(horizontal = 10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isLiveOnlyActive) Color.White else Color(0xFFEF4444)),
                    )
                    Text(
                        text = if (liveMatchCount > 0) "Live $liveMatchCount" else "Live",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLiveOnlyActive) Color.White else Color(0xFFEF4444),
                    )
                }
            }

            StatusPill(
                text = "Finished",
                isSelected = selectedStatusFilter == MatchStatusFilter.FINISHED && !isLiveOnlyActive,
                onClick = {
                    if (isLiveOnlyActive) onToggleLiveOnly()
                    onStatusFilterSelected(MatchStatusFilter.FINISHED)
                },
            )

            StatusPill(
                text = "Upcoming",
                isSelected = selectedStatusFilter == MatchStatusFilter.UPCOMING && !isLiveOnlyActive,
                onClick = {
                    if (isLiveOnlyActive) onToggleLiveOnly()
                    onStatusFilterSelected(MatchStatusFilter.UPCOMING)
                },
            )

            // MY CLUBS Pill
            Surface(
                onClick = {
                    if (favoriteClubs.isEmpty()) onOpenMyClubsSheet()
                    else onToggleMyClubs()
                },
                shape = RoundedCornerShape(20.dp),
                color = if (isMyClubsActive) Color(0xFFFFB300).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, if (isMyClubsActive) Color(0xFFFFB300) else Color.Transparent),
                modifier = Modifier.height(30.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 10.dp),
                ) {
                    Icon(
                        imageVector = if (isMyClubsActive || favoriteClubs.isNotEmpty()) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "My Clubs",
                        tint = if (isMyClubsActive || favoriteClubs.isNotEmpty()) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = if (favoriteClubs.isNotEmpty()) "My Clubs (${favoriteClubs.size})" else "My Clubs",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMyClubsActive) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.height(30.dp),
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
