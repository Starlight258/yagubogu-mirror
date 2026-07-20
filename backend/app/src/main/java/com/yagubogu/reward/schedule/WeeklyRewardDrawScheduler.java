package com.yagubogu.reward.schedule;

import com.yagubogu.reward.service.WeeklyRewardDrawResult;
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
            WeeklyRewardDrawResult result = weeklyRewardDrawService.drawWinnersForLastCompletedWeek();
            logResult(result);
        } catch (RuntimeException e) {
            log.error("[REWARD] Weekly reward draw failed", e);
            throw e;
        }
    }

    private void logResult(final WeeklyRewardDrawResult result) {
        switch (result) {
            case DRAWN -> log.info("[REWARD] Weekly reward draw completed");
            case ALREADY_DRAWN -> log.info("[REWARD] Weekly reward draw skipped: winners already drawn");
            case UNGRADED_PREDICTIONS_EXIST ->
                    log.warn("[REWARD] Weekly reward draw skipped: ungraded predictions remain");
            case NO_PARTICIPANTS -> log.info("[REWARD] Weekly reward draw skipped: no participants");
        }
    }
}
