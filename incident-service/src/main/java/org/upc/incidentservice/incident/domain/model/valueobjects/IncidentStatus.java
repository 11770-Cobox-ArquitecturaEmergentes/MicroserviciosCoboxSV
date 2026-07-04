package org.upc.incidentservice.incident.domain.model.valueobjects;

public enum IncidentStatus {
    OPEN,
    IN_PROGRESS,
    ESCALATED,
    RESOLVED,
    CLOSED;

    public boolean canTransitionTo(IncidentStatus targetStatus) {
        return switch (this) {
            case OPEN -> targetStatus == IN_PROGRESS || targetStatus == ESCALATED || targetStatus == CLOSED;
            case IN_PROGRESS -> targetStatus == ESCALATED || targetStatus == RESOLVED || targetStatus == CLOSED;
            case ESCALATED -> targetStatus == IN_PROGRESS || targetStatus == RESOLVED || targetStatus == CLOSED;
            case RESOLVED -> targetStatus == CLOSED;
            case CLOSED -> false;
        };
    }
}
