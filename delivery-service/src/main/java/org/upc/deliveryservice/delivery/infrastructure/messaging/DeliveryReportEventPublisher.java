package org.upc.deliveryservice.delivery.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.upc.deliveryservice.delivery.domain.model.aggregates.Order;

import java.util.LinkedHashMap;

@Component
public class DeliveryReportEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public DeliveryReportEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishOrder(String routingKey, Order order) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("id", order.getId());
        payload.put("type", "ORDER");
        payload.put("status", order.getOrderStatus() != null ? order.getOrderStatus().name() : null);
        payload.put("clientId", order.getClientId() != null ? order.getClientId().clientId() : null);
        payload.put("weightKg", order.getWeightKg() != null ? order.getWeightKg().getWeightKg() : null);
        publish(routingKey, payload);
    }

    private void publish(String routingKey, LinkedHashMap<String, Object> payload) {
        try {
            rabbitTemplate.convertAndSend("report.exchange", routingKey, objectMapper.writeValueAsString(payload));
        } catch (Exception ex) {
            // Reporting must not block delivery workflows.
        }
    }
}
