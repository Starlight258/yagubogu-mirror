package yagubogu.crawling.game.dto;

public record GameCodeCrawlResponse(
        int requested,
        int matched,
        int saved,
        int transformed
) {
}
