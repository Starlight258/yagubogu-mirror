package com.yagubogu.checkin.dto;

import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import java.time.LocalDate;
import java.util.List;
import java.time.LocalTime;

public record CheckInGameParam(
        Long checkInId,
        String stadiumFullName,
        CheckInGameTeamParam homeTeam,
        CheckInGameTeamParam awayTeam,
        LocalDate attendanceDate,
        LocalTime startAt,
        ScoreBoard homeScoreBoard,
        ScoreBoard awayScoreBoard,
        GameState gameState,
        String memo,
        List<String> imageUrls
) {
    public CheckInGameParam(
            Long checkInId,
            String stadiumFullName,
            CheckInGameTeamParam homeTeam,
            CheckInGameTeamParam awayTeam,
            LocalDate attendanceDate,
            LocalTime startAt,
            ScoreBoard homeScoreBoard,
            ScoreBoard awayScoreBoard,
            GameState gameState,
            String memo
    ) {
        this(checkInId, stadiumFullName, homeTeam, awayTeam, attendanceDate, startAt, homeScoreBoard, awayScoreBoard, gameState, memo, List.of());
    }
}
