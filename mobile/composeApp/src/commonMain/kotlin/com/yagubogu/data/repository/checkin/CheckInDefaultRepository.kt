package com.yagubogu.data.repository.checkin

import com.yagubogu.data.datasource.checkin.CheckInDataSource
import com.yagubogu.data.dto.response.checkin.CheckInCountsResponse
import com.yagubogu.data.dto.response.checkin.CheckInGameDto
import com.yagubogu.data.dto.response.checkin.CheckInHistoryResponse
import com.yagubogu.data.dto.response.checkin.CheckInImageDto
import com.yagubogu.data.dto.response.checkin.CheckInImagesResponse
import com.yagubogu.data.dto.response.checkin.CheckInReviewResponse
import com.yagubogu.data.dto.response.checkin.CheckInStatusResponse
import com.yagubogu.data.dto.response.checkin.FanRateByGameDto
import com.yagubogu.data.dto.response.checkin.FanRateResponse
import com.yagubogu.data.dto.response.checkin.MemoResponse
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountDto
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountsResponse
import com.yagubogu.ui.attendance.detail.model.CheckInPresignedUrlItem
import kotlinx.datetime.LocalDate

class CheckInDefaultRepository(
    private val checkInDataSource: CheckInDataSource,
) : CheckInRepository {
    override suspend fun addCheckIn(gameId: Long): Result<Unit> = checkInDataSource.addCheckIn(gameId)

    override suspend fun getCheckInCounts(year: Int): Result<Int> =
        checkInDataSource
            .getCheckInCounts(year)
            .map { checkInCountsResponse: CheckInCountsResponse ->
                checkInCountsResponse.checkInCounts
            }

    override suspend fun getStadiumFanRates(date: LocalDate): Result<List<FanRateByGameDto>> =
        checkInDataSource
            .getStadiumFanRates(date)
            .map { fanRateResponse: FanRateResponse ->
                fanRateResponse.fanRateByGames
            }

    override suspend fun getCheckInHistories(
        year: Int,
        month: Int?,
        sort: String,
        isWinOnly: Boolean,
    ): Result<List<CheckInGameDto>> =
        checkInDataSource
            .getCheckInHistories(year, month, sort, isWinOnly)
            .map { checkInHistoryResponse: CheckInHistoryResponse ->
                checkInHistoryResponse.checkInHistory
            }

    override suspend fun getCheckInStatus(date: LocalDate): Result<Boolean> =
        checkInDataSource
            .getCheckInStatus(date)
            .map { checkInStatusResponse: CheckInStatusResponse ->
                checkInStatusResponse.isCheckIn
            }

    override suspend fun getStadiumCheckInCounts(year: Int?): Result<List<StadiumCheckInCountDto>> =
        checkInDataSource
            .getStadiumCheckInCounts(year)
            .map { stadiumCheckInCountsResponse: StadiumCheckInCountsResponse ->
                stadiumCheckInCountsResponse.stadiums
            }

    override suspend fun addPastCheckIn(gameId: Long): Result<Unit> = checkInDataSource.addPastCheckIn(gameId)

    override suspend fun getGameReview(checkInId: Long): Result<CheckInReviewResponse> = checkInDataSource.getGameReview(checkInId)

    override suspend fun getMemo(checkInId: Long): Result<String?> =
        checkInDataSource
            .getMemo(checkInId)
            .map { memoResponse: MemoResponse ->
                memoResponse.memo
            }

    override suspend fun updateMemo(
        checkInId: Long,
        content: String,
    ): Result<Unit> = checkInDataSource.updateMemo(checkInId, content)

    override suspend fun deleteMemo(checkInId: Long): Result<Unit> = checkInDataSource.deleteMemo(checkInId)

    override suspend fun getImagePresignedUrl(
        contentType: String,
        contentLength: Long,
    ): Result<CheckInPresignedUrlItem> =
        checkInDataSource
            .getImagePresignedUrl(contentType, contentLength)
            .map { CheckInPresignedUrlItem(key = it.key, url = it.url) }

    override suspend fun getImages(checkInId: Long): Result<List<CheckInImageDto>> =
        checkInDataSource
            .getImages(checkInId)
            .map { checkInImagesResponse: CheckInImagesResponse ->
                checkInImagesResponse.images
            }

    override suspend fun addImage(
        checkInId: Long,
        imageKey: String,
    ): Result<CheckInImageDto> = checkInDataSource.addImage(checkInId, imageKey)

    override suspend fun deleteImage(
        checkInId: Long,
        imageId: Long,
    ): Result<Unit> = checkInDataSource.deleteImage(checkInId, imageId)
}
