package com.yagubogu.prediction.schedule;

import com.yagubogu.prediction.service.PredictionResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PredictionResultScheduler {

    private final PredictionResultService predictionResultService;

    @Scheduled(cron = "0 0 23 * * SUN", zone = "Asia/Seoul")
    public void finalizePendingPredictions() {
        try {
            log.info("[PREDICTION] Weekly prediction result finalization triggered by scheduler");
            predictionResultService.finalizePendingPredictions();
        } catch (RuntimeException e) {
            log.error("[PREDICTION] Weekly prediction result finalization failed", e);
            throw e;
        }
    }
}
