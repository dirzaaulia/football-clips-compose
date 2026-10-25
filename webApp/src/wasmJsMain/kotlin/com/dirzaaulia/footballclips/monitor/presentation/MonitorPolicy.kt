package com.dirzaaulia.footballclips.monitor.presentation

internal object MonitorPolicy {
    const val DAY_MINUTES = 1440
    const val WEEK_MINUTES = 10080
    const val SEASON_DIGITS = 4
    const val CODE_LIMIT = 30
    const val DEBOUNCE_LONG = 350L
    const val SESSION_ATTEMPTS = 40
    const val SESSION_POLL = 500L
    const val DASHBOARD_CYCLES = 8
    const val PAGE_SIZE = 50
    const val MIN_MINUTES = 15
    const val HOUR_MINUTES = 60
    const val MAX_MINUTES = 129600
    const val LIVE_POLL = 5_000L
    const val AGGREGATE_POLL = 30_000L
    const val DEBOUNCE = 300L
    const val MAX_ROWS = 500
    const val CATCH_UP_BATCHES = 10
    const val SUGGESTIONS = 6
    const val DESKTOP_COLUMNS = 4
    const val COMPACT_COLUMNS = 2
    const val THUMBNAIL_ASPECT = 16f / 9f
    const val CHART_OPACITY = 0.65f
}
