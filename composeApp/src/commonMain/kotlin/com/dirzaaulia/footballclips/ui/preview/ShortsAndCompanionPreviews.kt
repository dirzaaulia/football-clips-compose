package com.dirzaaulia.footballclips.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.domain.model.FootballShort
import com.dirzaaulia.footballclips.domain.model.PitchPlay
import com.dirzaaulia.footballclips.ui.score.companion.MatchReactionOverlay
import com.dirzaaulia.footballclips.ui.score.companion.PitchRadarDeck
import com.dirzaaulia.footballclips.ui.shorts.ShortsOverlayControls

val previewMockPitchPlays = listOf(
    PitchPlay(
        id = "play_1",
        clock = "14'",
        type = "shot",
        typeText = "Shot on Target",
        text = "Martin Odegaard right footed shot from outside the box saved.",
        xPercent = 76.5f,
        yPercent = 48.0f,
    ),
    PitchPlay(
        id = "play_2",
        clock = "38'",
        type = "corner",
        typeText = "Corner Kick",
        text = "Corner, Arsenal. Conceded by Levi Colwill.",
        xPercent = 98.0f,
        yPercent = 5.0f,
    ),
    PitchPlay(
        id = "play_3",
        clock = "67'",
        type = "goal",
        typeText = "Goal",
        text = "Goal! Arsenal 2, Chelsea 1. Bukayo Saka curls into the top corner!",
        xPercent = 88.5f,
        yPercent = 32.0f,
    ),
)

val previewMockShort = FootballShort(
    id = "short_preview_1",
    youtubeVideoId = "xyz98765432",
    title = "Bukayo Saka unreal 90th-minute solo goal vs Chelsea! 🎯🔥",
    channelId = "UCpryVRk_VDudG8SHXgWcG0w",
    channelName = "Arsenal Official",
    clubId = 359,
    category = "GOALS_SKILLS",
    thumbnailUrl = "https://i.ytimg.com/vi/xyz98765432/hqdefault.jpg",
    durationSeconds = 42,
    likeCount = 18420,
    isFeatured = true,
    publishedAt = "2026-11-08T19:30:00Z",
)

@Composable
fun LiveCompanionPreviewContent(deviceLabel: String) {
    MatrixScreenContainer(title = "2D Pitch Radar & Live Companion", deviceLabel = deviceLabel) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PitchRadarDeck(plays = previewMockPitchPlays)
            }
            MatchReactionOverlay(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun ShortsTabPreviewContent(deviceLabel: String) {
    MatrixScreenContainer(title = "Football Shorts Reels", deviceLabel = deviceLabel) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B0F17)),
        ) {
            ShortsOverlayControls(
                short = previewMockShort,
                isPlaying = true,
                isMuted = false,
                onTogglePlayPause = {},
                onToggleMute = {},
                onLike = {},
                onShare = {},
                onOpenYouTube = {},
            )
        }
    }
}
