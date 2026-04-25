package com.yagubogu.stat.repository;

import com.yagubogu.stat.domain.LocationCheckInRanking;
import com.yagubogu.stat.dto.LocationCheckInRankingCursorParam;
import com.yagubogu.stat.dto.LocationCheckInRankingParam;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationCheckInRankingRepository extends JpaRepository<LocationCheckInRanking, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO location_check_in_rankings(member_id, game_year, check_in_count, updated_at)
            VALUES (:memberId, :gameYear, 1, NOW())
            ON DUPLICATE KEY UPDATE
              check_in_count = check_in_count + 1,
              updated_at = NOW()
            """, nativeQuery = true)
    void upsertIncrement(
            @Param("memberId") long memberId,
            @Param("gameYear") int gameYear
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE location_check_in_rankings
            SET check_in_count = check_in_count - 1,
                updated_at = NOW()
            WHERE member_id = :memberId
              AND game_year = :gameYear
              AND check_in_count > 0
            """, nativeQuery = true)
    int decrement(
            @Param("memberId") long memberId,
            @Param("gameYear") int gameYear
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            DELETE FROM location_check_in_rankings
            WHERE member_id = :memberId
              AND game_year = :gameYear
              AND check_in_count = 0
            """, nativeQuery = true)
    void deleteZeroCount(
            @Param("memberId") long memberId,
            @Param("gameYear") int gameYear
    );

    @Query("""
            SELECT new com.yagubogu.stat.dto.LocationCheckInRankingCursorParam(
                ar.member.id,
                ar.checkInCount
            )
            FROM LocationCheckInRanking ar
            WHERE ar.member.id = :memberId
            AND ar.gameYear = :gameYear
            """)
    Optional<LocationCheckInRankingCursorParam> findCursorByMemberIdAndGameYear(
            @Param("memberId") long memberId,
            @Param("gameYear") int gameYear
    );

    @Query(value = """
            SELECT
                ranked.ranking AS ranking,
                ranked.memberId AS memberId,
                ranked.checkInCount AS checkInCount,
                ranked.nickname AS nickname,
                ranked.imageUrl AS imageUrl,
                ranked.teamShortName AS teamShortName
            FROM (
                SELECT
                    RANK() OVER (ORDER BY ar.check_in_count DESC) AS ranking,
                    ar.member_id AS memberId,
                    ar.check_in_count AS checkInCount,
                    m.nickname AS nickname,
                    m.image_url AS imageUrl,
                    t.short_name AS teamShortName
                FROM location_check_in_rankings ar
                JOIN members m ON m.member_id = ar.member_id
                JOIN teams t ON t.team_id = m.team_id
                WHERE ar.game_year = :gameYear
                  AND m.deleted_at IS NULL
                  AND m.team_id IS NOT NULL
            ) ranked
            WHERE (:cursorMemberId IS NULL
                   OR ranked.checkInCount < :cursorCheckInCount
                   OR (ranked.checkInCount = :cursorCheckInCount AND ranked.memberId > :cursorMemberId))
            ORDER BY ranked.checkInCount DESC, ranked.memberId ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<LocationCheckInRankingParam> findRankingsByCursor(
            @Param("gameYear") int gameYear,
            @Param("cursorMemberId") Long cursorMemberId,
            @Param("cursorCheckInCount") Integer cursorCheckInCount,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT
                ranked.ranking AS ranking,
                ranked.memberId AS memberId,
                ranked.checkInCount AS checkInCount,
                ranked.nickname AS nickname,
                ranked.imageUrl AS imageUrl,
                ranked.teamShortName AS teamShortName
            FROM (
                SELECT
                    RANK() OVER (ORDER BY ar.check_in_count DESC) AS ranking,
                    ar.member_id AS memberId,
                    ar.check_in_count AS checkInCount,
                    m.nickname AS nickname,
                    m.image_url AS imageUrl,
                    t.short_name AS teamShortName
                FROM location_check_in_rankings ar
                JOIN members m ON m.member_id = ar.member_id
                JOIN teams t ON t.team_id = m.team_id
                WHERE ar.game_year = :gameYear
                  AND m.deleted_at IS NULL
                  AND m.team_id IS NOT NULL
            ) ranked
            WHERE ranked.memberId = :memberId
            """, nativeQuery = true)
    Optional<LocationCheckInRankingParam> findRankingByMemberIdAndGameYear(
            @Param("memberId") long memberId,
            @Param("gameYear") int gameYear
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM location_check_in_rankings", nativeQuery = true)
    void deleteRankings();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO location_check_in_rankings(member_id, game_year, check_in_count, updated_at)
            SELECT
                c.member_id,
                YEAR(g.date) AS game_year,
                COUNT(*) AS check_in_count,
                NOW() AS updated_at
            FROM check_ins c
            JOIN games g ON g.game_id = c.game_id
            WHERE c.check_in_type = 'LOCATION_CHECK_IN'
            GROUP BY c.member_id, YEAR(g.date)
            """, nativeQuery = true)
    int insertAllFromCheckIns();
}
