package com.yagubogu.game.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameResultTest {

    @DisplayName("홈팀 점수가 높으면 HOME_WIN을 반환한다.")
    @Test
    void of_returnsHomeWin_whenHomeScoreIsHigher() {
        assertThat(GameResult.of(5, 3)).isEqualTo(GameResult.HOME_WIN);
    }

    @DisplayName("원정팀 점수가 높으면 AWAY_WIN을 반환한다.")
    @Test
    void of_returnsAwayWin_whenAwayScoreIsHigher() {
        assertThat(GameResult.of(3, 5)).isEqualTo(GameResult.AWAY_WIN);
    }

    @DisplayName("점수가 같으면 DRAW를 반환한다.")
    @Test
    void of_returnsDraw_whenScoresAreEqual() {
        assertThat(GameResult.of(3, 3)).isEqualTo(GameResult.DRAW);
    }
}
