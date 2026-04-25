CREATE TABLE review_crawl_retries
(
    id            BIGINT      AUTO_INCREMENT PRIMARY KEY,
    game_code     VARCHAR(20) NOT NULL,
    retry_count   INT         NOT NULL DEFAULT 0,
    next_retry_at DATETIME    NOT NULL,
    status        VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    created_at    DATETIME    NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE shedlock
(
    name       VARCHAR(64)  NOT NULL,
    lock_until DATETIME(3)  NOT NULL,
    locked_at  DATETIME(3)  NOT NULL,
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
