package com.yagubogu.stat.dto.v1;

import com.yagubogu.member.domain.Member;
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

    public static AttendanceRankingResponse emptyRanking(final Member member) {
        String teamShortName = member.getTeam() == null ? null : member.getTeam().getShortName();

        return new AttendanceRankingResponse(
                0,
                member.getId(),
                0,
                member.getNickname().toString(),
                member.getImageUrl(),
                teamShortName
        );
    }
}
