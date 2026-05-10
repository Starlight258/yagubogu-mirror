package yagubogu.crawling.game.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record GameDateCrawlRequest(
        @NotNull
        LocalDate date
) {
}
