package com.yagubogu.data.datasource.appconfig

import kotlinx.coroutines.flow.Flow

interface AppConfigLocalDataSource {
    val maintenanceIgnoreInfo: Flow<MaintenanceIgnoreInfo>

    suspend fun saveMaintenanceIgnoreInfo(
        id: Int,
        expiryTime: Long,
    )
}
