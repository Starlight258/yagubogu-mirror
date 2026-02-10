package com.yagubogu

import android.app.Application
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter
import co.touchlab.kermit.koin.KermitKoinLogger
import co.touchlab.kermit.koin.kermitLoggerModule
import co.touchlab.kermit.platformLogWriter
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yagubogu.di.androidModule
import com.yagubogu.di.datasourceModule
import com.yagubogu.di.networkModule
import com.yagubogu.di.repositoryModule
import com.yagubogu.di.serviceModule
import com.yagubogu.di.timeModule
import com.yagubogu.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

@OptIn(ExperimentalKermitApi::class)
class YaguBoguApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupLogging()
        startKoin()
    }

    private fun setupLogging() {
        if (BuildConfig.DEBUG) {
            // 개발 환경
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)
        }
        Logger.setLogWriters(
            if (BuildConfig.DEBUG) {
                platformLogWriter()
            } else {
                CrashlyticsLogWriter(Severity.Info)
            },
        )
    }

    private fun startKoin() {
        startKoin {
            androidContext(androidContext = this@YaguBoguApplication)

            logger(
                KermitKoinLogger(Logger.withTag("koin")),
            )
            modules(
                androidModule,
                datasourceModule,
                networkModule,
                repositoryModule,
                serviceModule,
                timeModule,
                viewModelModule,
                kermitLoggerModule(Logger),
            )
        }
    }
}
