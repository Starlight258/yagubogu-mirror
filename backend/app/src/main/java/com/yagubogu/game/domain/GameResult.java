package com.yagubogu.game.domain;

public enum GameResult {
    HOME_WIN,
    AWAY_WIN,
    DRAW,
    ;

    public static GameResult of(final int homeScore, final int awayScore) {
        if (homeScore > awayScore) {
            return HOME_WIN;
        }
        if (awayScore > homeScore) {
            return AWAY_WIN;
        }
        return DRAW;
    }
}
