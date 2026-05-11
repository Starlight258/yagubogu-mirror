package com.yagubogu.admin.dto;

import java.util.List;

public record CrawlingGameDateResponse(
        int requested,
        int matched,
        int saved,
        int skipped,
        int transformed,
        List<String> savedGameCodes,
        List<String> completedGameCodes
) {
}
