package com.yagubogu.outbox.domain;

public enum OutboxEventStatus {
    PENDING,
    PROCESSING,
    PROCESSED,
    FAILED,
}
