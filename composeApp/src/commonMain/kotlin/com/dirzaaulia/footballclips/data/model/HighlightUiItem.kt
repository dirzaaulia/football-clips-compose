package com.dirzaaulia.footballclips.data.model

import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.domain.model.Match

sealed class HighlightUiItem {
    data class OracleMatch(val match: Match) : HighlightUiItem()
    data class Highlight(val highlight: HighlightUiModel) : HighlightUiItem()
    data object BannerAd : HighlightUiItem()
}

val HighlightUiItem.uniqueId: String
    get() = when (this) {
        is HighlightUiItem.OracleMatch -> "match-${match.id}"
        is HighlightUiItem.Highlight -> "hl-${highlight.id}"
        is HighlightUiItem.BannerAd -> "ad-${hashCode()}"
    }

val HighlightUiItem.competitionName: String
    get() = when (this) {
        is HighlightUiItem.OracleMatch -> match.competitionName
        is HighlightUiItem.Highlight -> highlight.leagueName
        is HighlightUiItem.BannerAd -> ""
    }

val HighlightUiItem.competitionEmblemUrl: String?
    get() = when (this) {
        is HighlightUiItem.OracleMatch -> match.leagueEmblemUrl
        is HighlightUiItem.Highlight -> highlight.leagueLogo
        is HighlightUiItem.BannerAd -> null
    }

