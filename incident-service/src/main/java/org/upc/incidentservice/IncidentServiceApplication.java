package org.upc.incidentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class IncidentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentServiceApplication.class, args);
    }
}
