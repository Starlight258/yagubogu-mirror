CREATE TABLE outbox_events
(
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT 'Outbox 이벤트 식별자',
    event_type    VARCHAR(100) NOT NULL COMMENT '이벤트 종류',
    aggregate_id  VARCHAR(100) NOT NULL COMMENT '이벤트 대상 식별자',
    payload       JSON         NOT NULL COMMENT '이벤트 처리에 필요한 데이터 JSON',
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '처리 상태',
    retry_count   INT          NOT NULL DEFAULT 0 COMMENT '처리 실패 후 재시도 횟수',
    next_retry_at DATETIME(6)  NOT NULL COMMENT '다음 처리 시도 가능 시각',
    processed_at  DATETIME(6)  NULL COMMENT '처리 성공 시각',
    last_error    TEXT         NULL COMMENT '마지막 처리 실패 원인',
    created_at    DATETIME(6)  NOT NULL COMMENT '이벤트 생성 시각',
    updated_at    DATETIME(6)  NOT NULL COMMENT '마지막 상태 변경 시각',

    UNIQUE KEY uk_outbox_event (event_type, aggregate_id),
    INDEX idx_outbox_polling (status, next_retry_at, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
