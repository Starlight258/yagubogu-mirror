package com.yagubogu.ui.mapper

import com.yagubogu.data.dto.response.checkin.CheckInGameDto
import com.yagubogu.data.dto.response.checkin.CheckInGameTeamDto
import com.yagubogu.data.dto.response.checkin.CheckInImageDto
import com.yagubogu.data.dto.response.checkin.FanRateByGameDto
import com.yagubogu.data.dto.response.checkin.ScoreBoardDto
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountDto
import com.yagubogu.data.dto.response.checkin.TeamFanRateDto
import com.yagubogu.domain.model.GameResult
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.attendance.detail.model.CheckInImageItem
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.GameScoreBoard
import com.yagubogu.ui.attendance.model.GameState
import com.yagubogu.ui.attendance.model.GameTeam
import com.yagubogu.ui.home.model.StadiumFanRateItem
import com.yagubogu.ui.home.model.TeamFanRate
import com.yagubogu.ui.stats.detail.model.StadiumVisitCount
import kotlinx.datetime.LocalDateTime

fun FanRateByGameDto.toUiModel(): StadiumFanRateItem =
    StadiumFanRateItem(
        gameId = gameId,
        awayTeamFanRate = awayTeam.toUiModel(),
        homeTeamFanRate = homeTeam.toUiModel(),
    )

fun TeamFanRateDto.toUiModel(): TeamFanRate =
    TeamFanRate(
        team = Team.getByCode(code),
        teamName = name,
        fanRate = fanRate,
    )

fun CheckInGameDto.toUiModel(): AttendanceHistoryItem =
    AttendanceHistoryItem(
        id = checkInId,
        gameState = GameState.from(gameState),
        dateTime = LocalDateTime(attendanceDate, startAt),
        stadiumName = stadiumFullName,
        awayTeam = awayTeam.toUiModel(homeTeam),
        homeTeam = homeTeam.toUiModel(awayTeam),
        awayTeamScoreBoard = awayScoreBoard.toUiModel(),
        homeTeamScoreBoard = homeScoreBoard.toUiModel(),
    )

fun CheckInGameTeamDto.toUiModel(opponent: CheckInGameTeamDto): GameTeam =
    GameTeam(
        team = Team.getByCode(code),
        name = name,
        score = score?.toString() ?: "-",
        isMyTeam = isMyTeam,
        gameResult =
            if (score == null || opponent.score == null) {
                GameResult.DRAW
            } else {
                GameResult.from(score, opponent.score)
            },
    )

fun ScoreBoardDto.toUiModel(): GameScoreBoard =
    GameScoreBoard(
        runs = runs,
        hits = hits,
        errors = errors,
        basesOnBalls = basesOnBalls,
        scores = inningScores,
    )

fun StadiumCheckInCountDto.toUiModel(): StadiumVisitCount =
    StadiumVisitCount(
        location = location,
        visitCounts = checkInCounts,
    )

fun CheckInImageDto.toUiModel(): CheckInImageItem =
    CheckInImageItem(
        id = imageId,
        url = imageUrl,
    )
