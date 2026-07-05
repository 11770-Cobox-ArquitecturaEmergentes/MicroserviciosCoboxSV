package org.upc.aivalidationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AiValidationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiValidationServiceApplication.class, args);
    }
}
