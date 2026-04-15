package com.yagubogu.checkin.dto;

import com.yagubogu.game.domain.ScoreBoard;
import java.time.LocalDate;
import java.util.List;

public record CheckInGameParam(
        Long checkInId,
        String stadiumFullName,
        CheckInGameTeamParam homeTeam,
        CheckInGameTeamParam awayTeam,
        LocalDate attendanceDate,
        ScoreBoard homeScoreBoard,
        ScoreBoard awayScoreBoard,
        String memo,
        List<String> imageUrls
) {
    public CheckInGameParam(
            Long checkInId,
            String stadiumFullName,
            CheckInGameTeamParam homeTeam,
            CheckInGameTeamParam awayTeam,
            LocalDate attendanceDate,
            ScoreBoard homeScoreBoard,
            ScoreBoard awayScoreBoard,
            String memo
    ) {
        this(checkInId, stadiumFullName, homeTeam, awayTeam, attendanceDate, homeScoreBoard, awayScoreBoard, memo, List.of());
    }
}

