package com.yagubogu.reward.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 그 주 최고 점수 기록(주당 1건, week_start UNIQUE)만 담는다.
 * 당첨자 목록은 {@link GifticonIssuance}에 있다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "weekly_top_scores")
@Entity
public class WeeklyTopScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "top_score", nullable = false)
    private int topScore;

    @Column(name = "drawn_at", nullable = false)
    private LocalDateTime drawnAt;

    public WeeklyTopScore(final LocalDate weekStart, final int topScore, final LocalDateTime drawnAt) {
        this.weekStart = weekStart;
        this.topScore = topScore;
        this.drawnAt = drawnAt;
    }
}
