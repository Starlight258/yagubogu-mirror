package com.yagubogu.stat.dto.v1;

import com.yagubogu.stat.dto.AttendanceRankingParam;

public record AttendanceRankingResponse(
        long ranking,
        long memberId,
        int checkInCount,
        String nickname,
        String imageUrl,
        String teamShortName
) {

    public static AttendanceRankingResponse from(final AttendanceRankingParam param) {
        return new AttendanceRankingResponse(
                param.getRanking(),
                param.getMemberId(),
                param.getCheckInCount(),
                param.getNickname(),
                param.getImageUrl(),
                param.getTeamShortName()
        );
    }
}
