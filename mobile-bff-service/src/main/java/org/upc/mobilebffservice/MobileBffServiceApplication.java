package org.upc.mobilebffservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MobileBffServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MobileBffServiceApplication.class, args);
    }
}
