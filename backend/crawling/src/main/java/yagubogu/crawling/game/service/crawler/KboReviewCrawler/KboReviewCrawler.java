package yagubogu.crawling.game.service.crawler.KboReviewCrawler;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.config.KboCrawlerProperties;
import yagubogu.crawling.game.config.PlaywrightManager;
import yagubogu.crawling.game.dto.HitterRecordDto;
import yagubogu.crawling.game.dto.PitcherRecordDto;
import yagubogu.crawling.game.dto.ReviewData;
import yagubogu.crawling.game.service.crawler.page.KboReviewPage;

@Slf4j
public class KboReviewCrawler {

    private final KboCrawlerProperties properties;
    private final PlaywrightManager playwrightManager;

    public KboReviewCrawler(final KboCrawlerProperties properties,
                            final PlaywrightManager playwrightManager) {
        this.properties = properties;
        this.playwrightManager = playwrightManager;
    }

    public ReviewData crawlReview(final String gameCode) {
        log.info("[REVIEW] 크롤링 시작: gameCode={}", gameCode);

        return playwrightManager.withPage(page -> {
            KboReviewPage reviewPage = new KboReviewPage(page, properties, gameCode);
            reviewPage.navigateTo();

            List<HitterRecordDto> awayHitters = reviewPage.extractHitterRecords(
                    "tblAwayHitter1", "tblAwayHitter3");
            List<HitterRecordDto> homeHitters = reviewPage.extractHitterRecords(
                    "tblHomeHitter1", "tblHomeHitter3");
            List<PitcherRecordDto> awayPitchers = reviewPage.extractPitcherRecords("tblAwayPitcher");
            List<PitcherRecordDto> homePitchers = reviewPage.extractPitcherRecords("tblHomePitcher");

            log.info("[REVIEW] 크롤링 완료: gameCode={}", gameCode);
            return new ReviewData(gameCode, awayHitters, homeHitters, awayPitchers, homePitchers);
        });
    }
}
