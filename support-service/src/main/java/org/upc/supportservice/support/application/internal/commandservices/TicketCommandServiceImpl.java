package org.upc.supportservice.support.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.upc.supportservice.support.domain.exceptions.TicketNotFoundException;
import org.upc.supportservice.support.domain.model.aggregates.Ticket;
import org.upc.supportservice.support.domain.model.commands.CreateTicketCommand;
import org.upc.supportservice.support.domain.model.commands.DeleteTicketCommand;
import org.upc.supportservice.support.domain.model.commands.UpdateTicketCommand;
import org.upc.supportservice.support.domain.services.TicketCommandService;
import org.upc.supportservice.support.infrastructure.persistence.jpa.repositories.TicketRepository;

import java.util.Optional;

@Service
public class TicketCommandServiceImpl implements TicketCommandService {

    private final TicketRepository ticketRepository;

    public TicketCommandServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Optional<Ticket> handle(CreateTicketCommand command) {
        var ticket = new Ticket(
                command.title(),
                command.description(),
                command.category(),
                command.priority(),
                command.userId(),
                command.assignedTo()
        );
        var saved = ticketRepository.save(ticket);
        return Optional.of(saved);
    }

    @Override
    public Optional<Ticket> handle(UpdateTicketCommand command) {
        var ticket = ticketRepository.findById(command.id())
                .orElseThrow(() -> new TicketNotFoundException(command.id()));
        ticket.updateInformation(
                command.title(),
                command.description(),
                command.category(),
                command.priority(),
                command.status(),
                command.assignedTo()
        );
        var updated = ticketRepository.save(ticket);
        return Optional.of(updated);
    }

    @Override
    public void handle(DeleteTicketCommand command) {
        var ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(command.ticketId()));
        ticketRepository.delete(ticket);
    }
}