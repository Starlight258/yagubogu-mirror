package com.yagubogu.ui.attendance.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.data.repository.checkin.CheckInRepository
import com.yagubogu.data.repository.thirdparty.ThirdPartyRepository
import com.yagubogu.domain.attendance.AttendanceDiary
import com.yagubogu.domain.attendance.DeleteDiaryUseCase
import com.yagubogu.domain.attendance.LoadDiaryUseCase
import com.yagubogu.ui.attendance.detail.AttendanceDetailViewModel.Companion.DIARY_MAX_IMAGE_SIZE
import com.yagubogu.ui.attendance.detail.model.AttendanceDetailDiaryUiState
import com.yagubogu.ui.attendance.detail.model.AttendanceDetailUiEvent
import com.yagubogu.ui.attendance.detail.model.CheckInImageItem
import com.yagubogu.ui.attendance.detail.model.DiaryImageItem
import com.yagubogu.ui.attendance.detail.model.DiaryMode
import com.yagubogu.ui.common.component.image.ImageCompressionSpec
import com.yagubogu.ui.common.component.image.compressImage
import com.yagubogu.ui.mapper.toUiModel
import kotlinx.collections.immutable.ImmutableList
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.attendance_detail_delete_failed
import yagubogu.composeapp.generated.resources.attendance_detail_load_failed
import yagubogu.composeapp.generated.resources.attendance_detail_update_memo_failed
import yagubogu.composeapp.generated.resources.attendance_detail_upload_image_failed
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AttendanceDetailViewModel(
    private val gameId: Long,
    private val checkInRepository: CheckInRepository,
    private val thirdPartyRepository: ThirdPartyRepository,
    private val loadDiaryUseCase: LoadDiaryUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
) : ViewModel() {
    private val logger = Logger.withTag("AttendanceDetailViewModel")

    private val _attendanceDetailDiaryUiState = MutableStateFlow(AttendanceDetailDiaryUiState())
    val attendanceDetailDiaryUiState: StateFlow<AttendanceDetailDiaryUiState> =
        _attendanceDetailDiaryUiState.asStateFlow()

    private val _uiEvent =
        MutableSharedFlow<AttendanceDetailUiEvent>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    val uiEvent: SharedFlow<AttendanceDetailUiEvent> = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch { loadDiary() }
    }

    fun addImages(uris: List<String>) {
        _attendanceDetailDiaryUiState.update { state ->
            val combined: List<DiaryImageItem> =
                (state.images.filterNot { it.isEmpty } + uris.map { DiaryImageItem(uri = it) })
                    .take(DIARY_MAX_IMAGE_SIZE)
            state.copy(images = combined.padToMaxImageSize())
        }
    }

    fun deleteImage(index: Int) {
        val target = attendanceDetailDiaryUiState.value.images[index]
        _attendanceDetailDiaryUiState.update { state ->
            state.copy(
                images = state.images.filterIndexed { idx, _ -> idx != index }.padToMaxImageSize(),
            )
        }
        if (target.id == null) return

        viewModelScope.launch {
            checkInRepository
                .deleteImage(checkInId = gameId, imageId = target.id)
                .onFailure { e -> logger.e(e) { "이미지 삭제 실패: ${target.id}" } }
        }
    }

    fun deleteDiary() {
        _attendanceDetailDiaryUiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val imageIds = attendanceDetailDiaryUiState.value.images.mapNotNull { it.id }
            deleteDiaryUseCase(checkInId = gameId, imageIds = imageIds)
                .onSuccess { _attendanceDetailDiaryUiState.value = AttendanceDetailDiaryUiState() }
                .onFailure { e ->
                    logger.e(e) { "직관 기록 삭제 실패" }
                    _attendanceDetailDiaryUiState.update { it.copy(isLoading = false) }
                    _uiEvent.emit(AttendanceDetailUiEvent.ShowSnackbar(Res.string.attendance_detail_delete_failed))
                }
        }
    }

    fun editDiary() = _attendanceDetailDiaryUiState.update { it.copy(mode = DiaryMode.WRITE) }

    fun saveDiary(comment: String) {
        AnalyticsLogger.logEvent("diary_save_button")
        _attendanceDetailDiaryUiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val (memoSuccess, imagesSuccess) =
                coroutineScope {
                    val memo = async { updateMemo(checkInId = gameId, comment = comment) }
                    val images = async { uploadDiaryImages() }
                    memo.await() to images.await()
                }
            if (memoSuccess && imagesSuccess) {
                _attendanceDetailDiaryUiState.update { state ->
                    state.copy(comment = comment, mode = DiaryMode.READ, isLoading = false)
                }
            } else {
                _attendanceDetailDiaryUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun loadDiary() {
        loadDiaryUseCase(gameId)
            .onSuccess { diary -> applyLoadedDiary(diary) }
            .onFailure { e ->
                logger.e(e) { "직관 기록 조회 실패" }
                _attendanceDetailDiaryUiState.update { it.copy(isLoading = false) }
                _uiEvent.emit(AttendanceDetailUiEvent.ShowSnackbar(Res.string.attendance_detail_load_failed))
            }
    }

    private fun applyLoadedDiary(diary: AttendanceDiary) {
        val diaryImages: ImmutableList<DiaryImageItem> =
            diary.images
                .map { image -> DiaryImageItem(id = image.id, uri = image.url) }
                .take(DIARY_MAX_IMAGE_SIZE)
                .padToMaxImageSize()

        _attendanceDetailDiaryUiState.update { state ->
            state.copy(
                isLoading = false,
                mode = if (diary.hasContent) DiaryMode.READ else DiaryMode.WRITE,
                comment = diary.memo.orEmpty(),
                images = diaryImages,
            )
        }
    }

    private suspend fun updateMemo(
        checkInId: Long,
        comment: String,
    ): Boolean =
        checkInRepository
            .updateMemo(checkInId = checkInId, content = comment)
            .onFailure { e ->
                _uiEvent.emit(AttendanceDetailUiEvent.ShowSnackbar(Res.string.attendance_detail_update_memo_failed))
                logger.e(e) { "메모 저장 실패" }
            }.isSuccess

    private suspend fun uploadDiaryImages(): Boolean {
        val targets: List<DiaryImageItem> =
            attendanceDetailDiaryUiState.value.images.filter { !it.isEmpty && it.id == null }

        val results =
            coroutineScope {
                targets
                    .map { item: DiaryImageItem ->
                        async {
                            val uri = item.uri ?: return@async true
                            uploadDiaryImage(checkInId = gameId, sourceUri = uri)
                                .onSuccess { imageDto -> handleUploadDiaryImageSuccess(item, imageDto) }
                                .onFailure { e ->
                                    _uiEvent.emit(AttendanceDetailUiEvent.ShowSnackbar(Res.string.attendance_detail_upload_image_failed))
                                    logger.e(e) { "직관 이미지 업로드 실패: ${item.uri}" }
                                }.isSuccess
                        }
                    }.awaitAll()
            }
        return results.all { it }
    }

    private fun handleUploadDiaryImageSuccess(
        item: DiaryImageItem,
        imageItem: CheckInImageItem,
    ) {
        _attendanceDetailDiaryUiState.update { state ->
            val updated = state.images.toMutableList()
            val idx = updated.indexOfFirst { it.uri == item.uri && it.id == null }
            if (idx != -1) updated[idx] = item.copy(id = imageItem.id)
            state.copy(images = updated.toImmutableList())
        }
    }

    private suspend fun uploadDiaryImage(
        checkInId: Long,
        sourceUri: String,
    ): Result<CheckInImageItem> =
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
                .toUiModel()
        }

    /** 리스트 크기가 [DIARY_MAX_IMAGE_SIZE] 보다 작은 경우 기본 UI 모델로 채운 `ImmutableList` 반환. */
    private fun List<DiaryImageItem>.padToMaxImageSize(): ImmutableList<DiaryImageItem> =
        (this + List(DIARY_MAX_IMAGE_SIZE - size) { DiaryImageItem() }).toImmutableList()

    companion object {
        const val DIARY_MAX_IMAGE_SIZE = 3
    }
}
