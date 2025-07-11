package com.example.petmanagement.controller;

import com.example.petmanagement.dto.PetRequestDto;
import com.example.petmanagement.dto.PetResponseDto;
import com.example.petmanagement.dto.UserRequestDto;
import com.example.petmanagement.dto.UserResponseDto;
import com.example.petmanagement.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * Controller for managing pets and their owners.
 * Provides endpoints for CRUD operations and querying pets based on various criteria.
 */
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@Tag(name = "Pet Management", description = "APIs for managing pets and their owners")
public class PetController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PetController.class);

    /**
     * Service for handling pet-related operations.
     */
    private final PetService petService;

    /**
     * Creates a new Pet with the provided details.
     *
     * @param petRequestDto pet creation data including name, age, type
     * @return Created pet details with system-generated ID
     * @see PetRequestDto
     * @see PetResponseDto
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new pet",
            description = "Creates a new pet with the provided details. Returns the created pet information.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pet created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pet details provided"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access by user"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public PetResponseDto createPet(@Valid @RequestBody PetRequestDto petRequestDto) {
        LOGGER.info("Creating a new pet with details: {}", petRequestDto);
        return petService.createPet(petRequestDto);
    }

    @GetMapping("/by-city")
    @Operation(summary = "Get pets by city",
            description = "Retrieves a list of non deceased pets located in the specified city.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pets retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "City not found"),
            @ApiResponse(responseCode = "400", description = "Invalid city name provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access by user"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Page<PetResponseDto> getPetsByCity(
            @RequestParam String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        LOGGER.info("Fetching pets in city: {}", city);
        return petService.getPetsByCity(city,page,size);
    }

    @GetMapping("/by-women-in-city")
    @Operation(summary = "Get pets owned by women in a city",
            description = "Retrieves a list of pets owned by women in the specified city.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pets retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid city name provided"),
            @ApiResponse(responseCode = "404", description = "City not found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access by user"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Page<PetResponseDto> getPetsByWomenOwnersInCity(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        LOGGER.info("Fetching paginated pets owned by women in city: {}", city);
        return petService.getPetsByWomenInCity(city, page, size);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Update pet information",
            description = "Updates the information of a pet with the specified ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Pet updated successfully"),
            @ApiResponse(responseCode = "404", description = "Pet not found"),
            @ApiResponse(responseCode = "400", description = "Invalid pet details provided"),
            @ApiResponse(responseCode = "409", description = "Data conflict"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access by user"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public PetResponseDto updatePet(
            @PathVariable Long id,
            @Valid @RequestBody PetRequestDto petRequestDto) {
        LOGGER.info("Updating pet with ID: {} and details: {}", id, petRequestDto);
        return petService.updatePet(id, petRequestDto);
    }

    @PutMapping("/{id}/deceased")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark pet as deceased",
            description = "Marks a pet as deceased by its ID. Returns 404 if the pet is not found.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pet marked as deceased successfully"),
            @ApiResponse(responseCode = "404", description = "Pet not found"),
            @ApiResponse(responseCode = "400", description = "Invalid pet ID"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access by user"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void markPetAsDeceased(@PathVariable Long id) {
        LOGGER.info("Marking pet as deceased with ID: {}", id);
        petService.markPetAsDeceased(id);
    }

    @GetMapping("/{userId}/pets")
    @Operation(summary = "Get all pets owned by a user",
            description = "Retrieves a list of all pets owned by the specified user ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pets retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid userId"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access by user"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<PetResponseDto> getUserPets(@PathVariable Long userId) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        LOGGER.info("Fetching all pets for user ID: {}", userId);
        return petService.getPetsByOwnerId(userId);
    }
}