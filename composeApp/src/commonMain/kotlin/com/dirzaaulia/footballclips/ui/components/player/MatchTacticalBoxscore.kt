package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.domain.model.Match

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchTacticalBoxscore(
    match: Match,
    isSpoilerFree: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val isLive = match.isLive
    val hasEvents = !match.events.isNullOrEmpty()
    val hasStats = !match.statistics.isNullOrEmpty()
    val hasLineups = match.formations?.home?.starters?.isNotEmpty() == true ||
        match.formations?.away?.starters?.isNotEmpty() == true
    val hasStory = !match.editorialStory.isNullOrBlank()

    if (!hasEvents && !hasStats && !hasLineups && !hasStory && !isLive) return

    var isRevealed by remember { mutableStateOf(!isSpoilerFree || isLive) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabs = remember(hasEvents, hasStats, hasLineups, hasStory, isLive) {
        buildList {
            if (hasEvents) add("TIMELINE")
            if (hasEvents || isLive) add("PITCH RADAR")
            if (hasStats) add("STATISTICS")
            if (hasLineups) add("LINEUPS")
            if (hasStory) add("REPORT")
            if (isEmpty() && isLive) add("LIVE TELEMETRY")
        }
    }

    if (isSpoilerFree && !isRevealed && !isLive) {
        Card(
            onClick = { isRevealed = true },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column {
                        Text(
                            text = "Tactical Boxscore & Formations",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Spoiler Guarded • Tap to reveal stats & lineups",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = "Reveal ▾",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
        return
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (tabs.size > 1) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                ) {
                    itemsIndexed(tabs) { index, tabTitle ->
                        val isSelected = selectedTabIndex == index
                        val (icon, label) = when (tabTitle) {
                            "TIMELINE" -> Icons.Default.Schedule to "Timeline"
                            "PITCH RADAR" -> Icons.Default.LocationSearching to "Pitch Radar"
                            "STATISTICS" -> Icons.Default.BarChart to "Statistics"
                            "LINEUPS" -> Icons.Default.Groups to "Lineups"
                            "REPORT" -> Icons.Default.Description to "Report"
                            else -> Icons.Default.Schedule to tabTitle.lowercase().replaceFirstChar { it.uppercase() }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)
                            },
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                },
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedTabIndex = index },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = tabs.firstOrNull() ?: "Tactical Data",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp,
                )
            }

            val currentTab = tabs.getOrNull(selectedTabIndex) ?: tabs.firstOrNull()

            when (currentTab) {
                "TIMELINE" -> {
                    MatchEventsTimeline(
                        events = match.events,
                        homeName = match.homeTeamName,
                        awayName = match.awayTeamName,
                        isSpoilerFree = false,
                    )
                }
                "PITCH RADAR" -> {
                    MatchRadarTab(match = match, isLive = isLive)
                }
                "STATISTICS" -> {
                    TacticalStatisticsTab(match = match)
                }
                "LINEUPS" -> {
                    TacticalLineupsTab(match = match)
                }
                "REPORT" -> {
                    TacticalReportTab(match = match)
                }
                "LIVE TELEMETRY" -> {
                    LiveMatchTelemetryTab(match = match)
                }
            }
        }
    }
}
