package com.example.petmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

/**
 * Represents a Data Transfer Object (DTO) for the User entity.
 * This class is used to transfer user data between different layers of the application.
 * It contains details about the user such as ID, name, first name, address,
 * age, gender, deceased status, and associated pets.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record UserResponseDto(
        /**
         * The unique identifier for the user.
         */
        Long id,

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
        String gender,

        /**
         * Indicates whether the user is deceased.
         */
        boolean deceased,

        /**
         * The set of pets associated with the user.
         * This is represented as a collection of PetResponseDto objects.
         */
        Set<PetResponseDto> pets
) {}