package com.dirzaaulia.footballclips.monitor.presentation

import com.dirzaaulia.footballclips.monitor.domain.MatchRun
import com.dirzaaulia.footballclips.monitor.domain.MonitorRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

internal class TreeController(
    private val repository: MonitorRepository,
    private val state: MutableStateFlow<MonitorState>,
    private val scope: CoroutineScope,
) {
    private val permits = Semaphore(2)
    private val matchIds = mutableMapOf<String, String>()

    fun toggleMatch(match: MatchRun) {
        val key = "match:" + match.id.ifBlank { "skip:" + match.matchId }
        matchIds[key] = match.matchId
        toggle(key)
    }

    fun toggle(id: String) {
        state.update {
            val open = it.expanded.toMutableSet()
            if (!open.remove(id)) open.add(id)
            it.copy(expanded = open.toImmutableSet())
        }
        sessionPut("expanded:" + contextKey(state.value), state.value.expanded.joinToString("\n"))
        if (id in state.value.expanded &&
            (id.startsWith("cycle:") || id.startsWith("match:") || id.startsWith("source:"))
        ) {
            load(id, false)
        }
    }

    fun restore() {
        val open = sessionGet("expanded:" + contextKey(state.value)).split("\n").filter {
            it.isNotBlank()
        }.toImmutableSet()
        state.update { it.copy(expanded = open) }
        open.filter { it.startsWith("cycle:") || it.startsWith("match:") }.forEach { load(it, false) }
    }

    fun load(id: String, older: Boolean) {
        if (id in state.value.pending) return
        val snapshot = state.value
        state.update { it.copy(pending = it.pending.add(id)) }
        scope.launch {
            try {
                permits.withPermit {
                    when {
                        id.startsWith("cycle:") -> loadMatches(id, snapshot, older)
                        id.startsWith("source:") -> loadSources(id, snapshot)
                        else -> loadEvidence(id, snapshot, older)
                    }
                    state.update { it.copy(branchErrors = it.branchErrors.remove(id)) }
                }
            } catch (
                error: CancellationException,
            ) {
                throw error
            } catch (
                error: Exception,
            ) {
                state.update { it.copy(branchErrors = it.branchErrors.put(id, error.message.orEmpty())) }
            } finally {
                state.update { it.copy(pending = it.pending.remove(id)) }
            }
        }
    }

    private suspend fun loadMatches(id: String, snapshot: MonitorState, older: Boolean) {
        val previous = snapshot.branches[id] ?: repository.cachedMatches(id.removePrefix("cycle:"), snapshot.filter)
        val page = repository.matches(id.removePrefix("cycle:"), snapshot.filter, if (older) previous.cursor else "")
        val items = (page.items + previous.items).distinctBy {
            it.matchId
        }.sortedByDescending { it.date }.toImmutableList()
        val merged = page.copy(
            items = items,
            cursor = if (!older &&
                previous.items.size > page.items.size
            ) {
                previous.cursor
            } else {
                page.cursor
            },
        )
        if (state.value.filter == snapshot.filter) state.update { it.copy(branches = it.branches.put(id, merged)) }
    }

    private suspend fun loadEvidence(id: String, snapshot: MonitorState, older: Boolean) {
        val rawId = id.removePrefix("match:").removePrefix("skip:")
        val targetMatchId = matchIds[id] ?: rawId
        if (targetMatchId.isNotBlank()) {
            val coverage = repository.coverage(targetMatchId)
            state.update { it.copy(coverages = it.coverages.put(targetMatchId, coverage)) }
        }
        if (id.startsWith("match:skip:")) return

        val previous = snapshot.details[id]
        val page = repository.evidence(rawId, if (older) previous?.next.orEmpty() else "")
        val matchId = page.matchId.ifBlank { targetMatchId }
        if (matchId.isNotBlank() && matchId != targetMatchId) {
            val coverage = repository.coverage(matchId)
            state.update { it.copy(coverages = it.coverages.put(matchId, coverage)) }
        }
        val events = (page.events + previous?.events.orEmpty()).distinctBy {
            it.id
        }.sortedBy { it.id.toLongOrNull() }.toImmutableList()
        if (state.value.filter == snapshot.filter) {
            state.update { it.copy(details = it.details.put(id, page.copy(events = events))) }
        }
    }
    private suspend fun loadSources(id: String, snapshot: MonitorState) {
        val code = id.removePrefix("source:")
        val result = repository.sources(code, snapshot.filter.season.ifBlank { "2026" })
        state.update { it.copy(sourceChannels = it.sourceChannels.put(code, result)) }
    }
}
