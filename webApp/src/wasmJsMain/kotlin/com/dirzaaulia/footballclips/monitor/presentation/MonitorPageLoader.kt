package com.dirzaaulia.footballclips.monitor.presentation

import com.dirzaaulia.footballclips.monitor.domain.MatchRun
import com.dirzaaulia.footballclips.monitor.domain.MonitorRepository
import com.dirzaaulia.footballclips.monitor.domain.Page
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class MonitorPageLoader(
    private val repository: MonitorRepository,
    private val state: MutableStateFlow<MonitorState>,
) {
    suspend fun load(snapshot: MonitorState, older: Boolean) {
        when {
            snapshot.page == "operations" -> Unit

            snapshot.page == "logs" -> loadLogs(snapshot, older)

            snapshot.page == "competitions" -> {
                val competitions = repository.competitions()
                state.update { it.copy(competitions = competitions) }
            }

            snapshot.catalogMode.isNotBlank() -> loadCatalog(snapshot, older)

            else -> loadCycles(snapshot, older)
        }
    }

    private suspend fun loadCatalog(snapshot: MonitorState, older: Boolean) {
        val cursor = if (older) snapshot.matches.cursor else ""
        val page = repository.catalog(snapshot.filter, snapshot.catalogMode, cursor)
        state.update { it.copy(matches = mergeMatches(snapshot.matches, page, older)) }
        snapshot.match?.let { match ->
            val evidence = repository.evidence("match:${match.matchId}")
            state.update { it.copy(evidence = evidence) }
        }
    }

    private suspend fun loadCycles(snapshot: MonitorState, older: Boolean) {
        val cursor = if (older && snapshot.cycle == null) snapshot.cycles.cursor else ""
        val cached = if (snapshot.cycles.items.isEmpty()) repository.cachedCycles(snapshot.filter) else snapshot.cycles
        val page = repository.cycles(
            snapshot.filter,
            cursor,
            if (snapshot.page ==
                "dashboard"
            ) {
                MonitorPolicy.DASHBOARD_CYCLES
            } else {
                MonitorPolicy.PAGE_SIZE
            },
        )
        val merged = (page.items + cached.items).distinctBy { it.id }.sortedByDescending { it.time }.toImmutableList()
        val next = if (!older && cached.items.size > page.items.size) cached.cursor else page.cursor
        state.update { it.copy(cycles = page.copy(items = merged, cursor = next)) }
        val cycle = snapshot.cycle ?: return
        val matchCursor = if (older && snapshot.match == null) snapshot.matches.cursor else ""
        val matches = repository.matches(cycle.id, snapshot.filter, matchCursor)
        state.update {
            it.copy(matches = mergeMatches(snapshot.matches, matches, older && snapshot.match == null))
        }
        loadEvidence(snapshot, older)
    }

    private suspend fun loadEvidence(snapshot: MonitorState, older: Boolean) {
        val match = snapshot.match ?: return
        if (match.id.isBlank()) return
        val evidence = repository.evidence(match.id, if (older) snapshot.evidence.next else "")
        val events = if (older) (evidence.events + snapshot.evidence.events).distinctBy { it.id } else evidence.events
        state.update {
            it.copy(evidence = evidence.copy(events = events.toImmutableList()))
        }
    }

    private suspend fun loadLogs(snapshot: MonitorState, older: Boolean) {
        var after = if (older) {
            ""
        } else {
            snapshot.logs.items.maxOfOrNull {
                it.id.toLongOrNull() ?: 0
            }?.toString().orEmpty()
        }
        var collected = snapshot.logs.items
        var next = snapshot.logs.cursor
        repeat(MonitorPolicy.CATCH_UP_BATCHES) {
            val page = repository.logs(snapshot.filter, if (older) next else "", after, snapshot.cycle?.id.orEmpty())
            val merged = (collected + page.items).distinctBy { it.id }.sortedByDescending { it.id.toLongOrNull() }
            collected = merged.toImmutableList()
            if (older || after.isBlank()) next = page.cursor
            state.update { it.copy(logs = page.copy(items = collected, cursor = next)) }
            if (older || after.isBlank() || page.cursor.isBlank()) return
            after = page.items.lastOrNull()?.id.orEmpty()
        }
    }
}

private fun mergeMatches(previous: Page<MatchRun>, next: Page<MatchRun>, append: Boolean): Page<MatchRun> =
    if (!append) {
        next
    } else {
        next.copy(
            items = (previous.items + next.items).distinctBy {
                it.matchId
            }.toImmutableList(),
        )
    }
