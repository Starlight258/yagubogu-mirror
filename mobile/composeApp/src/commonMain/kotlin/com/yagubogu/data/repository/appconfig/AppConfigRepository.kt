package com.yagubogu.data.repository.appconfig

import com.yagubogu.ui.home.model.HomeNoticeInfo
import com.yagubogu.ui.home.model.MaintenanceInfo

interface AppConfigRepository {
    suspend fun fetchConfigs()

    suspend fun getMaintenanceInfo(): MaintenanceInfo

    suspend fun getHomeNoticeInfo(): HomeNoticeInfo

    suspend fun maintenanceDialogMarkAsIgnored(
        maintenanceId: Int,
        days: Int,
    )

    suspend fun homeNoticeDialogMarkAsIgnored(
        homeNoticeId: Int,
        days: Int,
    )

    fun isPastCheckInAdEnabled(): Boolean
}
