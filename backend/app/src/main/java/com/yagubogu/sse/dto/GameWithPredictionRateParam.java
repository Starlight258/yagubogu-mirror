package com.yagubogu.sse.dto;

import com.yagubogu.game.domain.Game;
import com.yagubogu.prediction.dto.TeamPredictionRateParam;

public record GameWithPredictionRateParam(
        long gameId,
        TeamPredictionRateParam homeTeam,
        TeamPredictionRateParam awayTeam
) {

    public static GameWithPredictionRateParam from(
            final Game game, final double homeTeamRate, final double awayTeamRate
    ) {
        return new GameWithPredictionRateParam(
                game.getId(),
                TeamPredictionRateParam.from(game.getHomeTeam(), homeTeamRate),
                TeamPredictionRateParam.from(game.getAwayTeam(), awayTeamRate)
        );
    }
}
