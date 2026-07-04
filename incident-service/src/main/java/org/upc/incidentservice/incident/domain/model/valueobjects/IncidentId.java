package org.upc.incidentservice.incident.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record IncidentId(@Column(name = "incident_id", nullable = false, unique = true, updatable = false) UUID incidentId) {

    public IncidentId() {
        this(null);
    }
}
