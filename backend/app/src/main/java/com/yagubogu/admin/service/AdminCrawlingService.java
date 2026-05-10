package com.yagubogu.admin.service;

import com.yagubogu.admin.client.CrawlingAdminClient;
import com.yagubogu.admin.dto.AdminCrawlingGamesRequest;
import com.yagubogu.admin.dto.AdminCrawlingGamesResponse;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminCrawlingService {

    private final CrawlingAdminClient crawlingAdminClient;
    private final GameRepository gameRepository;

    public AdminCrawlingGamesResponse crawlGames(final AdminCrawlingGamesRequest request) {
        List<String> gameCodes = normalizeGameCodes(request.gameCodes());
        long sleepMillis = request.resolvedSleepMillis();
        long reviewRetryDelayMinutes = request.resolvedReviewRetryDelayMinutes();

        int saved = 0;
        int skipped = 0;
        int reviewSaved = 0;
        int reviewQueued = 0;
        List<String> failedGameCodes = new ArrayList<>();

        for (int index = 0; index < gameCodes.size(); index++) {
            String gameCode = gameCodes.get(index);
            if (gameRepository.existsByGameCode(gameCode)) {
                skipped++;
                continue;
            }

            try {
                crawlingAdminClient.fetchGames(List.of(gameCode));
                Game savedGame = gameRepository.findByGameCode(gameCode)
                        .orElseThrow(() -> new IllegalStateException("Game was not saved: " + gameCode));
                saved++;

                if (savedGame.getGameState().isCompleted()) {
                    try {
                        crawlingAdminClient.fetchReview(gameCode);
                        reviewSaved++;
                    } catch (Exception reviewException) {
                        crawlingAdminClient.enqueueReviewRetry(gameCode, reviewRetryDelayMinutes);
                        reviewQueued++;
                        log.warn("[ADMIN_CRAWLING] Review crawl failed, queued retry: gameCode={}", gameCode,
                                reviewException);
                    }
                }
            } catch (Exception exception) {
                failedGameCodes.add(gameCode);
                log.error("[ADMIN_CRAWLING] Game crawl failed: gameCode={}", gameCode, exception);
            }

            sleepBetweenCalls(index, gameCodes.size(), sleepMillis);
        }

        return new AdminCrawlingGamesResponse(
                gameCodes.size(),
                saved,
                skipped,
                reviewSaved,
                reviewQueued,
                failedGameCodes.size(),
                failedGameCodes
        );
    }

    private List<String> normalizeGameCodes(final List<String> gameCodes) {
        return gameCodes.stream()
                .map(String::trim)
                .filter(gameCode -> !gameCode.isBlank())
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        ArrayList::new
                ));
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
