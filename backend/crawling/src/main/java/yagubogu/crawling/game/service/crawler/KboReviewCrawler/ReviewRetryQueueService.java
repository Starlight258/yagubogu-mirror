package yagubogu.crawling.game.service.crawler.KboReviewCrawler;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yagubogu.crawling.game.domain.ReviewCrawlRetry;
import yagubogu.crawling.game.domain.ReviewRetryStatus;
import yagubogu.crawling.game.repository.ReviewCrawlRetryRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewRetryQueueService {

    private final ReviewCrawlRetryRepository retryRepository;
    private final Clock clock;

    @Transactional
    public boolean enqueue(final String gameCode, final long delayMinutes) {
        if (retryRepository.existsByGameCodeAndStatus(gameCode, ReviewRetryStatus.PENDING)) {
            log.info("[REVIEW] 이미 예약된 리뷰 크롤링 스킵: gameCode={}", gameCode);
            return false;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime nextRetryAt = now.plusMinutes(delayMinutes);
        retryRepository.save(ReviewCrawlRetry.of(gameCode, nextRetryAt, now));
        log.info("[REVIEW] 리뷰 크롤링 예약: gameCode={}, nextRetryAt={}", gameCode, nextRetryAt);
        return true;
    }
}
