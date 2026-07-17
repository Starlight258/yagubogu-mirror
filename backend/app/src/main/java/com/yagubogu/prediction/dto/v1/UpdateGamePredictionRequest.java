package com.yagubogu.prediction.dto.v1;

import com.yagubogu.prediction.domain.PredictionPick;

public record UpdateGamePredictionRequest(
        long gameId,
        PredictionPick pick
) {
}
