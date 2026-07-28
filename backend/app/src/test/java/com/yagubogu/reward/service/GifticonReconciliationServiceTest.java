package com.yagubogu.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yagubogu.reward.client.GiftOrderLookupResult;
import com.yagubogu.reward.client.GiftOrderLookupUncertainException;
import com.yagubogu.reward.client.GiftOrderStatusClient;
import com.yagubogu.reward.client.GiftOrderVendorStatus;
import com.yagubogu.reward.config.GifticonReconciliationProperties;
import com.yagubogu.reward.domain.GifticonIssuance;
import com.yagubogu.reward.domain.GifticonIssuanceStatus;
import com.yagubogu.reward.domain.RecipientPhoneNumber;
import com.yagubogu.reward.dto.GifticonReconciliationTarget;
import com.yagubogu.reward.repository.GifticonIssuanceRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class GifticonReconciliationServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 28, 3, 0);

    @Mock
    private GifticonIssuanceRepository gifticonIssuanceRepository;

    @Mock
    private GiftOrderStatusClient giftOrderStatusClient;

    @Mock
    private TransactionTemplate transactionTemplate;

    private GifticonReconciliationService service;
    private GifticonIssuance issuance;

    @BeforeEach
    void setUp() {
        GifticonReconciliationProperties properties = new GifticonReconciliationProperties(
                Duration.ofMinutes(1),
                20,
                Duration.ofHours(6)
        );
        Clock clock = Clock.fixed(Instant.parse("2026-07-28T03:00:00Z"), ZoneOffset.UTC);
        service = new GifticonReconciliationService(
                gifticonIssuanceRepository,
                giftOrderStatusClient,
                new GifticonReconciliationBackoffPolicy(properties),
                properties,
                transactionTemplate,
                clock
        );
        issuance = requestInProgress("order-id");
        executeTransactionsImmediately();
    }

    @DisplayName("설정한 배치 크기로 대사 시각이 지난 발급 건을 조회한다")
    @Test
    void findDueIssuancesWithConfiguredBatchSize() {
        when(gifticonIssuanceRepository.findDueReconciliationTargets(any(), any()))
                .thenReturn(List.of());

        service.reconcileDueIssuances();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(gifticonIssuanceRepository).findDueReconciliationTargets(
                eq(NOW),
                pageableCaptor.capture()
        );
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
    }

    @DisplayName("외부 주문이 확인되면 접수 상태와 추적 번호를 복구한다")
    @Test
    void recoverAcceptedRequest() {
        prepareTarget(1L, "order-id", issuance);
        when(giftOrderStatusClient.findByExternalOrderId("order-id"))
                .thenReturn(new GiftOrderLookupResult.Found(123L, GiftOrderVendorStatus.ORDER_CREATED));

        service.reconcileDueIssuances();

        assertThat(issuance.getStatus()).isEqualTo(GifticonIssuanceStatus.REQUEST_ACCEPTED);
        assertThat(issuance.getReserveTraceId()).isEqualTo(123L);
        assertThat(issuance.getReconciliationAttemptCount()).isEqualTo(1);
        assertThat(issuance.getNextReconciliationAt()).isNull();
    }

    @DisplayName("외부 주문 생성 실패가 확인되면 다시 요청할 수 있는 상태로 전환한다")
    @Test
    void markCreationFailedRetryable() {
        prepareTarget(1L, "order-id", issuance);
        when(giftOrderStatusClient.findByExternalOrderId("order-id"))
                .thenReturn(new GiftOrderLookupResult.CreationFailed(
                        GiftOrderVendorStatus.INVALID_RECEIVER
                ));

        service.reconcileDueIssuances();

        assertThat(issuance.getStatus()).isEqualTo(GifticonIssuanceStatus.REQUEST_RETRYABLE);
        assertThat(issuance.getReconciliationAttemptCount()).isEqualTo(1);
        assertThat(issuance.getNextReconciliationAt()).isNull();
    }

    @DisplayName("외부 주문을 찾지 못하면 요청 중 상태를 유지하고 다음 조회를 예약한다")
    @Test
    void scheduleNextLookupWhenNotFound() {
        prepareTarget(1L, "order-id", issuance);
        when(giftOrderStatusClient.findByExternalOrderId("order-id"))
                .thenReturn(new GiftOrderLookupResult.NotFound());

        service.reconcileDueIssuances();

        assertThat(issuance.getStatus()).isEqualTo(GifticonIssuanceStatus.REQUEST_IN_PROGRESS);
        assertThat(issuance.getReconciliationAttemptCount()).isEqualTo(1);
        assertThat(issuance.getLastReconciledAt()).isEqualTo(NOW);
        assertThat(issuance.getNextReconciliationAt()).isEqualTo(NOW.plusMinutes(1));
    }

    @DisplayName("조회 결과를 판단할 수 없으면 요청 중 상태를 유지하고 다음 조회를 예약한다")
    @Test
    void scheduleNextLookupWhenUncertain() {
        prepareTarget(1L, "order-id", issuance);
        when(giftOrderStatusClient.findByExternalOrderId("order-id"))
                .thenThrow(new GiftOrderLookupUncertainException("lookup timeout"));

        service.reconcileDueIssuances();

        assertThat(issuance.getStatus()).isEqualTo(GifticonIssuanceStatus.REQUEST_IN_PROGRESS);
        assertThat(issuance.getReconciliationAttemptCount()).isEqualTo(1);
        assertThat(issuance.getNextReconciliationAt()).isEqualTo(NOW.plusMinutes(1));
        assertThat(issuance.getLastReconciliationError()).isEqualTo("lookup timeout");
    }

    @DisplayName("외부 조회 중 상태가 바뀌면 늦게 도착한 결과를 반영하지 않는다")
    @Test
    void ignoreLateResultAfterStatusChanged() {
        issuance.markRequestAccepted(10L, NOW.minusSeconds(1));
        prepareTarget(1L, "order-id", issuance);
        when(giftOrderStatusClient.findByExternalOrderId("order-id"))
                .thenReturn(new GiftOrderLookupResult.Found(123L, GiftOrderVendorStatus.ORDER_CREATED));

        service.reconcileDueIssuances();

        assertThat(issuance.getStatus()).isEqualTo(GifticonIssuanceStatus.REQUEST_ACCEPTED);
        assertThat(issuance.getReserveTraceId()).isEqualTo(10L);
        assertThat(issuance.getReconciliationAttemptCount()).isZero();
    }

    @DisplayName("한 발급 건의 조회 실패가 다음 발급 건의 대사를 막지 않는다")
    @Test
    void continueAfterTargetFailure() {
        GifticonIssuance nextIssuance = requestInProgress("order-next");
        when(gifticonIssuanceRepository.findDueReconciliationTargets(any(), any()))
                .thenReturn(List.of(
                        target(1L, "order-id"),
                        target(2L, "order-next")
                ));
        when(giftOrderStatusClient.findByExternalOrderId("order-id"))
                .thenThrow(new IllegalStateException("unexpected failure"));
        when(giftOrderStatusClient.findByExternalOrderId("order-next"))
                .thenReturn(new GiftOrderLookupResult.Found(
                        456L,
                        GiftOrderVendorStatus.ORDER_CREATED
                ));
        when(gifticonIssuanceRepository.findById(2L)).thenReturn(Optional.of(nextIssuance));

        service.reconcileDueIssuances();

        assertThat(nextIssuance.getStatus()).isEqualTo(GifticonIssuanceStatus.REQUEST_ACCEPTED);
        assertThat(nextIssuance.getReserveTraceId()).isEqualTo(456L);
    }

    @DisplayName("한 발급 건의 낙관적 락 충돌이 다음 발급 건의 대사를 막지 않는다")
    @Test
    void continueAfterOptimisticLockConflict() {
        GifticonIssuance nextIssuance = requestInProgress("order-next");
        when(gifticonIssuanceRepository.findDueReconciliationTargets(any(), any()))
                .thenReturn(List.of(
                        target(1L, "order-id"),
                        target(2L, "order-next")
                ));
        when(giftOrderStatusClient.findByExternalOrderId("order-id"))
                .thenReturn(new GiftOrderLookupResult.Found(
                        123L,
                        GiftOrderVendorStatus.ORDER_CREATED
                ));
        when(giftOrderStatusClient.findByExternalOrderId("order-next"))
                .thenReturn(new GiftOrderLookupResult.Found(
                        456L,
                        GiftOrderVendorStatus.ORDER_CREATED
                ));
        when(gifticonIssuanceRepository.findById(2L)).thenReturn(Optional.of(nextIssuance));
        org.mockito.Mockito.doThrow(
                        new ObjectOptimisticLockingFailureException(GifticonIssuance.class, 1L)
                )
                .doAnswer(invocation -> {
                    Consumer<TransactionStatus> callback = invocation.getArgument(0);
                    callback.accept(null);
                    return null;
                })
                .when(transactionTemplate)
                .executeWithoutResult(any());

        service.reconcileDueIssuances();

        assertThat(nextIssuance.getStatus()).isEqualTo(GifticonIssuanceStatus.REQUEST_ACCEPTED);
        assertThat(nextIssuance.getReserveTraceId()).isEqualTo(456L);
    }

    private void prepareTarget(
            final long issuanceId,
            final String externalOrderId,
            final GifticonIssuance targetIssuance
    ) {
        when(gifticonIssuanceRepository.findDueReconciliationTargets(any(), any()))
                .thenReturn(List.of(target(issuanceId, externalOrderId)));
        when(gifticonIssuanceRepository.findById(issuanceId))
                .thenReturn(Optional.of(targetIssuance));
    }

    private GifticonReconciliationTarget target(final long id, final String externalOrderId) {
        return new GifticonReconciliationTarget(id, externalOrderId, NOW.minusMinutes(1), 0);
    }

    private GifticonIssuance requestInProgress(final String externalOrderId) {
        GifticonIssuance targetIssuance =
                new GifticonIssuance(null, null, externalOrderId, NOW.minusHours(1));
        targetIssuance.prepareRequest(
                new RecipientPhoneNumber("01012345678"),
                NOW.minusMinutes(2)
        );
        targetIssuance.scheduleInitialReconciliation(
                NOW.minusMinutes(2),
                NOW.minusMinutes(1),
                null
        );
        return targetIssuance;
    }

    private void executeTransactionsImmediately() {
        org.mockito.Mockito.lenient().doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }
}
