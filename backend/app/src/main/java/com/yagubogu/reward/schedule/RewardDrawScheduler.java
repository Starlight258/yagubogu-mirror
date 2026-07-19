package com.yagubogu.reward.schedule;

import com.yagubogu.reward.service.RewardDrawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RewardDrawScheduler {

    private final RewardDrawService rewardDrawService;

    @Scheduled(cron = "0 30 23 * * SUN", zone = "Asia/Seoul")
    public void drawWeeklyReward() {
        try {
            log.info("[REWARD] Weekly reward draw triggered by scheduler");
            rewardDrawService.drawWeeklyReward();
        } catch (RuntimeException e) {
            log.error("[REWARD] Weekly reward draw failed", e);
            throw e;
        }
    }
}
