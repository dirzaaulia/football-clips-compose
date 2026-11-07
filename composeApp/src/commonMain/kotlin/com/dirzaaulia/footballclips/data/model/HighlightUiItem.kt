package com.dirzaaulia.footballclips.data.model

import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.util.normalizeLeagueName

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

fun HighlightUiItem.matchesLeague(league: com.dirzaaulia.footballclips.data.model.remote.BigLeague): Boolean {
    return when (this) {
        is HighlightUiItem.OracleMatch -> {
            if (match.competitionId.isNotBlank() && league.id.isNotBlank()) {
                if (match.competitionId.equals(league.id, ignoreCase = true)) return true
            }
            val matchNorm = match.competitionName.normalizeLeagueName()
            val leagueNorm = league.name.normalizeLeagueName()
            match.competitionName.equals(league.name, ignoreCase = true) || matchNorm.equals(leagueNorm, ignoreCase = true)
        }
        is HighlightUiItem.Highlight -> {
            val hlNorm = highlight.leagueName.normalizeLeagueName()
            val leagueNorm = league.name.normalizeLeagueName()
            highlight.leagueName.equals(league.name, ignoreCase = true) || hlNorm.equals(leagueNorm, ignoreCase = true)
        }
        is HighlightUiItem.BannerAd -> false
    }
}

