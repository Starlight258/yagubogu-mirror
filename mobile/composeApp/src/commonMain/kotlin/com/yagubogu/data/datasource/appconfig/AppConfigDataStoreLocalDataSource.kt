package com.yagubogu.data.datasource.appconfig

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppConfigDataStoreLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) : AppConfigLocalDataSource {
    override val maintenanceIgnoreInfo: Flow<MaintenanceIgnoreInfo> =
        dataStore.data.map { prefs ->
            MaintenanceIgnoreInfo(
                lastIgnoredId = prefs[LAST_IGNORED_ID_KEY] ?: -1,
                ignoreUntil = prefs[MAINTENANCE_IGNORE_UNTIL_KEY] ?: 0L,
            )
        }

    override suspend fun saveMaintenanceIgnoreInfo(
        id: Int,
        expiryTime: Long,
    ) {
        dataStore.edit { prefs ->
            prefs[LAST_IGNORED_ID_KEY] = id
            prefs[MAINTENANCE_IGNORE_UNTIL_KEY] = expiryTime
        }
    }

    companion object {
        private val LAST_IGNORED_ID_KEY = intPreferencesKey("last_ignored_id")
        private val MAINTENANCE_IGNORE_UNTIL_KEY = longPreferencesKey("maintenance_ignore_until")
    }
}
