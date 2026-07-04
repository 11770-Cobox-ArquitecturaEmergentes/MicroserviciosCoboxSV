package org.upc.maintenanceservice.maintenance.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public record Money(
        @Column(name = "amount", precision = 19, scale = 2) BigDecimal amount,
        @Column(name = "currency", length = 8) String currency
) {
    public Money() {
        this(null, null);
    }
}
