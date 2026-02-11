package com.yagubogu.ui.home.model

import com.yagubogu.ui.util.now
import com.yagubogu.ui.util.toInstant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.until
import kotlin.time.Clock
import kotlin.time.Instant

enum class OpeningDate(
    val year: Int,
    val instant: Instant,
) {
    YEAR_2026(year = 2026, instant = LocalDate(2026, 3, 28).toInstant()),
    ;

    companion object {
        private fun fromYear(year: Int): OpeningDate? = entries.find { it.year == year }

        fun getLeftTimeUntilOpening(clock: Clock): Long {
            val currentYear: Int = LocalDate.now(clock).year
            val openingDate: OpeningDate = fromYear(currentYear) ?: return 0L
            val time = clock.now().until(other = openingDate.instant, unit = DateTimeUnit.SECOND)
            return if (time < 0L) 0L else time
        }
    }
}
