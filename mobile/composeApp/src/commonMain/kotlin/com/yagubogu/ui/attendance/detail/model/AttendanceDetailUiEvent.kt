package com.yagubogu.ui.attendance.detail.model

sealed interface AttendanceDetailUiEvent {
    data object UpdateMemoFailed : AttendanceDetailUiEvent

    data object UploadImageFailed : AttendanceDetailUiEvent
}
