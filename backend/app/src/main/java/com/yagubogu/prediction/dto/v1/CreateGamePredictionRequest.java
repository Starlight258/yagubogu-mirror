package com.yagubogu.prediction.dto.v1;

import com.yagubogu.prediction.domain.PredictionPick;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateGamePredictionRequest(
        @Positive long gameId,
        @NotNull PredictionPick pick
) {
}
