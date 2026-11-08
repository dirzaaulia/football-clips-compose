package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge

@Composable
internal fun KnockoutMatchCard(
    match: Match,
    isFinal: Boolean,
    onClick: () -> Unit,
) {
    val isFinished = match.isFinished
    val isLive = match.isLive
    val homeScoreVal = match.homeScore ?: 0
    val awayScoreVal = match.awayScore ?: 0
    val homeWinner = isFinished && (
        homeScoreVal > awayScoreVal ||
            (homeScoreVal == awayScoreVal && (match.homePenalties ?: 0) > (match.awayPenalties ?: 0))
    )
    val awayWinner = isFinished && (
        awayScoreVal > homeScoreVal ||
            (homeScoreVal == awayScoreVal && (match.awayPenalties ?: 0) > (match.homePenalties ?: 0))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFinal) Color(0xFF181C20) else MaterialTheme.colorScheme.surface,
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(
                if (isFinal) {
                    Color(0xFFD4AF37).copy(alpha = 0.4f)
                } else if (isLive) {
                    Color(0xFF10B981)
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                },
            ),
        ),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = match.dateOnly.ifBlank { "TBD" },
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isLive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "LIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981),
                        )
                    }
                } else if (isFinished) {
                    Text(
                        text = "FT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (homeWinner) Color(0xFF10B981).copy(alpha = 0.12f) else Color.Transparent)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GlassLogoBadge(
                        logoUrl = match.homeTeamCrest,
                        contentDescription = match.homeTeamName,
                        size = 20.dp,
                        padding = 1.dp,
                    )
                    Text(
                        text = match.homeTeamName,
                        fontSize = 12.sp,
                        fontWeight = if (homeWinner) FontWeight.Bold else FontWeight.Medium,
                        color = if (homeWinner) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = if (match.hasPenalties) "${match.homeScore ?: 0} (${match.homePenalties})" else match.homeScore?.toString() ?: "-",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (homeWinner) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (awayWinner) Color(0xFF10B981).copy(alpha = 0.12f) else Color.Transparent)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GlassLogoBadge(
                        logoUrl = match.awayTeamCrest,
                        contentDescription = match.awayTeamName,
                        size = 20.dp,
                        padding = 1.dp,
                    )
                    Text(
                        text = match.awayTeamName,
                        fontSize = 12.sp,
                        fontWeight = if (awayWinner) FontWeight.Bold else FontWeight.Medium,
                        color = if (awayWinner) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = if (match.hasPenalties) "${match.awayScore ?: 0} (${match.awayPenalties})" else match.awayScore?.toString() ?: "-",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (awayWinner) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface,
                )
            }

            if (match.hasHighlight) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Watch Highlights",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
