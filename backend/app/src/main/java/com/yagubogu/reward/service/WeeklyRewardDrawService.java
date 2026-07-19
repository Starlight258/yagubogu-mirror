package com.yagubogu.reward.service;

import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.prediction.dto.WeeklyScoreParam;
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
    private final Clock clock;

    @Transactional
    public void drawWinners() {
        LocalDate weekStart = currentWeekStart();
        if (weeklyTopScoreRepository.existsByWeekStart(weekStart)) {
            return;
        }

        LocalDate weekEnd = weekStart.plusDays(6);
        List<WeeklyScoreParam> weeklyScores = predictionResultService.findWeeklyScores(weekStart, weekEnd);
        if (weeklyScores.isEmpty()) {
            return;
        }

        long topScore = weeklyScores.stream()
                .mapToLong(WeeklyScoreParam::score)
                .max()
                .orElseThrow();
        List<Long> winnerMemberIds = pickWinnerCandidates(weeklyScores, topScore);

        saveDraw(weekStart, (int) topScore, winnerMemberIds.stream().limit(WINNER_COUNT).toList());
    }

    private LocalDate currentWeekStart() {
        LocalDate today = LocalDate.now(clock);
        return today.minusDays(today.getDayOfWeek().getValue() - 1L);
    }

    private List<Long> pickWinnerCandidates(final List<WeeklyScoreParam> weeklyScores, final long topScore) {
        List<Long> candidates = weeklyScores.stream()
                .filter(score -> score.score() == topScore)
                .map(WeeklyScoreParam::memberId)
                .collect(Collectors.toList());
        Collections.shuffle(candidates, RANDOM);
        return candidates;
    }

    private void saveDraw(final LocalDate weekStart, final int topScore, final List<Long> winnerMemberIds) {
        LocalDateTime now = LocalDateTime.now(clock);
        WeeklyTopScore weeklyTopScore = weeklyTopScoreRepository.save(new WeeklyTopScore(weekStart, topScore, now));
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
