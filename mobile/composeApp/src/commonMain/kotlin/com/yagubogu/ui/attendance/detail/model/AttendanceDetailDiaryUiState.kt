package com.yagubogu.ui.attendance.detail.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class AttendanceDetailDiaryUiState(
    val isLoading: Boolean = true,
    val mode: DiaryMode = DiaryMode.WRITE,
    val images: ImmutableList<String> = persistentListOf(),
    val comment: String = "",
)
