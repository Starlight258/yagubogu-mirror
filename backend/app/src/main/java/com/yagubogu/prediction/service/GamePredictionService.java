package com.yagubogu.prediction.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.prediction.domain.GamePrediction;
import com.yagubogu.prediction.dto.v1.CreateGamePredictionRequest;
import com.yagubogu.prediction.dto.v1.GamePredictionResponse;
import com.yagubogu.prediction.dto.v1.UpdateGamePredictionRequest;
import com.yagubogu.prediction.repository.GamePredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GamePredictionService {

    private final GamePredictionRepository gamePredictionRepository;
    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public GamePredictionResponse submitPrediction(final Long memberId, final CreateGamePredictionRequest request) {
        Member member = getMember(memberId);
        Game game = getGame(request.gameId());

        GamePrediction gamePrediction = gamePredictionRepository.save(
                new GamePrediction(member, game, request.pick())
        );

        return GamePredictionResponse.from(gamePrediction);
    }

    @Transactional
    public GamePredictionResponse updatePrediction(final Long memberId, final UpdateGamePredictionRequest request) {
        Member member = getMember(memberId);
        Game game = getGame(request.gameId());

        GamePrediction gamePrediction = gamePredictionRepository.findByMemberAndGame(member, game)
                .orElseThrow(() -> new NotFoundException("GamePrediction is not found"));
        gamePrediction.updatePick(request.pick());

        return GamePredictionResponse.from(gamePrediction);
    }

    public GamePredictionResponse findPrediction(final Long memberId, final Long gameId) {
        Member member = getMember(memberId);
        Game game = getGame(gameId);

        GamePrediction gamePrediction = gamePredictionRepository.findByMemberAndGame(member, game)
                .orElseThrow(() -> new NotFoundException("GamePrediction is not found"));

        return GamePredictionResponse.from(gamePrediction);
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }

    private Game getGame(final Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game is not found"));
    }
}
