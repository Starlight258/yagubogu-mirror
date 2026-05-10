package com.yagubogu.admin.service;

import com.yagubogu.admin.client.CrawlingAdminClient;
import com.yagubogu.admin.dto.AdminCrawlingGamesRequest;
import com.yagubogu.admin.dto.AdminCrawlingGamesResponse;
import com.yagubogu.admin.dto.CrawlingGameDateResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminCrawlingService {

    private final CrawlingAdminClient crawlingAdminClient;

    public AdminCrawlingGamesResponse crawlGames(final AdminCrawlingGamesRequest request) {
        long sleepMillis = request.resolvedSleepMillis();
        long reviewRetryDelayMinutes = request.resolvedReviewRetryDelayMinutes();
        int reviewSaved = 0;
        int reviewQueued = 0;
        List<String> failedGameCodes = new ArrayList<>();

        CrawlingGameDateResponse crawlingResponse = crawlingAdminClient.fetchGames(request.date());
        List<String> completedGameCodes = crawlingResponse.completedGameCodes();
        for (int index = 0; index < completedGameCodes.size(); index++) {
            String gameCode = completedGameCodes.get(index);
            try {
                crawlingAdminClient.fetchReview(gameCode);
                reviewSaved++;
            } catch (Exception reviewException) {
                failedGameCodes.add(gameCode);
                crawlingAdminClient.enqueueReviewRetry(gameCode, reviewRetryDelayMinutes);
                reviewQueued++;
                log.warn("[ADMIN_CRAWLING] Review crawl failed, queued retry: gameCode={}", gameCode,
                        reviewException);
            }

            sleepBetweenCalls(index, completedGameCodes.size(), sleepMillis);
        }

        return new AdminCrawlingGamesResponse(
                crawlingResponse.requested(),
                crawlingResponse.saved(),
                crawlingResponse.skipped(),
                reviewSaved,
                reviewQueued,
                failedGameCodes.size(),
                crawlingResponse.savedGameCodes(),
                failedGameCodes
        );
    }

    private void sleepBetweenCalls(final int index, final int size, final long sleepMillis) {
        if (sleepMillis <= 0 || index >= size - 1) {
            return;
        }

        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Admin crawling sleep interrupted", exception);
        }
    }
}
