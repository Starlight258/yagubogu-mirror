package com.yagubogu.ui.attendance.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.data.dto.response.checkin.CheckInImageDto
import com.yagubogu.data.repository.checkin.CheckInRepository
import com.yagubogu.data.repository.thirdparty.ThirdPartyRepository
import com.yagubogu.ui.attendance.detail.model.AttendanceDetailDiaryUiState
import com.yagubogu.ui.attendance.detail.model.DiaryImageItem
import com.yagubogu.ui.attendance.detail.model.DiaryMode
import com.yagubogu.ui.common.component.image.ImageCompressionSpec
import com.yagubogu.ui.common.component.image.compressImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AttendanceDetailViewModel(
    private val gameId: Long,
    private val checkInRepository: CheckInRepository,
    private val thirdPartyRepository: ThirdPartyRepository,
) : ViewModel() {
    private val logger = Logger.withTag("AttendanceDetailViewModel")

    private val _attendanceDetailDiaryUiState = MutableStateFlow(AttendanceDetailDiaryUiState())
    val attendanceDetailDiaryUiState: StateFlow<AttendanceDetailDiaryUiState> =
        _attendanceDetailDiaryUiState.asStateFlow()

    fun addImages(uris: List<String>) {
        _attendanceDetailDiaryUiState.update {
            val combined: List<DiaryImageItem> =
                (it.images.filter { item -> !item.isEmpty } + uris.map { uri -> DiaryImageItem(uri = uri) })
                    .take(DIARY_MAX_IMAGE_SIZE)
            val updatedImages: ImmutableList<DiaryImageItem> =
                (combined + List(DIARY_MAX_IMAGE_SIZE - combined.size) { DiaryImageItem() }).toImmutableList()
            it.copy(images = updatedImages)
        }
    }

    fun deleteImage(index: Int) {
        val target = attendanceDetailDiaryUiState.value.images[index]
        _attendanceDetailDiaryUiState.update {
            val updatedImages: ImmutableList<DiaryImageItem> =
                (it.images.filterIndexed { idx, _ -> idx != index } + listOf(DiaryImageItem())).toImmutableList()
            it.copy(images = updatedImages)
        }

        if (target.id == null) return
        viewModelScope.launch {
            checkInRepository
                .deleteImage(checkInId = gameId, imageId = target.id)
                .onFailure { e -> logger.e(e) { "이미지 삭제 실패: ${target.id}" } }
        }
    }

    fun editDiary() = _attendanceDetailDiaryUiState.update { it.copy(mode = DiaryMode.WRITE) }

    fun saveDiary(comment: String) {
        AnalyticsLogger.logEvent("diary_save_button")
        _attendanceDetailDiaryUiState.update { it.copy(comment = comment, mode = DiaryMode.READ) }
        viewModelScope.launch { updateMemo(checkInId = gameId, comment = comment) }
        viewModelScope.launch { uploadDiaryImages() }
    }

    private suspend fun updateMemo(
        checkInId: Long,
        comment: String,
    ) {
        checkInRepository
            .updateMemo(checkInId = checkInId, content = comment)
            .onFailure { e -> logger.e(e) { "메모 저장 실패" } }
    }

    private suspend fun uploadDiaryImages() {
        // 로컬에 저장된(id == null인) 이미지만 업로드
        val targets: List<DiaryImageItem> =
            attendanceDetailDiaryUiState.value.images.filter { !it.isEmpty && it.id == null }

        targets.forEach { item: DiaryImageItem ->
            uploadDiaryImage(checkInId = gameId, sourceUri = item.uri ?: "")
                .onSuccess { imageDto -> handleUploadDiaryImageSuccess(item, imageDto) }
                .onFailure { e -> logger.e(e) { "직관 이미지 업로드 실패: ${item.uri}" } }
        }
    }

    private fun handleUploadDiaryImageSuccess(
        item: DiaryImageItem,
        imageDto: CheckInImageDto,
    ) {
        _attendanceDetailDiaryUiState.update { state ->
            val updated = state.images.toMutableList()
            val idx = updated.indexOfFirst { it.uri == item.uri && it.id == null }
            if (idx != -1) updated[idx] = item.copy(id = imageDto.imageId)
            state.copy(images = updated.toImmutableList())
        }
    }

    private suspend fun uploadDiaryImage(
        checkInId: Long,
        sourceUri: String,
    ): Result<CheckInImageDto> =
        runCatching {
            // 1. 압축
            val compressed = compressImage(sourceUri, ImageCompressionSpec.CheckIn)

            // 2. Presigned URL 요청
            val presigned =
                checkInRepository
                    .getImagePresignedUrl(compressed.mimeType, compressed.fileSize)
                    .getOrThrow()

            // 3. S3 업로드
            thirdPartyRepository
                .uploadImageToS3(
                    url = presigned.url,
                    imageFileUri = compressed.uri,
                    contentType = compressed.mimeType,
                    contentLength = compressed.fileSize,
                ).getOrThrow()

            // 4. 이미지 등록
            checkInRepository
                .addImage(checkInId, presigned.key)
                .getOrThrow()
        }

    companion object {
        const val DIARY_MAX_IMAGE_SIZE = 3
    }
}
