package com.yagubogu.ui.attendance.model

enum class GameState {
    SCHEDULED, // 경기 전
    LIVE, // 경기 중
    COMPLETED, // 경기 종료
    CANCELED, // 경기 취소
    ;

    companion object {
        fun from(state: String): GameState = entries.find { it.name == state } ?: throw IllegalArgumentException()
    }
}
