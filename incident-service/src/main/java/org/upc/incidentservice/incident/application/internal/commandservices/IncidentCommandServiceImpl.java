package org.upc.incidentservice.incident.application.internal.commandservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.stereotype.Service;
import org.upc.incidentservice.incident.domain.exceptions.IncidentNotFoundException;
import org.upc.incidentservice.incident.domain.model.aggregates.Incident;
import org.upc.incidentservice.incident.domain.model.commands.AssignResponsibleUserCommand;
import org.upc.incidentservice.incident.domain.model.commands.CreateIncidentCommand;
import org.upc.incidentservice.incident.domain.model.commands.UpdateIncidentStatusCommand;
import org.upc.incidentservice.incident.domain.model.valueobjects.IncidentId;
import org.upc.incidentservice.incident.domain.services.IncidentCommandService;
import org.upc.incidentservice.incident.infrastructure.persistence.jpa.repositories.IncidentRepository;

import java.util.LinkedHashMap;

@Service
public class IncidentCommandServiceImpl implements IncidentCommandService {

    private final IncidentRepository incidentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public IncidentCommandServiceImpl(IncidentRepository incidentRepository, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.incidentRepository = incidentRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Long handle(CreateIncidentCommand command) {
        var incident = new Incident(command);
        incidentRepository.save(incident);
        publishIncidentEvent("incident.created", incident);
        
        return incident.getId();
    }

    @Override
    public void handle(UpdateIncidentStatusCommand command) {
        var incidentId = new IncidentId(command.incidentId());
        incidentRepository.findByIncidentId(incidentId)
                .map(incident -> {
                    incident.updateStatus(command);
                    incidentRepository.save(incident);
                    publishIncidentEvent("incident.status-updated", incident);
                    return incident.getId();
                })
                .orElseThrow(() -> new IncidentNotFoundException(command.incidentId()));
    }

    @Override
    public void handle(AssignResponsibleUserCommand command) {
        var incidentId = new IncidentId(command.incidentId());
        incidentRepository.findByIncidentId(incidentId)
                .map(incident -> {
                    incident.assignResponsible(command);
                    incidentRepository.save(incident);
                    publishIncidentEvent("incident.assigned", incident);
                    return incident.getId();
                })
                .orElseThrow(() -> new IncidentNotFoundException(command.incidentId()));
    }

    private void publishIncidentEvent(String routingKey, Incident incident) {
        try {
            var payload = new LinkedHashMap<String, Object>();
            payload.put("id", incident.getIncidentId() != null ? incident.getIncidentId().incidentId() : null);
            payload.put("technicalId", incident.getId());
            payload.put("type", incident.getType());
            payload.put("description", incident.getDescription());
            payload.put("severity", incident.getSeverity() != null ? incident.getSeverity().name() : null);
            payload.put("status", incident.getStatus() != null ? incident.getStatus().name() : null);
            payload.put("reportedAt", incident.getReportedAt());
            payload.put("responsibleUserId", incident.getResponsibleUserId() != null ? incident.getResponsibleUserId().responsibleUserId() : null);
            payload.put("sourceType", incident.getSourceType());
            payload.put("sourceAlertId", incident.getSourceAlertId());
            payload.put("sourceClientEvidenceId", incident.getSourceClientEvidenceId());
            String message = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend("report.exchange", routingKey, message);
        } catch (JsonProcessingException | RuntimeException e) {
            // Reporting must not block the transactional incident workflow.
        }
    }
}
