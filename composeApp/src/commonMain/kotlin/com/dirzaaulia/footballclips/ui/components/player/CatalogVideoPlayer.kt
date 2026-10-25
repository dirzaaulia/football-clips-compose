package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.player.YouTubePlayerView

@Composable
internal fun CatalogVideoPlayer(item: HighlightUiItem?, videoId: String, onDismiss: (Boolean) -> Unit) {
    val match = (item as? HighlightUiItem.OracleMatch)?.match
    val uriHandler = LocalUriHandler.current
    if (match?.playbackMode == "external") {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text("This publisher allows playback on YouTube.")
            Button(onClick = { uriHandler.openUri("https://www.youtube.com/watch?v=$videoId") }) {
                Text("Watch on YouTube")
            }
        }
    } else {
        YouTubePlayerView(
            videoId = videoId,
            backupVideoId = match?.backupHighlightVideoId,
            regionInfo = match?.youtubeRegionInfo,
            backupRegionInfo = match?.backupYoutubeRegionInfo,
            onDismiss = onDismiss,
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
        )
    }
}
