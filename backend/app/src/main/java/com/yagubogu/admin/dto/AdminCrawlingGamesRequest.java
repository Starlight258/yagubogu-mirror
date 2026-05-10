package com.yagubogu.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AdminCrawlingGamesRequest(
        @NotEmpty
        List<String> gameCodes,

        @Min(0)
        @Max(60000)
        Long sleepMillis,

        @Min(0)
        @Max(1440)
        Long reviewRetryDelayMinutes
) {
    public long resolvedSleepMillis() {
        return sleepMillis == null ? 3000L : sleepMillis;
    }

    public long resolvedReviewRetryDelayMinutes() {
        return reviewRetryDelayMinutes == null ? 30L : reviewRetryDelayMinutes;
    }
}
