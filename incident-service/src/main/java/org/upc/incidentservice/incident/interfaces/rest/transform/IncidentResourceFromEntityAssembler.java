package org.upc.incidentservice.incident.interfaces.rest.transform;

import org.upc.incidentservice.incident.domain.model.aggregates.Incident;
import org.upc.incidentservice.incident.interfaces.rest.resources.IncidentResource;

public class IncidentResourceFromEntityAssembler {

    public static IncidentResource toResourceFromEntity(Incident entity) {
        return new IncidentResource(
                entity.getId(),
                entity.getIncidentId() != null ? entity.getIncidentId().incidentId() : null,
                entity.getType(),
                entity.getDescription(),
                entity.getReportedAt(),
                entity.getSeverity() != null ? entity.getSeverity().name() : null,
                entity.getStatus() != null ? entity.getStatus().name() : null,
                entity.getResponsibleUserId() != null ? entity.getResponsibleUserId().responsibleUserId() : null,
                entity.getSourceType(),
                entity.getSourceAlertId(),
                entity.getSourceClientEvidenceId()
        );
    }
}
