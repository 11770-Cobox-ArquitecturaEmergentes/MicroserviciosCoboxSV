package org.upc.reportservice.report.infrastructure.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.upc.reportservice.report.domain.model.entities.BronzeEvent;
import org.upc.reportservice.report.infrastructure.persistence.jpa.repositories.BronzeEventRepository;

@Component
public class BronzeEventListener {

    private final BronzeEventRepository bronzeEventRepository;

    public BronzeEventListener(BronzeEventRepository bronzeEventRepository) {
        this.bronzeEventRepository = bronzeEventRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.BRONZE_QUEUE)
    public void receiveIncidentCreated(String message) {
        System.out.println("Received message in Bronze Layer: " + message);
        BronzeEvent event = new BronzeEvent();
        event.setEventType("INCIDENT_CREATED");
        event.setRawData(message);
        bronzeEventRepository.save(event);
    }
}
