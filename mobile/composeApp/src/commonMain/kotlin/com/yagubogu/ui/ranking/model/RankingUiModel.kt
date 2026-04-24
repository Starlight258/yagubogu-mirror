package com.yagubogu.ui.ranking.model

import com.yagubogu.ui.common.model.MemberProfile

data class RankingUiModel(
    val type: RankingType,
    val topRankings: List<RankingProfileItem> = emptyList(),
    val myRanking: RankingProfileItem =
        when (type) {
            RankingType.CHECK_IN -> RankingProfileItem.CheckInRanking()
            RankingType.VICTORY_FAIRY -> RankingProfileItem.VictoryFairyRanking()
        },
    val nextCursorId: Long? = null,
    val hasNext: Boolean = true,
    val isLoading: Boolean = false,
    val selectedMemberProfile: MemberProfile? = null,
)
