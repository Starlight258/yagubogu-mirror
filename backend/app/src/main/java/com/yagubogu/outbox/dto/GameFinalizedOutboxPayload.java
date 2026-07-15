package com.yagubogu.outbox.dto;

import java.time.LocalDate;

public record GameFinalizedOutboxPayload(String gameCode, LocalDate date) {
}
