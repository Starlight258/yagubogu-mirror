package com.yagubogu.admin.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdminCrawlingGamesRequest(
        @NotNull
        @Min(1982)
        @Max(2100)
        Integer startYear,

        @NotNull
        @Min(1982)
        @Max(2100)
        Integer endYear,

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

    @AssertTrue(message = "startYear must be less than or equal to endYear")
    public boolean isValidYearRange() {
        return startYear == null || endYear == null || startYear <= endYear;
    }
}
