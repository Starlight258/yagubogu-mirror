package com.yagubogu.di

import com.yagubogu.domain.attendance.DeleteDiaryUseCase
import com.yagubogu.domain.attendance.LoadDiaryUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val useCaseModule =
    module {
        factoryOf(::LoadDiaryUseCase)
        factoryOf(::DeleteDiaryUseCase)
    }
