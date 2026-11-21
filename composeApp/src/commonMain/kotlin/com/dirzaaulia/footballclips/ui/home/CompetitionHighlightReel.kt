package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.ad.NativeAdStyle
import com.dirzaaulia.footballclips.ui.components.player.SpoilerThumbnailBox
import com.dirzaaulia.footballclips.ui.theme.VoltLime
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
fun CompetitionHighlightReel(
    displayMatches: List<HighlightUiItem>,
    league: BigLeague,
    compName: String,
    emblem: String,
    isAdsRemoved: Boolean,
    isSpoilerFreeMode: Boolean,
    revealedItemIds: Set<String>,
    onMatchClick: (Match) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
    onRevealItem: ((String) -> Unit)? = null,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
    ) {
        displayMatches.forEachIndexed { index, item ->
            val match = (item as? HighlightUiItem.OracleMatch)?.match
            if (match != null) {
                val isRevealed = revealedItemIds.contains(item.uniqueId)
                item(key = item.uniqueId) {
                    ReelHighlightCard(
                        match = match,
                        isSpoilerFree = isSpoilerFreeMode,
                        isRevealed = isRevealed,
                        onClick = { onMatchClick(match) },
                        onRevealClick = { onRevealItem?.invoke(item.uniqueId) },
                    )
                }
            }

            if (!isAdsRemoved && (index + 1) % 4 == 0) {
                item(key = "ad-${league.id}-$index") {
                    Box(modifier = Modifier.width(256.dp)) {
                        BannerAdItem(
                            modifier = Modifier.fillMaxWidth(),
                            style = NativeAdStyle.HIGHLIGHT,
                        )
                    }
                }
            }
        }

        item(key = "see-all-${league.id}") {
            ReelSeeAllCard(
                competitionName = compName,
                onSeeAll = onSeeAll,
            )
        }
    }
}

@Composable
private fun ReelHighlightCard(
    match: Match,
    isSpoilerFree: Boolean,
    isRevealed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRevealClick: (() -> Unit)? = null,
) {
    val thumbUrl = "https://img.youtube.com/vi/${match.highlightVideoId}/hqdefault.jpg".toProxyUrl()
    val showScores = !isSpoilerFree || isRevealed

    Surface(
        onClick = onClick,
        modifier = modifier.width(256.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            ) {
                SpoilerThumbnailBox(
                    isSpoilerFree = isSpoilerFree,
                    isRevealed = isRevealed,
                    onRevealClick = onRevealClick ?: onClick,
                    badgeAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AsyncImage(
                        model = thumbUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f),
                                    ),
                                ),
                            ),
                    )
                }

                if (match.homeScore != null && match.awayScore != null) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, VoltLime.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp),
                    ) {
                        Text(
                            text = if (showScores) "${match.homeScore} - ${match.awayScore}" else "BLURRED",
                            color = VoltLime,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "${match.homeTeamName} vs ${match.awayTeamName}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = match.competitionName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ReelSeeAllCard(
    competitionName: String,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onSeeAll,
        modifier = modifier
            .width(140.dp)
            .aspectRatio(1.2f),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "SEE ALL",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
            Text(
                text = competitionName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
