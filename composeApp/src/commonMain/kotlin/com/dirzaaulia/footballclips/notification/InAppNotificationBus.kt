package com.dirzaaulia.footballclips.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class InAppGoalEvent(
    val matchId: String,
    val title: String,
    val body: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeId: String,
    val awayId: String,
    val score: String,
    val timestamp: Long = 0L,
)

object InAppNotificationBus {
    private val _goalEvent = MutableStateFlow<InAppGoalEvent?>(null)
    val goalEvent: StateFlow<InAppGoalEvent?> = _goalEvent.asStateFlow()

    fun postGoalEvent(event: InAppGoalEvent) {
        _goalEvent.value = event
    }

    fun dismiss() {
        _goalEvent.value = null
    }
}
