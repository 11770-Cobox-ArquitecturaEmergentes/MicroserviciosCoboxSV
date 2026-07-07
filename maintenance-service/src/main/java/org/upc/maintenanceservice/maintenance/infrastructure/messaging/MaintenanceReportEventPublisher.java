package org.upc.maintenanceservice.maintenance.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.upc.maintenanceservice.maintenance.domain.model.aggregates.MaintenanceOrder;
import org.upc.maintenanceservice.maintenance.domain.model.aggregates.MaintenanceSchedule;

import java.util.LinkedHashMap;

@Component
public class MaintenanceReportEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public MaintenanceReportEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishOrder(String routingKey, MaintenanceOrder order) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("id", order.getId());
        payload.put("vehicleId", order.getVehicleId() != null ? order.getVehicleId().vehicleId() : null);
        payload.put("type", order.getMaintenanceType() != null ? order.getMaintenanceType().name() : null);
        payload.put("priority", order.getPriority() != null ? order.getPriority().name() : null);
        payload.put("status", order.getStatus() != null ? order.getStatus().name() : null);
        payload.put("reason", order.getReason() != null ? order.getReason().name() : null);
        payload.put("technicianId", order.getTechnicianId());
        publish(routingKey, payload);
    }

    public void publishSchedule(String routingKey, MaintenanceSchedule schedule) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("id", schedule.getId());
        payload.put("vehicleId", schedule.getVehicleId() != null ? schedule.getVehicleId().vehicleId() : null);
        payload.put("status", schedule.getStatus() != null ? schedule.getStatus().name() : null);
        payload.put("lastEvaluationAt", schedule.getLastEvaluationAt());
        payload.put("nextEvaluationAt", schedule.getNextEvaluationAt());
        publish(routingKey, payload);
    }

    private void publish(String routingKey, LinkedHashMap<String, Object> payload) {
        try {
            rabbitTemplate.convertAndSend("report.exchange", routingKey, objectMapper.writeValueAsString(payload));
        } catch (Exception ex) {
            // Reporting must not block maintenance workflows.
        }
    }
}
