package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.util.toLegibleColor

@Composable
internal fun TacticalStatisticsTab(match: Match) {
    val stats = match.statistics ?: return
    val homeStats = stats["home"] ?: emptyMap()
    val awayStats = stats["away"] ?: emptyMap()

    val homeColor = match.homeColor.toLegibleColor(MaterialTheme.colorScheme.primary)
    val awayColor = match.awayColor.toLegibleColor(Color(0xFF38BDF8))

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
        "totalPasses" to "Total Passes",
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MatchMomentumCanvas(
            homeTeam = match.homeTeamName,
            awayTeam = match.awayTeamName,
            homeScore = match.homeScore ?: 0,
            awayScore = match.awayScore ?: 0,
        )
        metricKeys.forEach { (key, label) ->
            val homeVal = homeStats[key]
            val awayVal = awayStats[key]
            if (homeVal != null || awayVal != null) {
                StatDuelRow(
                    label = label,
                    homeValue = homeVal ?: "-",
                    awayValue = awayVal ?: "-",
                    homeColor = homeColor,
                    awayColor = awayColor,
                )
            }
        }
    }
}

@Composable
private fun StatDuelRow(
    label: String,
    homeValue: String,
    awayValue: String,
    homeColor: Color,
    awayColor: Color,
) {
    val homeNum = homeValue.filter { it.isDigit() }.toFloatOrNull() ?: 1f
    val awayNum = awayValue.filter { it.isDigit() }.toFloatOrNull() ?: 1f
    val total = (homeNum + awayNum).coerceAtLeast(1f)
    val homeRatio = (homeNum / total).coerceIn(0.1f, 0.9f)
    val awayRatio = 1f - homeRatio

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = homeValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = homeColor,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = awayValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = awayColor,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .weight(homeRatio)
                    .fillMaxHeight()
                    .background(homeColor),
            )
            Spacer(modifier = Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .weight(awayRatio)
                    .fillMaxHeight()
                    .background(awayColor),
            )
        }
    }
}
