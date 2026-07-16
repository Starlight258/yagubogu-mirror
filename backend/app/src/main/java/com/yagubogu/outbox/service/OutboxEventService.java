package com.yagubogu.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.outbox.dto.GameFinalizedOutboxPayload;
import com.yagubogu.outbox.repository.OutboxEventRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OutboxEventService {

    private static final String GAME_FINALIZED_EVENT_TYPE = "GameFinalizedEvent";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional
    public void saveGameFinalizedEvent(final String gameCode, final LocalDate date) {
        try {
            String payload = objectMapper.writeValueAsString(new GameFinalizedOutboxPayload(gameCode, date));
            LocalDateTime now = LocalDateTime.now(clock);
            int inserted = outboxEventRepository.insertIgnore(GAME_FINALIZED_EVENT_TYPE, gameCode, payload, now);
            if (inserted == 0) {
                log.info("[OUTBOX] 이미 저장된 이벤트 스킵: gameCode={}", gameCode);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload: gameCode=" + gameCode, e);
        }
    }
}
