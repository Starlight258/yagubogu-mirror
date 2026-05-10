package yagubogu.crawling.game.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record GameCodeCrawlRequest(
        @NotEmpty
        List<String> gameCodes
) {
}
