CREATE TABLE talk_likes
(
    talk_like_id BIGINT      NOT NULL AUTO_INCREMENT,
    talk_id      BIGINT      NOT NULL,
    member_id    BIGINT      NOT NULL,
    created_at   DATETIME(6) NOT NULL,
    PRIMARY KEY (talk_like_id),
    UNIQUE (talk_id, member_id),
    CONSTRAINT fk_talk_likes_talk FOREIGN KEY (talk_id) REFERENCES talks (talk_id),
    CONSTRAINT fk_talk_likes_member FOREIGN KEY (member_id) REFERENCES members (member_id)
) ENGINE = InnoDB;
