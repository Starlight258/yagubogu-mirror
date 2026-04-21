package com.yagubogu.ui.attendance.detail.model

import com.yagubogu.ui.attendance.detail.AttendanceDetailViewModel.Companion.DIARY_MAX_IMAGE_SIZE
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class AttendanceDetailDiaryUiState(
    val isLoading: Boolean = true,
    val mode: DiaryMode = DiaryMode.WRITE,
    val images: ImmutableList<DiaryImageItem> = List(DIARY_MAX_IMAGE_SIZE) { DiaryImageItem() }.toImmutableList(),
    val comment: String = "",
) {
    val isImageEmpty: Boolean = images.all { it.isEmpty }
    val emptyImageCount: Long = images.count { it.isEmpty }.toLong()

    val imageUris: ImmutableList<String?> = images.map { it.uri }.toImmutableList()
}
