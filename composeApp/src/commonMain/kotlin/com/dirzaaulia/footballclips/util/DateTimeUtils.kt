package com.dirzaaulia.footballclips.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

object DateTimeUtils {
    private val _safeTimeZone: TimeZone by lazy {
        try {
            val systemTz = TimeZone.currentSystemDefault()
            // Test it immediately to ensure it's functional within the library's internal database
            Clock.System.now().toLocalDateTime(systemTz)
            systemTz
        } catch (t: Throwable) {
            TimeZone.UTC
        }
    }

    val safeTimeZone: TimeZone
        get() = _safeTimeZone
}
