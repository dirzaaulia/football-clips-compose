package com.dirzaaulia.footballclips.telemetry.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.telemetry.data.TelemetryRepository
import com.dirzaaulia.footballclips.telemetry.model.TelemetryNavSection
import com.dirzaaulia.footballclips.telemetry.ui.apm.TelemetryMetricsView
import com.dirzaaulia.footballclips.telemetry.ui.catalog.TelemetryCatalogView
import com.dirzaaulia.footballclips.telemetry.ui.components.TelemetryConfigView
import com.dirzaaulia.footballclips.telemetry.ui.components.TelemetryFilterBar
import com.dirzaaulia.footballclips.telemetry.ui.components.TelemetryTerminalStream
import com.dirzaaulia.footballclips.telemetry.ui.crashlytics.TelemetryCrashlyticsView
import com.dirzaaulia.footballclips.telemetry.ui.navigation.TelemetryFooterBar
import com.dirzaaulia.footballclips.telemetry.ui.notifications.TelemetryNotificationsView
import com.dirzaaulia.footballclips.telemetry.ui.pipelines.TeleStagesMonitor
import com.dirzaaulia.footballclips.telemetry.ui.pipelines.TelemetryPipelineScopeBar
import com.dirzaaulia.footballclips.telemetry.ui.standings.TelemetryStandingsConsistencyView
import com.dirzaaulia.footballclips.telemetry.ui.users.TelemetryUserVersionsView
import com.dirzaaulia.footballclips.telemetry.ui.verifier.TelemetryHighlightVerifierView

@Composable
fun TelemetrySectionContent(
    section: TelemetryNavSection,
    uiState: TelemetryUiState,
    repository: TelemetryRepository,
    onAction: (TelemetryUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        when (section) {
            TelemetryNavSection.CONSOLE_STREAM,
            TelemetryNavSection.PIPELINE_CYCLES,
            TelemetryNavSection.STREAM -> {
                item {
                    TelemetryMetricsView(
                        metrics = uiState.metrics,
                        apmLiveMetrics = uiState.apmLiveMetrics,
                    )
                }
                item {
                    TeleStagesMonitor(
                        metrics = uiState.metrics,
                        cycleTimestamp = uiState.latestCycleTimestamp,
                    )
                }
                item {
                    TelemetryPipelineScopeBar(
                        selectedPipelineScope = uiState.selectedPipelineScope,
                        onScopeSelected = { onAction(TelemetryUiAction.SelectPipelineScope(it)) },
                    )
                }
                item {
                    TelemetryFilterBar(
                        searchQuery = uiState.searchQuery,
                        onSearchChange = { onAction(TelemetryUiAction.SetSearchQuery(it)) },
                        selectedLevel = uiState.selectedLevel,
                        onLevelSelected = { onAction(TelemetryUiAction.SelectFilterLevel(it)) },
                        selectedDate = uiState.selectedDate,
                        onDateSelected = { onAction(TelemetryUiAction.SelectDate(it)) },
                        errorCount = uiState.metrics.totalErrors,
                    )
                }
                item {
                    TelemetryTerminalStream(
                        cycles = uiState.cycleHistory,
                        logs = uiState.filteredStream,
                        isLoading = uiState.isLoading,
                        searchQuery = uiState.searchQuery,
                        onClearSearch = { onAction(TelemetryUiAction.SetSearchQuery("")) },
                        onInspectCycle = { onAction(TelemetryUiAction.InspectCycle(it)) },
                        onInspectLog = { onAction(TelemetryUiAction.InspectLog(it)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            TelemetryNavSection.HIGHLIGHT_VERIFIER -> {
                item {
                    TelemetryHighlightVerifierView(
                        repository = repository,
                        onOpenVideo = { url -> TelemetryUtils.openUrlInNewTab(url) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            TelemetryNavSection.FIXTURE_SYNC -> {
                item {
                    TelemetryCatalogView(
                        repository = repository,
                        onOpenVideo = { url -> TelemetryUtils.openUrlInNewTab(url) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            TelemetryNavSection.STANDINGS_AUDIT -> {
                item {
                    TelemetryStandingsConsistencyView(
                        repository = repository,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            TelemetryNavSection.NOTIFICATIONS -> {
                item {
                    TelemetryNotificationsView(
                        payload = uiState.notificationsPayload,
                        audienceState = uiState.audienceState,
                        onInspectNotification = { onAction(TelemetryUiAction.InspectNotification(it)) },
                        onRefresh = { onAction(TelemetryUiAction.RefreshData) },
                    )
                }
            }
            TelemetryNavSection.USER_VERSIONS -> {
                item {
                    TelemetryUserVersionsView(
                        repository = repository,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            TelemetryNavSection.CRASHLYTICS -> {
                item {
                    TelemetryCrashlyticsView(
                        repository = repository,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            TelemetryNavSection.CONFIG -> {
                item {
                    TelemetryConfigView()
                }
            }
        }

        item {
            TelemetryFooterBar()
        }
    }
}
