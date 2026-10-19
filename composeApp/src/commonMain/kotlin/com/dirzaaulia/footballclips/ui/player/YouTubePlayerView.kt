package com.dirzaaulia.footballclips.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import com.dirzaaulia.footballclips.domain.model.RegionRestriction

@Composable
expect fun YouTubePlayerView(
    videoId: String, 
    backupVideoId: String? = null,
    regionInfo: RegionRestriction? = null,
    backupRegionInfo: RegionRestriction? = null,
    modifier: Modifier = Modifier,
    onDismiss: (Boolean) -> Unit = {}
)

