package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.components.affiliate.MatchDayGearRow
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge
import com.dirzaaulia.footballclips.ui.components.common.MarkdownText
import com.dirzaaulia.footballclips.util.MatchAnalysisUtils
import com.dirzaaulia.footballclips.util.shareText
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
internal fun VideoMetadata(
    item: HighlightUiItem,
    isSpoilerFree: Boolean = false,
    isScoreRevealed: Boolean = !isSpoilerFree,
    onRevealClick: () -> Unit = {},
    isMerchandiseEnabled: Boolean = true,
    hasVideo: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val meta = remember(item) { resolveMetadata(item) } ?: return
    val isGuarded = isSpoilerFree && !isScoreRevealed

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        VideoMetadataHeader(
            leagueName = meta.leagueName,
            date = meta.date,
            title = meta.title,
            item = item,
        )

        if (hasVideo && meta.scoreText != null && meta.homeCrest != null && meta.awayCrest != null) {
            VideoMetadataTeamsRow(
                homeName = (item as? HighlightUiItem.OracleMatch)?.match?.homeTeamName.orEmpty(),
                homeCrest = meta.homeCrest,
                homeScore = meta.homeScore,
                awayName = (item as? HighlightUiItem.OracleMatch)?.match?.awayTeamName.orEmpty(),
                awayCrest = meta.awayCrest,
                awayScore = meta.awayScore,
                isSpoilerFree = isGuarded,
                onRevealClick = onRevealClick,
            )
        } else if (!hasVideo) {
            Text(text = meta.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        }

        VideoMetadataMerchandise(item = item, isMerchandiseEnabled = isMerchandiseEnabled)

        if (!meta.recapAnalysis.isNullOrBlank()) {
            VideoMetadataAnalysis(
                item = item,
                recapAnalysis = meta.recapAnalysis,
                isSpoilerFree = isGuarded,
                onRevealClick = onRevealClick,
            )
        }

        if (item is HighlightUiItem.OracleMatch) {
            MatchTacticalBoxscore(
                match = item.match,
                isSpoilerFree = isGuarded,
            )
        }
    }
}

@Composable
private fun VideoMetadataMerchandise(
    item: HighlightUiItem,
    isMerchandiseEnabled: Boolean,
) {
    if (!isMerchandiseEnabled) return
    when (item) {
        is HighlightUiItem.OracleMatch -> MatchDayGearRow(
            homeTeamName = item.match.homeTeamName,
            homeTeamCrest = item.match.homeTeamCrest,
            awayTeamName = item.match.awayTeamName,
            awayTeamCrest = item.match.awayTeamCrest,
        )
        is HighlightUiItem.Highlight -> if (item.highlight.homeTeam.isNotBlank() && item.highlight.awayTeam.isNotBlank()) {
            MatchDayGearRow(
                homeTeamName = item.highlight.homeTeam,
                homeTeamCrest = item.highlight.homeTeamLogo.takeIf { it.isNotBlank() },
                awayTeamName = item.highlight.awayTeam,
                awayTeamCrest = item.highlight.awayTeamLogo.takeIf { it.isNotBlank() },
            )
        }
        else -> Unit
    }
}

private data class MetadataValues(
    val title: String, val leagueName: String, val date: String,
    val recapAnalysis: String?, val homeCrest: String?, val awayCrest: String?,
    val homeScore: Int?, val awayScore: Int?, val scoreText: String?,
)

private fun resolveMetadata(item: HighlightUiItem): MetadataValues? = when (item) {
    is HighlightUiItem.OracleMatch -> {
        val score = if (item.match.homeScore != null && item.match.awayScore != null) "${item.match.homeScore} - ${item.match.awayScore}" else null
        MetadataValues(
            title = "${item.match.homeTeamName} vs ${item.match.awayTeamName}",
            leagueName = item.match.competitionName,
            date = item.match.formattedLocalKickoff,
            recapAnalysis = MatchAnalysisUtils.getSummaryOrPreview(item.match),
            homeCrest = item.match.homeTeamCrest,
            awayCrest = item.match.awayTeamCrest,
            homeScore = item.match.homeScore,
            awayScore = item.match.awayScore,
            scoreText = score,
        )
    }
    is HighlightUiItem.Highlight -> MetadataValues(
        title = item.highlight.title,
        leagueName = item.highlight.leagueName,
        date = item.highlight.date,
        recapAnalysis = null,
        homeCrest = null,
        awayCrest = null,
        homeScore = null,
        awayScore = null,
        scoreText = null,
    )
    else -> null
}

@Composable
private fun VideoMetadataHeader(
    leagueName: String,
    date: String,
    title: String,
    item: HighlightUiItem,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = leagueName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            IconButton(
                onClick = {
                    val shareUrl = when (item) {
                        is HighlightUiItem.OracleMatch -> "https://fc.dirzaaulia.com/match/${item.match.id}"
                        is HighlightUiItem.Highlight -> item.highlight.videoUrl.ifBlank { "https://fc.dirzaaulia.com" }
                        else -> "https://fc.dirzaaulia.com"
                    }
                    shareText(title = title, text = "Watch highlights for $title on Football Highlights & Clips!", url = shareUrl)
                },
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun VideoMetadataTeamsRow(
    homeName: String,
    homeCrest: String,
    homeScore: Int?,
    awayName: String,
    awayCrest: String,
    awayScore: Int?,
    isSpoilerFree: Boolean,
    onRevealClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    GlassLogoBadge(
                        model = homeCrest.toProxyUrl(),
                        contentDescription = homeName,
                        size = 26.dp,
                        padding = 2.dp,
                    )
                    Text(
                        text = homeName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    GlassLogoBadge(
                        model = awayCrest.toProxyUrl(),
                        contentDescription = awayName,
                        size = 26.dp,
                        padding = 2.dp,
                    )
                    Text(
                        text = awayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            VideoScoreColumn(
                homeScore = homeScore,
                awayScore = awayScore,
                isSpoilerFree = isSpoilerFree,
                onRevealClick = onRevealClick,
            )
        }
    }
}

@Composable
private fun VideoScoreColumn(
    homeScore: Int?,
    awayScore: Int?,
    isSpoilerFree: Boolean,
    onRevealClick: () -> Unit,
) {
    if (isSpoilerFree) {
        Surface(
            onClick = onRevealClick,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = "Reveal Score",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Reveal",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = (homeScore ?: "-").toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = (awayScore ?: "-").toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun VideoMetadataAnalysis(
    item: HighlightUiItem,
    recapAnalysis: String,
    isSpoilerFree: Boolean,
    onRevealClick: () -> Unit,
) {
    val isUpcoming = item is HighlightUiItem.OracleMatch && MatchAnalysisUtils.isUpcoming(item.match)
    val isGuarded = !isUpcoming && isSpoilerFree
    val header = if (isUpcoming) "MATCH PREVIEW & TACTICAL SETUP" else "MATCH SUMMARY & TACTICAL BREAKDOWN"

    Card(
        onClick = { if (isGuarded) onRevealClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = header, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
            if (isGuarded) {
                Text(
                    text = "Analysis hidden in Spoiler-Free mode. Tap to reveal tactical analysis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                )
            } else {
                MarkdownText(markdown = recapAnalysis)
            }
        }
    }
}
