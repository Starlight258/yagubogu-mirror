package com.yagubogu.domain.model

import com.yagubogu.ui.util.toInstant
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

enum class OpeningDate(
    val year: Int,
    val instant: Instant,
) {
    YEAR_2026(year = 2026, instant = LocalDate(2026, 3, 28).toInstant()),
    ;

    companion object {
        fun fromYear(year: Int): OpeningDate? = OpeningDate.entries.find { it.year == year }
    }
}
