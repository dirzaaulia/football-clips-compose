package com.dirzaaulia.footballclips.monitor.data

import com.dirzaaulia.footballclips.monitor.domain.Cycle
import com.dirzaaulia.footballclips.monitor.domain.MatchRun
import com.dirzaaulia.footballclips.monitor.domain.Page
import io.ktor.http.encodeURLParameter
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonObject

internal fun restoredCycles(pages: List<JsonObject>): Page<Cycle> {
    val items = pages.flatMap {
        it.records("items")
    }.map { it.toCycle() }.distinctBy { it.id }.sortedByDescending { it.time }
    val oldest = pages.minByOrNull { it.records("items").firstOrNull()?.text("started_at").orEmpty() }
    val cursor = oldest?.obj("next_cursor")
    return Page(
        items.toImmutableList(),
        if (cursor.isNullOrEmpty()) {
            ""
        } else {
            "before=${cursor.text("before").encodeURLParameter()}&before_id=${cursor.text("before_id")}"
        },
    )
}

internal fun restoredMatches(pages: List<JsonObject>): Page<MatchRun> {
    val items = pages.flatMap { it.records("items") }.map { it.toMatch() }.distinctBy { it.matchId }
        .sortedWith(compareByDescending<MatchRun> { it.date }.thenByDescending { it.matchId.toLongOrNull() })
    val oldest = pages.minByOrNull { it.records("items").firstOrNull()?.text("utc_date").orEmpty() }
    return Page(items.toImmutableList(), oldest?.text("next_cursor", "").orEmpty())
}
