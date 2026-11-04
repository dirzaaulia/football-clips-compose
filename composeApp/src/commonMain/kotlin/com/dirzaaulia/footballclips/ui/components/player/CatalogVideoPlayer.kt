package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.player.YouTubePlayerView

@Composable
internal fun CatalogVideoPlayer(
    item: HighlightUiItem?,
    videoId: String,
    onDismiss: (Boolean) -> Unit,
) {
    val match = (item as? HighlightUiItem.OracleMatch)?.match
    val effectiveVideoId = if (videoId.isNotBlank()) videoId else match?.backupHighlightVideoId.orEmpty()
    val uriHandler = LocalUriHandler.current

    if (effectiveVideoId.isBlank()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color(0xFF0A0E14)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Highlight video unavailable for this match.",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    } else if (match?.playbackMode == "external") {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text("This publisher allows playback on YouTube.")
            Button(onClick = { uriHandler.openUri("https://www.youtube.com/watch?v=$effectiveVideoId") }) {
                Text("Watch on YouTube")
            }
        }
    } else {
        YouTubePlayerView(
            videoId = effectiveVideoId,
            backupVideoId = match?.backupHighlightVideoId,
            regionInfo = match?.youtubeRegionInfo,
            backupRegionInfo = match?.backupYoutubeRegionInfo,
            onDismiss = onDismiss,
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
        )
    }
}
