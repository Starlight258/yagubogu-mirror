package com.yagubogu.reward.schedule;

import static org.mockito.Mockito.verify;

import com.yagubogu.reward.service.GifticonReconciliationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GifticonReconciliationSchedulerTest {

    @Mock
    private GifticonReconciliationService gifticonReconciliationService;

    @InjectMocks
    private GifticonReconciliationScheduler scheduler;

    @DisplayName("대사 시각이 지난 기프티콘 발급 건 처리를 서비스에 위임한다")
    @Test
    void reconcileDueIssuances() {
        scheduler.reconcileDueIssuances();

        verify(gifticonReconciliationService).reconcileDueIssuances();
    }
}
