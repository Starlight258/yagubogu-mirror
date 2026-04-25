package com.yagubogu.game.dto;

public record HitterRecordParam(
        int battingOrder,
        String position,
        String playerName,
        int atBats,
        int hits,
        int rbi,
        int runs
) {
}
