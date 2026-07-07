package org.upc.fleetservice.fleet.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.upc.fleetservice.fleet.domain.model.aggregates.Driver;
import org.upc.fleetservice.fleet.domain.model.aggregates.Route;
import org.upc.fleetservice.fleet.domain.model.aggregates.Vehicle;

import java.util.LinkedHashMap;

@Component
public class FleetReportEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public FleetReportEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishRoute(String routingKey, Route route) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("id", route.getId());
        payload.put("type", "ROUTE");
        payload.put("status", route.getRouteStatus() != null ? route.getRouteStatus().name() : null);
        payload.put("driverId", route.getDriver() != null ? route.getDriver().getId() : null);
        payload.put("vehicleId", route.getVehicle() != null ? route.getVehicle().getId() : null);
        publish(routingKey, payload);
    }

    public void publishDriver(String routingKey, Driver driver) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("id", driver.getId());
        payload.put("type", "DRIVER");
        payload.put("status", driver.getDriverStatus() != null ? driver.getDriverStatus().name() : null);
        payload.put("email", driver.getEmail());
        publish(routingKey, payload);
    }

    public void publishVehicle(String routingKey, Vehicle vehicle) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("id", vehicle.getId());
        payload.put("type", "VEHICLE");
        payload.put("status", vehicle.getVehicleStatus() != null ? vehicle.getVehicleStatus().name() : null);
        payload.put("plateNumber", vehicle.getPlateNumber());
        publish(routingKey, payload);
    }

    private void publish(String routingKey, LinkedHashMap<String, Object> payload) {
        try {
            rabbitTemplate.convertAndSend("report.exchange", routingKey, objectMapper.writeValueAsString(payload));
        } catch (Exception ex) {
            // Reporting must not block fleet workflows.
        }
    }
}
