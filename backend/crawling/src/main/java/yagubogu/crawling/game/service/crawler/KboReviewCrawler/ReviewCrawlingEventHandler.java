package yagubogu.crawling.game.service.crawler.KboReviewCrawler;

import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.dto.HitterRecordParam;
import com.yagubogu.game.dto.PitcherRecordParam;
import com.yagubogu.game.event.GameFinalizedEvent;
import com.yagubogu.game.service.GameReviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import yagubogu.crawling.game.dto.HitterRecordDto;
import yagubogu.crawling.game.dto.PitcherRecordDto;
import yagubogu.crawling.game.dto.ReviewData;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewCrawlingEventHandler {

    private final KboReviewCrawler kboReviewCrawler;
    private final GameReviewService gameReviewService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGameFinalized(final GameFinalizedEvent event) {
        if (event.state() == GameState.CANCELED) {
            log.debug("[REVIEW] 취소 경기 스킵: date={}, home={}", event.date(), event.homeTeam());
            return;
        }

        try {
            ReviewData reviewData = kboReviewCrawler.crawlReview(event.gameCode());

            gameReviewService.saveReviewData(
                    reviewData.gameCode(),
                    toHitterParams(reviewData.awayHitters()),
                    toHitterParams(reviewData.homeHitters()),
                    toPitcherParams(reviewData.awayPitchers()),
                    toPitcherParams(reviewData.homePitchers())
            );

            log.info("[REVIEW] 저장 완료: gameCode={}", reviewData.gameCode());
        } catch (Exception e) {
            log.error("[REVIEW] 크롤링 실패: date={}, away={}, home={}",
                    event.date(), event.awayTeam(), event.homeTeam(), e);
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
