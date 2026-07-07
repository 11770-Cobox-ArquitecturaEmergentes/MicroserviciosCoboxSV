package org.upc.incidentservice.incident.domain.exceptions;

import org.upc.incidentservice.incident.domain.model.valueobjects.IncidentStatus;

public class InvalidIncidentStatusTransitionException extends RuntimeException {
    public InvalidIncidentStatusTransitionException(IncidentStatus currentStatus, IncidentStatus targetStatus) {
        super("Cannot transition incident status from " + currentStatus + " to " + targetStatus);
    }
}
