package com.yagubogu.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

// ========== HttpClient ==========
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GlobalClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StreamClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NoAuthClient

// ========== Ktorfit ==========
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GlobalKtorfit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NoAuthKtorfit
