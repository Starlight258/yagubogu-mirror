package com.yagubogu.di

import co.touchlab.kermit.Logger
import com.yagubogu.BuildConfig
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

val timeModule =
    module {
        single<Clock> {
            val logger: Logger = get<Logger> { parametersOf("timeModule") }
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
                    logger.e(e) { "디버그 Clock 생성 실패" }
                    Clock.system(kstZone)
                }

            clock
        }
    }
