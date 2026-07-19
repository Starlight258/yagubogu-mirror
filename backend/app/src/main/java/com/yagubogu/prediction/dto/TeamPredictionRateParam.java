package com.yagubogu.prediction.dto;

import com.yagubogu.team.domain.Team;

public record TeamPredictionRateParam(
        String name,
        String code,
        double predictionRate
) {

    public static TeamPredictionRateParam from(Team team, double rate) {
        return new TeamPredictionRateParam(
                team.getShortName(),
                team.getTeamCode(),
                rate
        );
    }
}
