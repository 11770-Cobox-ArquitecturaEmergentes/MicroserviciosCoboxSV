package org.upc.incidentservice.incident.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record ResponsibleUserId(@Column(name = "responsible_user_id") Long responsibleUserId) {

    public ResponsibleUserId() {
        this(null);
    }
}
