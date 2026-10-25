package com.dirzaaulia.footballclips.monitor.data

import com.dirzaaulia.footballclips.monitor.domain.ActivitySample
import com.dirzaaulia.footballclips.monitor.domain.Backlog
import com.dirzaaulia.footballclips.monitor.domain.CountryCoverage
import com.dirzaaulia.footballclips.monitor.domain.Dashboard
import com.dirzaaulia.footballclips.monitor.domain.Metric
import com.dirzaaulia.footballclips.monitor.domain.OriginUsage
import com.dirzaaulia.footballclips.monitor.domain.QuotaBucket
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonObject

internal fun JsonObject.toDashboard(): Dashboard = Dashboard(
    activationAt = obj("pending_rollout").obj("value").text("not_before", ""),
    metrics = dashboardMetrics(),
    quota = obj("quota").quotaBuckets(),
    scope = text("scope"), generated = text("generated_at"), reset = obj("quota").text("reset_at"),
    rollout = obj("rollout").text("stage"), fraction = obj("rollout").text("search_fraction"),
    series = obj("series").activitySamples(),
    countries = records("countries").map {
        CountryCoverage(it.text("country"), it.text("matches"), it.text("blocked"), it.text("unknown"))
    }.toImmutableList(),
    origins = records("quota_origins").map {
        OriginUsage(it.text("origin"), it.text("purpose"), it.text("bucket"), it.text("used"))
    }.toImmutableList(),
    jobs = records("jobs").map { Backlog(it.text("state"), it.text("count"), it.text("oldest")) }.toImmutableList(),
    delivery = records("outbox").map {
        Backlog(it.text("kind") + " · " + it.text("state"), it.text("count"), "")
    }.toImmutableList(),
)

private fun JsonObject.dashboardMetrics() = listOf(
    "total_matches" to "all", "total_finished" to "all_finished", "finished" to "finished",
    "with_highlights" to "covered", "missing" to "missing", "missing_percent" to "missing",
    "country_watchable" to "country", "country_gaps" to "blocked", "live" to "live", "stale_live" to "stale_live",
    "accepted" to "video_accepted", "watchable" to "video_watchable", "blocked" to "video_blocked",
    "unknown" to "video_unknown",
).map { (key, target) ->
    val source = obj(if (target.startsWith("video_")) "videos" else "catalog")
    Metric(key, source.text(key), target)
}.toImmutableList()

private fun JsonObject.quotaBuckets() = records("buckets").map {
    val used = it.text("estimated_used", "0").toLongOrNull() ?: 0L
    val allowance = it.text("allowance", "0").toLongOrNull() ?: 0L
    QuotaBucket(
        it.text("bucket"),
        used,
        allowance,
        (allowance - used).coerceAtLeast(0),
        it.text("reserved", "0").toLong(),
        it.text("reconciled_at"),
    )
}.toImmutableList()

private fun JsonObject.activitySamples() = records("items").map {
    ActivitySample(
        it.text("time"),
        it.text("events", "0").toLong(),
        it.text("errors", "0").toLong(),
        it.text("selections", "0").toLong(),
        it.text("visibility_ms").toDoubleOrNull(),
    )
}.toImmutableList()
