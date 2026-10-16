package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.domain.model.StarterPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchTacticalBoxscore(
    match: Match,
    isSpoilerFree: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hasStats = !match.statistics.isNullOrEmpty()
    val hasLineups = match.formations?.home?.starters?.isNotEmpty() == true ||
        match.formations?.away?.starters?.isNotEmpty() == true
    val hasStory = !match.editorialStory.isNullOrBlank()

    if (!hasStats && !hasLineups && !hasStory) return

    var isRevealed by remember { mutableStateOf(!isSpoilerFree) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabs = remember(hasStats, hasLineups, hasStory) {
        buildList {
            if (hasStats) add("STATISTICS")
            if (hasLineups) add("LINEUPS")
            if (hasStory) add("REPORT")
        }
    }

    if (isSpoilerFree && !isRevealed) {
        Card(
            onClick = { isRevealed = true },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Tactical Boxscore & Formations",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Spoiler Guarded • Tap to reveal stats & lineups",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Reveal ▾",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        return
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header / Tabs
            if (tabs.size > 1) {
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex.coerceIn(0, tabs.lastIndex),
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
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
                                    letterSpacing = 0.5.sp
                                )
                            }
                        )
                    }
                }
            } else {
                Text(
                    text = tabs.firstOrNull() ?: "TACTICAL DATA",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )
            }

            val currentTab = tabs.getOrNull(selectedTabIndex) ?: tabs.firstOrNull()

            when (currentTab) {
                "STATISTICS" -> {
                    TacticalStatisticsTab(match = match)
                }
                "LINEUPS" -> {
                    TacticalLineupsTab(match = match)
                }
                "REPORT" -> {
                    TacticalReportTab(match = match)
                }
            }
        }
    }
}

@Composable
private fun TacticalStatisticsTab(match: Match) {
    val stats = match.statistics ?: return
    val homeStats = stats["home"] ?: emptyMap()
    val awayStats = stats["away"] ?: emptyMap()

    val metricKeys = listOf(
        "possessionPct" to "Possession %",
        "totalShots" to "Total Shots",
        "shotsOnTarget" to "Shots on Target",
        "wonCorners" to "Corner Kicks",
        "foulsCommitted" to "Fouls",
        "yellowCards" to "Yellow Cards",
        "redCards" to "Red Cards",
        "offsides" to "Offsides",
        "saves" to "Saves",
        "passPct" to "Pass Accuracy %",
        "totalPasses" to "Total Passes"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        metricKeys.forEach { (key, label) ->
            val homeVal = homeStats[key]
            val awayVal = awayStats[key]
            if (homeVal != null || awayVal != null) {
                StatDuelRow(
                    label = label,
                    homeValue = homeVal ?: "-",
                    awayValue = awayVal ?: "-"
                )
            }
        }
    }
}

@Composable
private fun StatDuelRow(
    label: String,
    homeValue: String,
    awayValue: String
) {
    val homeNum = homeValue.filter { it.isDigit() }.toFloatOrNull() ?: 1f
    val awayNum = awayValue.filter { it.isDigit() }.toFloatOrNull() ?: 1f
    val total = (homeNum + awayNum).coerceAtLeast(1f)
    val homeRatio = (homeNum / total).coerceIn(0.1f, 0.9f)
    val awayRatio = 1f - homeRatio

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = homeValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = awayValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .weight(homeRatio)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .weight(awayRatio)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.secondary)
            )
        }
    }
}

@Composable
private fun TacticalLineupsTab(match: Match) {
    val homeForm = match.formations?.home
    val awayForm = match.formations?.away
    var selectedTeamTab by remember { mutableStateOf(0) } // 0: Home, 1: Away

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Formations overview banner
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${match.homeTeamName}: ${homeForm?.formation ?: "N/A"}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "vs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${match.awayTeamName}: ${awayForm?.formation ?: "N/A"}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Sub-tabs for Home XI vs Away XI
        SecondaryTabRow(
            selectedTabIndex = selectedTeamTab,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            Tab(
                selected = selectedTeamTab == 0,
                onClick = { selectedTeamTab = 0 },
                text = {
                    Text(
                        text = match.homeTeamName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
            Tab(
                selected = selectedTeamTab == 1,
                onClick = { selectedTeamTab = 1 },
                text = {
                    Text(
                        text = match.awayTeamName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }

        val activeStarters = if (selectedTeamTab == 0) {
            homeForm?.starters ?: emptyList()
        } else {
            awayForm?.starters ?: emptyList()
        }

        if (activeStarters.isEmpty()) {
            Text(
                text = "Starting XI lineup not yet officially confirmed.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                activeStarters.forEach { player ->
                    PlayerRowItem(player = player)
                }
            }
        }
    }
}

@Composable
private fun PlayerRowItem(player: StarterPlayer) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (player.jersey.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = player.jersey,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (player.position.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = player.position.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = "ACCREDITED JOURNALISM • $source",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        if (!headline.isNullOrBlank()) {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
        }

        Text(
            text = story,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )
    }
}
