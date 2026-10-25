package com.dirzaaulia.footballclips.monitor.domain

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class LogGroup(val id: String, val sequence: String, val match: String, val events: ImmutableList<LogLine>)

fun groupLogs(events: ImmutableList<LogLine>): ImmutableList<LogGroup> =
    events.groupBy { it.cycleId + ":" + it.match }.map { (identity, rows) ->
        LogGroup(
            identity,
            rows.first().cycle,
            rows.first().match,
            rows.sortedBy {
                it.id.toLongOrNull()
            }.toImmutableList(),
        )
    }.toImmutableList()
