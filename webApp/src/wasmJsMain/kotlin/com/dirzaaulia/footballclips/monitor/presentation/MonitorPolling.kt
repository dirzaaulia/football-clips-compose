package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
internal fun MonitorPolling(state: MonitorState, action: (MonitorAction) -> Unit) {
    LaunchedEffect(Unit) {
        action(MonitorAction.Refresh)
    }
    LaunchedEffect(state.live, state.match?.id, state.page) {
        while (true) {
            delay(
                if (state.match != null ||
                    state.page == "logs"
                ) {
                    MonitorPolicy.LIVE_POLL
                } else {
                    MonitorPolicy.AGGREGATE_POLL
                },
            )
            if (canPoll(state)) {
                action(MonitorAction.Refresh)
            }
        }
    }
}

private fun canPoll(state: MonitorState): Boolean {
    val sessionReady = tabVisible()
    return state.live && !state.loading && sessionReady
}
