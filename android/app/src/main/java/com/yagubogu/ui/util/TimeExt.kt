package com.yagubogu.ui.util

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

fun DayOfWeek.getDisplayName(): String =
    when (this) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }
