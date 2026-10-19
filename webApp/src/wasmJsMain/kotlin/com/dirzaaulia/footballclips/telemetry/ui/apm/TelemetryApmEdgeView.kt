package com.dirzaaulia.footballclips.telemetry.ui.apm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.telemetry.data.TelemetryRepository
import com.dirzaaulia.footballclips.telemetry.model.ApmLiveMetrics
import kotlinx.coroutines.launch

@Composable
fun TelemetryApmEdgeView(
    repository: TelemetryRepository,
    apmLiveMetrics: ApmLiveMetrics?,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var isBenchmarking by remember { mutableStateOf(false) }
    var edgeLatencyMs by remember { mutableStateOf<Long?>(null) }
    var directLatencyMs by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ApmHeaderSection()

        ApmPrometheusVectorSection(apmLiveMetrics = apmLiveMetrics)

        ApmEdgeBenchmarkSection(
            isBenchmarking = isBenchmarking,
            edgeLatencyMs = edgeLatencyMs,
            directLatencyMs = directLatencyMs,
            onRunBenchmark = {
                coroutineScope.launch {
                    isBenchmarking = true
                    val pair = repository.benchmarkLatencies()
                    edgeLatencyMs = pair.first
                    directLatencyMs = pair.second
                    isBenchmarking = false
                }
            },
        )

        ApmTtlArchitectureSection()

        ApmSlaMatrixSection()

        ApmFreeTierBudgetSection()
    }
}
