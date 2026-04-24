package com.yagubogu.ui.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.stats.StatsRepository
import com.yagubogu.ui.common.model.MemberProfile
import com.yagubogu.ui.mapper.toUiModel
import com.yagubogu.ui.ranking.model.RankingType
import com.yagubogu.ui.ranking.model.RankingUiModel
import com.yagubogu.ui.util.now
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

class RankingViewModel(
    private val type: RankingType,
    private val statsRepository: StatsRepository,
    private val memberRepository: MemberRepository,
    private val clock: Clock,
) : ViewModel() {
    private val logger = Logger.withTag("RankingViewModel")

    private val _rankingUiModel = MutableStateFlow(RankingUiModel(type))
    val rankingUiModel: StateFlow<RankingUiModel> = _rankingUiModel.asStateFlow()

    private val _profileDialogEvent = MutableSharedFlow<MemberProfile>()
    val profileDialogEvent: SharedFlow<MemberProfile> = _profileDialogEvent.asSharedFlow()

    fun fetchRanking(year: Int = LocalDate.now(clock).year) {
        viewModelScope.launch {
            val rankingResult: Result<RankingUiModel> =
                when (type) {
                    RankingType.CHECK_IN ->
                        statsRepository
                            .getCheckInRankings(
                                year = year,
                                before = rankingUiModel.value.nextCursorId,
                                limit = RANKING_LIMIT,
                            ).map { it.toUiModel() }

                    RankingType.VICTORY_FAIRY ->
                        statsRepository
                            .getVictoryFairyRankings(
                                year = year,
                                teamCode = null,
                                before = rankingUiModel.value.nextCursorId,
                                limit = RANKING_LIMIT,
                            ).map { it.toUiModel() }
                }
            rankingResult
                .onSuccess { ranking: RankingUiModel ->
                    _rankingUiModel.value = ranking
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "API 호출 실패" }
                }
        }
    }

    fun fetchMemberProfile(memberId: Long) {
        viewModelScope.launch {
            val memberProfileResult: Result<MemberProfile> =
                memberRepository.getMemberProfile(memberId).map { it.toUiModel() }
            memberProfileResult
                .onSuccess { memberProfile: MemberProfile ->
                    _profileDialogEvent.emit(memberProfile)
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "API 호출 실패 (fetchMemberProfile)" }
                }
        }
    }

    companion object {
        private const val RANKING_LIMIT = 25
    }
}
