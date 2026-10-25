package com.dirzaaulia.footballclips.monitor.data

import com.dirzaaulia.footballclips.monitor.domain.Candidate
import com.dirzaaulia.footballclips.monitor.domain.Check
import com.dirzaaulia.footballclips.monitor.domain.Cycle
import com.dirzaaulia.footballclips.monitor.domain.Evidence
import com.dirzaaulia.footballclips.monitor.domain.LogLine
import com.dirzaaulia.footballclips.monitor.domain.MatchRun
import com.dirzaaulia.footballclips.monitor.domain.SignalCount
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonObject

internal fun JsonObject.toCycle(): Cycle = Cycle(
    text("id"),
    text("sequence"),
    text("pipeline"),
    text("started_at"),
    text("mode"),
    text("pending", "0"),
    text("error", ""),
    signals(),
)

internal fun JsonObject.toMatch(): MatchRun {
    val rawOutcome = text("outcome", text("status"))
    val videoId = text("highlight_video_id", "")
    val outcome = if (rawOutcome == "FINISHED") {
        if (videoId.isNotBlank() && videoId != "null" && videoId != "Unknown") "covered" else "missing"
    } else {
        rawOutcome
    }
    return MatchRun(
        text("id", ""),
        text("match_id", text("id", "")),
        text("title", "${text("home_team_name")} — ${text("away_team_name")}"),
        text("competition_name", text("competition_name", text("competition_id"))),
        text("utc_date", ""),
        outcome,
        text("reason", ""),
        text("next_check_at", ""),
        signals(),
    )
}

internal fun JsonObject.toEvidence(): Evidence = Evidence(
    records("candidates").map { it.toCandidate() }.toImmutableList(),
    records("events").reversed().map { it.toLogLine() }.toImmutableList(),
    text("next_cursor", ""),
    obj("evaluation").text("match_id", ""),
)

private fun JsonObject.toCandidate(): Candidate = Candidate(
    text("video_id"),
    text("title"),
    text("channel_id"),
    text("published_at"),
    text("duration"),
    records("checks").map { it.toCheck() }.toImmutableList(),
)

private fun JsonObject.toCheck(): Check = Check(
    text("step"),
    text("state"),
    text("reason"),
    text("provider", ""),
    text("model", ""),
)

private fun JsonObject.toLogLine(): LogLine = LogLine(
    text("id"),
    obj("attributes").text("occurred_at", text("created_at")),
    text("level"),
    text("code"),
    text("message"),
    indicator = text("indicator", ""),
)

internal fun JsonObject.signals() = obj("indicators").map { (kind, count) ->
    SignalCount(kind, count.toString().trim('"'))
}.toImmutableList()
