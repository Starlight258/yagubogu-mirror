package com.yagubogu.reward.service;

import com.yagubogu.prediction.service.PredictionResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WeeklyRewardDrawBatchService {

    private final PredictionResultService predictionResultService;
    private final WeeklyRewardDrawService weeklyRewardDrawService;

    public WeeklyRewardDrawResult reconcilePredictionsAndDrawWinners() {
        predictionResultService.reconcileUngradedPredictions();
        return weeklyRewardDrawService.drawWinnersForLastCompletedWeek();
    }
}
