package com.yagubogu.reward.schedule;

import com.yagubogu.reward.service.GifticonReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 대사 시각이 지난 기프티콘 발급 건을 주기적으로 처리한다.
 */
@RequiredArgsConstructor
@Component
public class GifticonReconciliationScheduler {

    private final GifticonReconciliationService gifticonReconciliationService;

    /**
     * 설정된 간격마다 기프티콘 주문 대사를 실행한다.
     */
    @Scheduled(
            fixedDelayString = "${reward.gifticon.reconciliation.scheduler-delay:1m}",
            initialDelayString = "${reward.gifticon.reconciliation.scheduler-initial-delay:1m}"
    )
    public void reconcileDueIssuances() {
        gifticonReconciliationService.reconcileDueIssuances();
    }
}
