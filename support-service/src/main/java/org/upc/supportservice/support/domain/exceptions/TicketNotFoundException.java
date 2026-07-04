package org.upc.supportservice.support.domain.exceptions;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(Long ticketId) {
        super("Ticket with ID " + ticketId + " not found.");
    }
}