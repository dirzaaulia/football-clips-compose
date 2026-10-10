package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.LogStreamItem

@Composable
fun TelemetryAlertsView(
    alerts: List<LogStreamItem>,
    onInspectAlert: (LogStreamItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Failure Alerts & Exception Triage",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Isolated log feed of warnings, rate-limits, and errors. Click any alert to inspect stack trace.",
                    fontSize = 10.sp,
                    color = StitchTextMuted
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (alerts.any { it.isError }) StitchError.copy(alpha = 0.15f) else StitchSuccess.copy(alpha = 0.15f))
                    .border(1.dp, if (alerts.any { it.isError }) StitchError.copy(alpha = 0.4f) else StitchSuccess.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (alerts.isEmpty()) "Zero Active Alerts" else "${alerts.size} Active Alerts",
                    fontSize = 9.sp,
                    color = if (alerts.any { it.isError }) StitchError else StitchSuccess,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Alerts List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(620.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(alerts, key = { it.id }) { alert ->
                AlertItemCard(alert = alert, onClick = { onInspectAlert(alert) })
            }
            if (alerts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Warning, null, tint = StitchSuccess, modifier = Modifier.size(28.dp))
                            Text("No failure alerts or exceptions recorded in active cycles.", color = StitchTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("All worker subsystems operating within nominal parameters.", color = StitchTextMuted, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertItemCard(
    alert: LogStreamItem,
    onClick: () -> Unit
) {
    val isErr = alert.isError || alert.level == "ERROR"
    val accentColor = if (isErr) StitchError else StitchWarning

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(accentColor))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(alert.level, fontSize = 8.sp, color = accentColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Text(alert.timestamp, fontSize = 9.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
            Text(
                text = alert.cleanMessage,
                fontSize = 10.sp,
                color = StitchTextPrimary,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = StitchTextMuted, modifier = Modifier.size(16.dp))
    }
}
