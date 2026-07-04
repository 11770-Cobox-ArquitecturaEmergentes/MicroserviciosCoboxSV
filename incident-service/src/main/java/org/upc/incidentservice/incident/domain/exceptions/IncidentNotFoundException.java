package org.upc.incidentservice.incident.domain.exceptions;

public class IncidentNotFoundException extends RuntimeException {
    public IncidentNotFoundException(Object incidentId) {
        super("Incident not found with identifier: " + incidentId);
    }
}
