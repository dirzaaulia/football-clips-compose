package com.dirzaaulia.footballclips.ui.score.components

import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.matchesLeague
import com.dirzaaulia.footballclips.data.model.remote.BigLeague

internal fun buildFixtureGroupsWithAds(
    matches: List<HighlightUiItem>,
    dynamicCompetitions: List<BigLeague>,
    selectedCompetitionId: String?,
    isAdsRemoved: Boolean,
): List<Pair<BigLeague?, List<HighlightUiItem>>> {
    val rawMatches = matches.filter { it !is HighlightUiItem.BannerAd }
    val rawGroups = if (!selectedCompetitionId.isNullOrEmpty()) {
        listOf(null to rawMatches)
    } else {
        val remaining = rawMatches.toMutableList()
        val groups = mutableListOf<Pair<BigLeague?, List<HighlightUiItem>>>()
        dynamicCompetitions.forEach { comp ->
            val compMatches = remaining.filter { it.matchesLeague(comp) }
            if (compMatches.isNotEmpty()) {
                groups.add(comp to compMatches)
                remaining.removeAll(compMatches)
            }
        }
        if (remaining.isNotEmpty()) groups.add(null to remaining)
        groups
    }

    if (isAdsRemoved) return rawGroups

    var globalMatchCount = 0
    var adCount = 0
    val totalMatches = rawGroups.sumOf { it.second.size }
    return rawGroups.map { (comp, groupMatches) ->
        val listWithAds = mutableListOf<HighlightUiItem>()
        groupMatches.forEach { matchItem ->
            listWithAds.add(matchItem)
            globalMatchCount++
            if (globalMatchCount % 5 == 0 && globalMatchCount < totalMatches) {
                listWithAds.add(HighlightUiItem.BannerAd("fixture-feed-ad-${adCount++}"))
            }
        }
        comp to listWithAds
    }
}
