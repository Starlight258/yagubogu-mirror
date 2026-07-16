package com.yagubogu.outbox.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yagubogu.outbox.domain.OutboxEvent;
import com.yagubogu.outbox.dto.GameCompletedOutboxPayload;
import com.yagubogu.outbox.exception.UnsupportedOutboxEventTypeException;
import com.yagubogu.outbox.service.OutboxEventService;
import com.yagubogu.stat.service.StatSyncService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OutboxEventProcessorTest {

    private static final int BATCH_SIZE = 10;

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private StatSyncService statSyncService;

    private ObjectMapper objectMapper;
    private OutboxEventProcessor outboxEventProcessor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        outboxEventProcessor = new OutboxEventProcessor(outboxEventService, objectMapper, statSyncService, BATCH_SIZE);
    }

    @DisplayName("GAME_COMPLETED outbox를 처리하면 랭킹을 갱신하고 처리 완료로 표시한다")
    @Test
    void processPendingEvents_success() throws Exception {
        LocalDate date = LocalDate.of(2025, 7, 21);
        OutboxEvent event = gameCompletedEvent(1L, "20250721HTLT0", date);
        when(outboxEventService.claimPendingEvents(BATCH_SIZE)).thenReturn(List.of(event));

        outboxEventProcessor.processPendingEvents();

        verify(statSyncService).updateRankings(date);
        verify(outboxEventService).markProcessed(1L);
        verify(outboxEventService, never()).markFailed(any(), any());
    }

    @DisplayName("랭킹 갱신에 실패하면 outbox 이벤트를 재시도 대상으로 표시한다")
    @Test
    void processPendingEvents_failure() throws Exception {
        LocalDate date = LocalDate.of(2025, 7, 21);
        OutboxEvent event = gameCompletedEvent(1L, "20250721HTLT0", date);
        IllegalStateException failure = new IllegalStateException("ranking sync failed");
        when(outboxEventService.claimPendingEvents(BATCH_SIZE)).thenReturn(List.of(event));
        doThrow(failure).when(statSyncService).updateRankings(date);

        outboxEventProcessor.processPendingEvents();

        verify(outboxEventService).markFailed(1L, failure);
        verify(outboxEventService, never()).markProcessed(1L);
    }

    @DisplayName("지원하지 않는 outbox event type이면 실패로 표시한다")
    @Test
    void processPendingEvents_unsupportedEventType() throws Exception {
        OutboxEvent event = unsupportedEvent(1L, "20250721HTLT0");
        when(outboxEventService.claimPendingEvents(BATCH_SIZE)).thenReturn(List.of(event));

        outboxEventProcessor.processPendingEvents();

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(outboxEventService).markFailed(eq(1L), exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue()).isExactlyInstanceOf(UnsupportedOutboxEventTypeException.class);
        verify(statSyncService, never()).updateRankings(any());
        verify(outboxEventService, never()).markProcessed(1L);
    }

    private OutboxEvent gameCompletedEvent(final Long id, final String gameCode, final LocalDate date)
            throws Exception {
        String payload = objectMapper.writeValueAsString(new GameCompletedOutboxPayload(gameCode, date));
        OutboxEvent event = OutboxEvent.of(
                OutboxEventService.GAME_COMPLETED_EVENT_TYPE,
                gameCode,
                payload,
                LocalDateTime.of(2025, 7, 21, 23, 0)
        );
        ReflectionTestUtils.setField(event, "id", id);
        return event;
    }

    private OutboxEvent unsupportedEvent(final Long id, final String gameCode) {
        OutboxEvent event = OutboxEvent.of(
                "UNKNOWN",
                gameCode,
                "{}",
                LocalDateTime.of(2025, 7, 21, 23, 0)
        );
        ReflectionTestUtils.setField(event, "id", id);
        return event;
    }
}
