package com.yagubogu.di

import com.yagubogu.BuildKonfig
import com.yagubogu.ui.login.AndroidGoogleCredentialManager
import com.yagubogu.ui.login.auth.GoogleCredentialManager
import org.koin.androidx.scope.dsl.activityScope
import org.koin.dsl.module

actual val authModule =
    module {
        activityScope {
            scoped<GoogleCredentialManager> {
                AndroidGoogleCredentialManager(
                    context = get(),
                    serverClientId = BuildKonfig.WEB_CLIENT_ID,
                    nonce = "",
                )
            }
        }
    }
