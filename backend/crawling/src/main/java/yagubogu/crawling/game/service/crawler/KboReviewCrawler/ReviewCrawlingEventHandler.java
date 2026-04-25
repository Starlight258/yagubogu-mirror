package yagubogu.crawling.game.service.crawler.KboReviewCrawler;

import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.event.GameFinalizedEvent;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import yagubogu.crawling.game.domain.ReviewCrawlRetry;
import yagubogu.crawling.game.repository.ReviewCrawlRetryRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewCrawlingEventHandler {

    private static final long INITIAL_DELAY_MINUTES = 30;

    private final ReviewCrawlRetryRepository retryRepository;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGameFinalized(final GameFinalizedEvent event) {
        if (event.state() == GameState.CANCELED) {
            log.debug("[REVIEW] 취소 경기 스킵: date={}, home={}", event.date(), event.homeTeam());
            return;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime nextRetryAt = now.plusMinutes(INITIAL_DELAY_MINUTES);
        retryRepository.save(ReviewCrawlRetry.of(event.gameCode(), nextRetryAt, now));
        log.info("[REVIEW] 리뷰 크롤링 예약: gameCode={}, nextRetryAt={}", event.gameCode(), nextRetryAt);
    }
}
