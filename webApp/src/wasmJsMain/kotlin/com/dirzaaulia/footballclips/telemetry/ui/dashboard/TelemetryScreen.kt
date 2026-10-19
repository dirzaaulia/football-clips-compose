package com.dirzaaulia.footballclips.telemetry.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.telemetry.data.TelemetryRepository
import com.dirzaaulia.footballclips.telemetry.ui.cycles.TelemetryInspectorDrawer
import com.dirzaaulia.footballclips.telemetry.ui.navigation.TelemetryNavigationState
import com.dirzaaulia.footballclips.telemetry.ui.navigation.TelemetrySidebar
import com.dirzaaulia.footballclips.telemetry.ui.navigation.TelemetryTopNav
import com.dirzaaulia.footballclips.telemetry.ui.navigation.rememberTelemetryNavigationState
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBg
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary

@Composable
fun TelemetryScreen(
    repository: TelemetryRepository,
    modifier: Modifier = Modifier,
) {
    val viewModel = remember(repository) { TelemetryViewModel(repository) }
    val uiState by viewModel.uiState.collectAsState()
    val navState = rememberTelemetryNavigationState()

    TelemetryScreenContent(
        uiState = uiState,
        navState = navState,
        repository = repository,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
fun TelemetryScreenContent(
    uiState: TelemetryUiState,
    navState: TelemetryNavigationState,
    repository: TelemetryRepository,
    onAction: (TelemetryUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeCycleEvents = remember(uiState.selectedCycleItem, uiState.allRawEvents) {
        val cid = uiState.selectedCycleItem?.id
        if (cid != null) uiState.allRawEvents.filter { it.cycleId == cid } else emptyList()
    }

    Box(modifier = modifier.fillMaxSize().background(StitchBg)) {
        Row(modifier = Modifier.fillMaxSize()) {
            TelemetrySidebar(
                selectedSection = navState.currentDestination.section,
                onSectionSelected = { navState.navigateToSection(it) },
            )
            TelemetryMainContent(
                uiState = uiState,
                navState = navState,
                repository = repository,
                onAction = onAction,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }

        TelemetryInspectorDrawer(
            logItem = uiState.selectedInspectorItem,
            cycleItem = uiState.selectedCycleItem,
            notificationItem = uiState.selectedNotificationItem,
            cycleEvents = activeCycleEvents,
            onDismiss = { onAction(TelemetryUiAction.DismissInspector) },
        )
    }
}

@Composable
private fun TelemetryMainContent(
    uiState: TelemetryUiState,
    navState: TelemetryNavigationState,
    repository: TelemetryRepository,
    onAction: (TelemetryUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TelemetryTopNav(
            isLoading = uiState.isLoading,
            apmLiveMetrics = uiState.apmLiveMetrics,
            onRefresh = { onAction(TelemetryUiAction.RefreshData) },
            onTriggerCycle = { onAction(TelemetryUiAction.TriggerCycle) },
            onPurgeCache = { onAction(TelemetryUiAction.PurgeCache) },
            onRenewWebsub = { onAction(TelemetryUiAction.RenewWebSub) },
            actionMessage = uiState.actionMessage,
        )

        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = StitchPrimary,
                trackColor = StitchBorder,
            )
        }

        TelemetrySectionContent(
            section = navState.currentDestination.section,
            uiState = uiState,
            repository = repository,
            onAction = onAction,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
    }
}
