package com.yagubogu.domain.usecase

import com.yagubogu.data.repository.checkin.CheckInRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class DeleteDiaryUseCase(
    private val checkInRepository: CheckInRepository,
) {
    suspend operator fun invoke(
        checkInId: Long,
        imageIds: List<Long>,
    ): Result<Unit> {
        val results =
            coroutineScope {
                val imageJobs =
                    imageIds.map { id -> async { checkInRepository.deleteImage(checkInId, id) } }
                val memoJob = async { checkInRepository.deleteMemo(checkInId) }
                (imageJobs + memoJob).awaitAll()
            }
        return results.firstOrNull { it.isFailure } ?: Result.success(Unit)
    }
}
