package com.yagubogu

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yagubogu.common.YaguBoguDebugTree
import com.yagubogu.common.YaguBoguReleaseTree
import com.yagubogu.di.androidModule
import com.yagubogu.di.authModule
import com.yagubogu.di.datasourceModule
import com.yagubogu.di.networkModule
import com.yagubogu.di.repositoryModule
import com.yagubogu.di.serviceModule
import com.yagubogu.di.timeModule
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

@HiltAndroidApp
class YaguBoguApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin()
        setupLogging()
    }

    private fun startKoin() {
        startKoin {
            androidContext(androidContext = this@YaguBoguApplication)

            modules(
                androidModule,
                authModule,
                datasourceModule,
                networkModule,
                repositoryModule,
                serviceModule,
                timeModule,
            )
        }
    }

    private fun setupLogging() {
        if (BuildConfig.DEBUG) {
            // 개발 환경
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)
            Timber.plant(YaguBoguDebugTree())
        } else {
            // 운영 환경
            Timber.plant(YaguBoguReleaseTree())
        }
    }
}
