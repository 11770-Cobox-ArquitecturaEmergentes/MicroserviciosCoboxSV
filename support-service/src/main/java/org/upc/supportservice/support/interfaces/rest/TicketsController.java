package org.upc.supportservice.support.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.upc.supportservice.support.domain.model.commands.DeleteTicketCommand;
import org.upc.supportservice.support.domain.model.queries.*;
import org.upc.supportservice.support.domain.model.valueobjects.TicketCategory;
import org.upc.supportservice.support.domain.model.valueobjects.TicketPriority;
import org.upc.supportservice.support.domain.model.valueobjects.TicketStatus;
import org.upc.supportservice.support.domain.services.TicketCommandService;
import org.upc.supportservice.support.domain.services.TicketQueryService;
import org.upc.supportservice.support.interfaces.rest.resources.CreateTicketResource;
import org.upc.supportservice.support.interfaces.rest.resources.TicketResource;
import org.upc.supportservice.support.interfaces.rest.resources.UpdateTicketResource;
import org.upc.supportservice.support.interfaces.rest.transform.CreateTicketCommandFromResourceAssembler;
import org.upc.supportservice.support.interfaces.rest.transform.TicketResourceFromEntityAssembler;
import org.upc.supportservice.support.interfaces.rest.transform.UpdateTicketCommandFromResourceAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Tickets", description = "Ticket Management Endpoints")
public class TicketsController {

    private final TicketCommandService ticketCommandService;
    private final TicketQueryService ticketQueryService;

    public TicketsController(TicketCommandService ticketCommandService, TicketQueryService ticketQueryService) {
        this.ticketCommandService = ticketCommandService;
        this.ticketQueryService = ticketQueryService;
    }

    @Operation(summary = "Create a new ticket",
            description = "Creates a new support ticket with the provided information.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Ticket created successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TicketResource.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid ticket data",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @PostMapping
    public ResponseEntity<TicketResource> createTicket(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateTicketResource resource) {
        var createTicketCommand = CreateTicketCommandFromResourceAssembler.toCommandFromResource(resource, jwt.getSubject());
        var ticket = ticketCommandService.handle(createTicketCommand);
        if (ticket.isEmpty()) return ResponseEntity.badRequest().build();
        var ticketResource = TicketResourceFromEntityAssembler.toResourceFromEntity(ticket.get());
        return new ResponseEntity<>(ticketResource, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all tickets",
            description = "Retrieves a list of all support tickets in the system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = TicketResource.class))))
            })
    @GetMapping
    public ResponseEntity<List<TicketResource>> getAllTickets() {
        var tickets = ticketQueryService.handle(new GetAllTicketsQuery());
        var resources = tickets.stream()
                .map(TicketResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Get ticket by id",
            description = "Retrieves a support ticket by its unique identifier.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ticket retrieved successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TicketResource.class))),
                    @ApiResponse(responseCode = "404", description = "Ticket not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @GetMapping("/{id}")
    public ResponseEntity<TicketResource> getTicketById(@PathVariable Long id) {
        var ticket = ticketQueryService.handle(new GetTicketByIdQuery(id));
        if (ticket.isEmpty()) return ResponseEntity.notFound().build();
        var ticketResource = TicketResourceFromEntityAssembler.toResourceFromEntity(ticket.get());
        return ResponseEntity.ok(ticketResource);
    }

    @Operation(summary = "Update a ticket",
            description = "Updates the information of an existing support ticket.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ticket updated successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TicketResource.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid ticket data",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "404", description = "Ticket not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @PutMapping("/{id}")
    public ResponseEntity<TicketResource> updateTicket(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateTicketResource resource) {
        var updateTicketCommand = UpdateTicketCommandFromResourceAssembler.toCommandFromResource(resource, id);
        var ticket = ticketCommandService.handle(updateTicketCommand);
        if (ticket.isEmpty()) return ResponseEntity.notFound().build();
        var ticketResource = TicketResourceFromEntityAssembler.toResourceFromEntity(ticket.get());
        return ResponseEntity.ok(ticketResource);
    }

    @Operation(summary = "Delete a ticket",
            description = "Deletes a support ticket by its unique identifier.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Ticket deleted successfully",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Ticket not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketCommandService.handle(new DeleteTicketCommand(id));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get tickets by user",
            description = "Retrieves all support tickets created by a given user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = TicketResource.class))))
            })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketResource>> getTicketsByUser(@PathVariable String userId) {
        var tickets = ticketQueryService.handle(new GetTicketsByUserIdQuery(userId));
        var resources = tickets.stream()
                .map(TicketResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Get tickets by status",
            description = "Retrieves all support tickets filtered by status.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = TicketResource.class)))),
                    @ApiResponse(responseCode = "400", description = "Invalid status value",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TicketResource>> getTicketsByStatus(@PathVariable TicketStatus status) {
        var tickets = ticketQueryService.handle(new GetTicketsByStatusQuery(status));
        var resources = tickets.stream()
                .map(TicketResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Get tickets by priority",
            description = "Retrieves all support tickets filtered by priority.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = TicketResource.class)))),
                    @ApiResponse(responseCode = "400", description = "Invalid priority value",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TicketResource>> getTicketsByPriority(@PathVariable TicketPriority priority) {
        var tickets = ticketQueryService.handle(new GetTicketsByPriorityQuery(priority));
        var resources = tickets.stream()
                .map(TicketResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Get tickets by category",
            description = "Retrieves all support tickets filtered by category.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = TicketResource.class)))),
                    @ApiResponse(responseCode = "400", description = "Invalid category value",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TicketResource>> getTicketsByCategory(@PathVariable TicketCategory category) {
        var tickets = ticketQueryService.handle(new GetTicketsByCategoryQuery(category));
        var resources = tickets.stream()
                .map(TicketResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}