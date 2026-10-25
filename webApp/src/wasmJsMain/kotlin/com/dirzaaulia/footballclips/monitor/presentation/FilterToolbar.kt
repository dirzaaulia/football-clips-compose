package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
internal fun FilterToolbar(state: MonitorState, action: (MonitorAction) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Column {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Space.small),
        ) {
            TextButton(onClick = { open = !open }) {
                Icon(if (open) Icons.Rounded.ExpandLess else Icons.Rounded.Search, workspaceLabel("expand_filters"))
                Text(workspaceLabel(if (open) "collapse_filters" else "expand_filters"))
            }
            val active = listOf(
                state.filter.teamName,
                state.filter.opponentName,
                state.filter.competition,
                state.filter.season,
                state.filter.query,
            ).filter { it.isNotBlank() }
            active.forEach { Badge(it) }
            Text(
                if (state.filter.since.isNotBlank()) state.filter.since else state.filter.minutes.toString() + " min",
                style = MaterialTheme.typography.labelMedium,
            )
            if (active.isNotEmpty()) {
                TextButton(onClick = {
                    action(
                        MonitorAction.Filter(
                            state.filter.copy(
                                teamId = "",
                                teamName = "",
                                opponentId = "",
                                opponentName = "",
                                competition = "",
                                query = "",
                                season = "",
                            ),
                        ),
                    )
                }) { Text(label("clear")) }
            }
        }
        if (open) MonitorFilters(state, action)
    }
}
