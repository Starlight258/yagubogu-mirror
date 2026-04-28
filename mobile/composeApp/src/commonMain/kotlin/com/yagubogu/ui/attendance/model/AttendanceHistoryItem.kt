package com.yagubogu.ui.attendance.model

import androidx.compose.ui.graphics.Color
import com.yagubogu.domain.model.GameResult
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.util.color
import kotlinx.datetime.LocalDateTime

data class AttendanceHistoryItem(
    val id: Long,
    val gameState: GameState,
    val dateTime: LocalDateTime,
    val stadiumName: String,
    val awayTeam: GameTeam,
    val homeTeam: GameTeam,
    val awayTeamScoreBoard: GameScoreBoard,
    val homeTeamScoreBoard: GameScoreBoard,
) {
    val awayTeamColor: Color
        get() = determineTeamColor(awayTeam)
    val homeTeamColor: Color
        get() = determineTeamColor(homeTeam)

    private fun determineTeamColor(team: GameTeam): Color =
        if (team.isMyTeam && team.gameResult == GameResult.WIN) {
            team.team.color
        } else {
            Gray400
        }
}
