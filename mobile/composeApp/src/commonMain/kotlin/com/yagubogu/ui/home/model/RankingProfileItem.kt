package com.yagubogu.ui.home.model

sealed interface RankingProfileItem {
    val memberId: Long
    val rank: Int
    val nickname: String
    val profileImageUrl: String
    val teamName: String

    data class CheckInRanking(
        override val memberId: Long = 0L,
        override val rank: Int = 0,
        override val nickname: String = "",
        override val profileImageUrl: String = "",
        override val teamName: String = "",
        val count: Int = 0,
    ) : RankingProfileItem

    data class VictoryFairyRanking(
        override val memberId: Long = 0L,
        override val rank: Int = 0,
        override val nickname: String = "",
        override val profileImageUrl: String = "",
        override val teamName: String = "",
        val score: Double = 0.0,
    ) : RankingProfileItem
}
