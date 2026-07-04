package org.upc.supportservice.support.domain.model.queries;

import org.upc.supportservice.support.domain.model.valueobjects.TicketCategory;

public record GetTicketsByCategoryQuery(TicketCategory category) {
}