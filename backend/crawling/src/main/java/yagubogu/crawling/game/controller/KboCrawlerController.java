package yagubogu.crawling.game.controller;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

import com.yagubogu.game.dto.HitterRecordParam;
import com.yagubogu.game.dto.PitcherRecordParam;
import com.yagubogu.game.service.GameReviewService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yagubogu.crawling.game.dto.GameDateCrawlRequest;
import yagubogu.crawling.game.dto.GameDateCrawlResponse;
import yagubogu.crawling.game.dto.ReviewData;
import yagubogu.crawling.game.dto.ScoreboardResponse;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.GameCenterSyncService;
import yagubogu.crawling.game.service.crawler.KboReviewCrawler.KboReviewCrawler;
import yagubogu.crawling.game.service.crawler.KboReviewCrawler.ReviewRetryQueueService;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardService;

@RequiredArgsConstructor
@RestController
public class KboCrawlerController implements KboCrawlerControllerInterface {

    private final KboScoreboardService kboScoreboardService;
    private final GameCenterSyncService gameCenterSyncService;
    private final KboReviewCrawler kboReviewCrawler;
    private final GameReviewService gameReviewService;
    private final ReviewRetryQueueService reviewRetryQueueService;

    @Override
    public ResponseEntity<List<ScoreboardResponse>> fetchScoreboardRange(
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate endDate
    ) {
        List<ScoreboardResponse> responses = kboScoreboardService.fetchScoreboardRange(startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<Integer> fetchGameCenter(
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate date
    ) {
        int savedCount = gameCenterSyncService.fetchGameCenter(date);
        return ResponseEntity.ok(savedCount);
    }

    @Override
    public ResponseEntity<GameDateCrawlResponse> fetchGamesByDate(@RequestBody final GameDateCrawlRequest request) {
        GameDateCrawlResponse response = kboScoreboardService.fetchGamesByDate(request.date());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> fetchReview(@RequestParam String gameCode) {
        ReviewData reviewData = kboReviewCrawler.crawlReview(gameCode);

        List<HitterRecordParam> awayHitters = reviewData.awayHitters().stream()
                .map(d -> new HitterRecordParam(d.battingOrder(), d.position(), d.playerName(),
                        d.atBats(), d.hits(), d.rbi(), d.runs()))
                .toList();
        List<HitterRecordParam> homeHitters = reviewData.homeHitters().stream()
                .map(d -> new HitterRecordParam(d.battingOrder(), d.position(), d.playerName(),
                        d.atBats(), d.hits(), d.rbi(), d.runs()))
                .toList();
        List<PitcherRecordParam> awayPitchers = reviewData.awayPitchers().stream()
                .map(d -> new PitcherRecordParam(d.playerName(), d.result(), d.innings(),
                        d.battersFaced(), d.pitchCount(), d.atBats(), d.hitsAllowed(),
                        d.homeRunsAllowed(), d.walksAndHbp(), d.strikeouts(),
                        d.runsAllowed(), d.earnedRuns()))
                .toList();
        List<PitcherRecordParam> homePitchers = reviewData.homePitchers().stream()
                .map(d -> new PitcherRecordParam(d.playerName(), d.result(), d.innings(),
                        d.battersFaced(), d.pitchCount(), d.atBats(), d.hitsAllowed(),
                        d.homeRunsAllowed(), d.walksAndHbp(), d.strikeouts(),
                        d.runsAllowed(), d.earnedRuns()))
                .toList();

        gameReviewService.saveReviewData(reviewData.gameCode(),
                awayHitters, homeHitters, awayPitchers, homePitchers);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> enqueueReviewRetry(@RequestParam final String gameCode,
                                                   @RequestParam(defaultValue = "30") final long delayMinutes) {
        reviewRetryQueueService.enqueue(gameCode, delayMinutes);
        return ResponseEntity.ok().build();
    }
}
