package com.yagubogu.prediction.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.ConflictException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.prediction.domain.GamePrediction;
import com.yagubogu.prediction.domain.PredictionPick;
import com.yagubogu.prediction.dto.GameWithPredictionCountsParam;
import com.yagubogu.prediction.dto.v1.CreateGamePredictionRequest;
import com.yagubogu.prediction.dto.v1.GamePredictionResponse;
import com.yagubogu.prediction.dto.v1.UpdateGamePredictionRequest;
import com.yagubogu.prediction.repository.GamePredictionRepository;
import com.yagubogu.sse.dto.GameWithPredictionRateParam;
import com.yagubogu.sse.dto.event.PredictionCreatedEvent;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GamePredictionService {

    private static final double ROUND_FACTOR = 10.0;

    private final GamePredictionRepository gamePredictionRepository;
    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Clock clock;

    @Transactional
    public GamePredictionResponse submitPrediction(final Long memberId, final CreateGamePredictionRequest request) {
        Member member = getMember(memberId);
        Game game = getGame(request.gameId());

        validateBeforeClose(game);
        validateNotExists(member, game);
        GamePrediction gamePrediction = savePredictionSafely(member, game, request.pick());

        applicationEventPublisher.publishEvent(new PredictionCreatedEvent());

        return GamePredictionResponse.from(gamePrediction);
    }

    @Transactional
    public GamePredictionResponse updatePrediction(final Long memberId, final UpdateGamePredictionRequest request) {
        Member member = getMember(memberId);
        Game game = getGame(request.gameId());

        validateBeforeClose(game);
        GamePrediction gamePrediction = gamePredictionRepository.findByMemberAndGame(member, game)
                .orElseThrow(() -> new NotFoundException("GamePrediction is not found"));
        gamePrediction.updatePick(request.pick());

        applicationEventPublisher.publishEvent(new PredictionCreatedEvent());

        return GamePredictionResponse.from(gamePrediction);
    }

    public GamePredictionResponse findPrediction(final Long memberId, final Long gameId) {
        Member member = getMember(memberId);
        Game game = getGame(gameId);

        GamePrediction gamePrediction = gamePredictionRepository.findByMemberAndGame(member, game)
                .orElseThrow(() -> new NotFoundException("GamePrediction is not found"));

        return GamePredictionResponse.from(gamePrediction);
    }

    public List<GameWithPredictionRateParam> buildPredictionEventData() {
        LocalDate today = LocalDate.now(clock);
        List<GameWithPredictionRateParam> result = new ArrayList<>();

        List<GameWithPredictionCountsParam> responses =
                gamePredictionRepository.findGamesWithPredictionCountsByDate(today);
        for (GameWithPredictionCountsParam response : responses) {
            Game game = response.game();
            long homeCounts = response.homePredictionCounts();
            long awayCounts = response.awayPredictionCounts();
            long totalCounts = response.totalPredictionCounts();

            double homeRate = calculateRoundRate(homeCounts, totalCounts);
            double awayRate = calculateRoundRate(awayCounts, totalCounts);
            result.add(GameWithPredictionRateParam.from(game, homeRate, awayRate));
        }

        return result;
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }

    private Game getGame(final Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game is not found"));
    }

    private void validateBeforeClose(final Game game) {
        LocalDateTime closesAt = game.getDate().atTime(game.getStartAt());
        if (!LocalDateTime.now(clock).isBefore(closesAt)) {
            throw new UnprocessableEntityException("Prediction is closed after game start");
        }
    }

    private void validateNotExists(final Member member, final Game game) {
        if (gamePredictionRepository.existsByMemberAndGame(member, game)) {
            throw new ConflictException("GamePrediction is already exists");
        }
    }

    private GamePrediction savePredictionSafely(final Member member, final Game game, final PredictionPick pick) {
        try {
            return gamePredictionRepository.save(new GamePrediction(member, game, pick));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("GamePrediction is already exists");
        }
    }

    private double calculateRoundRate(final long counts, final long total) {
        if (total == 0) {
            return 0.0;
        }

        return Math.round(((double) counts / total) * 1000) / ROUND_FACTOR;
    }
}
