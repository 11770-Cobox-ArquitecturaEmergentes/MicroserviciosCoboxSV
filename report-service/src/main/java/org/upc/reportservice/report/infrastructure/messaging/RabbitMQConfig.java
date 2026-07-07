package org.upc.reportservice.report.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String REPORT_EXCHANGE = "report.exchange";
    public static final String BRONZE_QUEUE = "report.bronze.queue";
    public static final String INCIDENT_ROUTING_KEY = "incident.created";

    @Bean
    public TopicExchange reportExchange() {
        return new TopicExchange(REPORT_EXCHANGE);
    }

    @Bean
    public Queue bronzeQueue() {
        return new Queue(BRONZE_QUEUE, true);
    }

    @Bean
    public Binding bindingBronzeQueue(Queue bronzeQueue, TopicExchange reportExchange) {
        return BindingBuilder.bind(bronzeQueue).to(reportExchange).with(INCIDENT_ROUTING_KEY);
    }
}
