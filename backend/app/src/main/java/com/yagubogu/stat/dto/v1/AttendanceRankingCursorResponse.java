package com.yagubogu.stat.dto.v1;

import com.yagubogu.stat.dto.AttendanceRankingParam;
import java.util.List;

public record AttendanceRankingCursorResponse(
        AttendanceRankingResponse myRanking,
        List<AttendanceRankingResponse> rankings,
        Long nextCursorId,
        boolean hasNext
) {

    public static AttendanceRankingCursorResponse from(
            final AttendanceRankingResponse myRanking,
            final List<AttendanceRankingParam> rankingParams,
            final Long nextCursorId,
            final boolean hasNext
    ) {
        List<AttendanceRankingResponse> rankings = rankingParams.stream()
                .map(AttendanceRankingResponse::from)
                .toList();

        return new AttendanceRankingCursorResponse(
                myRanking,
                rankings,
                nextCursorId,
                hasNext
        );
    }
}
