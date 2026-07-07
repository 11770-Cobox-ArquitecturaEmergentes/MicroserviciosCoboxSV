package org.upc.maintenanceservice.maintenance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record Timelapse(Integer days) {
    public Timelapse() {
        this(null);
    }
}
