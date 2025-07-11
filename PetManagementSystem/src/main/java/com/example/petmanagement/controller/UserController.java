package com.example.petmanagement.controller;

import com.example.petmanagement.dto.UserRequestDto;
import com.example.petmanagement.dto.UserResponseDto;
import com.example.petmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users and their pets")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    /**
     * Service for handling user-related operations.
     */
    private final UserService userService;

    /**
     * Creates a new user with the provided details.
     *
     * @param userRequestDto User creation data including name, age, gender and address
     * @return Created user details with system-generated ID
     * @see UserRequestDto
     * @see UserResponseDto
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user",
            description = "Creates a new user with the provided details. Returns the created user information.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user details provided"),
            @ApiResponse(responseCode = "404", description = "Required resources not found"),
            @ApiResponse(responseCode = "409", description = "Data conflict (e.g. duplicate entry)"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access by user"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public UserResponseDto createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        LOGGER.info("Creating a new user with details: {}", userRequestDto);
        return userService.createUser(userRequestDto);
    }

    /**
     * Marks a user as deceased by their ID.
     *
     * @param id ID of the user to mark as deceased
     */
    @PutMapping("/{id}/deceased")
    @Operation(summary = "Mark user as deceased",
            description = "Updates a user's status to deceased. This operation cannot be undone.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User successfully marked as deceased"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID provided"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "User already marked as deceased"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access by user"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markUserAsDeceased(@PathVariable Long id) {
        LOGGER.info("Update user status to deceased for the userId: {}", id);
        userService.markUserAsDeceased(id);
    }

    /**
     * Updates user information for the specified user ID.
     *
     * @param id ID of the user to update
     * @param userRequestDto Updated user details including name, age, gender, and address
     * @return Updated user information with all fields
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user information",
            description = "Updates the user details for the specified ID. All fields will be updated with provided values.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user details provided"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Data conflict "),
            @ApiResponse(responseCode = "403", description = "Unauthorized access by user"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public UserResponseDto updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDto userRequestDto) {
        LOGGER.info("Updates user information for the specified user id {} and user  {}", id,userRequestDto);
        return userService.updateUser(id, userRequestDto);
    }

    /**
     * Assigns a pet to a user if business conditions are met.
     *
     * @param userId ID of the user to assign the pet to
     * @param petId ID of the pet to be assigned
     * @return Updated user information with assigned pet
     * @see UserResponseDto
     */
    @PostMapping("/{userId}/pets/{petId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Assign pet to user",
            description = "Creates a pet ownership relationship between user and pet. "
                    + "Assignment succeeds if: "
                    + "1) Pet has no existing owners, OR "
                    + "2) User shares address with pet's current owners")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pet successfully assigned to user"),
            @ApiResponse(responseCode = "400", description = "Invalid assignment request"),
            @ApiResponse(responseCode = "404", description = "User or pet not found"),
            @ApiResponse(responseCode = "409", description = "Data conflict (e.g. address mismatch)"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access by user"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public UserResponseDto assignPetToUser(
            @PathVariable Long userId,
            @PathVariable Long petId) {
        LOGGER.info("Assigning pet {} to user {}", petId, userId);
        return userService.assignPetToUser(userId, petId);
    }


    /**
     * Removes a pet from a user's ownership (soft delete).
     * Sets the 'deceased' flag to true rather than physically deleting the relationship.
     *
     *
     * @param userId ID of the user from whom to remove the pet
     * @param petId ID of the pet to be marked as deceased
     */
    @DeleteMapping("/{userId}/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft delete pet from user",
            description = "Marks the pet as deceased rather than physically deleting. "
                    + "Maintains referential integrity while logically removing the pet.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pet successfully marked as deceased"),
            @ApiResponse(responseCode = "400", description = "Invalid request - pet not assigned to user"),
            @ApiResponse(responseCode = "404", description = "User or pet not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access by user")
    })
    public void removePetFromUser(
            @PathVariable Long userId,
            @PathVariable Long petId) {
        LOGGER.info("Soft deleting pet {} from user {}", petId, userId);
        userService.removePetFromUser(userId, petId);
    }



    /**
     * Retrieves all active users who own a specific type of pet in a given city.
     * Returns a list of non-deceased users that have at least one living pet of the specified type
     * and reside in the requested city. The response includes complete user details with their
     * associated pets.
     *
     * @param petType Type of pet to filter by (e.g., "dog", "cat", "bird"). Case-sensitive.
     * @param city    City name to filter by. Case-sensitive.
     * @return List of {@link UserResponseDto} objects containing user information and their pets.
     *         Returns empty list if no matching users found.
     */
    @GetMapping("/by-pet-and-city")
    @Operation(
            summary = "Get users by pet type and city",
            description = "Retrieves active users owning living pets of specified type in the given city. "
                    + "Includes complete user details with their pet information. "
                    + "Both parameters are required and case-sensitive."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "Successfully retrieved users matching criteria"),
            @ApiResponse(responseCode = "400",description = "Invalid input parameters - petType or city missing/empty"),
            @ApiResponse(responseCode = "500",description = "Internal server error while processing request"),
            @ApiResponse(responseCode = "404", description = "Pet or city not found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access by user"),
    })
    public List<UserResponseDto> getUsersByPetTypeAndCity(
            @RequestParam String petType,
            @RequestParam String city) {
        return userService.getUsersByPetTypeAndCity(petType, city);
    }
}