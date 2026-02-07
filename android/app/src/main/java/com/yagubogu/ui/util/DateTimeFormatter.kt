package com.yagubogu.ui.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char

// yyyy.MM.dd 포맷터
val yyyyMMddFormatter: DateTimeFormat<LocalDate> =
    LocalDate
        .Format {
            year()
            char('.')
            monthNumber()
            char('.')
            day()
        }

// hh:mm 포맷터
val hhmmFormatter: DateTimeFormat<LocalTime> =
    LocalTime
        .Format {
            hour()
            char(':')
            minute()
        }

// "오전/오후 hh:mm" 포맷터
val amPmhhmmFormatter: DateTimeFormat<LocalDateTime> =
    LocalDateTime.Format {
        amPmMarker("오전", "오후")
        char(' ')
        amPmHour()
        char(':')
        minute()
    }
