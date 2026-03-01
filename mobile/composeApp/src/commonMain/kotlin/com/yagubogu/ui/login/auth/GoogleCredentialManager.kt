package com.yagubogu.ui.login.auth

interface GoogleCredentialManager {
    suspend fun getGoogleCredentialResult(): GoogleCredentialResult

    suspend fun signOut(): Result<Unit>
}