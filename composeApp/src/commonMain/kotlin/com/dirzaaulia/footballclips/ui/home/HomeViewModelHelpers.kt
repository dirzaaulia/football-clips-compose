package com.dirzaaulia.footballclips.ui.home

import com.dirzaaulia.footballclips.data.model.HighlightUiItem

internal fun buildListWithAds(items: List<HighlightUiItem>, isAdsRemoved: Boolean): List<HighlightUiItem> {
    if (items.isEmpty()) return emptyList()
    if (isAdsRemoved) return items

    val result = mutableListOf<HighlightUiItem>()
    for (i in items.indices) {
        result.add(items[i])
        if ((i + 1) % 6 == 0 && i < items.size - 1) {
            result.add(HighlightUiItem.BannerAd("ad-$i"))
        }
    }
    return result
}

internal fun isLeagueMatch(target: String?, candidate: String?): Boolean {
    if (target == null || candidate == null) return false
    val t = target.lowercase().trim()
    val c = candidate.lowercase().trim()
    if (t == c) return true

    val plRegex = Regex(".*\\b(pl|epl|premier league)\\b.*", RegexOption.IGNORE_CASE)
    if (plRegex.matches(t) && plRegex.matches(c)) return true

    return t.contains(c) || c.contains(t)
}
