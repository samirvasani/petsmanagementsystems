package com.example.petmanagement;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@EnableJpaAuditing
@OpenAPIDefinition(info = @Info(
        title = "Pet Management API",
        version = "1.0",
        description = "API for managing users and their pets"
))
public class PetManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetManagementApplication.class, args);
    }
}