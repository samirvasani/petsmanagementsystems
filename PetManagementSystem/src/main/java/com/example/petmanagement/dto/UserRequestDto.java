package com.example.petmanagement.dto;

/**
 * Represents a Data Transfer Object (DTO) for creating or updating a User entity.
 * This class is used to transfer user data between different layers of the application.
 * It contains details about the user such as name, first name, address, age, and gender.
 */
public record UserRequestDto(
        /**
         * The name of the user.
         */
        String name,

        /**
         * The first name of the user.
         */
        String firstName,

        /**
         * The address associated with the user.
         * This is represented as an AddressDto object.
         */
        AddressDto address,

        /**
         * The age of the user.
         */
        Integer age,

        /**
         * The gender of the user.
         * This can be one of the following values: MALE, FEMALE, OTHER.
         */
        String gender
) {}