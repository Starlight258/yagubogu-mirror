package com.yagubogu.outbox.dto;

import java.time.LocalDate;

public record GameCompletedOutboxPayload(String gameCode, LocalDate date) {
}
