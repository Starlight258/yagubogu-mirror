package com.yagubogu.domain.attendance

import com.yagubogu.data.repository.checkin.CheckInRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class LoadDiaryUseCase(
    private val checkInRepository: CheckInRepository,
) {
    suspend operator fun invoke(checkInId: Long): Result<AttendanceDiary> =
        runCatching {
            coroutineScope {
                val memoDeferred = async { checkInRepository.getMemo(checkInId).getOrThrow() }
                val imagesDeferred = async { checkInRepository.getImages(checkInId).getOrThrow() }
                AttendanceDiary(
                    memo = memoDeferred.await(),
                    images = imagesDeferred.await().map { AttendanceDiary.Image(id = it.imageId, url = it.imageUrl) },
                )
            }
        }
}
