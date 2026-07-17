package com.yagubogu.prediction.repository;

import com.yagubogu.game.domain.Game;
import com.yagubogu.member.domain.Member;
import com.yagubogu.prediction.domain.GamePrediction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GamePredictionRepository extends JpaRepository<GamePrediction, Long> {

    Optional<GamePrediction> findByMemberAndGame(Member member, Game game);
}
