package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.ui.components.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
internal fun TeamBadgeColumn(
    name: String,
    logoUrl: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (logoUrl != null) {
            AsyncImage(
                model = logoUrl.toProxyUrl(),
                contentDescription = name,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun TeamItem(
    name: String,
    crest: String?,
    modifier: Modifier = Modifier,
    showName: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SubcomposeAsyncImage(
            model = crest?.toProxyUrl(),
            contentDescription = null,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveLoadingIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                }
            },
            modifier = Modifier.size(if (showName) 44.dp else 36.dp),
            contentScale = ContentScale.Fit
        )
        if (showName) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun MatchStatusBadge(match: Match, isProminent: Boolean = false) {
    if (match.isLive) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )

        Surface(
            color = Color.Red.copy(alpha = 0.15f),
            shape = RoundedCornerShape(if (isProminent) 12.dp else 8.dp),
            border = BorderStroke(
                if (isProminent) 2.dp else 1.dp, 
                Color.Red.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = if (isProminent) 12.dp else 8.dp, 
                    vertical = if (isProminent) 6.dp else 4.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isProminent) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.width(if (isProminent) 8.dp else 6.dp))
                Text(
                    text = "LIVE",
                    style = if (isProminent) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                    color = Color.Red,
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (isProminent) 1.sp else 0.sp
                )
            }
        }
    } else if (match.isFinished) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(if (isProminent) 12.dp else 8.dp)
        ) {
            Text(
                text = "FINISHED",
                style = if (isProminent) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(
                    horizontal = if (isProminent) 12.dp else 8.dp, 
                    vertical = if (isProminent) 6.dp else 4.dp
                )
            )
        }
    } else {
        val statusText = if (match.status == "TIMED" || match.status == "SCHEDULED") "UPCOMING" else match.status.replace("_", " ")
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(if (isProminent) 12.dp else 8.dp)
        ) {
            Text(
                text = statusText,
                style = if (isProminent) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(
                    horizontal = if (isProminent) 12.dp else 8.dp, 
                    vertical = if (isProminent) 6.dp else 4.dp
                )
            )
        }
    }
}

@Composable
fun ScoreDisplay(
    homeScore: Int?,
    awayScore: Int?,
    isLive: Boolean = false,
    isSpoilerFree: Boolean = false,
    isRevealed: Boolean = false,
    onRevealClick: (() -> Unit)? = null
) {
    if (isSpoilerFree && !isRevealed) {
        Text(
            text = "? - ?",
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .then(if (onRevealClick != null) Modifier.clickable { onRevealClick() } else Modifier),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 2.sp
        )
    } else {
        Text(
            text = "${homeScore ?: 0} - ${awayScore ?: 0}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (isLive) Color.Red else MaterialTheme.colorScheme.onSurface,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
