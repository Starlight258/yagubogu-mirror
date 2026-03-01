package com.yagubogu.di

import com.yagubogu.data.datasource.auth.AuthDataSource
import com.yagubogu.data.datasource.auth.AuthRemoteDataSource
import com.yagubogu.data.datasource.checkin.CheckInDataSource
import com.yagubogu.data.datasource.checkin.CheckInRemoteDataSource
import com.yagubogu.data.datasource.game.GameDataSource
import com.yagubogu.data.datasource.game.GameRemoteDataSource
import com.yagubogu.data.datasource.location.LocationDataSource
import com.yagubogu.data.datasource.location.LocationLocalDataSource
import com.yagubogu.data.datasource.member.MemberDataSource
import com.yagubogu.data.datasource.member.MemberRemoteDataSource
import com.yagubogu.data.datasource.stadium.StadiumDataSource
import com.yagubogu.data.datasource.stadium.StadiumRemoteDataSource
import com.yagubogu.data.datasource.stats.StatsDataSource
import com.yagubogu.data.datasource.stats.StatsRemoteDataSource
import com.yagubogu.data.datasource.stream.StreamDataSource
import com.yagubogu.data.datasource.stream.StreamRemoteDataSource
import com.yagubogu.data.datasource.talk.TalkDataSource
import com.yagubogu.data.datasource.talk.TalkRemoteDataSource
import com.yagubogu.data.datasource.thirdparty.ThirdPartyDataSource
import com.yagubogu.data.datasource.thirdparty.ThirdPartyRemoteDataSource
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val datasourceModule =
    module {
        singleOf(::AuthRemoteDataSource) { bind<AuthDataSource>() }

        singleOf(::MemberRemoteDataSource) { bind<MemberDataSource>() }

        singleOf(::CheckInRemoteDataSource) { bind<CheckInDataSource>() }

        singleOf(::StatsRemoteDataSource) { bind<StatsDataSource>() }

        singleOf(::LocationLocalDataSource) { bind<LocationDataSource>() }

        singleOf(::StadiumRemoteDataSource) { bind<StadiumDataSource>() }

        singleOf(::StreamRemoteDataSource) { bind<StreamDataSource>() }

        singleOf(::GameRemoteDataSource) { bind<GameDataSource>() }

        singleOf(::ThirdPartyRemoteDataSource) { bind<ThirdPartyDataSource>() }

        singleOf(::TalkRemoteDataSource) { bind<TalkDataSource>() }
    }
