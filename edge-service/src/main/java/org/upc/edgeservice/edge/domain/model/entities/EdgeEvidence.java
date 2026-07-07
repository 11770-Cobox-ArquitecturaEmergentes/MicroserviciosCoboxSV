package org.upc.edgeservice.edge.domain.model.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.upc.edgeservice.edge.domain.model.aggregates.SyncBatch;
import org.upc.edgeservice.edge.domain.model.valueobjects.EvidenceStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_edge_evidences_client_evidence_id", columnNames = "client_evidence_id"))
public class EdgeEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_evidence_id", nullable = false, updatable = false)
    private UUID clientEvidenceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sync_batch_id", nullable = false)
    private SyncBatch syncBatch;

    @Column(nullable = false)
    private Long orderId;

    private Long routeId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, length = 1000)
    private String objectKey;

    @Column(nullable = false, length = 128)
    private String sha256;

    private String mimeType;

    private Long sizeBytes;

    @Column(nullable = false)
    private Instant capturedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvidenceStatus status;

    public EdgeEvidence(UUID clientEvidenceId, SyncBatch syncBatch, Long orderId, Long routeId, String type,
                        String objectKey, String sha256, String mimeType, Long sizeBytes, Instant capturedAt) {
        this.clientEvidenceId = clientEvidenceId;
        this.syncBatch = syncBatch;
        this.orderId = orderId;
        this.routeId = routeId;
        this.type = type;
        this.objectKey = objectKey;
        this.sha256 = sha256;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.capturedAt = capturedAt;
        this.status = EvidenceStatus.RECORDED;
    }
}
