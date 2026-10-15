package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.AudienceStatePayload
import com.dirzaaulia.footballclips.telemetry.model.NotificationLogItem
import com.dirzaaulia.footballclips.telemetry.model.NotificationTelemetryPayload

@Composable
fun TelemetryNotificationsView(
    payload: NotificationTelemetryPayload,
    audienceState: AudienceStatePayload,
    onInspectNotification: (NotificationLogItem) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("ALL") }

    val filteredNotifications = remember(payload.notifications, searchQuery, selectedType) {
        payload.notifications.filter { item ->
            val matchesType = selectedType == "ALL" || item.eventType.equals(selectedType, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.body.contains(searchQuery, ignoreCase = true) ||
                item.homeTeam.contains(searchQuery, ignoreCase = true) ||
                item.awayTeam.contains(searchQuery, ignoreCase = true)
            matchesType && matchesQuery
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Automated Club Notifications & Audience Telemetry",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Real-time push fanout monitoring, distinct recipient audience, and multi-device distribution.",
                    fontSize = 10.sp,
                    color = StitchTextMuted
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchPrimary.copy(alpha = 0.15f))
                        .border(1.dp, StitchPrimary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .clickable { onRefresh() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, null, tint = StitchPrimary, modifier = Modifier.size(12.dp))
                        Text("Refresh", fontSize = 9.sp, color = StitchPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Top KPI Cards
        NotificationKpiHeader(payload = payload, audienceState = audienceState)

        // Search & Filter
        NotificationFilterBar(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            selectedType = selectedType,
            onTypeSelected = { selectedType = it }
        )

        // Notification Log Feed
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(620.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredNotifications, key = { it.id }) { item ->
                NotificationItemCard(item = item, onClick = { onInspectNotification(item) })
            }
            if (filteredNotifications.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Notifications, null, tint = StitchPrimary, modifier = Modifier.size(28.dp))
                            Text("No club push notifications match the selected filter.", color = StitchTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("Automated alerts for favorite clubs will log here as match cycles run.", color = StitchTextMuted, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}
