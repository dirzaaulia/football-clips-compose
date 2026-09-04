package com.dirzaaulia.footballclips.data.model

import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.domain.model.Match

sealed class HighlightUiItem {
    data class SupabaseMatch(val match: Match) : HighlightUiItem()
    data class Highlight(val highlight: HighlightUiModel) : HighlightUiItem()
    data object BannerAd : HighlightUiItem()
}

val HighlightUiItem.uniqueId: String
    get() = when (this) {
        is HighlightUiItem.SupabaseMatch -> "match-${match.id}"
        is HighlightUiItem.Highlight -> "highlight-${highlight.id}"
        else -> "none"
    }
