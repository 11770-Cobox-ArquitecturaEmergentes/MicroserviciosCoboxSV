package org.upc.edgeservice.edge.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.upc.edgeservice.edge.domain.model.valueobjects.SyncBatchStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_sync_batches_client_batch_id", columnNames = "client_batch_id"))
public class SyncBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_batch_id", nullable = false, updatable = false)
    private UUID clientBatchId;

    @Column(nullable = false)
    private Long driverId;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private Instant sentAt;

    @Column(nullable = false)
    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncBatchStatus status;

    public SyncBatch(UUID clientBatchId, Long driverId, String deviceId, Instant sentAt, SyncBatchStatus status) {
        this.clientBatchId = clientBatchId;
        this.driverId = driverId;
        this.deviceId = deviceId;
        this.sentAt = sentAt;
        this.status = status;
        this.receivedAt = Instant.now();
    }

    public void updateStatus(SyncBatchStatus status) {
        this.status = status;
    }
}
