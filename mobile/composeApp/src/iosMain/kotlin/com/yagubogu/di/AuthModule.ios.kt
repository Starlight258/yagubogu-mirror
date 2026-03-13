package com.yagubogu.di

import com.yagubogu.ui.login.GoogleSignInDelegate
import com.yagubogu.ui.login.IosGoogleCredentialManager
import com.yagubogu.ui.login.auth.GoogleCredentialManager
import org.koin.dsl.module

actual val authModule =
    module {
        single<GoogleCredentialManager> {
            IosGoogleCredentialManager(get())
        }
    }
