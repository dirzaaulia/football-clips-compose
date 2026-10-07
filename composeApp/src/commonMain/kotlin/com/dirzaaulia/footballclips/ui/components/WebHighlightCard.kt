package com.dirzaaulia.footballclips.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.adaptive.LocalAnimatedVisibilityScope
import com.dirzaaulia.footballclips.ui.adaptive.LocalSharedTransitionScope
import com.dirzaaulia.footballclips.util.toProxyUrl

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun WebHighlightCard(
    item: HighlightUiItem,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSpoilerFree: Boolean = false,
    isRevealed: Boolean = false,
    onRevealClick: (() -> Unit)? = null
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    val title: String
    val thumbnail: String
    val leagueName: String
    val leagueLogo: String
    val date: String
    val videoId: String

    when (item) {
        is HighlightUiItem.SupabaseMatch -> {
            title = "${item.match.homeTeamName} vs ${item.match.awayTeamName}"
            thumbnail = "https://img.youtube.com/vi/${item.match.highlightVideoId}/maxresdefault.jpg"
            leagueName = item.match.competitionName
            leagueLogo = item.match.leagueEmblemUrl
            date = item.match.dateOnly
            videoId = item.match.highlightVideoId ?: ""
        }
        is HighlightUiItem.Highlight -> {
            title = item.highlight.title
            thumbnail = item.highlight.thumbnail
            leagueName = item.highlight.leagueName
            leagueLogo = item.highlight.leagueLogo
            date = item.highlight.date
            videoId = item.highlight.embedHtml
        }
        else -> return
    }

    Column(
        modifier = modifier
            .clickable { onClick(videoId) }
            .then(
                if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            rememberSharedContentState(key = "player-${item.uniqueId}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                } else Modifier
            )
    ) {
        SpoilerThumbnailBox(
            isSpoilerFree = isSpoilerFree,
            isRevealed = isRevealed,
            onRevealClick = onRevealClick,
            badgeAlignment = Alignment.Center,
            iconOnly = item is HighlightUiItem.Highlight,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            SubcomposeAsyncImage(
                model = thumbnail.toProxyUrl(),
                contentDescription = title,
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
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leagueLogo.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = leagueLogo.toProxyUrl(),
                        contentDescription = null,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ExpressiveLoadingIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp
                                )
                            }
                        },
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = leagueName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Text(
                    text = " • ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )

                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun TeamInfo(name: String, logo: String, modifier: Modifier = Modifier, isEnd: Boolean = false) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isEnd) Arrangement.End else Arrangement.Start
    ) {
        if (!isEnd) {
            TeamLogo(logo)
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (isEnd) TextAlign.End else TextAlign.Start,
            modifier = Modifier.weight(1f, fill = false)
        )
        
        if (isEnd) {
            Spacer(modifier = Modifier.width(8.dp))
            TeamLogo(logo)
        }
    }
}

@Composable
fun TeamLogo(logo: String) {
    if (logo.isNotEmpty()) {
        SubcomposeAsyncImage(
            model = logo.toProxyUrl(),
            contentDescription = null,
            loading = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveLoadingIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 1.5.dp
                    )
                }
            },
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.8f))
                .padding(2.dp)
        )
    }
}
