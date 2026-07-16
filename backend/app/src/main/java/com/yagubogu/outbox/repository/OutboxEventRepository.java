package com.yagubogu.outbox.repository;

import com.yagubogu.outbox.domain.OutboxEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO outbox_events
                (event_type, aggregate_id, payload, status, retry_count, next_retry_at, created_at, updated_at)
            VALUES
                (:eventType, :aggregateId, :payload, 'PENDING', 0, :now, :now, :now)
            """, nativeQuery = true)
    int insertIgnore(String eventType, String aggregateId, String payload, LocalDateTime now);

    @Query(value = """
            SELECT *
            FROM outbox_events
            WHERE status = 'PENDING'
              AND next_retry_at <= :now
            ORDER BY id
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> findPendingForUpdate(LocalDateTime now, int limit);
}
