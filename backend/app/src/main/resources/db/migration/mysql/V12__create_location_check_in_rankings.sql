CREATE TABLE location_check_in_rankings
(
    location_check_in_ranking_id BIGINT      NOT NULL AUTO_INCREMENT,
    member_id             BIGINT      NOT NULL,
    check_in_count        INT         NOT NULL DEFAULT 0,
    game_year             INT         NOT NULL,
    updated_at            DATETIME(6) NULL,
    PRIMARY KEY (location_check_in_ranking_id),
    UNIQUE KEY uq_location_check_in_rankings_member_year (member_id, game_year),
    INDEX idx_location_check_in_rankings_year_count_member (game_year, check_in_count DESC, member_id ASC),
    CONSTRAINT fk_location_check_in_rankings_member FOREIGN KEY (member_id) REFERENCES members (member_id)
) ENGINE = InnoDB;
