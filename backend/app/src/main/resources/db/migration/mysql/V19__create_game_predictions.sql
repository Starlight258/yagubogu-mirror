CREATE TABLE game_predictions
(
    id         BIGINT      AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT      NOT NULL,
    game_id    BIGINT      NOT NULL,
    pick       VARCHAR(10) NOT NULL,
    status     VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    CONSTRAINT fk_game_predictions_member FOREIGN KEY (member_id) REFERENCES members (member_id),
    CONSTRAINT fk_game_predictions_game FOREIGN KEY (game_id) REFERENCES games (game_id),
    CONSTRAINT uk_game_predictions_member_game UNIQUE (member_id, game_id),
    INDEX idx_game_predictions_game_status (game_id, status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
