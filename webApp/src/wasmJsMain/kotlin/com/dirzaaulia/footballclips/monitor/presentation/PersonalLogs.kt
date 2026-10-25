package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.monitor.domain.LogGroup
import com.dirzaaulia.footballclips.monitor.domain.LogLine
import com.dirzaaulia.footballclips.monitor.domain.groupLogs
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Composable
internal fun PersonalLogs(state: MonitorState, action: (MonitorAction) -> Unit) {
    PipelinePicker(state, action)
    Panel(label("events")) {
        Row {
            TextButton(onClick = { exportJson(exportLogs(state)) }) { Text(label("export")) }
            TextButton(onClick = { exportCsv(exportLogs(state)) }) { Text(label("export_csv")) }
        }
        if (state.logs.items.isEmpty()) {
            if (state.loading) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(Space.medium),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(label("loading"))
                }
            } else {
                Text(label("empty"))
            }
        }
        LazyColumn(Modifier.fillMaxWidth().height(Space.logViewport)) {
            items(groupLogs(state.logs.items), key = { it.id }) { group -> LogGroupRow(group, state, action) }
        }
        if (state.logs.cursor.isNotBlank()) {
            TextButton(onClick = {
                action(MonitorAction.Older)
            }) { Text(label("older")) }
        }
    }
}

@Composable
private fun LogGroupRow(group: LogGroup, state: MonitorState, action: (MonitorAction) -> Unit) {
    TextButton(onClick = { action(MonitorAction.Expand(group.id)) }, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Text(label("cycles") + " #" + group.sequence, style = MaterialTheme.typography.labelMedium)
            Text(group.match.ifBlank { label("unassociated") }, style = MaterialTheme.typography.titleSmall)
        }
        Badge(group.events.size.toString())
    }
    if (group.id in state.expanded) group.events.forEach { LogEntry(it) }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
}

@Composable
private fun LogEntry(event: LogLine) {
    Column(verticalArrangement = Arrangement.spacedBy(Space.tiny)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Space.small)) {
            Badge(event.level)
            Text(displayTime(event.time), style = MaterialTheme.typography.labelMedium)
            Text(event.code, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        if (event.match.isNotBlank()) Text(event.match, style = MaterialTheme.typography.titleSmall)
        Text(event.message, style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

private fun exportLogs(state: MonitorState): String = JsonObject(
    mapOf(
        "events" to JsonArray(
            state.logs.items.map { event ->
                JsonObject(
                    mapOf(
                        "created_at" to JsonPrimitive(event.time),
                        "level" to JsonPrimitive(event.level),
                        "code" to JsonPrimitive(event.code),
                        "message" to JsonPrimitive(event.message),
                        "match" to JsonPrimitive(event.match),
                        "cycle" to JsonPrimitive(event.cycle),
                    ),
                )
            },
        ),
    ),
).toString()
