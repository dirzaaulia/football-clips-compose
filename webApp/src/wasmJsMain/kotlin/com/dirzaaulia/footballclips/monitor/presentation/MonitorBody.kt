package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun MonitorBody(state: MonitorState, action: (MonitorAction) -> Unit, modifier: Modifier) {
    when (state.page) {
        "pipelines" -> PipelineTree(state, action, modifier)
        "competitions" -> CompetitionDirectory(state, action, modifier)
        "clubs" -> ClubsDirectory(state, action, modifier)
        else -> DashboardScroll(state, action, modifier)
    }
}
