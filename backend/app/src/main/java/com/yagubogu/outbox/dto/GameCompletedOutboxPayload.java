package com.yagubogu.outbox.dto;

import com.yagubogu.outbox.exception.InvalidOutboxPayloadException;
import java.time.LocalDate;

public record GameCompletedOutboxPayload(String gameCode, LocalDate date) {

    public GameCompletedOutboxPayload {
        if (gameCode == null || gameCode.isBlank()) {
            throw new InvalidOutboxPayloadException("gameCode must not be blank");
        }
        if (date == null) {
            throw new InvalidOutboxPayloadException("date must not be null");
        }
    }
}
