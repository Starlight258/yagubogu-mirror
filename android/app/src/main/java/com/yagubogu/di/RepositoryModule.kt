package com.yagubogu.di

import com.yagubogu.data.repository.auth.AuthDefaultRepository
import com.yagubogu.data.repository.auth.AuthRepository
import com.yagubogu.data.repository.checkin.CheckInDefaultRepository
import com.yagubogu.data.repository.checkin.CheckInRepository
import com.yagubogu.data.repository.game.GameDefaultRepository
import com.yagubogu.data.repository.game.GameRepository
import com.yagubogu.data.repository.location.LocationDefaultRepository
import com.yagubogu.data.repository.location.LocationRepository
import com.yagubogu.data.repository.member.MemberDefaultRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.stadium.StadiumDefaultRepository
import com.yagubogu.data.repository.stadium.StadiumRepository
import com.yagubogu.data.repository.stats.StatsDefaultRepository
import com.yagubogu.data.repository.stats.StatsRepository
import com.yagubogu.data.repository.stream.StreamDefaultRepository
import com.yagubogu.data.repository.stream.StreamRepository
import com.yagubogu.data.repository.talk.TalkDefaultRepository
import com.yagubogu.data.repository.talk.TalkRepository
import com.yagubogu.data.repository.thirdparty.ThirdPartyDefaultRepository
import com.yagubogu.data.repository.thirdparty.ThirdPartyRepository
import org.koin.dsl.module

val repositoryModule =
    module {
        single<AuthRepository> { AuthDefaultRepository(authDataSource = get(), tokenManager = get()) }

        single<MemberRepository> {
            MemberDefaultRepository(memberDataSource = get(), tokenManager = get())
        }

        single<CheckInRepository> { CheckInDefaultRepository(checkInDataSource = get()) }

        single<StatsRepository> { StatsDefaultRepository(statsDataSource = get()) }

        single<LocationRepository> { LocationDefaultRepository(locationDataSource = get()) }

        single<StadiumRepository> { StadiumDefaultRepository(stadiumDataSource = get()) }

        single<StreamRepository> { StreamDefaultRepository(streamDataSource = get()) }

        single<GameRepository> { GameDefaultRepository(gameDataSource = get()) }

        single<ThirdPartyRepository> { ThirdPartyDefaultRepository(thirdPartyDataSource = get()) }

        single<TalkRepository> { TalkDefaultRepository(talkDataSource = get()) }
    }
