package com.yagubogu.prediction.domain;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GamePredictionTest {

    @DisplayName("무승부 경기는 예측을 VOID로 채점한다")
    @Test
    void grade_marksVoid_whenDraw() {
        // given
        Game game = newCompletedGame(3, 3);
        GamePrediction prediction = new GamePrediction(null, game, PredictionPick.HOME);

        // when
        prediction.grade(game);

        // then
        assertThat(prediction.getStatus()).isEqualTo(PredictionStatus.VOID);
    }

    @DisplayName("승리 팀을 맞춘 예측은 WON으로 채점한다")
    @Test
    void grade_marksWon_whenPickMatchesWinner() {
        // given
        Game game = newCompletedGame(5, 3);
        GamePrediction prediction = new GamePrediction(null, game, PredictionPick.HOME);

        // when
        prediction.grade(game);

        // then
        assertThat(prediction.getStatus()).isEqualTo(PredictionStatus.WON);
    }

    @DisplayName("패배 팀을 고른 예측은 LOST로 채점한다")
    @Test
    void grade_marksLost_whenPickMismatchesWinner() {
        // given
        Game game = newCompletedGame(5, 3);
        GamePrediction prediction = new GamePrediction(null, game, PredictionPick.AWAY);

        // when
        prediction.grade(game);

        // then
        assertThat(prediction.getStatus()).isEqualTo(PredictionStatus.LOST);
    }

    @DisplayName("원정팀 승리를 맞춘 예측은 WON으로 채점한다")
    @Test
    void grade_marksWon_whenAwayPickMatchesAwayWin() {
        // given
        Game game = newCompletedGame(3, 5);
        GamePrediction prediction = new GamePrediction(null, game, PredictionPick.AWAY);

        // when
        prediction.grade(game);

        // then
        assertThat(prediction.getStatus()).isEqualTo(PredictionStatus.WON);
    }

    @DisplayName("경기가 취소되면 예측을 VOID로 채점한다")
    @Test
    void grade_marksVoid_whenCanceled() {
        // given
        Game game = newCanceledGame();
        GamePrediction prediction = new GamePrediction(null, game, PredictionPick.HOME);

        // when
        prediction.grade(game);

        // then
        assertThat(prediction.getStatus()).isEqualTo(PredictionStatus.VOID);
    }

    private Game newCompletedGame(final int homeScore, final int awayScore) {
        return new Game(
                null, null, null,
                LocalDate.now(), LocalTime.of(18, 30), "gameCode",
                homeScore, awayScore, null, null,
                null, null, GameState.COMPLETED
        );
    }

    private Game newCanceledGame() {
        return new Game(
                null, null, null,
                LocalDate.now(), LocalTime.of(18, 30), "gameCode",
                null, null, null, null,
                null, null, GameState.CANCELED
        );
    }
}
