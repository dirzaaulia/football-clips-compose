package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dirzaaulia.footballclips.data.model.HighlightUiItem

internal class MobileVideoPlayerState {
    var selectedItem by mutableStateOf<HighlightUiItem?>(null)
    var selectedVideoHtml by mutableStateOf<String?>(null)
    var showVideoPlayer by mutableStateOf(false)
    var onDismissVideo by mutableStateOf<((Boolean) -> Unit)?>(null)

    fun onVideoClick(item: HighlightUiItem, onDismiss: (Boolean) -> Unit) {
        selectedItem = item
        selectedVideoHtml = when (item) {
            is HighlightUiItem.SupabaseMatch -> item.match.highlightVideoId
            is HighlightUiItem.Highlight -> item.highlight.embedHtml
            else -> null
        }
        onDismissVideo = onDismiss
        showVideoPlayer = selectedVideoHtml != null
    }

    fun onDismiss(isExternal: Boolean) {
        showVideoPlayer = false
        onDismissVideo?.invoke(isExternal)
    }
}
