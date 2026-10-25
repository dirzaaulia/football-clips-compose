package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge
import com.dirzaaulia.footballclips.ui.components.player.SpoilerThumbnailBox
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
internal fun LandscapeHighlightRowCard(
    item: HighlightUiItem,
    isSpoilerFreeMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val meta = extractHighlightMeta(item)
    val matchMeta = extractMatchMeta(item)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth().height(92.dp),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(132.dp).fillMaxHeight()) {
                SpoilerThumbnailBox(
                    isSpoilerFree = isSpoilerFreeMode,
                    isRevealed = false,
                    onRevealClick = onClick,
                    iconOnly = true,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AsyncImage(
                        model = meta.thumbnail.toProxyUrl(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            LandscapeRowDetails(
                meta = meta,
                matchMeta = matchMeta,
                isSpoilerFree = isSpoilerFreeMode,
                onScoreClick = onClick,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

internal data class HighlightMeta(
    val title: String,
    val thumbnail: String,
    val leagueName: String,
    val leagueEmblem: String?,
    val date: String,
)

internal data class MatchMeta(
    val scoreText: String?,
    val isLive: Boolean,
    val homeCrest: String?,
    val awayCrest: String?,
)

internal fun extractHighlightMeta(item: HighlightUiItem): HighlightMeta {
    return when (item) {
        is HighlightUiItem.OracleMatch -> HighlightMeta(
            title = "${item.match.homeTeamName} vs ${item.match.awayTeamName}",
            thumbnail = "https://img.youtube.com/vi/${item.match.highlightVideoId}/mqdefault.jpg",
            leagueName = item.match.competitionName,
            leagueEmblem = item.match.leagueEmblemUrl,
            date = item.match.dateOnly,
        )
        is HighlightUiItem.Highlight -> HighlightMeta(
            title = item.highlight.title,
            thumbnail = item.highlight.thumbnail,
            leagueName = item.highlight.leagueName,
            leagueEmblem = item.highlight.leagueLogo,
            date = item.highlight.date,
        )
        else -> HighlightMeta("", "", "", null, "")
    }
}

internal fun extractMatchMeta(item: HighlightUiItem): MatchMeta {
    return when (item) {
        is HighlightUiItem.OracleMatch -> MatchMeta(
            scoreText = if (item.match.isFinished || item.match.isLive) {
                "${item.match.homeScore ?: 0} - ${item.match.awayScore ?: 0}"
            } else {
                item.match.kickoffTime
            },
            isLive = item.match.isLive,
            homeCrest = item.match.homeTeamCrest,
            awayCrest = item.match.awayTeamCrest,
        )
        is HighlightUiItem.Highlight -> MatchMeta(
            scoreText = null,
            isLive = false,
            homeCrest = item.highlight.homeTeamLogo,
            awayCrest = item.highlight.awayTeamLogo,
        )
        else -> MatchMeta(null, false, null, null)
    }
}

@Composable
private fun LandscapeRowDetails(
    meta: HighlightMeta,
    matchMeta: MatchMeta,
    isSpoilerFree: Boolean,
    onScoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
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
                if (!meta.leagueEmblem.isNullOrEmpty()) {
                    GlassLogoBadge(model = meta.leagueEmblem.toProxyUrl(), size = 16.dp, padding = 1.dp)
                }
                Text(
                    text = meta.leagueName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = meta.date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = meta.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        LandscapeRowFooter(
            scoreText = matchMeta.scoreText,
            isLive = matchMeta.isLive,
            isSpoilerFree = isSpoilerFree,
            homeCrest = matchMeta.homeCrest,
            awayCrest = matchMeta.awayCrest,
            onScoreClick = onScoreClick,
        )
    }
}

@Composable
private fun LandscapeRowFooter(
    scoreText: String?,
    isLive: Boolean,
    isSpoilerFree: Boolean,
    homeCrest: String?,
    awayCrest: String?,
    onScoreClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isSpoilerFree && scoreText != null) {
            Surface(
                onClick = onScoreClick,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp),
            ) {
                Text(
                    text = "? - ?",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        } else if (scoreText != null) {
            Surface(
                color = if (isLive) Color(0xFFEF4444).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(4.dp),
            ) {
                Text(
                    text = scoreText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = if (isLive) Color(0xFFEF4444) else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        } else {
            Spacer(modifier = Modifier.width(4.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (!homeCrest.isNullOrEmpty()) {
                GlassLogoBadge(model = homeCrest.toProxyUrl(), size = 18.dp, padding = 1.dp)
            }
            if (!awayCrest.isNullOrEmpty()) {
                GlassLogoBadge(model = awayCrest.toProxyUrl(), size = 18.dp, padding = 1.dp)
            }
        }
    }
}
