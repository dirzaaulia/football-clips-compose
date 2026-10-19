package com.dirzaaulia.footballclips.telemetry.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.telemetry.model.CycleHistoryItem
import com.dirzaaulia.footballclips.telemetry.model.LogStreamItem
import com.dirzaaulia.footballclips.telemetry.ui.cycles.TelemetryCycleStreamCard
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTerminalBg
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.TelemetryTypography

@Composable
fun TelemetryTerminalStream(
    cycles: List<CycleHistoryItem>,
    logs: List<LogStreamItem>,
    isLoading: Boolean = false,
    searchQuery: String = "",
    onClearSearch: () -> Unit = {},
    onInspectCycle: (CycleHistoryItem) -> Unit,
    onInspectLog: (LogStreamItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val logsByCycle = remember(logs) { logs.groupBy { it.cycleId } }
    val activeCycles = remember(cycles, logsByCycle, searchQuery) {
        if (searchQuery.isBlank()) cycles
        else cycles.filter { (logsByCycle[it.id]?.isNotEmpty() == true) }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StitchTerminalBg)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp)),
    ) {
        TerminalWindowTitleBar(
            isLoading = isLoading,
            activeCyclesCount = activeCycles.size,
            logsCount = logs.size,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(640.dp)
                .background(StitchTerminalBg)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(activeCycles, key = { it.id }) { cycle ->
                    val cycleLogs = (logsByCycle[cycle.id] ?: emptyList()).sortedBy { it.id }
                    TelemetryCycleStreamCard(
                        cycle = cycle,
                        logs = cycleLogs,
                        onInspectCycle = onInspectCycle,
                        onInspectLog = onInspectLog,
                    )
                }

                if (activeCycles.isEmpty()) {
                    item {
                        TerminalEmptyOrLoadingState(
                            isLoading = isLoading,
                            searchQuery = searchQuery,
                            onClearSearch = onClearSearch,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StitchSurface)
                .border(1.dp, StitchBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchSuccess))
                Text(
                    text = "Live Cycle Stream \u2022 Click 'Inspect Run' to view step-by-step accordions",
                    style = TelemetryTypography.labelSmall.copy(color = StitchTextSecondary),
                )
            }
            Text(
                text = "${cycles.size} Cycles Active",
                style = TelemetryTypography.labelSmall.copy(color = StitchPrimary),
            )
        }
    }
}
