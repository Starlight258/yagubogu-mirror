package com.yagubogu.ui.home.ranking.model

data class RankingUiModel(
    val type: RankingType,
    val topRankings: List<RankingProfileItem> = emptyList(),
    val myRanking: RankingProfileItem =
        when (type) {
            RankingType.CHECK_IN -> RankingProfileItem.CheckInRanking()
            RankingType.VICTORY_FAIRY -> RankingProfileItem.VictoryFairyRanking()
        },
)
