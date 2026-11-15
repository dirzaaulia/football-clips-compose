package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.ui.components.common.PulsingRadarDot
import com.dirzaaulia.footballclips.ui.theme.GhostBorder
import com.dirzaaulia.footballclips.ui.theme.MatchPulseRed
import com.dirzaaulia.footballclips.ui.theme.ObsidianSurfaceTier1
import com.dirzaaulia.footballclips.ui.theme.VoltLime
import com.dirzaaulia.footballclips.ui.theme.VoltLimeDarkText
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
fun ModernHeroCard(
    match: Match,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSpoilerFree: Boolean = false,
    isRevealed: Boolean = false,
    onRevealClick: (() -> Unit)? = null,
) {
    val thumbUrl = "https://img.youtube.com/vi/${match.highlightVideoId}/hqdefault.jpg".toProxyUrl()

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        shape = RoundedCornerShape(16.dp),
        color = ObsidianSurfaceTier1,
        border = BorderStroke(1.dp, GhostBorder),
        shadowElevation = 8.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = thumbUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            // Multi-Layer Vignette Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.6f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f),
                            ),
                        ),
                    ),
            )

            // Top Header Tags
            HeroTopTags(match = match)

            // Center Kinetic Play Glyph
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(VoltLime),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = VoltLimeDarkText,
                    modifier = Modifier.size(28.dp),
                )
            }

            // Bottom Showdown Info
            HeroBottomInfo(
                match = match,
                isSpoilerFree = isSpoilerFree,
                isRevealed = isRevealed,
                onRevealClick = onRevealClick,
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }
    }
}

@Composable
private fun HeroTopTags(
    match: Match,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.75f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PulsingRadarDot(color = MatchPulseRed, size = 6.dp)
                Text(
                    text = "MATCH OF THE WEEK",
                    color = VoltLime,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                )
            }
        }

        Surface(
            color = Color.Black.copy(alpha = 0.75f),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        ) {
            Text(
                text = "1080p60",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun HeroBottomInfo(
    match: Match,
    isSpoilerFree: Boolean,
    isRevealed: Boolean,
    onRevealClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "${match.homeTeamName} vs ${match.awayTeamName}",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = match.competitionName.uppercase(),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )

            val showScores = !isSpoilerFree || isRevealed
            if (match.homeScore != null && match.awayScore != null) {
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (showScores) VoltLime.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.clickable(enabled = isSpoilerFree) { onRevealClick?.invoke() },
                ) {
                    Text(
                        text = if (showScores) "${match.homeScore} - ${match.awayScore}" else "REVEAL SCORE",
                        color = VoltLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}
