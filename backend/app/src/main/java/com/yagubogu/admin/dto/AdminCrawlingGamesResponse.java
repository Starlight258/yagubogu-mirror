package com.yagubogu.admin.dto;

import java.util.List;

public record AdminCrawlingGamesResponse(
        int requested,
        int saved,
        int skipped,
        int reviewSaved,
        int reviewQueued,
        int failed,
        List<String> failedGameCodes
) {
}
