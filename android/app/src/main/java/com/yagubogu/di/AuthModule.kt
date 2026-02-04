package com.yagubogu.di

import com.yagubogu.BuildConfig
import com.yagubogu.ui.login.auth.GoogleCredentialManager
import com.yagubogu.ui.main.YaguBoguActivity
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val authModule =
    module {
        scope<YaguBoguActivity> {
            scoped<GoogleCredentialManager> {
                GoogleCredentialManager(
                    context = androidContext(),
                    serverClientId = BuildConfig.WEB_CLIENT_ID,
                    nonce = "",
                )
            }
        }
    }
