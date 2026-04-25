package yagubogu.crawling.game.config;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.KboGameCenterCrawler;
import yagubogu.crawling.game.service.crawler.KboReviewCrawler.KboReviewCrawler;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardCrawler;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "9m")
@EnableConfigurationProperties({
        KboCrawlerProperties.class,
        CrawlerSchedulerProperties.class,
        PerGameRetryProperties.class,
        GlobalBackoffProperties.class
})
public class KboCrawlerConfig {

    @Bean
    public PlaywrightManager playwrightManager() {
        return new PlaywrightManager();
    }

    @Bean
    public KboScoreboardCrawler kboScoreboardCrawler(
            final KboCrawlerProperties properties,
            final PlaywrightManager playwrightManager) {
        return new KboScoreboardCrawler(
                properties,
                playwrightManager
        );
    }

    @Bean
    public KboGameCenterCrawler kboGameCenterCrawler(
            final KboCrawlerProperties properties,
            final PlaywrightManager playwrightManager) {
        return new KboGameCenterCrawler(
                properties,
                playwrightManager
        );
    }

    @Bean
    public KboReviewCrawler kboReviewCrawler(
            final KboCrawlerProperties properties,
            final PlaywrightManager playwrightManager) {
        return new KboReviewCrawler(
                properties,
                playwrightManager
        );
    }
}
