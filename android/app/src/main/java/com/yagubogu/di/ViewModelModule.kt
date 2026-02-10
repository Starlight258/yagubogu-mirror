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
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val viewModelModule =
    module {
        viewModelOf(::YaguBoguViewModel)

        viewModel {
            AttendanceHistoryViewModel(
                checkInRepository = get(),
                gameRepository = get(),
                logger = get { parametersOf("AttendanceHistoryViewModel") },
            )
        }

        viewModel {
            BadgeViewModel(
                memberRepository = get(),
                logger = get { parametersOf("BadgeViewModel") },
            )
        }

        viewModel {
            FavoriteTeamViewModel(
                memberRepository = get(),
                logger = get { parametersOf("FavoriteTeamViewModel") },
            )
        }

        viewModel {
            HomeViewModel(
                memberRepository = get(),
                checkInRepository = get(),
                statsRepository = get(),
                locationRepository = get(),
                stadiumRepository = get(),
                streamRepository = get(),
                clock = get(),
                logger = get { parametersOf("HomeViewModel") },
            )
        }

        viewModel {
            LivetalkViewModel(
                gameRepository = get(),
                clock = get(),
                logger = get { parametersOf("LivetalkViewModel") },
            )
        }

        viewModel { (gameId: Long, isVerified: Boolean) ->
            LivetalkChatViewModel(
                gameId = gameId,
                isVerified = isVerified,
                talkRepository = get(),
                gameRepository = get(),
                memberRepository = get(),
                logger = get { parametersOf("LivetalkChatViewModel") },
            )
        }

        viewModel {
            LoginViewModel(
                authRepository = get(),
                memberRepository = get(),
                logger = get { parametersOf("LoginViewModel") },
            )
        }

        viewModelOf(::MainViewModel)

        viewModel {
            SettingViewModel(
                memberRepository = get(),
                authRepository = get(),
                thirdPartyRepository = get(),
                clock = get(),
                logger = get { parametersOf("SettingViewModel") },
            )
        }

        viewModel {
            StatsViewModel(
                statsRepository = get(),
                memberRepository = get(),
                checkInRepository = get(),
                logger = get { parametersOf("StatsViewModel") },
            )
        }
    }
