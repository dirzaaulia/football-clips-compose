package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.monitor.domain.ActivitySample
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun ActivityCharts(state: MonitorState, action: (MonitorAction) -> Unit) {
    val samples = state.dashboard.series
    val errorColor = Color(0xFFEF4444)
    val hasAnyError = samples.any { it.errors > 0 }

    Panel(title = "Recorded Event Activity & Cycle Trend") {
        if (samples.isEmpty()) {
            Text(label("no_samples"))
        } else {
            val maxEvents = samples.maxOf { it.events }.coerceAtLeast(1)
            if (hasAnyError) ChartAlertNotice(errorColor)
            ChartLegend()
            ChartContainer(samples = samples, maxEvents = maxEvents, onNavigate = {
                action(MonitorAction.Navigate("pipelines"))
            })
            ChartTimeAxis(samples = samples, maxEvents = maxEvents)
        }
    }
}

@Composable
private fun ChartAlertNotice(errorColor: Color) {
    Row(
        modifier = Modifier.padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = "Cycle Errors",
            tint = errorColor,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "Attention required: Recent cycles contain errors or rejections (marked with 🔴 dots)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = errorColor,
        )
    }
}

@Composable
private fun ChartLegend() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            label = "Total Events recorded in cycle",
        )
        LegendItem(color = Color(0xFFEF4444), label = "Warnings / Errors (🔴)")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(modifier = Modifier.size(10.dp), shape = RoundedCornerShape(2.dp), color = color) {}
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChartContainer(samples: ImmutableList<ActivitySample>, maxEvents: Long, onNavigate: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(160.dp).clickable { onNavigate() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChartYAxisScale(maxEvents = maxEvents)
        Spacer(modifier = Modifier.width(8.dp))
        ChartCanvas(samples = samples, maxEvents = maxEvents, modifier = Modifier.weight(1f).fillMaxHeight())
    }
}

@Composable
private fun ChartYAxisScale(maxEvents: Long) {
    Column(
        modifier = Modifier.fillMaxHeight().padding(vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = maxEvents.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = (maxEvents / 2).toString(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "0",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChartCanvas(samples: ImmutableList<ActivitySample>, maxEvents: Long, modifier: Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val errorColor = Color(0xFFEF4444)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

        // Draw horizontal grid lines (100%, 50%, 0%)
        drawLine(gridColor, Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1f, pathEffect = dashEffect)
        drawLine(gridColor, Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 1f, pathEffect = dashEffect)
        drawLine(gridColor, Offset(0f, h), Offset(w, h), strokeWidth = 1f)

        val barWidth = w / samples.size
        samples.forEachIndexed { index, sample ->
            val xPos = index * barWidth
            val totalHeight = (h * sample.events / maxEvents).coerceAtLeast(4f)
            val errorHeight = if (sample.errors > 0) (h * sample.errors / maxEvents).coerceAtLeast(4f) else 0f

            drawRoundRect(
                color = primaryColor.copy(alpha = 0.7f),
                topLeft = Offset(xPos + 1.dp.toPx(), h - totalHeight),
                size = Size((barWidth - 2.dp.toPx()).coerceAtLeast(2f), totalHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            )

            if (errorHeight > 0f) {
                drawRoundRect(
                    color = errorColor,
                    topLeft = Offset(xPos + 1.dp.toPx(), h - errorHeight),
                    size = Size((barWidth - 2.dp.toPx()).coerceAtLeast(2f), errorHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                )
                drawCircle(
                    color = errorColor,
                    radius = 3.dp.toPx(),
                    center = Offset(xPos + (barWidth / 2f), (h - totalHeight - 6.dp.toPx()).coerceAtLeast(4.dp.toPx())),
                )
            }
        }
    }
}

@Composable
private fun ChartTimeAxis(samples: ImmutableList<ActivitySample>, maxEvents: Long) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = displayTime(samples.first().time),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Peak: $maxEvents events/cycle • Click to inspect cycles",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = displayTime(samples.last().time),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
