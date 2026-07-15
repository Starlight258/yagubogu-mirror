package com.yagubogu.outbox.repository;

import com.yagubogu.outbox.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    boolean existsByEventTypeAndAggregateId(String eventType, String aggregateId);
}
