package com.yagubogu.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AdminCrawlingGamesRequest(
        @NotNull
        LocalDate date,

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
