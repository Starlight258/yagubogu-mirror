package com.yagubogu.admin.dto;

import java.util.List;

public record CrawlingGameCodesRequest(
        List<String> gameCodes
) {
}
