package com.dirzaaulia.footballclips.monitor.presentation

import com.dirzaaulia.footballclips.monitor.domain.Candidate
import com.dirzaaulia.footballclips.monitor.domain.Cycle
import com.dirzaaulia.footballclips.monitor.domain.LogLine
import com.dirzaaulia.footballclips.monitor.domain.MatchRun

internal sealed interface TreeRow {
    val key: String
    data class CycleRow(val cycle: Cycle) : TreeRow {
        override val key = "cycle:" + cycle.id
    }
    data class MatchRow(val parent: String, val match: MatchRun) : TreeRow {
        override val key = parent + ":match:" + match.matchId
    }
    data class Summary(val parent: String, val match: MatchRun) : TreeRow {
        override val key = parent + ":summary"
    }
    data class Video(val parent: String, val match: MatchRun, val video: Candidate) : TreeRow {
        override val key =
            parent + ":video:" + video.id
    }
    data class Event(val parent: String, val event: LogLine) : TreeRow {
        override val key = parent + ":event:" + event.id
    }
    data class EmptyCycleNotice(val parent: String, val message: String) : TreeRow {
        override val key = parent + ":empty_notice"
    }
    data class Footer(override val key: String, val branch: String, val cursor: String) : TreeRow
}

internal fun treeRows(state: MonitorState): List<TreeRow> = buildList {
    if (state.catalogMode.isNotBlank()) {
        state.matches.items.forEach { match -> addMatch("catalog", match, state) }
        add(TreeRow.Footer("root-footer", "", state.matches.cursor))
    } else {
        state.cycles.items.forEach { cycle ->
            val parent = "cycle:" + cycle.id
            add(TreeRow.CycleRow(cycle))
            if (parent in state.expanded) {
                val branchMatches = state.branches[parent]?.items
                val isLoaded = parent in state.branches
                if (isLoaded && (branchMatches == null || branchMatches.isEmpty())) {
                    val notice = if (cycle.pipeline.contains("fixture", true)) {
                        "Cycle #${cycle.sequence} completed: 0 fixture modifications required. All scores and schedules are up-to-date."
                    } else if (cycle.pipeline.contains("youtube", true) || cycle.pipeline.contains("highlight", true)) {
                        "Cycle #${cycle.sequence} completed: No highlight scraping candidates required processing."
                    } else {
                        "Cycle #${cycle.sequence} completed: 0 events or items updated."
                    }
                    add(TreeRow.EmptyCycleNotice(parent, notice))
                } else {
                    branchMatches?.forEach { match -> addMatch(parent, match, state) }
                }
                add(TreeRow.Footer(parent + ":footer", parent, state.branches[parent]?.cursor.orEmpty()))
            }
        }
        add(TreeRow.Footer("root-footer", "", state.cycles.cursor))
    }
}

private fun MutableList<TreeRow>.addMatch(parent: String, match: MatchRun, state: MonitorState) {
    add(TreeRow.MatchRow(parent, match))
    val id = "match:" + match.id.ifBlank { "skip:" + match.matchId }
    if (id !in state.expanded) return
    add(TreeRow.Summary(id, match))
    val evidence = state.details[id]
    if (evidence == null) {
        add(TreeRow.Footer(id + ":footer", id, ""))
        return
    }
    evidence.candidates.forEach { add(TreeRow.Video(id, match, it)) }
    evidence.events.forEach { add(TreeRow.Event(id, it)) }
    add(TreeRow.Footer(id + ":footer", id, evidence.next))
}
