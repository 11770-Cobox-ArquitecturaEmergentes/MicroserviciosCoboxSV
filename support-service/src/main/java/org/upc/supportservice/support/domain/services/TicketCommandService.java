package org.upc.supportservice.support.domain.services;

import org.upc.supportservice.support.domain.model.aggregates.Ticket;
import org.upc.supportservice.support.domain.model.commands.CreateTicketCommand;
import org.upc.supportservice.support.domain.model.commands.DeleteTicketCommand;
import org.upc.supportservice.support.domain.model.commands.UpdateTicketCommand;

import java.util.Optional;

public interface TicketCommandService {
    Optional<Ticket> handle(CreateTicketCommand command);
    Optional<Ticket> handle(UpdateTicketCommand command);
    void handle(DeleteTicketCommand command);
}