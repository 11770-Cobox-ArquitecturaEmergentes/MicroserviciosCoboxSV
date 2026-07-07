package org.upc.incidentservice.incident.interfaces.rest;

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
import org.upc.incidentservice.incident.domain.model.commands.AssignResponsibleUserCommand;
import org.upc.incidentservice.incident.domain.model.commands.CreateIncidentCommand;
import org.upc.incidentservice.incident.domain.model.commands.UpdateIncidentStatusCommand;
import org.upc.incidentservice.incident.domain.model.queries.GetAllIncidentsQuery;
import org.upc.incidentservice.incident.domain.model.queries.GetIncidentByIdQuery;
import org.upc.incidentservice.incident.domain.model.queries.GetIncidentBySourceAlertIdQuery;
import org.upc.incidentservice.incident.domain.model.queries.GetIncidentByTechnicalIdQuery;
import org.upc.incidentservice.incident.domain.services.IncidentCommandService;
import org.upc.incidentservice.incident.domain.services.IncidentQueryService;
import org.upc.incidentservice.incident.interfaces.rest.resources.*;
import org.upc.incidentservice.incident.interfaces.rest.transform.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/incidents", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Incidents", description = "Incident Management Endpoints")
public class IncidentController {

    private final IncidentCommandService incidentCommandService;
    private final IncidentQueryService incidentQueryService;

    public IncidentController(IncidentCommandService incidentCommandService, IncidentQueryService incidentQueryService) {
        this.incidentCommandService = incidentCommandService;
        this.incidentQueryService = incidentQueryService;
    }

    @Operation(summary = "Create a new incident",
            description = "Creates a new incident and returns its full representation.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Incident created successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = IncidentResource.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data provided",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @PostMapping
    public ResponseEntity<IncidentResource> createIncident(@Valid @RequestBody CreateIncidentResource resource) {
        var command = CreateIncidentCommandFromResourceAssembler.toCommandFromResource(resource);
        var technicalId = incidentCommandService.handle(command);
        var incident = incidentQueryService.handle(new GetIncidentByTechnicalIdQuery(technicalId));
        if (incident.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var incidentResource = IncidentResourceFromEntityAssembler.toResourceFromEntity(incident.get());
        return new ResponseEntity<>(incidentResource, HttpStatus.CREATED);
    }

    @Operation(summary = "Update the status of an incident",
            description = "Updates the lifecycle status of a specific incident identified by its UUID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Incident status updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = IncidentResource.class))),
                    @ApiResponse(responseCode = "404", description = "Incident not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @PatchMapping("/{incidentId}/status")
    public ResponseEntity<IncidentResource> updateIncidentStatus(
            @PathVariable UUID incidentId,
            @Valid @RequestBody UpdateIncidentStatusResource resource) {
        var command = UpdateIncidentStatusCommandFromResourceAssembler.toCommandFromResource(incidentId, resource);
        incidentCommandService.handle(command);
        var incident = incidentQueryService.handle(new GetIncidentByIdQuery(incidentId));
        if (incident.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var incidentResource = IncidentResourceFromEntityAssembler.toResourceFromEntity(incident.get());
        return ResponseEntity.ok(incidentResource);
    }

    @Operation(summary = "Assign a responsible user to an incident",
            description = "Assigns a responsible user to a specific incident identified by its UUID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Incident updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = IncidentResource.class))),
                    @ApiResponse(responseCode = "404", description = "Incident not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @PatchMapping("/{incidentId}/assign")
    public ResponseEntity<IncidentResource> assignResponsibleUser(
            @PathVariable UUID incidentId,
            @Valid @RequestBody AssignResponsibleUserResource resource) {
        var command = AssignResponsibleUserCommandFromResourceAssembler.toCommandFromResource(incidentId, resource);
        incidentCommandService.handle(command);
        var incident = incidentQueryService.handle(new GetIncidentByIdQuery(incidentId));
        if (incident.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var incidentResource = IncidentResourceFromEntityAssembler.toResourceFromEntity(incident.get());
        return ResponseEntity.ok(incidentResource);
    }

    @Operation(summary = "Get an incident by its source AI alert",
            description = "Retrieves the incident linked to a SmartVision alert UUID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Incident found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = IncidentResource.class))),
                    @ApiResponse(responseCode = "404", description = "Incident not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @GetMapping("/source/ai-alert/{alertId}")
    public ResponseEntity<IncidentResource> getIncidentByAiAlertId(@PathVariable UUID alertId) {
        var incident = incidentQueryService.handle(new GetIncidentBySourceAlertIdQuery(alertId));
        if (incident.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var incidentResource = IncidentResourceFromEntityAssembler.toResourceFromEntity(incident.get());
        return ResponseEntity.ok(incidentResource);
    }

    @Operation(summary = "Get an incident by its UUID",
            description = "Retrieves the details of a single incident by its business UUID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Incident found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = IncidentResource.class))),
                    @ApiResponse(responseCode = "404", description = "Incident not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @GetMapping("/{incidentId}")
    public ResponseEntity<IncidentResource> getIncidentById(@PathVariable UUID incidentId) {
        var incident = incidentQueryService.handle(new GetIncidentByIdQuery(incidentId));
        if (incident.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var incidentResource = IncidentResourceFromEntityAssembler.toResourceFromEntity(incident.get());
        return ResponseEntity.ok(incidentResource);
    }

    @Operation(summary = "Get all incidents",
            description = "Retrieves a list of all existing incidents.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of incidents retrieved successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = IncidentResource.class))))
            })
    @GetMapping
    public ResponseEntity<List<IncidentResource>> getAllIncidents() {
        var incidents = incidentQueryService.handle(new GetAllIncidentsQuery());
        var incidentResources = incidents.stream()
                .map(IncidentResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(incidentResources);
    }
}
