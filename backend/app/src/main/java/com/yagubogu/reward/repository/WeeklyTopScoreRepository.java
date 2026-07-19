package com.yagubogu.reward.repository;

import com.yagubogu.reward.domain.WeeklyTopScore;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyTopScoreRepository extends JpaRepository<WeeklyTopScore, Long> {

    boolean existsByWeekStart(LocalDate weekStart);

    Optional<WeeklyTopScore> findByWeekStart(LocalDate weekStart);
}
