package org.upc.incidentservice.incident.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import org.upc.incidentservice.incident.domain.exceptions.InvalidIncidentStatusTransitionException;
import org.upc.incidentservice.incident.domain.model.commands.AssignResponsibleUserCommand;
import org.upc.incidentservice.incident.domain.model.commands.CreateIncidentCommand;
import org.upc.incidentservice.incident.domain.model.commands.UpdateIncidentStatusCommand;
import org.upc.incidentservice.incident.domain.model.events.IncidentAssignedEvent;
import org.upc.incidentservice.incident.domain.model.events.IncidentReportedEvent;
import org.upc.incidentservice.incident.domain.model.events.IncidentStatusUpdatedEvent;
import org.upc.incidentservice.incident.domain.model.valueobjects.*;
import org.upc.incidentservice.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
public class Incident extends AuditableAbstractAggregateRoot<Incident> {

    @Embedded
    @AttributeOverride(name = "incidentId", column = @Column(name = "incident_id", nullable = false, unique = true, updatable = false))
    private IncidentId incidentId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime reportedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Embedded
    @AttributeOverride(name = "responsibleUserId", column = @Column(name = "responsible_user_id"))
    private ResponsibleUserId responsibleUserId;

    private String sourceType;

    @Column(unique = true)
    private UUID sourceAlertId;

    private UUID sourceClientEvidenceId;

    protected Incident() {
    }

    public Incident(CreateIncidentCommand command) {
        this.incidentId = new IncidentId(UUID.randomUUID());
        this.type = command.type();
        this.description = command.description();
        this.reportedAt = LocalDateTime.now();
        this.severity = command.severity();
        this.status = command.severity() == IncidentSeverity.CRITICAL ? IncidentStatus.ESCALATED : IncidentStatus.OPEN;
        if (command.responsibleUserId() != null) {
            this.responsibleUserId = new ResponsibleUserId(command.responsibleUserId());
        }
        this.sourceType = command.sourceType() == null || command.sourceType().isBlank() ? "MANUAL" : command.sourceType();
        this.sourceAlertId = command.sourceAlertId();
        this.sourceClientEvidenceId = command.sourceClientEvidenceId();
        this.registerEvent(new IncidentReportedEvent(this.incidentId.incidentId(), this.type, this.severity, this.status));
    }

    public void updateStatus(UpdateIncidentStatusCommand command) {
        this.updateStatus(command.status());
    }

    public void updateStatus(IncidentStatus targetStatus) {
        Objects.requireNonNull(targetStatus, "Incident status cannot be null");
        if (this.status == targetStatus) {
            return;
        }
        if (!this.status.canTransitionTo(targetStatus)) {
            throw new InvalidIncidentStatusTransitionException(this.status, targetStatus);
        }
        this.status = targetStatus;
        this.registerEvent(new IncidentStatusUpdatedEvent(this.incidentId.incidentId(), this.status));
    }

    public void assignResponsible(AssignResponsibleUserCommand command) {
        this.assignResponsible(command.responsibleUserId());
    }

    public void assignResponsible(Long responsibleUserId) {
        Objects.requireNonNull(responsibleUserId, "Responsible user id cannot be null");
        var newResponsibleUserId = new ResponsibleUserId(responsibleUserId);
        if (this.responsibleUserId != null && this.responsibleUserId.equals(newResponsibleUserId)) {
            return;
        }
        this.responsibleUserId = newResponsibleUserId;
        this.registerEvent(new IncidentAssignedEvent(this.incidentId.incidentId(), responsibleUserId));
    }
}
