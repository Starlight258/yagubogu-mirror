package com.yagubogu.stat.dto;

public interface AttendanceRankingParam {

    long getRanking();

    long getMemberId();

    int getCheckInCount();

    String getNickname();

    String getImageUrl();

    String getTeamShortName();
}
