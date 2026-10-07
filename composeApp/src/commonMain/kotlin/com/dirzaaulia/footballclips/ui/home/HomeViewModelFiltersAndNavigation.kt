package com.dirzaaulia.footballclips.ui.home

import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.domain.model.Match

internal fun findVideoById(
    supabaseMatches: List<Match>,
    allHighlights: List<HighlightUiModel>,
    itemId: String
): HighlightUiItem? {
    return (supabaseMatches.map { HighlightUiItem.SupabaseMatch(it) } + 
            allHighlights.map { HighlightUiItem.Highlight(it) })
        .find { it.uniqueId == itemId }
}
