package com.dirzaaulia.footballclips.telemetry.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.AudienceStatePayload
import com.dirzaaulia.footballclips.telemetry.model.NotificationLogItem
import com.dirzaaulia.footballclips.telemetry.model.NotificationTelemetryPayload
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchCyan
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPurple
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchWarning

@Composable
fun NotificationKpiHeader(
    payload: NotificationTelemetryPayload,
    audienceState: AudienceStatePayload,
    modifier: Modifier = Modifier,
) {
    val totalNotifs = payload.notifications.size
    val globalUsers = audienceState.globalMetrics.distinctUsers.coerceAtLeast(
        payload.notifications.maxOfOrNull { it.audience.distinctUsers } ?: 0,
    )
    val globalDevices = audienceState.globalMetrics.totalDevices.coerceAtLeast(
        payload.notifications.maxOfOrNull { it.audience.totalDevices } ?: 0,
    )
    val ratio = if (globalUsers > 0) {
        ((globalDevices.toDouble() / globalUsers) * 100).toInt() / 100.0
    } else {
        0.0
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        KpiCard("TOTAL DISPATCHED", "$totalNotifs Alerts", "FCM Topics Fanout", StitchPrimary, Modifier.weight(1f))
        KpiCard("DISTINCT USERS", "$globalUsers Reached", "Unique human recipients", StitchSuccess, Modifier.weight(1f))
        KpiCard("DEVICE FANOUT", "$globalDevices Total", "${audienceState.globalMetrics.androidDevices} Android / ${audienceState.globalMetrics.webDevices} Web", StitchPurple, Modifier.weight(1.1f))
        KpiCard("FANOUT RATIO", "${ratio}x", "Devices per distinct user", StitchWarning, Modifier.weight(1f))
        KpiCard("DATABASE USAGE", "Self-Hosted PG16", "Zero quota / Free Oracle VM", StitchCyan, Modifier.weight(1.1f))
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = StitchTextMuted, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = accentColor)
        Text(subtitle, fontSize = 9.sp, color = StitchTextSecondary, maxLines = 1)
    }
}

@Composable
fun NotificationFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val types = listOf("ALL", "SCORE", "KICKOFF", "HIGHLIGHT", "FULLTIME")
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            types.forEach { type ->
                val isSelected = selectedType == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) StitchPrimary.copy(alpha = 0.15f) else StitchSurface)
                        .border(1.dp, if (isSelected) StitchPrimary else StitchBorder, RoundedCornerShape(6.dp))
                        .clickable { onTypeSelected(type) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = if (type == "ALL") "All Events" else type,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) StitchPrimary else StitchTextSecondary,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .width(260.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(StitchSurface)
                .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 2.dp),
        ) {
            TextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Filter by club or title...", fontSize = 11.sp, color = StitchTextMuted) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = StitchTextPrimary,
                    unfocusedTextColor = StitchTextPrimary,
                ),
                textStyle = TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
            )
        }
    }
}

@Composable
fun NotificationItemCard(
    item: NotificationLogItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val badgeColor = when (item.eventType.lowercase()) {
        "score" -> StitchSuccess
        "kickoff" -> StitchPrimary
        "highlight" -> StitchPurple
        "fulltime" -> StitchSecondary
        else -> StitchWarning
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(item.eventType.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = badgeColor, fontFamily = FontFamily.Monospace)
                }
                Text(item.timestamp.takeLast(13).take(8), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = StitchTextMuted)
                Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary)
            }
            Text(item.body, fontSize = 11.sp, color = StitchTextSecondary)

            if (item.eventType.equals("highlight", ignoreCase = true)) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchPurple.copy(alpha = 0.12f))
                        .border(1.dp, StitchPurple.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Default.PlayCircle, null, tint = StitchPurple, modifier = Modifier.size(13.dp))
                    Text(
                        "HIGHLIGHT READY: Notification sent to ${item.audience.distinctUsers} fans who saved this club as favorite",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchPurple,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                MetricPill(Icons.Default.People, "${item.audience.distinctUsers} Users", StitchPrimary)
                MetricPill(Icons.Default.Devices, "${item.audience.totalDevices} Devices (${item.audience.platformBreakdown.android} A / ${item.audience.platformBreakdown.web} W)", StitchPurple)
                MetricPill(Icons.Default.SyncAlt, "${item.audience.deviceToUserRatio}x Ratio", StitchWarning)
                if (item.audience.distribution.multiDeviceUsers > 0) {
                    MetricPill(Icons.Default.DeviceHub, "${item.audience.distribution.multiDeviceUsers} Multi-Dev Users", StitchSuccess)
                }
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = StitchTextMuted, modifier = Modifier.size(20.dp))
    }
}
