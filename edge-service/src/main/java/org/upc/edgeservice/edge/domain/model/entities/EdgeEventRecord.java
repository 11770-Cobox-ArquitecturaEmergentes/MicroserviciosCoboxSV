package org.upc.edgeservice.edge.domain.model.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.upc.edgeservice.edge.domain.model.aggregates.SyncBatch;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_edge_events_client_event_id", columnNames = "client_event_id"))
public class EdgeEventRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_event_id", nullable = false, updatable = false)
    private UUID clientEventId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sync_batch_id", nullable = false)
    private SyncBatch syncBatch;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(length = 10000)
    private String payload;

    @Column(nullable = false)
    private Instant occurredAt;

    public EdgeEventRecord(UUID clientEventId, SyncBatch syncBatch, String type, String aggregateType,
                           String aggregateId, String payload, Instant occurredAt) {
        this.clientEventId = clientEventId;
        this.syncBatch = syncBatch;
        this.type = type;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.occurredAt = occurredAt;
    }
}
