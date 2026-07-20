CREATE TABLE weekly_top_scores
(
    id         BIGINT      AUTO_INCREMENT PRIMARY KEY COMMENT '주간 최고 점수 기록 식별자',
    week_start DATE        NOT NULL COMMENT '대상 주의 시작일(월요일)',
    top_score  INT         NOT NULL COMMENT '그 주 최고 점수',
    drawn_at   DATETIME(6) NOT NULL COMMENT '추첨 확정 시각',

    UNIQUE KEY uk_weekly_top_scores_week_start (week_start)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE gifticon_issuances
(
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '기프티콘 발급 식별자',
    weekly_top_score_id BIGINT       NOT NULL COMMENT '주간 최고 점수 기록',
    member_id           BIGINT       NOT NULL COMMENT '당첨 회원',
    external_order_id   VARCHAR(70)  NOT NULL COMMENT '카카오 발송 멱등키',
    status              VARCHAR(20)  NOT NULL COMMENT '발급 상태',
    created_at          DATETIME(6)  NOT NULL COMMENT '생성 시각',
    updated_at          DATETIME(6)  NOT NULL COMMENT '마지막 상태 변경 시각',

    CONSTRAINT fk_gifticon_issuances_weekly_top_score FOREIGN KEY (weekly_top_score_id) REFERENCES weekly_top_scores (id),
    CONSTRAINT fk_gifticon_issuances_member FOREIGN KEY (member_id) REFERENCES members (member_id),
    UNIQUE KEY uk_gifticon_issuances_external_order (external_order_id),
    UNIQUE KEY uk_gifticon_issuances_weekly_top_score_member (weekly_top_score_id, member_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
