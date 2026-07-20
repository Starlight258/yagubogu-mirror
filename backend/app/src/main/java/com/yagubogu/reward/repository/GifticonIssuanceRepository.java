package com.yagubogu.reward.repository;

import com.yagubogu.reward.domain.GifticonIssuance;
import com.yagubogu.reward.domain.WeeklyTopScore;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GifticonIssuanceRepository extends JpaRepository<GifticonIssuance, Long> {

    List<GifticonIssuance> findAllByWeeklyTopScore(WeeklyTopScore weeklyTopScore);
}
