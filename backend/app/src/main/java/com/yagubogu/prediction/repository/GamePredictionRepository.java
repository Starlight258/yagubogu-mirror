package com.yagubogu.prediction.repository;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.member.domain.Member;
import com.yagubogu.prediction.domain.GamePrediction;
import com.yagubogu.prediction.domain.PredictionStatus;
import com.yagubogu.prediction.dto.GameWithPredictionCountsParam;
import com.yagubogu.prediction.dto.WeeklyScoreParam;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GamePredictionRepository extends JpaRepository<GamePrediction, Long> {

    Optional<GamePrediction> findByMemberAndGame(Member member, Game game);

    boolean existsByMemberAndGame(Member member, Game game);

    List<GamePrediction> findAllByGame(Game game);

    @Query("SELECT DISTINCT p.game FROM GamePrediction p "
            + "WHERE p.status = :status AND p.game.gameState IN :gameStates")
    List<Game> findPendingSettlementGames(
            @Param("status") PredictionStatus status,
            @Param("gameStates") Collection<GameState> gameStates
    );

    @Query("SELECT new com.yagubogu.prediction.dto.WeeklyScoreParam(p.member.id, COUNT(p)) "
            + "FROM GamePrediction p "
            + "WHERE p.status = :status AND p.game.date BETWEEN :weekStart AND :weekEnd "
            + "GROUP BY p.member.id")
    List<WeeklyScoreParam> findWeeklyScores(
            @Param("status") PredictionStatus status,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd
    );

    @Query("""
            SELECT new com.yagubogu.prediction.dto.GameWithPredictionCountsParam(
                g,
                COUNT(p.id),
                SUM(CASE WHEN p.pick = com.yagubogu.prediction.domain.PredictionPick.HOME THEN 1L ELSE 0L END),
                SUM(CASE WHEN p.pick = com.yagubogu.prediction.domain.PredictionPick.AWAY THEN 1L ELSE 0L END)
            )
            FROM Game g
            LEFT JOIN GamePrediction p ON p.game = g
            WHERE g.date = :date
            GROUP BY g
            """)
    List<GameWithPredictionCountsParam> findGamesWithPredictionCountsByDate(@Param("date") LocalDate date);
}
