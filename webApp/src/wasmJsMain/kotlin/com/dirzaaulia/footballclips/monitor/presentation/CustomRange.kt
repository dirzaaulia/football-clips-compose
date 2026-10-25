package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
internal fun CustomRange(state: MonitorState, action: (MonitorAction) -> Unit) {
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    Row(horizontalArrangement = Arrangement.spacedBy(Space.small)) {
        OutlinedTextField(start, { start = it }, label = { Text(label("since_wib")) }, modifier = Modifier.weight(1f))
        OutlinedTextField(end, { end = it }, label = { Text(label("until_wib")) }, modifier = Modifier.weight(1f))
        TextButton(onClick = {
            val since = wibInput(start)
            val until = wibInput(end)
            if (since.isNotBlank() && until.isNotBlank()) {
                action(MonitorAction.Filter(state.filter.copy(since = since, until = until)))
            }
        }) { Text(label("apply_range")) }
    }
}
