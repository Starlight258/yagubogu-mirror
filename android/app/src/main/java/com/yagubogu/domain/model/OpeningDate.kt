package com.yagubogu.domain.model

import kotlinx.datetime.LocalDate

enum class OpeningDate(
    val year: Int,
    val date: LocalDate,
) {
    YEAR_2026(year = 2026, date = LocalDate(2026, 3, 28)),
    ;

    companion object {
        fun fromYear(year: Int): OpeningDate? = OpeningDate.entries.find { it.year == year }
    }
}
