package com.yagubogu.di

import com.yagubogu.BuildConfig
import org.koin.dsl.module
import timber.log.Timber
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

val timeModule =
    module {
        single<Clock> {
            val kstZone = ZoneId.of("Asia/Seoul")

            val clock: Clock =
                runCatching {
                    if (BuildConfig.DEBUG) {
                        val localDateTime = LocalDateTime.parse(BuildConfig.DEBUG_FIXED_DATE)
                        val fixedZonedDateTime = localDateTime.atZone(kstZone)

                        Clock.fixed(fixedZonedDateTime.toInstant(), kstZone)
                    } else {
                        Clock.system(kstZone)
                    }
                }.getOrElse { e: Throwable ->
                    Timber.e("디버그 Clock 생성 실패: $e")
                    Clock.system(kstZone)
                }

            clock
        }
    }
