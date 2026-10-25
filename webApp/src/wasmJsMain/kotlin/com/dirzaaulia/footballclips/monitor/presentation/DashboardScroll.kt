package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
internal fun DashboardScroll(state: MonitorState, action: (MonitorAction) -> Unit, modifier: Modifier) {
    val context = contextKey(state)
    val scroll = rememberScrollState()
    LaunchedEffect(context) { scroll.scrollTo(sessionGet("dashboard:" + context).toIntOrNull() ?: 0) }
    LaunchedEffect(context) {
        snapshotFlow { scroll.value }.distinctUntilChanged().collect {
            sessionPut("dashboard:" + context, it.toString())
        }
    }
    Column(modifier.verticalScroll(scroll)) { MonitorDashboard(state, action) }
}
