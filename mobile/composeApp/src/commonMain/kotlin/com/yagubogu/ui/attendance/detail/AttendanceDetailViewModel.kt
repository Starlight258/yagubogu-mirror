package com.yagubogu.ui.attendance.detail

import androidx.lifecycle.ViewModel
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.attendance.detail.model.AttendanceDetailDiaryUiState
import com.yagubogu.ui.attendance.detail.model.DiaryMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AttendanceDetailViewModel(
    private val gameId: Long,
) : ViewModel() {
    private val _attendanceDetailDiaryUiState = MutableStateFlow(AttendanceDetailDiaryUiState())
    val attendanceDetailDiaryUiState: StateFlow<AttendanceDetailDiaryUiState> =
        _attendanceDetailDiaryUiState.asStateFlow()

    fun addImages(uris: List<String>) {
        _attendanceDetailDiaryUiState.update {
            val combined = (it.images.filterNotNull() + uris).take(DIARY_MAX_IMAGE_SIZE)
            val updatedImages: ImmutableList<String?> =
                (combined + List(DIARY_MAX_IMAGE_SIZE - combined.size) { null }).toImmutableList()
            it.copy(images = updatedImages)
        }
    }

    fun deleteImage(index: Int) {
        _attendanceDetailDiaryUiState.update {
            val updatedImages: ImmutableList<String?> =
                (it.images.filterIndexed { idx, _ -> idx != index } + listOf(null)).toImmutableList()
            it.copy(images = updatedImages)
        }
    }

    fun editDiary() {
        _attendanceDetailDiaryUiState.update { it.copy(mode = DiaryMode.WRITE) }
    }

    fun saveDiary(comment: String) {
        AnalyticsLogger.logEvent("diary_save_button")
        _attendanceDetailDiaryUiState.update {
            it.copy(comment = comment, mode = DiaryMode.READ)
        }
    }

    companion object {
        const val DIARY_MAX_IMAGE_SIZE = 3
    }
}
