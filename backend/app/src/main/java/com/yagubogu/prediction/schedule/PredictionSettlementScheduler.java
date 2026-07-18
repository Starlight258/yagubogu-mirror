package com.yagubogu.prediction.schedule;

import com.yagubogu.prediction.service.PredictionSettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PredictionSettlementScheduler {

    private final PredictionSettlementService predictionSettlementService;

    @Scheduled(cron = "0 0 23 * * SUN", zone = "Asia/Seoul")
    public void settleWeeklyGames() {
        try {
            log.info("[PREDICTION] Weekly prediction settlement triggered by scheduler");
            predictionSettlementService.settlePendingGames();
        } catch (RuntimeException e) {
            log.error("[PREDICTION] Weekly prediction settlement failed", e);
            throw e;
        }
    }
}
