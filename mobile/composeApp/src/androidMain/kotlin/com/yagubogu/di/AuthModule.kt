package com.yagubogu.di

import com.yagubogu.BuildKonfig
import com.yagubogu.ui.login.AndroidGoogleCredentialManager
import com.yagubogu.ui.login.auth.GoogleCredentialManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.scope.dsl.activityScope
import org.koin.dsl.module

actual val authModule = module {
    activityScope {
        scoped<GoogleCredentialManager> {
            AndroidGoogleCredentialManager(
                context = androidContext(),
                serverClientId = BuildKonfig.BASE_URL,
                nonce = "",
            )
        }
    }
}