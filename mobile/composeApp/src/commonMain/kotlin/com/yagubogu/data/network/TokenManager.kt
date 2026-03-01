package com.yagubogu.data.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(
    private val context: Context,
) {
    private var cachedAccessToken: String? = null
    private var cachedRefreshToken: String? = null

    private val mutex = Mutex()

    suspend fun getAccessToken(): String? =
        mutex.withLock {
            if (cachedAccessToken == null) {
                cachedAccessToken = context.dataStore.data.first()[ACCESS_TOKEN_KEY]
            }
            cachedAccessToken
        }

    suspend fun getRefreshToken(): String? =
        mutex.withLock {
            if (cachedRefreshToken == null) {
                cachedRefreshToken = context.dataStore.data.first()[REFRESH_TOKEN_KEY]
            }
            cachedRefreshToken
        }

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
    ) {
        mutex.withLock {
            cachedAccessToken = accessToken
            cachedRefreshToken = refreshToken
        }

        context.dataStore.edit { prefs: MutablePreferences ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun clearTokens() {
        mutex.withLock {
            cachedAccessToken = null
            cachedRefreshToken = null
        }

        context.dataStore.edit { prefs: MutablePreferences ->
            prefs.remove(ACCESS_TOKEN_KEY)
            prefs.remove(REFRESH_TOKEN_KEY)
        }
    }

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }
}
