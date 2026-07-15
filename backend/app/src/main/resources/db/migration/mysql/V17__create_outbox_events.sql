CREATE TABLE outbox_events
(
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    event_type    VARCHAR(100) NOT NULL,
    aggregate_id  VARCHAR(100) NOT NULL,
    payload       JSON         NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count   INT          NOT NULL DEFAULT 0,
    next_retry_at DATETIME(6)  NOT NULL,
    processed_at  DATETIME(6)  NULL,
    last_error    TEXT         NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,

    UNIQUE KEY uk_outbox_event (event_type, aggregate_id),
    INDEX idx_outbox_polling (status, next_retry_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
