package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.ui.components.common.MarkdownText

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
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex.coerceIn(0, tabs.lastIndex),
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {},
                ) {
                    tabs.forEachIndexed { index, tabTitle ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = tabTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                )
                            },
                        )
                    }
                }
            } else {
                Text(
                    text = tabs.firstOrNull() ?: "TACTICAL DATA",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp,
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

@Composable
private fun LiveMatchTelemetryTab(match: Match) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444)),
                )
                Text(
                    text = "MATCH IN PROGRESS • ${match.displayClock ?: "LIVE"}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEF4444),
                )
            }
            Text(
                text = "${match.homeScore ?: 0} - ${match.awayScore ?: 0}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Live telemetry streaming from ESPN API. Key events, boxscore dual-meters, and tactical formations update in real-time as match progresses.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TacticalReportTab(match: Match) {
    val story = match.editorialStory ?: return
    val headline = match.editorialHeadline
    val source = match.editorialSource ?: "ESPN"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            shape = RoundedCornerShape(6.dp),
        ) {
            Text(
                text = "ACCREDITED JOURNALISM • $source",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }

        if (!headline.isNullOrBlank()) {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp,
            )
        }

        MarkdownText(markdown = story)
    }
}
