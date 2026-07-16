package yagubogu.crawling.game.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "review_crawl_retries",
        uniqueConstraints = @UniqueConstraint(name = "uk_review_retry_game", columnNames = "game_code")
)
@Entity
public class ReviewCrawlRetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_code", nullable = false)
    private String gameCode;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "next_retry_at", nullable = false)
    private LocalDateTime nextRetryAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewRetryStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ReviewCrawlRetry of(final String gameCode, final LocalDateTime nextRetryAt,
                                      final LocalDateTime now) {
        ReviewCrawlRetry retry = new ReviewCrawlRetry();
        retry.gameCode = gameCode;
        retry.retryCount = 0;
        retry.nextRetryAt = nextRetryAt;
        retry.status = ReviewRetryStatus.PENDING;
        retry.createdAt = now;
        return retry;
    }

    public void markSuccess() {
        this.status = ReviewRetryStatus.SUCCESS;
    }

    public void markFailed() {
        this.status = ReviewRetryStatus.FAILED;
    }

    public void scheduleNextRetry(final LocalDateTime nextRetryAt) {
        this.retryCount++;
        this.nextRetryAt = nextRetryAt;
    }
}
