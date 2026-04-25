package com.yagubogu.stat.dto.v1;

import com.yagubogu.stat.dto.LocationCheckInRankingParam;
import java.util.List;

public record LocationCheckInRankingCursorResponse(
        LocationCheckInRankingResponse myRanking,
        List<LocationCheckInRankingResponse> rankings,
        Long nextCursorId,
        boolean hasNext
) {

    public static LocationCheckInRankingCursorResponse from(
            final LocationCheckInRankingResponse myRanking,
            final List<LocationCheckInRankingParam> rankingParams,
            final Long nextCursorId,
            final boolean hasNext
    ) {
        List<LocationCheckInRankingResponse> rankings = rankingParams.stream()
                .map(LocationCheckInRankingResponse::from)
                .toList();

        return new LocationCheckInRankingCursorResponse(
                myRanking,
                rankings,
                nextCursorId,
                hasNext
        );
    }
}
