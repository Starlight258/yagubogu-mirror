package com.yagubogu.di

import com.yagubogu.data.datasource.location.LocationDataSource
import com.yagubogu.data.datasource.location.LocationLocalDataSource
import com.yagubogu.data.datasource.thirdparty.ThirdPartyDataSource
import com.yagubogu.data.datasource.thirdparty.AndroidThirdPartyRemoteDataSource
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf

actual fun Module.registerPlatformDataSources() {
    singleOf(::LocationLocalDataSource) { bind<LocationDataSource>() }

    singleOf(::AndroidThirdPartyRemoteDataSource) { bind<ThirdPartyDataSource>() }
}
