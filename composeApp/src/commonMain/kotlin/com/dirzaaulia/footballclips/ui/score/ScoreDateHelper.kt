package com.dirzaaulia.footballclips.ui.score

import com.dirzaaulia.footballclips.util.DateTimeUtils
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

data class DateOption(
    val displayDay: String,
    val displayDate: String,
    val date: String,
    val isToday: Boolean
)

internal fun generateDateOptions(): List<DateOption> {
    val now = Clock.System.now()
    val timeZone = DateTimeUtils.safeTimeZone

    return (-30..30).map { daysOffset ->
        val date = try {
            (now + daysOffset.days).toLocalDateTime(timeZone)
        } catch (_: Throwable) {
            try {
                (now + daysOffset.days).toLocalDateTime(TimeZone.UTC)
            } catch (_: Throwable) {
                Clock.System.now().toLocalDateTime(TimeZone.UTC)
            }
        }
        
        val dayName = when (daysOffset) {
            -1 -> "Yest"
            0 -> "Today"
            1 -> "Tmrw"
            else -> date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        }

        DateOption(
            displayDay = dayName,
            displayDate = "${date.day} ${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }}",
            date = "${date.year}-${(date.month.ordinal + 1).toString().padStart(2, '0')}-${date.day.toString().padStart(2, '0')}",
            isToday = daysOffset == 0
        )
    }
}
