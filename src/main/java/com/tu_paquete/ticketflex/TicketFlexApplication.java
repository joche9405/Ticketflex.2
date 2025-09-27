package com.tu_paquete.ticketflex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.tu_paquete.ticketflex.repository.jpa")
@EnableMongoRepositories(basePackages = "com.tu_paquete.ticketflex.repository.mongo")
public class TicketFlexApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketFlexApplication.class, args);
    }
}
