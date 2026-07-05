package org.upc.aivalidationservice.validation.domain.model.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.upc.aivalidationservice.validation.domain.model.valueobjects.AlertSeverity;
import org.upc.aivalidationservice.validation.domain.model.valueobjects.AlertStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_ai_alert_client_evidence_type",
        columnNames = {"client_evidence_id", "type"}
))
public class AiAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private UUID alertId;

    @Column(name = "client_evidence_id", nullable = false, updatable = false)
    private UUID clientEvidenceId;

    @Column(nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private Instant createdAt;

    public AiAlert(UUID clientEvidenceId, String type, AlertSeverity severity, String message) {
        this.alertId = UUID.randomUUID();
        this.clientEvidenceId = clientEvidenceId;
        this.type = type;
        this.severity = severity;
        this.status = AlertStatus.OPEN;
        this.message = message;
        this.createdAt = Instant.now();
    }
}
