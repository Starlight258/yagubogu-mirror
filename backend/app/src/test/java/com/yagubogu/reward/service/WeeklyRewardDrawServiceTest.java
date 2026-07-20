package com.yagubogu.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.prediction.repository.GamePredictionRepository;
import com.yagubogu.prediction.service.PredictionResultService;
import com.yagubogu.reward.repository.GifticonIssuanceRepository;
import com.yagubogu.reward.repository.WeeklyTopScoreRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class WeeklyRewardDrawServiceTest {

    @Mock
    private WeeklyTopScoreRepository weeklyTopScoreRepository;

    @Mock
    private GifticonIssuanceRepository gifticonIssuanceRepository;

    @Mock
    private PredictionResultService predictionResultService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GamePredictionRepository gamePredictionRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    private WeeklyRewardDrawService weeklyRewardDrawService;
    private LocalDate monday;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-20T00:00:00Z"), ZoneOffset.UTC);
        monday = LocalDate.of(2026, 7, 13);
        weeklyRewardDrawService = new WeeklyRewardDrawService(
                weeklyTopScoreRepository,
                gifticonIssuanceRepository,
                predictionResultService,
                memberRepository,
                gamePredictionRepository,
                clock,
                transactionTemplate
        );
    }

    @DisplayName("동시 추첨의 중복 저장 예외는 이미 추첨된 결과로 처리한다")
    @Test
    void drawWinners_absorbsDuplicateDrawRace() {
        // given
        when(transactionTemplate.execute(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate week_start"));
        when(weeklyTopScoreRepository.existsByWeekStart(monday)).thenReturn(true);

        // when
        WeeklyRewardDrawResult result = weeklyRewardDrawService.drawWinnersForLastCompletedWeek();

        // then
        assertThat(result).isEqualTo(WeeklyRewardDrawResult.ALREADY_DRAWN);
    }

    @DisplayName("추첨 기록이 없다면 무결성 예외를 중복 추첨으로 오인하지 않는다")
    @Test
    void drawWinners_rethrowsUnrelatedIntegrityViolation() {
        // given
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("unrelated integrity violation");
        when(transactionTemplate.execute(any())).thenThrow(exception);
        when(weeklyTopScoreRepository.existsByWeekStart(monday)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> weeklyRewardDrawService.drawWinnersForLastCompletedWeek())
                .isSameAs(exception);
    }
}
