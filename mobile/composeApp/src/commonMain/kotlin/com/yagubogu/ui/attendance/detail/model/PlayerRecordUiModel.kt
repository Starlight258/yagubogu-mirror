package com.yagubogu.ui.attendance.detail.model

data class PlayerRecordUiModel(
    val awayTeamHitters: List<HitterRecord>,
    val awayTeamPitchers: List<PitcherRecord>,
    val homeTeamHitters: List<HitterRecord>,
    val homeTeamPitchers: List<PitcherRecord>,
) {
    data class HitterRecord(
        val battingOrder: Int,
        val position: String,
        val playerName: String,
        val atBats: Int,
        val hits: Int,
        val rbi: Int,
        val runs: Int,
    )

    data class PitcherRecord(
        val playerName: String,
        val result: String,
        val innings: String,
        val pitchCount: Int,
        val hitsAllowed: Int,
        val walksAndHbp: Int,
        val strikeouts: Int,
        val runsAllowed: Int,
        val earnedRuns: Int,
    )
}
