package com.example.poisson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PoissonBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(PoissonBackendApplication.class, args);
    }
}

