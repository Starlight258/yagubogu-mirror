package com.yagubogu.ui.util

import androidx.annotation.StringRes
import com.yagubogu.R
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import kotlin.time.Clock

private val KST: TimeZone = TimeZone.of("Asia/Seoul")

fun LocalDate.Companion.now(
    clock: Clock = Clock.System,
    timeZone: TimeZone = KST,
): LocalDate = clock.todayIn(timeZone)

fun LocalTime.Companion.now(
    clock: Clock = Clock.System,
    timeZone: TimeZone = KST,
): LocalTime = clock.now().toLocalDateTime(timeZone).time

fun LocalDateTime.Companion.now(
    clock: Clock = Clock.System,
    timeZone: TimeZone = KST,
): LocalDateTime = clock.now().toLocalDateTime(timeZone)

fun YearMonth.Companion.now(
    clock: Clock = Clock.System,
    timeZone: TimeZone = KST,
): YearMonth = clock.todayIn(timeZone).yearMonth

fun LocalDate.minusDays(value: Int): LocalDate = minus(value, DateTimeUnit.DAY)

fun YearMonth.plusMonths(value: Int): YearMonth = plus(value, DateTimeUnit.MONTH)

fun YearMonth.minusMonths(value: Int): YearMonth = minus(value, DateTimeUnit.MONTH)

fun YearMonth.minusYears(value: Int): YearMonth = minus(value, DateTimeUnit.YEAR)

@StringRes
fun DayOfWeek.getDisplayNameResId(): Int =
    when (this) {
        DayOfWeek.MONDAY -> R.string.day_monday
        DayOfWeek.TUESDAY -> R.string.day_tuesday
        DayOfWeek.WEDNESDAY -> R.string.day_wednesday
        DayOfWeek.THURSDAY -> R.string.day_thursday
        DayOfWeek.FRIDAY -> R.string.day_friday
        DayOfWeek.SATURDAY -> R.string.day_saturday
        DayOfWeek.SUNDAY -> R.string.day_sunday
    }

fun LocalDateTime.formatToAmPm(
    amText: String,
    pmText: String,
): String {
    val marker: String = if (hour < 12) amText else pmText
    val hour12: Int = if (hour % 12 == 0) 12 else hour % 12
    val minuteStr: String = minute.toString().padStart(2, '0')

    return "$marker $hour12:$minuteStr"
}
