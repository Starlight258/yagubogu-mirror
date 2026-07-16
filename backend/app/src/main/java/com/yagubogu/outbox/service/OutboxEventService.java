package com.yagubogu.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.outbox.dto.GameCompletedOutboxPayload;
import com.yagubogu.outbox.domain.OutboxEvent;
import com.yagubogu.outbox.exception.OutboxEventNotFoundException;
import com.yagubogu.outbox.exception.OutboxPayloadSerializationException;
import com.yagubogu.outbox.repository.OutboxEventRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OutboxEventService {

    public static final String GAME_COMPLETED_EVENT_TYPE = "GAME_COMPLETED";
    private static final int MAX_RETRY_COUNT = 5;
    private static final int[] RETRY_DELAY_MINUTES = {1, 5, 30};
    private static final int LAST_ERROR_MAX_LENGTH = 2000;

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional
    public void saveGameCompletedEvent(final String gameCode, final LocalDate date) {
        try {
            String payload = objectMapper.writeValueAsString(new GameCompletedOutboxPayload(gameCode, date));
            LocalDateTime now = LocalDateTime.now(clock);
            int inserted = outboxEventRepository.insertIgnore(GAME_COMPLETED_EVENT_TYPE, gameCode, payload, now);
            if (inserted == 0) {
                log.info("[OUTBOX] 이미 저장된 이벤트 스킵: gameCode={}", gameCode);
            }
        } catch (JsonProcessingException e) {
            throw new OutboxPayloadSerializationException(gameCode, e);
        }
    }

    @Transactional
    public List<OutboxEvent> claimPendingEvents(final int limit) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<OutboxEvent> events = outboxEventRepository.findPendingForUpdate(now, limit);
        events.forEach(event -> event.markProcessing(now));
        return events;
    }

    @Transactional
    public void markProcessed(final Long eventId) {
        LocalDateTime now = LocalDateTime.now(clock);
        OutboxEvent event = outboxEventRepository.findById(eventId)
                .orElseThrow(() -> new OutboxEventNotFoundException(eventId));
        event.markProcessed(now);
    }

    @Transactional
    public void markFailed(final Long eventId, final Exception exception) {
        LocalDateTime now = LocalDateTime.now(clock);
        OutboxEvent event = outboxEventRepository.findById(eventId)
                .orElseThrow(() -> new OutboxEventNotFoundException(eventId));

        int nextRetryCount = event.getRetryCount() + 1;
        String lastError = formatLastError(exception);
        if (nextRetryCount >= MAX_RETRY_COUNT) {
            event.markFailedPermanently(nextRetryCount, lastError, now);
            return;
        }

        LocalDateTime nextRetryAt = now.plusMinutes(calculateRetryDelayMinutes(nextRetryCount));
        event.markFailed(nextRetryCount, nextRetryAt, lastError, now);
    }

    private int calculateRetryDelayMinutes(final int retryCount) {
        int retryDelayIndex = Math.min(Math.max(0, retryCount - 1), RETRY_DELAY_MINUTES.length - 1);
        return RETRY_DELAY_MINUTES[retryDelayIndex];
    }

    private String formatLastError(final Exception exception) {
        String message = exception.getClass().getSimpleName() + ": " + exception.getMessage();
        if (message.length() <= LAST_ERROR_MAX_LENGTH) {
            return message;
        }
        return message.substring(0, LAST_ERROR_MAX_LENGTH);
    }
}
