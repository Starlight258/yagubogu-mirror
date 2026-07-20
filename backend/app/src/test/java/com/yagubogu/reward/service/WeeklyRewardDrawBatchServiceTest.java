package com.yagubogu.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.yagubogu.prediction.service.PredictionResultService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeeklyRewardDrawBatchServiceTest {

    @Mock
    private PredictionResultService predictionResultService;

    @Mock
    private WeeklyRewardDrawService weeklyRewardDrawService;

    @InjectMocks
    private WeeklyRewardDrawBatchService weeklyRewardDrawBatchService;

    @DisplayName("미채점 예측을 보정한 후 주간 당첨자를 추첨한다")
    @Test
    void reconcilePredictionsAndDrawWinners_reconcilesBeforeDraw() {
        // given
        when(weeklyRewardDrawService.drawWinnersForLastCompletedWeek())
                .thenReturn(WeeklyRewardDrawResult.DRAWN);

        // when
        WeeklyRewardDrawResult result = weeklyRewardDrawBatchService.reconcilePredictionsAndDrawWinners();

        // then
        InOrder inOrder = inOrder(predictionResultService, weeklyRewardDrawService);
        inOrder.verify(predictionResultService).reconcileUngradedPredictions();
        inOrder.verify(weeklyRewardDrawService).drawWinnersForLastCompletedWeek();
        assertThat(result).isEqualTo(WeeklyRewardDrawResult.DRAWN);
    }

    @DisplayName("미채점 예측 보정에 실패하면 주간 당첨자를 추첨하지 않는다")
    @Test
    void reconcilePredictionsAndDrawWinners_doesNotDrawWhenReconciliationFails() {
        // given
        RuntimeException failure = new RuntimeException("reconciliation failed");
        doThrow(failure).when(predictionResultService).reconcileUngradedPredictions();

        // when & then
        assertThatThrownBy(weeklyRewardDrawBatchService::reconcilePredictionsAndDrawWinners)
                .isSameAs(failure);
        verifyNoInteractions(weeklyRewardDrawService);
    }
}
