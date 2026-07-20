package com.yagubogu.reward.service;

import com.yagubogu.global.exception.ConflictException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.prediction.domain.PredictionStatus;
import com.yagubogu.prediction.dto.WeeklyScoreParam;
import com.yagubogu.prediction.repository.GamePredictionRepository;
import com.yagubogu.prediction.service.PredictionResultService;
import com.yagubogu.reward.domain.GifticonIssuance;
import com.yagubogu.reward.domain.WeeklyTopScore;
import com.yagubogu.reward.repository.GifticonIssuanceRepository;
import com.yagubogu.reward.repository.WeeklyTopScoreRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class WeeklyRewardDrawService {

    private static final int WINNER_COUNT = 3;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final WeeklyTopScoreRepository weeklyTopScoreRepository;
    private final GifticonIssuanceRepository gifticonIssuanceRepository;
    private final PredictionResultService predictionResultService;
    private final MemberRepository memberRepository;
    private final GamePredictionRepository gamePredictionRepository;
    private final Clock clock;

    @Transactional
    public WeeklyRewardDrawResult drawWinnersForLastCompletedWeek() {
        LocalDate monday = lastCompletedWeekMonday();
        return attemptToDrawWinnersForWeek(monday);
    }

    @Transactional
    public void drawWinnersForWeek(final LocalDate monday) {
        WeeklyRewardDrawResult result = attemptToDrawWinnersForWeek(monday);
        switch (result) {
            case ALREADY_DRAWN -> throw new ConflictException("Winners have already been drawn: monday=" + monday);
            case UNGRADED_PREDICTIONS_EXIST ->
                    throw new UnprocessableEntityException("Ungraded predictions remain: monday=" + monday);
            case DRAWN, NO_PARTICIPANTS -> {
            }
        }
    }

    private LocalDate lastCompletedWeekMonday() {
        LocalDate today = LocalDate.now(clock);
        LocalDate mostRecentSunday = today.minusDays(today.getDayOfWeek().getValue() % 7);
        return mostRecentSunday.minusDays(6);
    }

    private boolean hasAlreadyDrawnWinners(final LocalDate monday) {
        return weeklyTopScoreRepository.existsByWeekStart(monday);
    }

    private boolean hasUngradedPredictions(final LocalDate monday) {
        LocalDate sunday = monday.plusDays(6);
        return gamePredictionRepository.existsByStatusAndGame_DateBetween(
                PredictionStatus.SUBMITTED, monday, sunday);
    }

    private WeeklyRewardDrawResult attemptToDrawWinnersForWeek(final LocalDate monday) {
        if (hasAlreadyDrawnWinners(monday)) {
            return WeeklyRewardDrawResult.ALREADY_DRAWN;
        }
        if (hasUngradedPredictions(monday)) {
            return WeeklyRewardDrawResult.UNGRADED_PREDICTIONS_EXIST;
        }

        LocalDate sunday = monday.plusDays(6);
        List<WeeklyScoreParam> weeklyScores = predictionResultService.findWeeklyScores(monday, sunday);
        if (weeklyScores.isEmpty()) {
            return WeeklyRewardDrawResult.NO_PARTICIPANTS;
        }

        long topScore = weeklyScores.stream()
                .mapToLong(WeeklyScoreParam::score)
                .max()
                .orElseThrow();
        List<Long> winnerMemberIds = pickWinnerCandidates(weeklyScores, topScore);

        saveDraw(monday, (int) topScore, winnerMemberIds.stream().limit(WINNER_COUNT).toList());
        return WeeklyRewardDrawResult.DRAWN;
    }

    private List<Long> pickWinnerCandidates(final List<WeeklyScoreParam> weeklyScores, final long topScore) {
        List<Long> candidates = weeklyScores.stream()
                .filter(score -> score.score() == topScore)
                .map(WeeklyScoreParam::memberId)
                .collect(Collectors.toList());
        Collections.shuffle(candidates, RANDOM);
        return candidates;
    }

    private void saveDraw(final LocalDate monday, final int topScore, final List<Long> winnerMemberIds) {
        LocalDateTime now = LocalDateTime.now(clock);
        WeeklyTopScore weeklyTopScore = weeklyTopScoreRepository.save(new WeeklyTopScore(monday, topScore, now));
        for (Long memberId : winnerMemberIds) {
            Member member = getMember(memberId);
            GifticonIssuance issuance = new GifticonIssuance(
                    weeklyTopScore, member, UUID.randomUUID().toString(), now);
            gifticonIssuanceRepository.save(issuance);
        }
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }
}
