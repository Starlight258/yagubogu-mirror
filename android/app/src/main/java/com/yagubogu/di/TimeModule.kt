package com.yagubogu.di

import com.yagubogu.BuildConfig
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.koin.dsl.module
import timber.log.Timber
import kotlin.time.Clock
import kotlin.time.Instant

val timeModule =
    module {
        single<Clock> {
            val clock: Clock =
                runCatching {
                    if (BuildConfig.DEBUG) {
                        val kstZone = TimeZone.of("Asia/Seoul")
                        val localDateTime = LocalDateTime.parse(BuildConfig.DEBUG_FIXED_DATE)
                        val fixedInstant: Instant = localDateTime.toInstant(kstZone)

                        FixedClock(fixedInstant)
                    } else {
                        Clock.System
                    }
                }.getOrElse { e: Throwable ->
                    Timber.e("디버그 Clock 생성 실패: $e")
                    Clock.System
                }

            clock
        }
    }

private class FixedClock(
    private val fixedInstant: Instant,
) : Clock {
    override fun now(): Instant = fixedInstant
}
