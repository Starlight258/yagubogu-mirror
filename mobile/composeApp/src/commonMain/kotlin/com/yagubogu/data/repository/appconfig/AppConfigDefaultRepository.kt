package com.yagubogu.data.repository.appconfig

import com.yagubogu.data.datasource.appconfig.AppConfigLocalDataSource
import com.yagubogu.data.datasource.appconfig.AppConfigRemoteDataSource
import com.yagubogu.ui.home.model.MaintenanceInfo
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

class AppConfigDefaultRepository(
    private val remoteDataSource: AppConfigRemoteDataSource,
    private val localDataSource: AppConfigLocalDataSource,
    private val clock: Clock,
) : AppConfigRepository {
    override suspend fun fetchConfigs() {
        remoteDataSource.fetchAndActivate()
    }

    override suspend fun getMaintenanceInfo(): MaintenanceInfo {
        val remoteResponse = remoteDataSource.getMaintenanceResponse()
        val localIgnoreInfo = localDataSource.maintenanceIgnoreInfo.first()

        val currentTime = clock.now().toEpochMilliseconds()

        val shouldShow =
            when {
                !remoteResponse.isShow -> false // 점검중 아님
                remoteResponse.id > localIgnoreInfo.lastIgnoredId -> true // 새로운 점검 공지사항(ID 증가)
                currentTime > localIgnoreInfo.ignoreUntil -> true // 'n일간 보지 않기' 만료
                else -> false // 'n일간 보지 않기' 작동중
            }

        return MaintenanceInfo(
            id = remoteResponse.id,
            remoteIsShow = remoteResponse.isShow,
            shouldShowPopup = shouldShow,
            emoji = remoteResponse.emoji,
            title = remoteResponse.title,
            message = remoteResponse.message,
            skippableDays = remoteResponse.skippableDays,
            isLoginBlock = remoteResponse.isLoginBlock,
        )
    }

    override suspend fun maintenanceDialogMarkAsIgnored(
        maintenanceId: Int,
        days: Int,
    ) {
        val expiryTime = clock.now().toEpochMilliseconds() + (days * 24 * 60 * 60 * 1000L)
        localDataSource.saveMaintenanceIgnoreInfo(maintenanceId, expiryTime)
    }

    override fun isPastCheckInAdEnabled(): Boolean = remoteDataSource.getBoolean("is_past_check_in_ad_enabled")
}
