package com.dirzaaulia.footballclips.monitor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.monitor.domain.LoadDashboard
import com.dirzaaulia.footballclips.monitor.domain.MonitorRepository
import com.dirzaaulia.footballclips.monitor.domain.SearchTeams
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MonitorViewModel(
    private val repository: MonitorRepository,
    private val loadDashboard: LoadDashboard,
    private val searchTeams: SearchTeams,
) : ViewModel() {
    private val state = MutableStateFlow(MonitorState(filter = initialFilter(), page = initialPage()))
    val uiState = state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), state.value)
    private val pages = MonitorPageLoader(repository, state)
    private val tree = TreeController(repository, state, viewModelScope)
    private val history = WorkspaceHistory()
    private var request: Job? = null
    private var suggestions: Job? = null
    private var restored = false
    private var aggregateAt = 0.0

    fun onAction(action: MonitorAction) {
        when (action) {
            is MonitorAction.TeamSearch -> suggest(action.value)
            is MonitorAction.Expand -> tree.toggle(action.id)
            is MonitorAction.SelectCycle -> tree.toggle("cycle:" + action.cycle.id)
            is MonitorAction.SelectMatch -> tree.toggleMatch(action.match)
            is MonitorAction.MoreBranch -> tree.load(action.id, true)
            MonitorAction.ToggleLive -> state.update { it.copy(live = !it.live) }
            is MonitorAction.Correct -> correct(action)
            else -> change(action)
        }
    }

    private fun change(action: MonitorAction) {
        request?.cancel()
        if (!restored) {
            restored = true
            tree.restore()
        }
        history.save(state.value)
        state.update { history.restore(reduceMonitor(it, action)) }
        if (action is MonitorAction.Navigate || action is MonitorAction.Filter ||
            action is MonitorAction.SelectTeam
        ) {
            tree.restore()
        }
        if (action == MonitorAction.Older) state.update { it.copy(live = false) }
        if (action is MonitorAction.Filter || action is MonitorAction.SelectTeam) aggregateAt = 0.0
        if (action is MonitorAction.Navigate && action.recordHistory) pushNavigation(action.page)
        saveFilter(state.value.filter)

        val needsFetch = when (action) {
            is MonitorAction.Navigate -> {
                when (action.page) {
                    "dashboard" -> state.value.dashboard.metrics.isEmpty()
                    "pipelines" -> state.value.cycles.items.isEmpty() && state.value.catalogMode.isBlank()
                    "competitions" -> state.value.competitions.isEmpty()
                    else -> false
                }
            }

            else -> true
        }
        if (needsFetch) {
            refresh(action == MonitorAction.Older)
        }
    }

    private fun suggest(query: String) {
        suggestions?.cancel()
        suggestions = viewModelScope.launch {
            delay(MonitorPolicy.DEBOUNCE)
            attempt { state.update { it.copy(teams = searchTeams(query)) } }
        }
    }

    private fun refresh(older: Boolean) {
        request = viewModelScope.launch {
            state.update { it.copy(loading = true) }
            val snapshot = state.value
            attempt {
                refreshDashboard(snapshot)
                pages.load(snapshot, older)
                state.update { it.copy(error = "", updatedAt = browserTime()) }
            }
            state.update { it.copy(loading = false) }
        }
    }

    private suspend fun refreshDashboard(snapshot: MonitorState) {
        if (epochMillis(browserTime()) - aggregateAt < MonitorPolicy.AGGREGATE_POLL) return
        val dashboard = loadDashboard(snapshot.filter)
        state.update { it.copy(dashboard = dashboard) }
        aggregateAt = epochMillis(browserTime())
    }

    private fun correct(action: MonitorAction.Correct) {
        viewModelScope.launch {
            val matchId =
                action.matchId ?: if (action.undo) state.value.correction?.first else state.value.match?.matchId
            if (matchId == null) return@launch
            attempt {
                repository.correct(matchId, action.videoId, action.undo)
                state.update { it.copy(correction = if (action.undo) null else matchId to action.videoId) }
                onAction(MonitorAction.Refresh)
            }
        }
    }

    // Presentation failure boundary retains last good state; cancellation is always propagated.
    @Suppress("TooGenericExceptionCaught")
    private suspend fun attempt(block: suspend () -> Unit) {
        try {
            block()
        } catch (
            error: CancellationException,
        ) {
            throw error
        } catch (
            error: Exception,
        ) {
            state.update { it.copy(error = error.message.orEmpty(), loading = false) }
        }
    }
}
