package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge
import com.dirzaaulia.footballclips.ui.components.common.shimmerEffect
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
            GlassLogoBadge(
                model = logoUrl.toProxyUrl(),
                contentDescription = name,
                size = 48.dp,
                padding = 4.dp,
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
        GlassLogoBadge(
            model = crest?.toProxyUrl(),
            contentDescription = name,
            size = if (showName) 44.dp else 36.dp,
            padding = 4.dp,
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
    when {
        match.isLive -> LiveMatchStatusBadge(match = match, isProminent = isProminent)
        match.isFinished -> FinishedMatchStatusBadge(isProminent = isProminent)
        else -> UpcomingMatchStatusBadge(status = match.status, isProminent = isProminent)
    }
}

@Composable
private fun LiveMatchStatusBadge(match: Match, isProminent: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )
    val isPaused = match.status.equals("PAUSED", ignoreCase = true) || match.displayClock?.equals("HT", ignoreCase = true) == true
    val liveText = when {
        isPaused -> "HT"
        !match.displayClock.isNullOrBlank() -> "LIVE ${match.displayClock}"
        else -> "LIVE"
    }

    Surface(
        color = Color.Red.copy(alpha = 0.15f),
        shape = RoundedCornerShape(if (isProminent) 12.dp else 8.dp),
        border = BorderStroke(
            if (isProminent) 2.dp else 1.dp,
            Color.Red.copy(alpha = 0.6f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (isProminent) 12.dp else 8.dp,
                vertical = if (isProminent) 6.dp else 4.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(if (isProminent) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = alpha)),
            )
            Spacer(modifier = Modifier.width(if (isProminent) 8.dp else 6.dp))
            Text(
                text = liveText,
                style = if (isProminent) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                color = Color.Red,
                fontWeight = FontWeight.Black,
                letterSpacing = if (isProminent) 1.sp else 0.sp,
            )
        }
    }
}

@Composable
private fun FinishedMatchStatusBadge(isProminent: Boolean) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(if (isProminent) 12.dp else 8.dp),
    ) {
        Text(
            text = "FINISHED",
            style = if (isProminent) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(
                horizontal = if (isProminent) 12.dp else 8.dp,
                vertical = if (isProminent) 6.dp else 4.dp,
            ),
        )
    }
}

@Composable
private fun UpcomingMatchStatusBadge(status: String, isProminent: Boolean) {
    val statusText = if (status == "TIMED" || status == "SCHEDULED") "UPCOMING" else status.replace("_", " ")
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(if (isProminent) 12.dp else 8.dp),
    ) {
        Text(
            text = statusText,
            style = if (isProminent) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(
                horizontal = if (isProminent) 12.dp else 8.dp,
                vertical = if (isProminent) 6.dp else 4.dp,
            ),
        )
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
    // Live matches never hide score; spoiler shielding is strictly for finished matches
    if (isSpoilerFree && !isRevealed && !isLive) {
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
