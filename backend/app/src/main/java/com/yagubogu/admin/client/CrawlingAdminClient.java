package com.yagubogu.admin.client;

import com.yagubogu.admin.dto.CrawlingGameDateRequest;
import com.yagubogu.admin.dto.CrawlingGameDateResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Component
public class CrawlingAdminClient {

    @Qualifier("crawlingRestClient")
    private final RestClient crawlingRestClient;

    public CrawlingGameDateResponse fetchGames(final LocalDate date) {
        return crawlingRestClient.post()
                .uri("/api/kbo/games/by-date")
                .body(new CrawlingGameDateRequest(date))
                .retrieve()
                .body(CrawlingGameDateResponse.class);
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
