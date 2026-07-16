package com.yagubogu.stat.schedule;

import com.yagubogu.stat.service.StatSyncService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StatScheduler {

    private final StatSyncService statSyncService;

    @Scheduled(cron = "0 0 3 * * *")
    public void updateVictoryRanking() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        triggerRankingUpdate(yesterday, "daily scheduler");
    }

    private void triggerRankingUpdate(final LocalDate targetDate, final String triggerSource) {
        try {
            log.info("[STAT] Update victory ranking triggered by {} for date {}", triggerSource, targetDate);
            statSyncService.updateRankings(targetDate);
        } catch (RuntimeException e) {
            log.error("[{}]- {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
