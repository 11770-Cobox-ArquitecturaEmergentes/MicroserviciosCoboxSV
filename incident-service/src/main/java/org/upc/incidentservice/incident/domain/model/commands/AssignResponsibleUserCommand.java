package org.upc.incidentservice.incident.domain.model.commands;

import java.util.UUID;

public record AssignResponsibleUserCommand(UUID incidentId, Long responsibleUserId) {
}
