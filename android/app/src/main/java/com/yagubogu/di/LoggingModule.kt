package com.yagubogu.di

import co.touchlab.kermit.Logger
import org.koin.dsl.module

/**
 * Kermit의 전역 싱글톤 Logger를 제공하는 Koin 모듈
 */
val loggingModule = module {
    // Hilt의 @Provides @Singleton과 동일한 역할
    single { Logger }
}