package com.yagubogu.outbox.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.outbox.domain.OutboxEvent;
import com.yagubogu.outbox.dto.GameCompletedOutboxPayload;
import com.yagubogu.outbox.exception.UnsupportedOutboxEventTypeException;
import com.yagubogu.outbox.service.OutboxEventService;
import com.yagubogu.stat.service.StatSyncService;
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

    public OutboxEventProcessor(
            final OutboxEventService outboxEventService,
            final ObjectMapper objectMapper,
            final StatSyncService statSyncService,
            @Value("${outbox.worker.batch-size:20}") final int batchSize
    ) {
        this.outboxEventService = outboxEventService;
        this.objectMapper = objectMapper;
        this.statSyncService = statSyncService;
        this.batchSize = batchSize;
    }

    @Scheduled(
            fixedDelayString = "${outbox.worker.fixed-delay-ms:10000}",
            initialDelayString = "${outbox.worker.initial-delay-ms:10000}"
    )
    public void processPendingEvents() {
        List<OutboxEvent> events = outboxEventService.claimPendingEvents(batchSize);
        for (OutboxEvent event : events) {
            process(event);
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
