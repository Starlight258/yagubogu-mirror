package com.yagubogu.stat.dto.v1;

import com.yagubogu.member.domain.Member;
import com.yagubogu.stat.dto.LocationCheckInRankingParam;

public record LocationCheckInRankingResponse(
        long ranking,
        long memberId,
        int checkInCount,
        String nickname,
        String imageUrl,
        String teamShortName
) {

    public static LocationCheckInRankingResponse from(final LocationCheckInRankingParam param) {
        return new LocationCheckInRankingResponse(
                param.getRanking(),
                param.getMemberId(),
                param.getCheckInCount(),
                param.getNickname(),
                param.getImageUrl(),
                param.getTeamShortName()
        );
    }

    public static LocationCheckInRankingResponse emptyRanking(final Member member) {
        String teamShortName = member.getTeam() == null ? null : member.getTeam().getShortName();

        return new LocationCheckInRankingResponse(
                0,
                member.getId(),
                0,
                member.getNickname().toString(),
                member.getImageUrl(),
                teamShortName
        );
    }
}
