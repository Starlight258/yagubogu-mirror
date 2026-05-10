package yagubogu.crawling.game.dto;

import java.util.List;

public record GameDateCrawlResponse(
        int requested,
        int matched,
        int saved,
        int skipped,
        int transformed,
        List<String> savedGameCodes,
        List<String> completedGameCodes
) {
}
