package yagubogu.crawling.game.service.crawler.KboReviewCrawler;

import com.yagubogu.game.dto.HitterRecordParam;
import com.yagubogu.game.dto.PitcherRecordParam;
import com.yagubogu.game.service.GameReviewService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yagubogu.crawling.game.domain.ReviewCrawlRetry;
import yagubogu.crawling.game.domain.ReviewRetryStatus;
import yagubogu.crawling.game.dto.HitterRecordDto;
import yagubogu.crawling.game.dto.PitcherRecordDto;
import yagubogu.crawling.game.dto.ReviewData;
import yagubogu.crawling.game.repository.ReviewCrawlRetryRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewRetryScheduler {

    private static final int MAX_RETRY = 6;
    private static final long RETRY_INTERVAL_MINUTES = 30;

    private final ReviewCrawlRetryRepository retryRepository;
    private final KboReviewCrawler kboReviewCrawler;
    private final GameReviewService gameReviewService;
    private final Clock clock;

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    @SchedulerLock(name = "processReviewRetryQueue", lockAtLeastFor = "30s")
    public void processRetryQueue() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<ReviewCrawlRetry> dueItems = retryRepository.findByStatusAndNextRetryAtLessThanEqual(
                ReviewRetryStatus.PENDING, now);

        if (dueItems.isEmpty()) {
            return;
        }

        log.info("[REVIEW-RETRY] 처리 대상: {}건", dueItems.size());

        for (ReviewCrawlRetry item : dueItems) {
            try {
                ReviewData reviewData = kboReviewCrawler.crawlReview(item.getGameCode());
                gameReviewService.saveReviewData(
                        reviewData.gameCode(),
                        toHitterParams(reviewData.awayHitters()),
                        toHitterParams(reviewData.homeHitters()),
                        toPitcherParams(reviewData.awayPitchers()),
                        toPitcherParams(reviewData.homePitchers())
                );
                item.markSuccess();
                retryRepository.save(item);
                log.info("[REVIEW-RETRY] 성공: gameCode={}, retryCount={}",
                        item.getGameCode(), item.getRetryCount());
            } catch (Exception e) {
                int nextRetryCount = item.getRetryCount() + 1;
                if (nextRetryCount >= MAX_RETRY) {
                    item.markFailed();
                    retryRepository.save(item);
                    log.error("[REVIEW-RETRY] 최대 재시도 횟수({}) 초과, 포기: gameCode={}",
                            MAX_RETRY, item.getGameCode(), e);
                } else {
                    item.scheduleNextRetry(now.plusMinutes(RETRY_INTERVAL_MINUTES));
                    retryRepository.save(item);
                    log.warn("[REVIEW-RETRY] 실패, 재시도 예약: gameCode={}, retryCount={}/{}, nextRetryAt={}",
                            item.getGameCode(), nextRetryCount, MAX_RETRY, item.getNextRetryAt());
                }
            }
        }
    }

    private List<HitterRecordParam> toHitterParams(final List<HitterRecordDto> dtos) {
        return dtos.stream()
                .map(d -> new HitterRecordParam(
                        d.battingOrder(), d.position(), d.playerName(),
                        d.atBats(), d.hits(), d.rbi(), d.runs()))
                .toList();
    }

    private List<PitcherRecordParam> toPitcherParams(final List<PitcherRecordDto> dtos) {
        return dtos.stream()
                .map(d -> new PitcherRecordParam(
                        d.playerName(), d.result(), d.innings(),
                        d.battersFaced(), d.pitchCount(), d.atBats(),
                        d.hitsAllowed(), d.homeRunsAllowed(), d.walksAndHbp(),
                        d.strikeouts(), d.runsAllowed(), d.earnedRuns()))
                .toList();
    }
}
