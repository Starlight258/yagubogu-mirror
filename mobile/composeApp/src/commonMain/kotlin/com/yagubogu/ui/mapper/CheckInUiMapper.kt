package com.yagubogu.ui.mapper

import com.yagubogu.data.dto.response.checkin.CheckInGameDto
import com.yagubogu.data.dto.response.checkin.CheckInGameTeamDto
import com.yagubogu.data.dto.response.checkin.CheckInImageDto
import com.yagubogu.data.dto.response.checkin.CheckInReviewResponse
import com.yagubogu.data.dto.response.checkin.FanRateByGameDto
import com.yagubogu.data.dto.response.checkin.HitterRecordDto
import com.yagubogu.data.dto.response.checkin.PitcherRecordDto
import com.yagubogu.data.dto.response.checkin.ScoreBoardDto
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountDto
import com.yagubogu.data.dto.response.checkin.TeamFanRateDto
import com.yagubogu.domain.model.GameResult
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.attendance.detail.model.CheckInImageItem
import com.yagubogu.ui.attendance.detail.model.PlayerRecordUiModel
import com.yagubogu.ui.attendance.model.AttendanceHistoryItem
import com.yagubogu.ui.attendance.model.GameScoreBoard
import com.yagubogu.ui.attendance.model.GameState
import com.yagubogu.ui.attendance.model.GameTeam
import com.yagubogu.ui.attendance.model.TeamType
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
        awayTeam = awayTeam.toUiModel(homeTeam, TeamType.AWAY),
        homeTeam = homeTeam.toUiModel(awayTeam, TeamType.HOME),
        awayTeamScoreBoard = awayScoreBoard.toUiModel(),
        homeTeamScoreBoard = homeScoreBoard.toUiModel(),
    )

fun CheckInGameTeamDto.toUiModel(
    opponent: CheckInGameTeamDto,
    type: TeamType,
): GameTeam =
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
        type = type,
    )

fun ScoreBoardDto.toUiModel(): GameScoreBoard =
    GameScoreBoard(
        runs = runs,
        hits = hits,
        errors = errors,
        basesOnBalls = basesOnBalls,
        scores = inningScores,
    )

fun CheckInReviewResponse.toUiModel(): PlayerRecordUiModel =
    PlayerRecordUiModel(
        awayTeamHitters = awayHitters.map { it.toUiModel() },
        awayTeamPitchers = awayPitchers.map { it.toUiModel() },
        homeTeamHitters = homeHitters.map { it.toUiModel() },
        homeTeamPitchers = homePitchers.map { it.toUiModel() },
    )

fun HitterRecordDto.toUiModel(): PlayerRecordUiModel.HitterRecord =
    PlayerRecordUiModel.HitterRecord(
        battingOrder = battingOrder,
        position = position,
        playerName = playerName,
        atBats = atBats,
        hits = hits,
        rbi = rbi,
        runs = runs,
    )

fun PitcherRecordDto.toUiModel(): PlayerRecordUiModel.PitcherRecord =
    PlayerRecordUiModel.PitcherRecord(
        playerName = playerName,
        result = result,
        innings = innings.toString(),
        pitchCount = pitchCount,
        hitsAllowed = hitsAllowed,
        walksAndHbp = walksAndHbp,
        strikeouts = strikeouts,
        runsAllowed = runsAllowed,
        earnedRuns = earnedRuns,
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
