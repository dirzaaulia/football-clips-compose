package com.dirzaaulia.footballclips.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun YouTubePlayerView(
    videoId: String, 
    modifier: Modifier = Modifier,
    onDismiss: (Boolean) -> Unit = {}
)
