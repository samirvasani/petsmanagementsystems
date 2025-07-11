package com.example.petmanagement.dto;

/**
 * Represents a Data Transfer Object (DTO) for the Address entity.
 * This class is used to transfer address data between different layers of the application.
 * It contains details about the address such as city, type, name, and number.
 */
public record AddressDto(
        /**
         * The city where the address is located.
         */
        String city,

        /**
         * The type of the address, e.g., road, street, avenue.
         */
        String type,

        /**
         * The name of the address, e.g., the name of the street or avenue.
         */
        String addressName,

        /**
         * The number of the address, e.g., house or building number.
         */
        String number
) {}