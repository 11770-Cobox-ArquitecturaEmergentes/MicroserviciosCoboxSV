package org.upc.mobilebffservice.mobile.domain.model.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.upc.mobilebffservice.mobile.domain.model.valueobjects.OutboxStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_outbox_event_aggregate", columnNames = {"event_type", "aggregate_id"}))
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(nullable = false, length = 10000)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private Integer retryCount;

    private Instant processedAt;

    @Column(length = 2000)
    private String lastError;

    public OutboxMessage(String eventType, String aggregateId, String payload, Instant occurredAt) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.occurredAt = occurredAt;
        this.retryCount = 0;
    }

    public void markProcessing() {
        this.status = OutboxStatus.PROCESSING;
        this.lastError = null;
    }

    public void markPublished(Instant processedAt) {
        this.status = OutboxStatus.PUBLISHED;
        this.processedAt = processedAt;
        this.lastError = null;
    }

    public void markFailed(String error) {
        this.status = OutboxStatus.FAILED;
        this.retryCount = this.retryCount == null ? 1 : this.retryCount + 1;
        this.lastError = truncate(error);
    }

    public void markDeadLettered(String error, Instant processedAt) {
        this.status = OutboxStatus.DEAD_LETTERED;
        this.processedAt = processedAt;
        this.lastError = truncate(error);
    }

    private String truncate(String value) {
        if (value == null) return null;
        return value.length() <= 2000 ? value : value.substring(0, 2000);
    }
}
