package com.yagubogu.ui.attendance.model

data class AttendanceFilterState(
    val year: Int,
    val isWinOnly: Boolean = false,
    val isYearly: Boolean = false,
)
