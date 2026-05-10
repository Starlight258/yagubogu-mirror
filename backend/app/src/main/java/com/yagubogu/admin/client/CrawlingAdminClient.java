package com.yagubogu.admin.client;

import com.yagubogu.admin.dto.CrawlingGameCodesRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Component
public class CrawlingAdminClient {

    @Qualifier("crawlingRestClient")
    private final RestClient crawlingRestClient;

    public void fetchGames(final List<String> gameCodes) {
        crawlingRestClient.post()
                .uri("/api/kbo/games/by-codes")
                .body(new CrawlingGameCodesRequest(gameCodes))
                .retrieve()
                .toBodilessEntity();
    }

    public void fetchReview(final String gameCode) {
        crawlingRestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/kbo/review")
                        .queryParam("gameCode", gameCode)
                        .build())
                .retrieve()
                .toBodilessEntity();
    }

    public void enqueueReviewRetry(final String gameCode, final long delayMinutes) {
        crawlingRestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/kbo/review/retries")
                        .queryParam("gameCode", gameCode)
                        .queryParam("delayMinutes", delayMinutes)
                        .build())
                .retrieve()
                .toBodilessEntity();
    }
}
