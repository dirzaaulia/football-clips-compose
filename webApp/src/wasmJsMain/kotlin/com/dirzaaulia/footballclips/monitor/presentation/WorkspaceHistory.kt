package com.dirzaaulia.footballclips.monitor.presentation

internal class WorkspaceHistory {
    private val views = linkedMapOf<String, MonitorState>()
    fun save(state: MonitorState) {
        views[contextKey(state)] = state
        if (views.size > 8) views.remove(views.keys.first())
    }
    fun restore(next: MonitorState): MonitorState = views[contextKey(next)]?.copy(
        live = next.live,
        loading = false,
        pending = kotlinx.collections.immutable.persistentSetOf(),
    ) ?: next
}
