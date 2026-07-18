package com.yagubogu.prediction.dto.v1;

import com.yagubogu.prediction.domain.GamePrediction;
import com.yagubogu.prediction.domain.PredictionPick;
import com.yagubogu.prediction.domain.PredictionStatus;

public record GamePredictionResponse(
        Long id,
        Long gameId,
        PredictionPick pick,
        PredictionStatus status
) {

    public static GamePredictionResponse from(final GamePrediction gamePrediction) {
        return new GamePredictionResponse(
                gamePrediction.getId(),
                gamePrediction.getGame().getId(),
                gamePrediction.getPick(),
                gamePrediction.getStatus()
        );
    }
}
