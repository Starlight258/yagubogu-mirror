CREATE TABLE attendance_rankings
(
    attendance_ranking_id BIGINT      NOT NULL AUTO_INCREMENT,
    member_id             BIGINT      NOT NULL,
    check_in_count        INT         NOT NULL DEFAULT 0,
    game_year             INT         NOT NULL,
    updated_at            DATETIME(6) NULL,
    PRIMARY KEY (attendance_ranking_id),
    UNIQUE KEY uq_attendance_rankings_member_year (member_id, game_year),
    INDEX idx_attendance_rankings_year_count_member (game_year, check_in_count DESC, member_id ASC),
    CONSTRAINT fk_attendance_rankings_member FOREIGN KEY (member_id) REFERENCES members (member_id)
) ENGINE = InnoDB;
