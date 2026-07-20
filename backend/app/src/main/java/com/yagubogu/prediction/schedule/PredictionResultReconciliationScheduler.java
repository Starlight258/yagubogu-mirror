package com.yagubogu.prediction.schedule;

import com.yagubogu.prediction.service.PredictionResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PredictionResultReconciliationScheduler {

    private final PredictionResultService predictionResultService;

    @Scheduled(cron = "0 0 23 * * SUN", zone = "Asia/Seoul")
    public void reconcileUngradedPredictions() {
        try {
            log.info("[PREDICTION] Weekly prediction result reconciliation triggered by scheduler");
            predictionResultService.reconcileUngradedPredictions();
        } catch (RuntimeException e) {
            log.error("[PREDICTION] Weekly prediction result reconciliation failed", e);
            throw e;
        }
    }
}
