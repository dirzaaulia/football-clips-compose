package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.domain.model.FootballShort
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Diversifies Shorts using time-decay hype scoring, session jitter, and an anti-streak
 * channel cooldown so users never see consecutive clips from the same channel.
 */
internal fun diversifyShortsFeed(
    shorts: List<FootballShort>,
    limit: Int,
    cooldown: Int = 4,
): List<FootballShort> {
    if (shorts.size <= 1) return shorts.take(limit)

    val nowMs = Clock.System.now().toEpochMilliseconds()
    val remaining = shorts
        .map { item -> item to computeEffectiveScore(item, nowMs) }
        .sortedByDescending { it.second }
        .toMutableList()

    val result = ArrayList<FootballShort>(min(limit, remaining.size))
    val recentChannels = ArrayDeque<String>()

    while (result.size < limit && remaining.isNotEmpty()) {
        val chosenIdx = remaining.indexOfFirst { (item, _) ->
            item.channelId !in recentChannels
        }.let { idx ->
            if (idx >= 0) idx else pickLeastRecentChannelIndex(remaining, recentChannels)
        }
        val picked = remaining.removeAt(chosenIdx).first
        result.add(picked)
        recentChannels.addLast(picked.channelId)
        if (recentChannels.size > cooldown) {
            recentChannels.removeFirst()
        }
    }
    return result
}

private fun computeEffectiveScore(
    item: FootballShort,
    nowMs: Long,
): Double {
    val pubMs = runCatching { Instant.parse(item.publishedAt).toEpochMilliseconds() }.getOrDefault(nowMs)
    val ageHours = max(0.0, (nowMs - pubMs) / 3_600_000.0)
    val decay = min(30.0, ageHours * 0.08)
    val jitter = Random.nextDouble(0.0, 12.0)
    val baseScore = if (item.isFeatured) 20.0 else 0.0
    return baseScore - decay + jitter
}

private fun pickLeastRecentChannelIndex(
    remaining: List<Pair<FootballShort, Double>>,
    recentChannels: ArrayDeque<String>,
): Int {
    var bestIdx = 0
    var oldestPos = recentChannels.size
    for (i in remaining.indices) {
        val pos = recentChannels.lastIndexOf(remaining[i].first.channelId)
        if (pos < oldestPos) {
            oldestPos = pos
            bestIdx = i
        }
    }
    return bestIdx
}
