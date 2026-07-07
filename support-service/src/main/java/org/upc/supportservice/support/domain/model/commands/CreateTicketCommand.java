package org.upc.supportservice.support.domain.model.commands;

import org.upc.supportservice.support.domain.model.valueobjects.TicketCategory;
import org.upc.supportservice.support.domain.model.valueobjects.TicketPriority;

public record CreateTicketCommand(
        String title,
        String description,
        TicketCategory category,
        TicketPriority priority,
        String userId,
        Long assignedTo
) {
}