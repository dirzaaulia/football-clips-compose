package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.monitor.domain.Metric

@Composable
internal fun MonitorDashboard(state: MonitorState, action: (MonitorAction) -> Unit) {
    if (state.loading && state.dashboard.metrics.isEmpty()) {
        DashboardLoadingState()
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(Space.medium)) {
        // Section 1: Attention & Operational Alerts
        DashboardSectionTitle("Critical Health & Coverage Alerts", Color(0xFFEF4444))
        AlertsMetricCards(state.dashboard.metrics, action)

        // Section 2: Current Season Catalog
        DashboardSectionTitle("Season Catalog & Fixture Tracking", MaterialTheme.colorScheme.primary)
        CatalogMetricCards(state.dashboard.metrics, action)

        // Section 3: YouTube Ingestion & Availability
        DashboardSectionTitle("YouTube Video Intake & Geo Availability", Color(0xFF0EA5E9))
        VideoMetricCards(state.dashboard.metrics, action)

        // Activity Trend Chart with Attention Icons
        ActivityCharts(state, action)

        // Regional and Backlog Panels
        CountryPanel(state, action)
        BacklogPanel(state)
        QuotaPanel(state)
    }
}

@Composable
private fun DashboardSectionTitle(title: String, accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = Space.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.size(width = 4.dp, height = 16.dp).background(accentColor, RoundedCornerShape(2.dp)))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AlertsMetricCards(metrics: List<Metric>, action: (MonitorAction) -> Unit) {
    val alertKeys = listOf("missing_percent", "missing", "stale_live", "country_gaps")
    val alertMetrics = metrics.filter { it.key in alertKeys }

    BoxWithConstraints {
        val columns = if (maxWidth >= Space.compact) 4 else 2
        Column(verticalArrangement = Arrangement.spacedBy(Space.small)) {
            alertMetrics.chunked(columns).forEach { rowMetrics ->
                Row(horizontalArrangement = Arrangement.spacedBy(Space.small)) {
                    rowMetrics.forEach { metric ->
                        StyledDashboardCard(
                            metric = metric,
                            action = action,
                            icon = if (metric.key == "missing_percent" ||
                                metric.key == "missing"
                            ) {
                                Icons.Rounded.Error
                            } else {
                                Icons.Rounded.Warning
                            },
                            accentColor = if (metric.key == "missing_percent") Color(0xFFEF4444) else Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogMetricCards(metrics: List<Metric>, action: (MonitorAction) -> Unit) {
    val catalogKeys = listOf("total_matches", "total_finished", "finished", "with_highlights")
    val catalogMetrics = metrics.filter { it.key in catalogKeys }

    BoxWithConstraints {
        val columns = if (maxWidth >= Space.compact) 4 else 2
        Column(verticalArrangement = Arrangement.spacedBy(Space.small)) {
            catalogMetrics.chunked(columns).forEach { rowMetrics ->
                Row(horizontalArrangement = Arrangement.spacedBy(Space.small)) {
                    rowMetrics.forEach { metric ->
                        StyledDashboardCard(
                            metric = metric,
                            action = action,
                            icon = Icons.Rounded.CheckCircle,
                            accentColor = Color(0xFF22C55E),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoMetricCards(metrics: List<Metric>, action: (MonitorAction) -> Unit) {
    val videoKeys = listOf("accepted", "watchable", "country_watchable", "blocked", "unknown")
    val videoMetrics = metrics.filter { it.key in videoKeys }

    BoxWithConstraints {
        val columns = if (maxWidth >= Space.compact) 3 else 2
        Column(verticalArrangement = Arrangement.spacedBy(Space.small)) {
            videoMetrics.chunked(columns).forEach { rowMetrics ->
                Row(horizontalArrangement = Arrangement.spacedBy(Space.small)) {
                    rowMetrics.forEach { metric ->
                        StyledDashboardCard(
                            metric = metric,
                            action = action,
                            icon = Icons.Rounded.PlayCircle,
                            accentColor = Color(0xFF0EA5E9),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StyledDashboardCard(
    metric: Metric,
    action: (MonitorAction) -> Unit,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier,
) {
    Card(
        onClick = { action(MonitorAction.Metric(metric.target)) },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.padding(Space.medium),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = label(if (metric.key == "unknown") "unknown_videos" else metric.key),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = if (metric.key == "missing_percent") "${metric.value}%" else metric.value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = accentColor,
            )
        }
    }
}

@Composable
private fun DashboardLoadingState() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = Space.medium),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Space.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space.small),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Loading telemetry metrics and live catalog...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Querying PostgREST and calculating real match coverage",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
