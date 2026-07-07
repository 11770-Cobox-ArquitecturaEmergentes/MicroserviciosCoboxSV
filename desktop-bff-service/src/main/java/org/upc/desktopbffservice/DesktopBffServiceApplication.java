package org.upc.desktopbffservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DesktopBffServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DesktopBffServiceApplication.class, args);
    }
}
