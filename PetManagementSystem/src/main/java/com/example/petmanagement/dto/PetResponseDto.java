package com.example.petmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a Data Transfer Object (DTO) for the Pet entity.
 * This class is used to transfer pet data between different layers of the application.
 * It contains details about the pet such as ID, name, age, type, and deceased status.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PetResponseDto(
        /**
         * The unique identifier for the pet.
         */
        Long id,

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
        String type,

        /**
         * Indicates whether the pet is deceased.
         */
        boolean deceased
) {}