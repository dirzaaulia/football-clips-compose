package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.ui.adaptive.LocalAnimatedVisibilityScope
import com.dirzaaulia.footballclips.ui.adaptive.LocalSharedTransitionScope
import com.dirzaaulia.footballclips.ui.components.SpoilerThumbnailBox
import com.dirzaaulia.footballclips.util.toProxyUrl

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ModernHighlightCard(
    match: Match,
    onWatchHighlightClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSpoilerFree: Boolean = false,
    isRevealed: Boolean = false,
    isFavorite: Boolean = false,
    showTeamName: Boolean = true,
    onRevealClick: (() -> Unit)? = null
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isLandscape = maxWidth < 300.dp || maxHeight < 300.dp
        val shouldShowName = showTeamName && !isLandscape

        Surface(
            onClick = { onWatchHighlightClick(match.highlightVideoId.orEmpty()) },
            modifier = Modifier
                .fillMaxWidth()
                .then(
                if (sharedTransitionScope != null && animatedVisibilityScope != null && match.highlightVideoId != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            rememberSharedContentState(key = "player-${HighlightUiItem.SupabaseMatch(match).uniqueId}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isFavorite) 1.5.dp else 1.dp,
            color = if (isFavorite) Color(0xFFFFB300).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                SpoilerThumbnailBox(
                    isSpoilerFree = isSpoilerFree,
                    isRevealed = isRevealed,
                    onRevealClick = onRevealClick,
                    badgeAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AsyncImage(
                        model = "https://img.youtube.com/vi/${match.highlightVideoId}/maxresdefault.jpg".toProxyUrl(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LeagueHeader(
                            competitionName = match.competitionName,
                            competitionId = match.competitionId,
                            isDark = true
                        )
                        if (isFavorite) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⭐", fontSize = 12.sp)
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (match.isLive) {
                            MatchStatusBadge(match = match)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (match.homeTeamCrest != null) {
                        SubcomposeAsyncImage(
                            model = match.homeTeamCrest.toProxyUrl(),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            contentScale = ContentScale.Fit
                        )
                        if (shouldShowName) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                    if (shouldShowName) {
                        Text(
                            text = match.homeTeamName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            softWrap = true,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    if (match.isFinished || match.isLive) {
                        ScoreDisplay(
                            homeScore = match.homeScore,
                            awayScore = match.awayScore,
                            isSpoilerFree = isSpoilerFree,
                            isRevealed = isRevealed,
                            onRevealClick = onRevealClick
                        )
                    } else {
                        Text(
                            text = match.formattedLocalKickoff,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    if (shouldShowName) {
                        Text(
                            text = match.awayTeamName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            softWrap = true,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    if (match.awayTeamCrest != null) {
                        if (shouldShowName) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        SubcomposeAsyncImage(
                            model = match.awayTeamCrest.toProxyUrl(),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}
}
