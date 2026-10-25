package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
internal fun MonitorFilters(state: MonitorState, action: (MonitorAction) -> Unit) {
    var advanced by remember { mutableStateOf(false) }
    var message by remember(state.filter.query) { mutableStateOf(state.filter.query) }
    Panel(label("filters")) {
        SearchFields(state, action, message) { message = it }
        Row(horizontalArrangement = Arrangement.spacedBy(Space.small)) {
            TextButton(onClick = { advanced = true }) { Text(label("details")) }
            TimePresets(state, action)
        }
    }
    if (advanced) AdvancedFilters(state, action) { advanced = false }
    LaunchedEffect(message) {
        delay(MonitorPolicy.DEBOUNCE_LONG)
        if (message != state.filter.query) action(MonitorAction.Filter(state.filter.copy(query = message)))
    }
}

@Composable
private fun SearchFields(
    state: MonitorState,
    action: (MonitorAction) -> Unit,
    message: String,
    onMessage: (String) -> Unit,
) {
    BoxWithConstraints {
        FlowRow(
            maxItemsInEachRow = if (maxWidth < 600.dp) 1 else 2,
            horizontalArrangement = Arrangement.spacedBy(Space.small),
            verticalArrangement = Arrangement.spacedBy(Space.small),
        ) {
            TeamSearch(state, action, Modifier.weight(1f))
            OutlinedTextField(
                message,
                onMessage,
                label = { Text(label("message")) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AdvancedFilters(state: MonitorState, action: (MonitorAction) -> Unit, dismiss: () -> Unit) {
    ModalSideSheet(
        visible = true,
        title = label("details"),
        onDismissRequest = dismiss,
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(Space.medium),
            verticalArrangement = Arrangement.spacedBy(Space.medium),
        ) {
            TeamSearch(state, action, Modifier.fillMaxWidth(), opponent = true)
            FilterOptions(state, action)
            CustomRange(state, action)
            TextButton(onClick = dismiss) { Text(label("back")) }
        }
    }
}

@Composable
private fun TeamSearch(
    state: MonitorState,
    action: (MonitorAction) -> Unit,
    modifier: Modifier,
    opponent: Boolean = false,
) {
    val selected = if (opponent) state.filter.opponentName else state.filter.teamName
    var value by remember(selected) { mutableStateOf(selected) }
    var suggesting by remember { mutableStateOf(false) }
    Column(modifier) {
        OutlinedTextField(
            value,
            {
                value = it
                suggesting = true
                action(MonitorAction.TeamSearch(it))
            },
            label = { Text(label(if (opponent) "opponent" else "team")) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                TextButton(onClick = {
                    value = ""
                    suggesting = false
                    clearTeam(state, action, opponent)
                }) { Text(label("clear")) }
            },
        )
        if (suggesting && value.isNotBlank()) {
            state.teams.take(MonitorPolicy.SUGGESTIONS).forEach { team ->
                TextButton(onClick = {
                    suggesting = false
                    value = team.name
                    action(MonitorAction.SelectTeam(team, opponent))
                }) {
                    Text(team.name + " · " + team.context)
                }
            }
        }
    }
}

private fun clearTeam(state: MonitorState, action: (MonitorAction) -> Unit, opponent: Boolean) {
    val filter = if (opponent) {
        state.filter.copy(opponentId = "", opponentName = "")
    } else {
        state.filter.copy(teamId = "", teamName = "")
    }
    action(MonitorAction.Filter(filter))
}

@Composable
private fun FilterOptions(state: MonitorState, action: (MonitorAction) -> Unit) {
    var country by remember(state.filter.country) { mutableStateOf(state.filter.country) }
    var competition by remember(state.filter.competition) { mutableStateOf(state.filter.competition) }
    var season by remember(state.filter.season) { mutableStateOf(state.filter.season) }
    Row(horizontalArrangement = Arrangement.spacedBy(Space.small)) {
        OutlinedTextField(country, {
            country = it.uppercase().take(2)
        }, label = { Text(label("country")) }, modifier = Modifier.weight(1f), singleLine = true)
        OutlinedTextField(competition, {
            competition = it.uppercase().take(MonitorPolicy.CODE_LIMIT)
        }, label = { Text(label("competition")) }, modifier = Modifier.weight(1f), singleLine = true)
        OutlinedTextField(season, {
            season = it.filter(Char::isDigit).take(MonitorPolicy.SEASON_DIGITS)
        }, label = { Text(label("season")) }, modifier = Modifier.weight(1f), singleLine = true)
        TextButton(onClick = {
            if (country.length ==
                2
            ) {
                action(
                    MonitorAction.Filter(
                        state.filter.copy(country = country, competition = competition, season = season),
                    ),
                )
            }
        }) { Text(label("refresh")) }
    }
}

@Composable
private fun TimePresets(state: MonitorState, action: (MonitorAction) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Space.small),
    ) {
        listOf(
            MonitorPolicy.HOUR_MINUTES to "last_hour",
            MonitorPolicy.DAY_MINUTES to "last_day",
            MonitorPolicy.WEEK_MINUTES to "last_week",
            MonitorPolicy.MAX_MINUTES to "all_history",
        ).forEach { (minutes, key) ->
            FilterChip(state.filter.minutes == minutes, {
                action(MonitorAction.Filter(state.filter.copy(minutes = minutes, since = "", until = "")))
            }, label = { Text(label(key)) })
        }
    }
}
