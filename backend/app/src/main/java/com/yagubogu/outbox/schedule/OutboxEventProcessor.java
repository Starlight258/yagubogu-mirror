package com.yagubogu.outbox.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.outbox.domain.OutboxEvent;
import com.yagubogu.outbox.dto.GameCompletedOutboxPayload;
import com.yagubogu.outbox.exception.UnsupportedOutboxEventTypeException;
import com.yagubogu.outbox.service.OutboxEventService;
import com.yagubogu.stat.service.StatSyncService;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OutboxEventProcessor {

    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;
    private final StatSyncService statSyncService;
    private final int batchSize;
    private final int recoveryBatchSize;
    private final Duration processingTimeout;

    public OutboxEventProcessor(
            final OutboxEventService outboxEventService,
            final ObjectMapper objectMapper,
            final StatSyncService statSyncService,
            @Value("${outbox.worker.batch-size:20}") final int batchSize,
            @Value("${outbox.worker.recovery-batch-size:50}") final int recoveryBatchSize,
            @Value("${outbox.worker.processing-timeout-minutes:30}") final long processingTimeoutMinutes
    ) {
        this.outboxEventService = outboxEventService;
        this.objectMapper = objectMapper;
        this.statSyncService = statSyncService;
        this.batchSize = batchSize;
        this.recoveryBatchSize = recoveryBatchSize;
        this.processingTimeout = Duration.ofMinutes(processingTimeoutMinutes);
    }

    @Scheduled(
            fixedDelayString = "${outbox.worker.fixed-delay-ms:60000}",
            initialDelayString = "${outbox.worker.initial-delay-ms:60000}"
    )
    public void processPendingEvents() {
        List<OutboxEvent> events = outboxEventService.claimPendingEvents(batchSize);
        for (OutboxEvent event : events) {
            process(event);
        }
    }

    @Scheduled(
            fixedDelayString = "${outbox.worker.recovery-fixed-delay-ms:600000}",
            initialDelayString = "${outbox.worker.recovery-initial-delay-ms:600000}"
    )
    public void recoverTimedOutProcessingEvents() {
        int recoveredCount = outboxEventService.recoverTimedOutProcessingEvents(processingTimeout, recoveryBatchSize);
        if (recoveredCount > 0) {
            log.warn("[OUTBOX] Recovered timed out PROCESSING events: count={}", recoveredCount);
        }
    }

    private void process(final OutboxEvent event) {
        try {
            if (!OutboxEventService.GAME_COMPLETED_EVENT_TYPE.equals(event.getEventType())) {
                throw new UnsupportedOutboxEventTypeException(event.getEventType());
            }

            GameCompletedOutboxPayload payload = readPayload(event);
            statSyncService.updateRankings(payload.date());
            outboxEventService.markProcessed(event.getId());
            log.info("[OUTBOX] Processed event: id={}, type={}, aggregateId={}",
                    event.getId(), event.getEventType(), event.getAggregateId());
        } catch (RuntimeException | JsonProcessingException e) {
            log.error("[OUTBOX] Failed to process event: id={}, type={}, aggregateId={}",
                    event.getId(), event.getEventType(), event.getAggregateId(), e);
            outboxEventService.markFailed(event.getId(), e);
        }
    }

    private GameCompletedOutboxPayload readPayload(final OutboxEvent event) throws JsonProcessingException {
        return objectMapper.readValue(event.getPayload(), GameCompletedOutboxPayload.class);
    }
}
