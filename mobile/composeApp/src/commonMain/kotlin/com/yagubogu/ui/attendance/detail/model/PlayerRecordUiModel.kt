package com.yagubogu.ui.attendance.detail.model

data class PlayerRecordUiModel(
    val awayTeamHitters: List<HitterRecord>,
    val awayTeamPitchers: List<PitcherRecord>,
    val homeTeamHitters: List<HitterRecord>,
    val homeTeamPitchers: List<PitcherRecord>,
) {
    data class HitterRecord(
        val battingOrder: Int, // 타순
        val position: String, // 수비 포지션
        val playerName: String, // 선수 이름
        val atBats: Int, // 타수
        val hits: Int, // 안타 수
        val rbi: Int, // 타점
        val runs: Int, // 득점
    )

    data class PitcherRecord(
        val playerName: String, // 선수 이름
        val result: String, // 등판 결과
        val innings: String, // 투구 이닝
        val hitsAllowed: Int, // 피안타 수
        val runsAllowed: Int, // 실점
        val earnedRuns: Int, // 자책점
        val strikeouts: Int, // 삼진 수
        val walksAndHbp: Int, // 4사구 수
        val pitchCount: Int, // 투구 수
    )
}
