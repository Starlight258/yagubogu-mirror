package com.yagubogu.prediction.dto;

import com.yagubogu.game.domain.Game;

public record GameWithPredictionCountsParam(
        Game game,
        Long totalPredictionCounts,
        Long homePredictionCounts,
        Long awayPredictionCounts
) {
}
