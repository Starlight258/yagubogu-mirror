package com.yagubogu.talk.repository;

import com.yagubogu.talk.domain.TalkLike;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TalkLikeRepository extends JpaRepository<TalkLike, Long> {

    boolean existsByTalkIdAndMemberId(long talkId, long memberId);

    long countByTalkId(long talkId);

    @Modifying
    @Query("DELETE FROM TalkLike tl WHERE tl.talk.id = :talkId AND tl.member.id = :memberId")
    void deleteByTalkIdAndMemberId(@Param("talkId") long talkId, @Param("memberId") long memberId);

    @Query("""
            SELECT tl.talk.id, COUNT(tl)
            FROM TalkLike tl
            WHERE tl.talk.id IN :talkIds
            GROUP BY tl.talk.id
            """)
    List<Object[]> countByTalkIds(@Param("talkIds") List<Long> talkIds);

    @Query("""
            SELECT tl.talk.id
            FROM TalkLike tl
            WHERE tl.member.id = :memberId AND tl.talk.id IN :talkIds
            """)
    List<Long> findLikedTalkIds(@Param("memberId") long memberId, @Param("talkIds") List<Long> talkIds);
}
