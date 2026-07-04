package org.upc.supportservice.support.domain.model.commands;

import org.upc.supportservice.support.domain.model.valueobjects.TicketCategory;
import org.upc.supportservice.support.domain.model.valueobjects.TicketPriority;
import org.upc.supportservice.support.domain.model.valueobjects.TicketStatus;

public record UpdateTicketCommand(
        Long id,
        String title,
        String description,
        TicketCategory category,
        TicketPriority priority,
        TicketStatus status,
        Long assignedTo
) {
}