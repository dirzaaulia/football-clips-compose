package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge
import com.dirzaaulia.footballclips.ui.components.player.SpoilerThumbnailBox
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
internal fun HighlightThumbnailBox(
    match: Match,
    isSpoilerFree: Boolean,
    isRevealed: Boolean,
    isFavorite: Boolean,
    onRevealClick: (() -> Unit)?,
) {
    Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        SpoilerThumbnailBox(
            isSpoilerFree = isSpoilerFree,
            isRevealed = isRevealed,
            onRevealClick = onRevealClick,
            badgeAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            AsyncImage(
                model = "https://img.youtube.com/vi/${match.highlightVideoId}/maxresdefault.jpg".toProxyUrl(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f),
                        ),
                    ),
                ),
            )
        }
        HighlightHeaderOverlay(match = match, isFavorite = isFavorite)
    }
}

@Composable
internal fun HighlightHeaderOverlay(match: Match, isFavorite: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.55f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            ) {
                val leagueLogo = match.leagueEmblemUrl
                GlassLogoBadge(
                    model = leagueLogo.toProxyUrl(),
                    contentDescription = match.competitionName,
                    size = 18.dp,
                    padding = 1.dp,
                )
                if (isFavorite) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorite",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
        }
        Surface(
            color = Color.Black.copy(alpha = 0.55f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        ) {
            if (match.isLive) {
                MatchStatusBadge(match = match)
            } else {
                Text(
                    text = match.dateOnly,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}
