package com.dirzaaulia.footballclips.telemetry.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.telemetry.data.TelemetryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TelemetryViewModel(
    private val repository: TelemetryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TelemetryUiState())
    val uiState: StateFlow<TelemetryUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<TelemetryUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        loadData()
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                loadData()
            }
        }
    }

    fun onAction(action: TelemetryUiAction) {
        when (action) {
            is TelemetryUiAction.RefreshData -> loadData()
            is TelemetryUiAction.TriggerCycle -> triggerCycle()
            is TelemetryUiAction.PurgeCache -> purgeCache()
            is TelemetryUiAction.RenewWebSub -> renewWebSub()
            is TelemetryUiAction.SetSearchQuery -> _uiState.update { it.copy(searchQuery = action.query) }
            is TelemetryUiAction.SelectFilterLevel -> _uiState.update { it.copy(selectedLevel = action.level) }
            is TelemetryUiAction.SelectDate -> _uiState.update { it.copy(selectedDate = action.date) }
            is TelemetryUiAction.SelectPipelineScope -> _uiState.update { it.copy(selectedPipelineScope = action.scope) }
            is TelemetryUiAction.InspectLog -> _uiState.update { it.copy(selectedInspectorItem = action.item) }
            is TelemetryUiAction.InspectCycle -> _uiState.update { it.copy(selectedCycleItem = action.item) }
            is TelemetryUiAction.InspectNotification -> _uiState.update { it.copy(selectedNotificationItem = action.item) }
            is TelemetryUiAction.DismissInspector -> _uiState.update {
                it.copy(
                    selectedInspectorItem = null,
                    selectedCycleItem = null,
                    selectedNotificationItem = null,
                )
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.getLiveApmMetrics().onSuccess { metrics ->
                _uiState.update { it.copy(apmLiveMetrics = metrics) }
            }

            repository.getSyncedMatches(100).onSuccess { matches ->
                _uiState.update { it.copy(syncedMatches = matches) }
            }

            val cyclesResult = repository.getRecentCycles(500)
            cyclesResult.onSuccess { cycles ->
                val cycleIds = cycles.map { it.id }
                val allEvents = repository.getEventsForCycles(cycleIds).getOrDefault(emptyList())
                val eventsByCycle = allEvents.groupBy { it.cycleId }
                val parsedLogs = TelemetryParser.parseLogStream(cycles, allEvents)
                val highlights = TelemetryDataMappers.extractDiscoveredHighlights(allEvents, parsedLogs)
                val cycleHistory = TelemetryDataMappers.extractCycleHistory(cycles, eventsByCycle)

                _uiState.update { current ->
                    val initialDate = if (current.isInitialLoad && cycles.isNotEmpty()) {
                        cycles.firstOrNull()?.timestamp?.substringBefore(",")?.trim()
                    } else {
                        current.selectedDate
                    }
                    current.copy(
                        rawCycles = cycles,
                        allRawEvents = allEvents,
                        logStream = parsedLogs,
                        discoveredHighlights = highlights,
                        cycleHistory = cycleHistory,
                        selectedDate = initialDate,
                        isInitialLoad = false,
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to load telemetry from GCS.") }
            }

            repository.getNotificationsTelemetry().onSuccess { notif ->
                _uiState.update { it.copy(notificationsPayload = notif) }
            }
            repository.getAudienceState().onSuccess { audience ->
                _uiState.update { it.copy(audienceState = audience) }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun triggerCycle() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionMessage = "Dispatching cycle trigger to Cloud Run...") }
            repository.triggerRunCycle().onSuccess {
                _uiState.update { it.copy(actionMessage = "Cycle successfully queued on Cloud Run fleet!") }
                loadData()
            }.onFailure { err ->
                _uiState.update { it.copy(actionMessage = "Trigger failed: ${err.message}") }
            }
        }
    }

    private fun purgeCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionMessage = "Invalidating Cloudflare Edge Cache...") }
            repository.purgeEdgeCache().onSuccess {
                _uiState.update { it.copy(actionMessage = "Cloudflare Edge Cache purged successfully!") }
            }.onFailure { err ->
                _uiState.update { it.copy(actionMessage = "Cache purge failed: ${err.message}") }
            }
        }
    }

    private fun renewWebSub() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionMessage = "📡 Renewing YouTube WebSub subscriptions for 96 club channels...") }
            repository.renewWebSubSubscriptions().onSuccess {
                _uiState.update { it.copy(actionMessage = "✅ All 96 official channel WebSub subscriptions renewed!") }
            }.onFailure { err ->
                _uiState.update { it.copy(actionMessage = "❌ WebSub renewal failed: ${err.message}") }
            }
        }
    }
}
