package com.dirzaaulia.footballclips.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.components.player.SpoilerThumbnailBox
import com.dirzaaulia.footballclips.util.toProxyUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroCard(
    item: HighlightUiItem,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isCinematic: Boolean = false,
    isLandscape: Boolean = false,
    isSpoilerFree: Boolean = false,
    isRevealed: Boolean = false,
    onRevealClick: (() -> Unit)? = null,
    excitementTag: String? = null,
) {
    val meta = extractHeroMetadata(item) ?: return
    val shape = RoundedCornerShape(14.dp)

    BoxWithConstraints(modifier = modifier) {
        val landscapeMode = isLandscape || (maxHeight < 300.dp && maxHeight != Dp.Infinity)
        val isFillMax = maxHeight != Dp.Infinity && maxHeight > 200.dp
        val cardMod = if (isFillMax) {
            Modifier.fillMaxSize()
        } else {
            Modifier
                .fillMaxWidth()
                .height(if (isCinematic) 360.dp else if (landscapeMode) 180.dp else 240.dp)
        }

        Card(
            onClick = { onClick(meta.videoId) },
            modifier = cardMod,
            shape = shape,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                HeroCardThumbnail(
                    thumbnail = meta.thumbnail,
                    shape = shape,
                    isSpoilerFree = isSpoilerFree,
                    isRevealed = isRevealed,
                    onRevealClick = onRevealClick ?: { onClick(meta.videoId) },
                    isHighlight = item is HighlightUiItem.Highlight,
                )

                if (landscapeMode) {
                    HeroLandscapeOverlay(
                        leagueLogo = meta.leagueLogo,
                        leagueName = meta.leagueName,
                        excitementTag = excitementTag,
                        title = meta.title,
                        date = meta.date,
                        homeLogo = meta.homeLogo,
                        awayLogo = meta.awayLogo,
                        isSpoilerFree = isSpoilerFree,
                        isRevealed = isRevealed,
                    )
                } else {
                    HeroPortraitOverlay(
                        isCinematic = isCinematic,
                        excitementTag = excitementTag,
                        leagueLogo = meta.leagueLogo,
                        leagueName = meta.leagueName,
                        title = meta.title,
                        date = meta.date,
                        homeLogo = meta.homeLogo,
                        awayLogo = meta.awayLogo,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCardThumbnail(
    thumbnail: String,
    shape: RoundedCornerShape,
    isSpoilerFree: Boolean,
    isRevealed: Boolean,
    onRevealClick: () -> Unit,
    isHighlight: Boolean,
) {
    SpoilerThumbnailBox(
        isSpoilerFree = isSpoilerFree,
        isRevealed = isRevealed,
        onRevealClick = onRevealClick,
        badgeAlignment = Alignment.TopStart,
        iconOnly = isHighlight,
        modifier = Modifier
            .fillMaxSize()
            .clip(shape),
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
                    contentAlignment = Alignment.Center,
                ) {
                    ExpressiveLoadingIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 6.dp,
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
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
                            Color.Black,
                        ),
                    ),
                ),
        )
    }
}

private data class HeroItemMeta(
    val title: String,
    val thumbnail: String,
    val leagueName: String,
    val leagueLogo: String,
    val date: String,
    val videoId: String,
    val homeLogo: String,
    val awayLogo: String,
)

private fun extractHeroMetadata(item: HighlightUiItem): HeroItemMeta? {
    return when (item) {
        is HighlightUiItem.OracleMatch -> HeroItemMeta(
            title = "${item.match.homeTeamName} vs ${item.match.awayTeamName}",
            thumbnail = "https://img.youtube.com/vi/${item.match.highlightVideoId}/maxresdefault.jpg",
            leagueName = item.match.competitionName,
            leagueLogo = item.match.leagueEmblemUrl,
            date = item.match.dateOnly,
            videoId = item.match.highlightVideoId ?: "",
            homeLogo = item.match.homeTeamCrest ?: "",
            awayLogo = item.match.awayTeamCrest ?: "",
        )
        is HighlightUiItem.Highlight -> HeroItemMeta(
            title = item.highlight.title,
            thumbnail = item.highlight.thumbnail,
            leagueName = item.highlight.leagueName,
            leagueLogo = item.highlight.leagueLogo,
            date = item.highlight.date,
            videoId = item.highlight.embedHtml,
            homeLogo = item.highlight.homeTeamLogo,
            awayLogo = item.highlight.awayTeamLogo,
        )
        else -> null
    }
}
