package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
internal fun CompactLiveMatchCard(
    match: Match,
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isFavorite) Color(0xFFFFB300).copy(alpha = 0.6f) else Color(0xFFEF4444).copy(alpha = 0.35f)

    Surface(
        modifier = modifier
            .width(250.dp)
            .height(86.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E2227),
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            LiveCardHeaderRow(match = match)
            LiveTeamScoreRow(
                name = match.homeTeamName,
                crestUrl = match.homeTeamCrest,
                score = match.homeScore,
            )
            LiveTeamScoreRow(
                name = match.awayTeamName,
                crestUrl = match.awayTeamCrest,
                score = match.awayScore,
            )
        }
    }
}

@Composable
internal fun SeeAllLiveMatchesCard(remaining: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(96.dp)
            .height(86.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = Color(0xFF1E2227),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "+$remaining More",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFEF4444),
            )
            Text(
                text = "See all >",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun LiveCardHeaderRow(match: Match) {
    val statusText = if (match.status.equals("PAUSED", ignoreCase = true)) {
        "HT"
    } else if (match.displayClock != null) {
        "${match.displayClock}'"
    } else {
        "LIVE"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f, fill = false),
        ) {
            AsyncImage(
                model = match.leagueEmblemUrl.toProxyUrl(),
                contentDescription = match.competitionName,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = match.competitionName.ifBlank { match.competitionId },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7098F8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "KO ${match.kickoffTime}",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(color = Color(0xFFEF4444).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                Text(
                    text = statusText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                )
            }
        }
    }
}

@Composable
private fun LiveTeamScoreRow(
    name: String,
    crestUrl: String?,
    score: Int?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        GlassLogoBadge(
            model = crestUrl?.toProxyUrl(),
            contentDescription = name,
            size = 18.dp,
            padding = 1.dp,
        )
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = (score ?: 0).toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFF6B6B),
        )
    }
}
