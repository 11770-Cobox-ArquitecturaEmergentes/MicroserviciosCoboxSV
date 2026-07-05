package org.upc.mobilebffservice.mobile.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.upc.mobilebffservice.mobile.domain.model.entities.OutboxMessage;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {
    boolean existsByEventTypeAndAggregateId(String eventType, String aggregateId);
}
