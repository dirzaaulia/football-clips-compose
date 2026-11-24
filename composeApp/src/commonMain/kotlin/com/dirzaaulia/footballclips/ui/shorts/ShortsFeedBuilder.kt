package com.dirzaaulia.footballclips.ui.shorts

import com.dirzaaulia.footballclips.domain.model.FootballShort

internal fun buildShortsFeedItems(
    shorts: List<FootballShort>,
    isAdsRemoved: Boolean,
): List<ShortsFeedItem> {
    if (shorts.isEmpty()) return emptyList()
    val items = mutableListOf<ShortsFeedItem>()
    var adIndex = 0
    shorts.forEachIndexed { index, short ->
        items.add(ShortsFeedItem.Video(short, itemKey = "${short.youtubeVideoId}_$index"))
        if (!isAdsRemoved && (index + 1) % 6 == 0) {
            items.add(ShortsFeedItem.Ad(adIndex++))
        }
    }
    return items
}

internal fun appendShortsFeedItems(
    currentItems: List<ShortsFeedItem>,
    newShorts: List<FootballShort>,
    isAdsRemoved: Boolean,
): List<ShortsFeedItem> {
    if (newShorts.isEmpty()) return currentItems
    val items = currentItems.toMutableList()
    var videoCount = currentItems.count { it is ShortsFeedItem.Video }
    var adIndex = currentItems.count { it is ShortsFeedItem.Ad }

    newShorts.forEach { short ->
        val key = "${short.youtubeVideoId}_${items.size}"
        items.add(ShortsFeedItem.Video(short, itemKey = key))
        videoCount++
        if (!isAdsRemoved && videoCount % 6 == 0) {
            items.add(ShortsFeedItem.Ad(adIndex++))
        }
    }
    return items
}

internal fun createLoopFallbackBatch(
    cachedShorts: List<FootballShort>,
    currentItems: List<ShortsFeedItem>,
    isAdsRemoved: Boolean,
): List<ShortsFeedItem> {
    if (cachedShorts.isEmpty()) return currentItems
    val loopedBatch = cachedShorts.shuffled().take(12)
    return appendShortsFeedItems(currentItems, loopedBatch, isAdsRemoved)
}
