package com.yagubogu.di

import com.yagubogu.ui.attendance.AttendanceHistoryViewModel
import com.yagubogu.ui.badge.BadgeViewModel
import com.yagubogu.ui.favorite.FavoriteTeamViewModel
import com.yagubogu.ui.home.HomeViewModel
import com.yagubogu.ui.livetalk.LivetalkViewModel
import com.yagubogu.ui.livetalk.chat.LivetalkChatViewModel
import com.yagubogu.ui.login.LoginViewModel
import com.yagubogu.ui.main.MainViewModel
import com.yagubogu.ui.main.YaguBoguViewModel
import com.yagubogu.ui.setting.SettingViewModel
import com.yagubogu.ui.stats.StatsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule =
    module {
        viewModel { YaguBoguViewModel(authRepository = get(), memberRepository = get()) }

        viewModel { AttendanceHistoryViewModel(checkInRepository = get(), gameRepository = get()) }

        viewModel { BadgeViewModel(memberRepository = get()) }

        viewModel { FavoriteTeamViewModel(memberRepository = get()) }

        viewModel {
            HomeViewModel(
                memberRepository = get(),
                checkInRepository = get(),
                statsRepository = get(),
                locationRepository = get(),
                stadiumRepository = get(),
                streamRepository = get(),
                clock = get(),
            )
        }

        viewModel { LivetalkViewModel(gameRepository = get(), clock = get()) }

        viewModel { (gameId: Long, isVerified: Boolean) ->
            LivetalkChatViewModel(
                gameId = gameId,
                isVerified = isVerified,
                talkRepository = get(),
                gameRepository = get(),
                memberRepository = get(),
            )
        }

        viewModel { LoginViewModel(authRepository = get(), memberRepository = get()) }

        viewModel { MainViewModel() }

        viewModel {
            SettingViewModel(
                memberRepository = get(),
                authRepository = get(),
                thirdPartyRepository = get(),
                clock = get(),
            )
        }

        viewModel {
            StatsViewModel(
                statsRepository = get(),
                memberRepository = get(),
                checkInRepository = get(),
            )
        }
    }
