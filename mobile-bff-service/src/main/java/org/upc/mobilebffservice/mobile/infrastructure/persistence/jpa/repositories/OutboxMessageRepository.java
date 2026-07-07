package org.upc.mobilebffservice.mobile.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.upc.mobilebffservice.mobile.domain.model.entities.OutboxMessage;
import org.upc.mobilebffservice.mobile.domain.model.valueobjects.OutboxStatus;

import java.util.Collection;
import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {
    boolean existsByEventTypeAndAggregateId(String eventType, String aggregateId);
    List<OutboxMessage> findTop20ByStatusInOrderByOccurredAtAsc(Collection<OutboxStatus> statuses);
}
