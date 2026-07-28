package com.yagubogu.reward.service;

import com.yagubogu.reward.client.GiftOrderLookupResult;
import com.yagubogu.reward.client.GiftOrderLookupUncertainException;
import com.yagubogu.reward.client.GiftOrderStatusClient;
import com.yagubogu.reward.config.GifticonReconciliationProperties;
import com.yagubogu.reward.domain.GifticonIssuance;
import com.yagubogu.reward.domain.GifticonIssuanceStatus;
import com.yagubogu.reward.dto.GifticonReconciliationTarget;
import com.yagubogu.reward.repository.GifticonIssuanceRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 요청 진행 중인 기프티콘 발급 건을 외부 주문과 대사해 상태를 보정한다.
 *
 * <p>외부 조회는 DB 트랜잭션 밖에서 수행한다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GifticonReconciliationService {

    private final GifticonIssuanceRepository gifticonIssuanceRepository;
    private final GiftOrderStatusClient giftOrderStatusClient;
    private final GifticonReconciliationBackoffPolicy backoffPolicy;
    private final GifticonReconciliationProperties properties;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    /**
     * 대사 시각이 지난 발급 건을 제한된 개수만 조회해 각각 처리한다.
     */
    public void reconcileDueIssuances() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<GifticonReconciliationTarget> targets =
                gifticonIssuanceRepository.findDueReconciliationTargets(
                        now,
                        PageRequest.of(0, properties.batchSize())
                );
        for (GifticonReconciliationTarget target : targets) {
            reconcileSafely(target);
        }
    }

    /**
     * 한 발급 건의 실패가 같은 배치의 다른 발급 건 처리를 막지 않게 분리한다.
     */
    private void reconcileSafely(final GifticonReconciliationTarget target) {
        try {
            reconcile(target);
        } catch (ObjectOptimisticLockingFailureException exception) {
            log.warn(
                    "Gifticon reconciliation result was not applied due to concurrent update: issuanceId={}",
                    target.id()
            );
        } catch (RuntimeException exception) {
            log.error(
                    "Gifticon reconciliation failed: issuanceId={}, externalOrderId={}",
                    target.id(),
                    target.externalOrderId(),
                    exception
            );
        }
    }

    /**
     * 외부 주문을 조회한 뒤 확인된 결과만 짧은 트랜잭션으로 반영한다.
     */
    private void reconcile(final GifticonReconciliationTarget target) {
        try {
            GiftOrderLookupResult result =
                    giftOrderStatusClient.findByExternalOrderId(target.externalOrderId());
            applyLookupResult(target.id(), result);
        } catch (GiftOrderLookupUncertainException exception) {
            recordUncertainResult(target.id(), exception.getMessage());
        }
    }

    private void applyLookupResult(final long issuanceId, final GiftOrderLookupResult result) {
        switch (result) {
            case GiftOrderLookupResult.Found found -> updateIfInProgress(
                    issuanceId,
                    issuance -> issuance.recoverRequestAccepted(
                            found.reserveTraceId(),
                            LocalDateTime.now(clock)
                    )
            );
            case GiftOrderLookupResult.CreationFailed ignored -> updateIfInProgress(
                    issuanceId,
                    issuance -> issuance.markCreationFailedRetryable(LocalDateTime.now(clock))
            );
            case GiftOrderLookupResult.NotFound ignored ->
                    updateIfInProgress(issuanceId, issuance -> {
                        LocalDateTime now = LocalDateTime.now(clock);
                        issuance.recordReconciliationNotFound(
                                now,
                                nextReconciliationAt(issuance, now)
                        );
                    });
        }
    }

    private void recordUncertainResult(final long issuanceId, final String error) {
        updateIfInProgress(issuanceId, issuance -> {
            LocalDateTime now = LocalDateTime.now(clock);
            issuance.recordReconciliationUncertain(
                    now,
                    nextReconciliationAt(issuance, now),
                    error
            );
        });
    }

    /**
     * 외부 조회 중 상태가 이미 바뀐 발급 건에는 늦게 도착한 결과를 반영하지 않는다.
     */
    private void updateIfInProgress(
            final long issuanceId,
            final Consumer<GifticonIssuance> update
    ) {
        transactionTemplate.executeWithoutResult(status ->
                gifticonIssuanceRepository.findById(issuanceId)
                        .filter(issuance ->
                                issuance.getStatus() == GifticonIssuanceStatus.REQUEST_IN_PROGRESS)
                        .ifPresent(update)
        );
    }

    private LocalDateTime nextReconciliationAt(
            final GifticonIssuance issuance,
            final LocalDateTime now
    ) {
        int nextAttempt = issuance.getReconciliationAttemptCount() + 1;
        return now.plus(backoffPolicy.delayForAttempt(nextAttempt));
    }
}
