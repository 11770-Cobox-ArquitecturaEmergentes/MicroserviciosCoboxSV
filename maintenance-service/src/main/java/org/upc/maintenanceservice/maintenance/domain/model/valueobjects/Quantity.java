package org.upc.maintenanceservice.maintenance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record Quantity(Integer quantity) {
    public Quantity() {
        this(null);
    }
}
