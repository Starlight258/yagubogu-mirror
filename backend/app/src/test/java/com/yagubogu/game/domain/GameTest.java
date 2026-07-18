package com.yagubogu.game.domain;

import com.yagubogu.game.exception.InvalidGameStateException;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    @DisplayName("경기를 COMPLETED로 전환할 때 점수가 없으면 예외가 발생한다.")
    @Test
    void updateGameState_throwsException_whenCompletedWithoutScores() {
        // when & then
        assertThatThrownBy(() -> newGame(null, null, GameState.COMPLETED))
                .isInstanceOf(InvalidGameStateException.class);
    }

    @DisplayName("경기를 COMPLETED로 전환할 때 점수가 있으면 정상적으로 상태가 변경된다.")
    @Test
    void updateGameState_succeeds_whenCompletedWithScores() {
        // when
        Game game = newGame(5, 3, GameState.COMPLETED);

        // then
        assertThat(game.getGameState()).isEqualTo(GameState.COMPLETED);
    }

    @DisplayName("경기를 CANCELED로 전환할 때는 점수가 없어도 예외가 발생하지 않는다.")
    @Test
    void updateGameState_succeeds_whenCanceledWithoutScores() {
        // when
        Game game = newGame(null, null, GameState.CANCELED);

        // then
        assertThat(game.getGameState()).isEqualTo(GameState.CANCELED);
    }

    @DisplayName("홈팀 점수가 높으면 HOME_WIN을 반환한다.")
    @Test
    void getResult_returnsHomeWin_whenHomeScoreIsHigher() {
        // when
        Game game = newGame(5, 3, GameState.COMPLETED);

        // then
        assertThat(game.getResult()).isEqualTo(GameResult.HOME_WIN);
    }

    @DisplayName("원정팀 점수가 높으면 AWAY_WIN을 반환한다.")
    @Test
    void getResult_returnsAwayWin_whenAwayScoreIsHigher() {
        // when
        Game game = newGame(3, 5, GameState.COMPLETED);

        // then
        assertThat(game.getResult()).isEqualTo(GameResult.AWAY_WIN);
    }

    @DisplayName("점수가 같으면 DRAW를 반환한다.")
    @Test
    void getResult_returnsDraw_whenScoresAreEqual() {
        // when
        Game game = newGame(3, 3, GameState.COMPLETED);

        // then
        assertThat(game.getResult()).isEqualTo(GameResult.DRAW);
    }

    private Game newGame(final Integer homeScore, final Integer awayScore, final GameState gameState) {
        return new Game(
                null, null, null,
                LocalDate.now(), LocalTime.of(18, 30), "gameCode",
                homeScore, awayScore, null, null,
                null, null, gameState
        );
    }
}
