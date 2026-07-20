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
    public void gradePredictionsForGame(final String gameCode) {
        Game game = getGame(gameCode);
        validateFinalized(game);
        gradePredictions(game);
    }

    @Transactional
    public void reconcileUngradedPredictions() {
        List<Game> gamesWithUngradedPredictions =
                gamePredictionRepository.findFinalizedGamesWithUngradedPredictions(
                PredictionStatus.SUBMITTED, GameState.finalizedStates()
        );

        for (Game game : gamesWithUngradedPredictions) {
            gradePredictions(game);
        }
    }

    public List<WeeklyScoreParam> findWeeklyScores(final LocalDate monday, final LocalDate sunday) {
        return gamePredictionRepository.findWeeklyScores(PredictionStatus.WON, monday, sunday);
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

    private void gradePredictions(final Game game) {
        List<GamePrediction> predictions = gamePredictionRepository.findAllByGame(game);
        for (GamePrediction prediction : predictions) {
            prediction.grade(game);
        }
    }
}
