package com.example.petmanagement.dto;

/**
 * Represents a Data Transfer Object (DTO) for creating or updating a Pet entity.
 * This class is used to transfer pet data between different layers of the application.
 * It contains details about the pet such as name, age, and type.
 */
public record PetRequestDto(
        /**
         * The name of the pet.
         */
        String name,

        /**
         * The age of the pet.
         */
        Integer age,

        /**
         * The type of the pet, e.g., spider, snake, cat, dog.
         */
        String type
) {}