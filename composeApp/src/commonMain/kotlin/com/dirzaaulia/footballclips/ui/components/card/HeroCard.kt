package com.dirzaaulia.footballclips.ui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge
import com.dirzaaulia.footballclips.ui.components.player.SpoilerThumbnailBox
import com.dirzaaulia.footballclips.util.toProxyUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroCard(
    item: HighlightUiItem,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isCinematic: Boolean = false,
    isSpoilerFree: Boolean = false,
    isRevealed: Boolean = false,
    onRevealClick: (() -> Unit)? = null,
    excitementTag: String? = null
) {
    val shape = RoundedCornerShape(14.dp)
    
    val title: String
    val thumbnail: String
    val leagueName: String
    val leagueLogo: String
    val date: String
    val videoId: String
    val homeLogo: String
    val awayLogo: String

    when (item) {
        is HighlightUiItem.OracleMatch -> {
            title = "${item.match.homeTeamName} vs ${item.match.awayTeamName}"
            thumbnail = "https://img.youtube.com/vi/${item.match.highlightVideoId}/maxresdefault.jpg"
            leagueName = item.match.competitionName
            leagueLogo = item.match.leagueEmblemUrl
            date = item.match.dateOnly
            videoId = item.match.highlightVideoId ?: ""
            homeLogo = item.match.homeTeamCrest ?: ""
            awayLogo = item.match.awayTeamCrest ?: ""
        }
        is HighlightUiItem.Highlight -> {
            title = item.highlight.title
            thumbnail = item.highlight.thumbnail
            leagueName = item.highlight.leagueName
            leagueLogo = item.highlight.leagueLogo
            date = item.highlight.date
            videoId = item.highlight.embedHtml
            homeLogo = item.highlight.homeTeamLogo
            awayLogo = item.highlight.awayTeamLogo
        }
        else -> return
    }

    BoxWithConstraints(modifier = modifier) {
        val isLandscape = maxHeight < 300.dp && maxHeight != Dp.Infinity
        val isFillMax = maxHeight != Dp.Infinity && maxHeight > 200.dp
        
        val cardMod = if (isFillMax) Modifier.fillMaxSize() else Modifier.fillMaxWidth().height(if (isCinematic) 360.dp else if (isLandscape) 180.dp else 240.dp)

        Card(
            onClick = { onClick(videoId) },
            modifier = cardMod,
            shape = shape,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
        Box {
            SpoilerThumbnailBox(
                isSpoilerFree = isSpoilerFree,
                isRevealed = isRevealed,
                onRevealClick = onRevealClick ?: { onClick(videoId) },
                badgeAlignment = Alignment.TopStart,
                iconOnly = item is HighlightUiItem.Highlight,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
            ) {
                SubcomposeAsyncImage(
                    model = thumbnail.toProxyUrl(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            ExpressiveLoadingIndicator(
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 6.dp
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.8f),
                                    Color.Black
                                )
                            )
                        )
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 16.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = excitementTag?.uppercase() ?: "FEATURED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    letterSpacing = 1.sp
                )
            }
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(if (isCinematic) 40.dp else 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (leagueLogo.isNotEmpty()) {
                        GlassLogoBadge(
                            model = leagueLogo.toProxyUrl(),
                            contentDescription = leagueName,
                            size = if (isCinematic) 36.dp else 28.dp,
                            padding = if (isCinematic) 4.dp else 3.dp,
                        )
                        Spacer(modifier = Modifier.width(if (isCinematic) 12.dp else 8.dp))
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = leagueName.uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = if (isCinematic) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(if (isCinematic) 16.dp else 8.dp))
                
                Text(
                    text = title,
                    style = if (isCinematic) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = if (isCinematic) 42.sp else 28.sp
                )

                Spacer(modifier = Modifier.height(if (isCinematic) 32.dp else 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Watch Highlight",
                            color = Color.White,
                            style = if (isCinematic) MaterialTheme.typography.titleLarge else MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = date,
                            color = Color.White.copy(alpha = 0.6f),
                            style = if (isCinematic) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall
                        )
                    }

                    Row {
                        TeamLogoFeatured(homeLogo, isCinematic)
                        Spacer(modifier = Modifier.width(if (isCinematic) 12.dp else 8.dp))
                        TeamLogoFeatured(awayLogo, isCinematic)
                    }
                }
            }
        }
    }
}
}
