package com.yagubogu.domain.model

import kotlinx.datetime.LocalDate

enum class OpeningDate(
    val date: LocalDate,
) {
    YEAR_2026(date = LocalDate(2026, 3, 28)),
    ;

    companion object {
        fun fromYear(year: Int): OpeningDate? = entries.find { it.date.year == year }
    }
}
