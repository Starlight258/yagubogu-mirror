package yagubogu.crawling.game.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import yagubogu.crawling.game.domain.ReviewCrawlRetry;
import yagubogu.crawling.game.domain.ReviewRetryStatus;

public interface ReviewCrawlRetryRepository extends JpaRepository<ReviewCrawlRetry, Long> {

    List<ReviewCrawlRetry> findByStatusAndNextRetryAtLessThanEqual(ReviewRetryStatus status, LocalDateTime now);

    boolean existsByGameCodeAndStatus(String gameCode, ReviewRetryStatus status);
}
