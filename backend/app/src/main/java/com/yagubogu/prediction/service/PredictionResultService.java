package com.yagubogu.prediction.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.prediction.domain.GamePrediction;
import com.yagubogu.prediction.domain.PredictionStatus;
import com.yagubogu.prediction.dto.WeeklyScoreParam;
import com.yagubogu.prediction.repository.GamePredictionRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PredictionResultService {

    private final GamePredictionRepository gamePredictionRepository;
    private final GameRepository gameRepository;

    @Transactional
    public void finalizePendingPredictions() {
        List<Game> pendingGames = gamePredictionRepository.findFinalizedGamesWithPendingPredictions(
                PredictionStatus.SUBMITTED, GameState.finalizedStates()
        );

        for (Game game : pendingGames) {
            updatePredictionResults(game);
        }
    }

    /**
     * 경기 결과 정정 등으로 이미 확정된 예측(WON/LOST/VOID)까지 다시 계산해야 할 때
     * gameCode로 경기 하나만 지정해 결과를 다시 계산한다. finalizePendingPredictions()와
     * 달리 SUBMITTED 예측이 남아있지 않은 경기도 대상이 될 수 있다.
     */
    @Transactional
    public void recalculateGamePredictionResults(final String gameCode) {
        Game game = getGame(gameCode);
        validateFinalized(game);
        updatePredictionResults(game);
    }

    public List<WeeklyScoreParam> findWeeklyScores(final LocalDate weekStart, final LocalDate weekEnd) {
        return gamePredictionRepository.findWeeklyScores(PredictionStatus.WON, weekStart, weekEnd);
    }

    private Game getGame(final String gameCode) {
        return gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new NotFoundException("Game is not found: gameCode=" + gameCode));
    }

    private void validateFinalized(final Game game) {
        if (!game.getGameState().isFinalized()) {
            throw new UnprocessableEntityException(
                    "Game is not finalized: gameCode=" + game.getGameCode() + ", state=" + game.getGameState());
        }
    }

    private void updatePredictionResults(final Game game) {
        List<GamePrediction> predictions = gamePredictionRepository.findAllByGame(game);
        for (GamePrediction prediction : predictions) {
            prediction.updateResult(game);
        }
    }
}
