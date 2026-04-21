package com.yagubogu.ui.attendance.detail.model

import com.yagubogu.ui.attendance.detail.AttendanceDetailViewModel.Companion.DIARY_MAX_IMAGE_SIZE
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class AttendanceDetailDiaryUiState(
    val isLoading: Boolean = true,
    val mode: DiaryMode = DiaryMode.WRITE,
    val images: ImmutableList<String?> = List(DIARY_MAX_IMAGE_SIZE) { null }.toImmutableList(),
    val comment: String = "",
) {
    val isImageEmpty: Boolean get() = images.all { it == null }
    val emptyImageCount: Long get() = images.count { it == null }.toLong()
}
