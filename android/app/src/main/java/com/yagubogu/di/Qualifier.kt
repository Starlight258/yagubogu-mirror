package com.yagubogu.di

sealed interface Qualifier {
    data object BaseUrl : Qualifier

    data object GlobalClient : Qualifier

    data object StreamClient : Qualifier

    data object TokenRefreshMutex : Qualifier
}
