package org.upc.mobilebffservice.mobile.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.upc.mobilebffservice.mobile.domain.model.valueobjects.UploadIntentStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_upload_intents_upload_intent_id", columnNames = "upload_intent_id"),
        @UniqueConstraint(name = "uk_upload_intents_client_evidence_id", columnNames = "client_evidence_id")
})
public class UploadIntent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upload_intent_id", nullable = false, updatable = false)
    private UUID uploadIntentId;

    @Column(name = "client_evidence_id", nullable = false, updatable = false)
    private UUID clientEvidenceId;

    @Column(nullable = false)
    private Long driverId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long routeId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, length = 1000)
    private String objectKey;

    @Column(nullable = false, length = 128)
    private String sha256;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadIntentStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant confirmedAt;

    public UploadIntent(UUID clientEvidenceId, Long driverId, Long orderId, Long routeId, String type, String objectKey,
                        String sha256, String mimeType, Long sizeBytes, Instant expiresAt) {
        this.uploadIntentId = UUID.randomUUID();
        this.clientEvidenceId = clientEvidenceId;
        this.driverId = driverId;
        this.orderId = orderId;
        this.routeId = routeId;
        this.type = type;
        this.objectKey = objectKey;
        this.sha256 = sha256;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.status = UploadIntentStatus.CREATED;
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
    }

    public boolean isExpired(Instant now) {
        return this.status != UploadIntentStatus.CONFIRMED && this.expiresAt.isBefore(now);
    }

    public void refreshExpiration(Instant expiresAt) {
        this.expiresAt = expiresAt;
        if (this.status == UploadIntentStatus.EXPIRED) {
            this.status = UploadIntentStatus.CREATED;
        }
    }

    public void markExpired() {
        if (this.status != UploadIntentStatus.CONFIRMED) {
            this.status = UploadIntentStatus.EXPIRED;
        }
    }

    public void markConfirmed(Instant confirmedAt) {
        this.status = UploadIntentStatus.CONFIRMED;
        this.confirmedAt = confirmedAt;
    }

    public void markFailed() {
        if (this.status != UploadIntentStatus.CONFIRMED) {
            this.status = UploadIntentStatus.FAILED;
        }
    }
}
