package com.yagubogu.data.repository.appconfig

import com.yagubogu.ui.home.model.MaintenanceInfo

interface AppConfigRepository {
    suspend fun fetchConfigs()

    suspend fun getMaintenanceInfo(): MaintenanceInfo

    suspend fun maintenanceDialogMarkAsIgnored(
        maintenanceId: Int,
        days: Int,
    )

    fun isPastCheckInAdEnabled(): Boolean
}
