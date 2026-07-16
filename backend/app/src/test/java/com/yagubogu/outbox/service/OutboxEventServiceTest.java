package com.yagubogu.outbox.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.outbox.domain.OutboxEvent;
import com.yagubogu.outbox.domain.OutboxEventStatus;
import com.yagubogu.outbox.repository.OutboxEventRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OutboxEventServiceTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2025, 7, 21, 23, 30);
    private static final Clock CLOCK = Clock.fixed(NOW.atZone(ZONE_ID).toInstant(), ZONE_ID);

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private OutboxEventService outboxEventService;

    @BeforeEach
    void setUp() {
        outboxEventService = new OutboxEventService(outboxEventRepository, new ObjectMapper(), CLOCK);
    }

    @DisplayName("timeout된 PROCESSING 이벤트를 재시도 대상으로 복구한다")
    @Test
    void recoverTimedOutProcessingEvents_retry() {
        OutboxEvent event = processingEvent(1L, 0);
        when(outboxEventRepository.findTimedOutProcessingForUpdate(NOW.minusMinutes(10), 10))
                .thenReturn(List.of(event));

        int recoveredCount = outboxEventService.recoverTimedOutProcessingEvents(Duration.ofMinutes(10), 10);

        assertSoftly(softly -> {
            softly.assertThat(recoveredCount).isEqualTo(1);
            softly.assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
            softly.assertThat(event.getRetryCount()).isEqualTo(1);
            softly.assertThat(event.getNextRetryAt()).isEqualTo(NOW.plusMinutes(1));
            softly.assertThat(event.getLastError()).contains("OutboxProcessingTimeoutException");
            softly.assertThat(event.getUpdatedAt()).isEqualTo(NOW);
        });
    }

    @DisplayName("timeout 복구 후 최대 재시도 횟수에 도달하면 FAILED로 변경한다")
    @Test
    void recoverTimedOutProcessingEvents_failed() {
        OutboxEvent event = processingEvent(1L, 4);
        when(outboxEventRepository.findTimedOutProcessingForUpdate(NOW.minusMinutes(10), 10))
                .thenReturn(List.of(event));

        int recoveredCount = outboxEventService.recoverTimedOutProcessingEvents(Duration.ofMinutes(10), 10);

        assertSoftly(softly -> {
            softly.assertThat(recoveredCount).isEqualTo(1);
            softly.assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
            softly.assertThat(event.getRetryCount()).isEqualTo(5);
            softly.assertThat(event.getLastError()).contains("OutboxProcessingTimeoutException");
            softly.assertThat(event.getUpdatedAt()).isEqualTo(NOW);
        });
    }

    @DisplayName("실패 횟수에 따라 재시도 시간을 1분, 5분, 30분으로 단계적으로 증가시킨다")
    @Test
    void markFailed_retryInterval() {
        OutboxEvent firstFailureEvent = pendingEvent(1L, 0);
        OutboxEvent secondFailureEvent = pendingEvent(2L, 1);
        OutboxEvent thirdFailureEvent = pendingEvent(3L, 2);
        OutboxEvent fourthFailureEvent = pendingEvent(4L, 3);
        when(outboxEventRepository.findById(1L)).thenReturn(Optional.of(firstFailureEvent));
        when(outboxEventRepository.findById(2L)).thenReturn(Optional.of(secondFailureEvent));
        when(outboxEventRepository.findById(3L)).thenReturn(Optional.of(thirdFailureEvent));
        when(outboxEventRepository.findById(4L)).thenReturn(Optional.of(fourthFailureEvent));

        RuntimeException failure = new RuntimeException("temporary failure");
        outboxEventService.markFailed(1L, failure);
        outboxEventService.markFailed(2L, failure);
        outboxEventService.markFailed(3L, failure);
        outboxEventService.markFailed(4L, failure);

        assertSoftly(softly -> {
            softly.assertThat(firstFailureEvent.getRetryCount()).isEqualTo(1);
            softly.assertThat(firstFailureEvent.getNextRetryAt()).isEqualTo(NOW.plusMinutes(1));
            softly.assertThat(secondFailureEvent.getRetryCount()).isEqualTo(2);
            softly.assertThat(secondFailureEvent.getNextRetryAt()).isEqualTo(NOW.plusMinutes(5));
            softly.assertThat(thirdFailureEvent.getRetryCount()).isEqualTo(3);
            softly.assertThat(thirdFailureEvent.getNextRetryAt()).isEqualTo(NOW.plusMinutes(30));
            softly.assertThat(fourthFailureEvent.getRetryCount()).isEqualTo(4);
            softly.assertThat(fourthFailureEvent.getNextRetryAt()).isEqualTo(NOW.plusMinutes(30));
        });
    }

    @DisplayName("다섯 번째 실패부터는 FAILED로 변경하고 재시도 시각을 갱신하지 않는다")
    @Test
    void markFailed_exceedMaxRetry() {
        OutboxEvent event = pendingEvent(1L, 4);
        LocalDateTime beforeFailureNextRetryAt = event.getNextRetryAt();
        when(outboxEventRepository.findById(1L)).thenReturn(Optional.of(event));

        outboxEventService.markFailed(1L, new RuntimeException("permanent failure"));

        assertSoftly(softly -> {
            softly.assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
            softly.assertThat(event.getRetryCount()).isEqualTo(5);
            softly.assertThat(event.getNextRetryAt()).isEqualTo(beforeFailureNextRetryAt);
            softly.assertThat(event.getLastError()).contains("RuntimeException");
            softly.assertThat(event.getUpdatedAt()).isEqualTo(NOW);
        });
        verify(outboxEventRepository).findById(1L);
    }

    private OutboxEvent processingEvent(final Long id, final int retryCount) {
        OutboxEvent event = pendingEvent(id, retryCount);
        event.markProcessing(LocalDateTime.of(2025, 7, 21, 23, 0));
        return event;
    }

    private OutboxEvent pendingEvent(final Long id, final int retryCount) {
        OutboxEvent event = OutboxEvent.of(
                OutboxEventService.GAME_COMPLETED_EVENT_TYPE,
                "20250721HTLT0",
                "{}",
                LocalDateTime.ofInstant(Instant.parse("2025-07-21T13:00:00Z"), ZONE_ID)
        );
        ReflectionTestUtils.setField(event, "id", id);
        ReflectionTestUtils.setField(event, "retryCount", retryCount);
        return event;
    }
}
