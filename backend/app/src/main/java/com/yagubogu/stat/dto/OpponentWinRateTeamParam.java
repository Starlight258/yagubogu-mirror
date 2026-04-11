package com.yagubogu.stat.dto;

public record OpponentWinRateTeamParam(
        int rank,
        Long teamId,
        String name,
        String shortName,
        String teamCode,
        long wins,
        long losses,
        long draws,
        double winRate
) {
}
