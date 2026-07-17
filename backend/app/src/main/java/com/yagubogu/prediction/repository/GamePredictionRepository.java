package com.yagubogu.prediction.repository;

import com.yagubogu.prediction.domain.GamePrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GamePredictionRepository extends JpaRepository<GamePrediction, Long> {
}
