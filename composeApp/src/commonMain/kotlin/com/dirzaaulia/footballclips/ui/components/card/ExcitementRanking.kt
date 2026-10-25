package com.dirzaaulia.footballclips.ui.components.card

import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import kotlin.math.abs

private val BIG_CLUB_PATTERNS = listOf(
    "real madrid", "barcelona", "atletico madrid",
    "manchester city", "man city", "arsenal", "liverpool",
    "manchester united", "man utd", "chelsea", "tottenham",
    "newcastle", "bayern", "dortmund", "leverkusen",
    "juventus", "inter", "milan", "napoli", "roma", "psg", "paris"
)

private fun isBigClub(name: String?): Boolean {
    if (name.isNullOrBlank()) return false
    val n = name.lowercase()
    return BIG_CLUB_PATTERNS.any { n.contains(it) }
}

data class RankedHighlightItem(
    val item: HighlightUiItem,
    val score: Int,
    val tag: String
)

fun calculateExcitementRank(item: HighlightUiItem): RankedHighlightItem {
    val home: String
    val away: String
    val homeScore: Int?
    val awayScore: Int?

    when (item) {
        is HighlightUiItem.OracleMatch -> {
            home = item.match.homeTeamName
            away = item.match.awayTeamName
            homeScore = item.match.homeScore
            awayScore = item.match.awayScore
        }
        is HighlightUiItem.Highlight -> {
            home = item.highlight.homeTeam
            away = item.highlight.awayTeam
            homeScore = null
            awayScore = null
        }
        is HighlightUiItem.BannerAd -> {
            return RankedHighlightItem(item, -1, "")
        }
    }

    val hBig = isBigClub(home)
    val aBig = isBigClub(away)
    val isClashOfTitans = hBig && aBig
    val isBigMatch = hBig || aBig

    val hasScores = homeScore != null && awayScore != null
    val totalGoals = if (homeScore != null && awayScore != null) homeScore + awayScore else 0
    val goalDiff = if (homeScore != null && awayScore != null) abs(homeScore - awayScore) else 99

    var score = 0
    var tag = "🔥 Must-Watch"

    if (isClashOfTitans) {
        score += 65
        tag = "👑 Clash of Titans"
    } else if (isBigMatch) {
        score += 40
        tag = "🌟 Heavyweight Duel"
    }

    if (totalGoals >= 5) {
        score += 50
        tag = "🔥 Goal Fest • $totalGoals Goals"
    } else if (totalGoals == 4) {
        score += 35
        tag = "⚡ 4-Goal Thriller"
    } else if (totalGoals == 3) {
        score += 20
    }

    if (hasScores && goalDiff <= 1 && totalGoals >= 3) {
        score += 15
        if (tag == "🔥 Must-Watch") tag = "⚡ Dramatic Decider"
    }

    return RankedHighlightItem(item, score, tag)
}

fun arrangeExcitementCarouselPages(
    items: List<HighlightUiItem>,
    isAdsRemoved: Boolean,
    isWasmWeb: Boolean
): List<Pair<HighlightUiItem, String>> {
    val videoItems = items.filter { it !is HighlightUiItem.BannerAd }
    if (videoItems.isEmpty()) return emptyList()

    val ranked = videoItems.map { calculateExcitementRank(it) }
        .sortedByDescending { it.score }
        .take(6)

    if (isAdsRemoved || isWasmWeb) {
        return ranked.map { it.item to it.tag }
    }

    val result = mutableListOf<Pair<HighlightUiItem, String>>()
    var adIndex = 0
    ranked.forEachIndexed { index, rankedItem ->
        result.add(rankedItem.item to rankedItem.tag)
        if ((index + 1) % 2 == 0 && index < ranked.size - 1) {
            result.add(HighlightUiItem.BannerAd to "")
        }
    }
    return result
}
