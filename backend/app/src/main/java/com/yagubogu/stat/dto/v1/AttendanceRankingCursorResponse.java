package com.yagubogu.stat.dto.v1;

import com.yagubogu.stat.dto.AttendanceRankingParam;
import com.yagubogu.talk.dto.CursorResultParam;
import java.util.List;

public record AttendanceRankingCursorResponse(CursorResultParam<AttendanceRankingResponse> cursorResult) {

    public static AttendanceRankingCursorResponse from(
            final List<AttendanceRankingParam> rankingParams,
            final Long nextCursorId,
            final boolean hasNext
    ) {
        List<AttendanceRankingResponse> rankings = rankingParams.stream()
                .map(AttendanceRankingResponse::from)
                .toList();

        return new AttendanceRankingCursorResponse(
                new CursorResultParam<>(rankings, nextCursorId, hasNext)
        );
    }
}
