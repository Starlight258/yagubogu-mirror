package com.yagubogu.reward.schedule;

import com.yagubogu.reward.service.WeeklyRewardDrawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WeeklyRewardDrawScheduler {

    private final WeeklyRewardDrawService weeklyRewardDrawService;

    @Scheduled(cron = "0 30 23 * * SUN", zone = "Asia/Seoul")
    public void drawWinners() {
        try {
            log.info("[REWARD] Weekly reward draw triggered by scheduler");
            weeklyRewardDrawService.drawWinners();
        } catch (RuntimeException e) {
            log.error("[REWARD] Weekly reward draw failed", e);
            throw e;
        }
    }
}
