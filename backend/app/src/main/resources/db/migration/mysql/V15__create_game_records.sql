CREATE TABLE game_hitter_records
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id       BIGINT       NOT NULL,
    is_home_team  BOOLEAN      NOT NULL,
    batting_order INT          NOT NULL,
    position      VARCHAR(20)  NOT NULL,
    player_name   VARCHAR(50)  NOT NULL,
    at_bats       INT          NOT NULL,
    hits          INT          NOT NULL,
    rbi           INT          NOT NULL,
    runs          INT          NOT NULL,
    CONSTRAINT fk_hitter_game FOREIGN KEY (game_id) REFERENCES games (game_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE game_pitcher_records
(
    id                BIGINT      AUTO_INCREMENT PRIMARY KEY,
    game_id           BIGINT      NOT NULL,
    is_home_team      BOOLEAN     NOT NULL,
    player_name       VARCHAR(50) NOT NULL,
    result            VARCHAR(10) NULL,
    innings           VARCHAR(10) NOT NULL,
    batters_faced     INT         NOT NULL,
    pitch_count       INT         NOT NULL,
    at_bats           INT         NOT NULL,
    hits_allowed      INT         NOT NULL,
    home_runs_allowed INT         NOT NULL,
    walks_and_hbp     INT         NOT NULL,
    strikeouts        INT         NOT NULL,
    runs_allowed      INT         NOT NULL,
    earned_runs       INT         NOT NULL,
    CONSTRAINT fk_pitcher_game FOREIGN KEY (game_id) REFERENCES games (game_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
