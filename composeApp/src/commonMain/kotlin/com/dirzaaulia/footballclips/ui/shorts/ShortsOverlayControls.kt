package com.dirzaaulia.footballclips.ui.shorts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.domain.model.FootballShort
import com.dirzaaulia.footballclips.ui.theme.MatchPulseRed
import com.dirzaaulia.footballclips.ui.theme.ObsidianSurfaceTier2
import com.dirzaaulia.footballclips.ui.theme.VoltLime
import com.dirzaaulia.footballclips.ui.theme.VoltLimeDarkText
import kotlinx.coroutines.delay

@Composable
fun ShortsOverlayControls(
    short: FootballShort,
    isPlaying: Boolean,
    isMuted: Boolean,
    onTogglePlayPause: () -> Unit,
    onToggleMute: () -> Unit,
    onShare: () -> Unit,
    onOpenYouTube: () -> Unit,
    modifier: Modifier = Modifier,
    onLike: () -> Unit = {},
) {
    var showPulse by remember { mutableStateOf(false) }

    LaunchedEffect(showPulse) {
        if (showPulse) {
            delay(400)
            showPulse = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showPulse = true
                        onTogglePlayPause()
                    },
                )
            },
    ) {
        ShortsRightActionRail(
            isMuted = isMuted,
            onShare = onShare,
            onToggleMute = onToggleMute,
            onOpenYouTube = onOpenYouTube,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 80.dp),
        )

        ShortsBottomMetadata(
            short = short,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, end = 80.dp, bottom = 24.dp),
        )

        AnimatedVisibility(
            visible = !isPlaying || showPulse,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Box(
                modifier = Modifier.size(64.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp),
                )
            }
        }
    }
}

@Composable
private fun ShortsRightActionRail(
    isMuted: Boolean,
    onShare: () -> Unit,
    onToggleMute: () -> Unit,
    onOpenYouTube: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ShortsActionItem(icon = Icons.Default.Share, label = "Share", onClick = onShare)
        ShortsActionItem(
            icon = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
            label = if (isMuted) "Muted" else "Sound",
            onClick = onToggleMute,
        )
        ShortsActionItem(
            icon = Icons.AutoMirrored.Filled.OpenInNew,
            label = "YouTube",
            tint = MatchPulseRed,
            onClick = onOpenYouTube,
        )

    }
}

@Composable
private fun ShortsActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() },
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(ObsidianSurfaceTier2.copy(alpha = 0.85f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ShortsBottomMetadata(
    short: FootballShort,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = VoltLime,
            ) {
                Text(
                    text = short.category.replace("_", " "),
                    color = VoltLimeDarkText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
            Text(
                text = "@${short.channelName}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.15f),
            ) {
                Text(
                    text = "Subscribe",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        Text(
            text = short.title,
            color = Color.White.copy(alpha = 0.95f),
            fontSize = 13.sp,
            maxLines = 2,
            lineHeight = 18.sp,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = VoltLime,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = "Peter Drury Official Commentary • Live Pitch Acoustics",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
