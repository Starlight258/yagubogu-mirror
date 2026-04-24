package com.yagubogu.ui.home.model

sealed interface RankingItem {
    val topRankings: List<RankingProfileItem>
    val myRanking: RankingProfileItem

    data class VictoryFairyRanking(
        override val topRankings: List<RankingProfileItem> = emptyList(),
        override val myRanking: RankingProfileItem = RankingProfileItem(),
    ) : RankingItem
}
