package com.yagubogu.data.repository.appconfig

import com.yagubogu.data.datasource.appconfig.AppConfigLocalDataSource
import com.yagubogu.data.datasource.appconfig.AppConfigRemoteDataSource
import com.yagubogu.data.datasource.appconfig.IgnoreInfo
import com.yagubogu.data.dto.response.appconfig.AppConfigPopupDialogResponse
import com.yagubogu.data.dto.response.appconfig.MaintenanceResponse
import com.yagubogu.ui.home.model.HomeNoticeInfo
import com.yagubogu.ui.home.model.MaintenanceInfo
import kotlinx.coroutines.flow.Flow
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

    override suspend fun getMaintenanceInfo(): MaintenanceInfo =
        getNoticeInfoInternal(
            remoteCall = { remoteDataSource.getMaintenanceResponse() },
            localFlow = localDataSource.maintenanceIgnoreInfo,
            mapper = { res: AppConfigPopupDialogResponse, shouldShow: Boolean ->
                MaintenanceInfo(
                    id = res.id,
                    remoteIsShow = res.isShow,
                    shouldShowPopup = shouldShow,
                    emoji = res.emoji,
                    title = res.title,
                    message = res.message,
                    skippableDays = res.skippableDays,
                    isLoginBlock = (res as? MaintenanceResponse)?.isLoginBlock ?: false,
                )
            },
        )

    override suspend fun getHomeNoticeInfo(): HomeNoticeInfo =
        getNoticeInfoInternal(
            remoteCall = { remoteDataSource.getHomeNoticeResponse() },
            localFlow = localDataSource.homeNoticeIgnoreInfo,
            mapper = { res: AppConfigPopupDialogResponse, shouldShow: Boolean ->
                HomeNoticeInfo(
                    id = res.id,
                    remoteIsShow = res.isShow,
                    shouldShowPopup = shouldShow,
                    emoji = res.emoji,
                    title = res.title,
                    message = res.message,
                    skippableDays = res.skippableDays,
                )
            },
        )

    override suspend fun markMaintenanceDialogAsIgnored(
        maintenanceId: Int,
        days: Int,
    ) {
        val expiryTime = days.toExpiryMillis()
        localDataSource.saveMaintenanceIgnoreInfo(maintenanceId, expiryTime)
    }

    override suspend fun markHomeNoticeDialogAsIgnored(
        homeNoticeId: Int,
        days: Int,
    ) {
        val expiryTime = days.toExpiryMillis()
        localDataSource.saveHomeNoticeIgnoreInfo(homeNoticeId, expiryTime)
    }

    override fun isPastCheckInAdEnabled(): Boolean = remoteDataSource.getBoolean("is_past_check_in_ad_enabled")

    private suspend fun <T> getNoticeInfoInternal(
        remoteCall: suspend () -> AppConfigPopupDialogResponse,
        localFlow: Flow<IgnoreInfo>,
        mapper: (AppConfigPopupDialogResponse, Boolean) -> T,
    ): T {
        val remoteResponse = remoteCall()
        val localIgnoreInfo = localFlow.first()
        val currentTime = clock.now().toEpochMilliseconds()

        val shouldShow =
            when {
                !remoteResponse.isShow -> false
                remoteResponse.id > localIgnoreInfo.lastIgnoredId -> true
                currentTime > localIgnoreInfo.ignoreUntil -> true
                else -> false
            }

        return mapper(remoteResponse, shouldShow)
    }

    private fun Int.toExpiryMillis(): Long = clock.now().toEpochMilliseconds() + (this * 86400_000L)
}
