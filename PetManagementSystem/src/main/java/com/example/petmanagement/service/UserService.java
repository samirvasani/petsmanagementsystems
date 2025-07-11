package com.example.petmanagement.service;

import com.example.petmanagement.dto.AddressDto;
import com.example.petmanagement.dto.PetResponseDto;
import com.example.petmanagement.dto.UserRequestDto;
import com.example.petmanagement.dto.UserResponseDto;
import com.example.petmanagement.exception.BadRequestException;
import com.example.petmanagement.exception.ResourceNotFoundException;
import com.example.petmanagement.model.Address;
import com.example.petmanagement.model.Pet;
import com.example.petmanagement.model.User;
import com.example.petmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *The user service contains all business logic of pet management system
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final AddressService addressService; // I can create new UserFactory class and check there if address exist there.This to avoid using addressService
    private final PetService petService;


    /**
     * Creates a new user with address after validating all constraints.
     * Assumption: for this POC method we considered only non deceased (alive) users for checking homonyms.
     * And if it exists then we still allow to add user.
     *
     * @param userRequestDto DTO containing user details (must not be null)
     * @return UserResponseDto with created user data
     * @throws BadRequestException if validation fails
     * @throws DataAccessException if database operations fail
     */
    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        LOGGER.info("Create User for the given {}", userRequestDto);
        try {
            Objects.requireNonNull(userRequestDto, "User request DTO cannot be null");
            validateUserRequest(userRequestDto);

            if(userRepository.homonymExists(userRequestDto.name(),userRequestDto.firstName()).size() >1){
                LOGGER.warn("Potential homonym detected for {} {}",
                        userRequestDto.name(), userRequestDto.firstName());
            }

            Address address = addressService.getOrCreateAddress(userRequestDto.address());

            User user = new User();
            user.setName(userRequestDto.name());
            user.setFirstName(userRequestDto.firstName());
            user.setAddress(address);
            user.setAge(userRequestDto.age());
            user.setGender(userRequestDto.gender());

            User savedUser = userRepository.save(user);
            return mapToUserResponse(savedUser);
        } catch (DataAccessException ex) {
            LOGGER.error("Database error while creating user", ex);
            throw ex;
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Invalid user creation request", ex);
            throw new BadRequestException(ex.getMessage(), ex);
        }
    }

    /**
     * Mark a User as deceased
     * This method updates the deceased status of a user to true
     *
     * @param userId the ID of the user to be marked as deceased
     * @throws ResourceNotFoundException if the user with the given ID does not exist
     * @throws BadRequestException       if an unexpected error occurs during the operation
     */
    @Transactional
    public void markUserAsDeceased(Long userId) {
        LOGGER.info("Mark user as deceased for the given user id {}", userId);
        try {
            User user = loadActiveUserWithAddress(userId);
            user.setDeceased(true);
            userRepository.save(user);
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred while marking user as deceased for user ID {}: {}", userId, e.getMessage(), e);
            throw new BadRequestException("Failed to mark user as deceased due to an unexpected error.", e);
        }
    }

    /**
     * Updates an existing user's information with the provided details.
     *
     * @param id             the ID of the user to update (must not be null)
     * @param userRequestDto the DTO containing the updated user details (must not be null)
     * @return UserResponseDto containing the updated user information
     * @throws ResourceNotFoundException if no user exists with the given ID
     * @throws BadRequestException       if id,userdto,age,gender is null
     * @throws DataAccessException       if there is a database access problem
     * @see UserRequestDto
     * @see UserResponseDto
     */
    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        LOGGER.info("Updating user with ID: {} with details: {}", id, userRequestDto);
        try {
            /*User user = userRepository.findById(id)
                    .filter(u -> !u.isDeceased())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Active user not found with id: " + id +
                                    " (either doesn't exist or is deceased)"));*/

            User user = loadActiveUserWithAddress(id);

            if (userRequestDto.name() != null) user.setName(userRequestDto.name());
            if (userRequestDto.firstName() != null) user.setFirstName(userRequestDto.firstName());
            if (userRequestDto.age() != null) user.setAge(userRequestDto.age());
            if (userRequestDto.gender() != null) user.setGender(userRequestDto.gender());
            if (userRequestDto.address() != null) {
                Address address = addressService.getOrCreateAddress(userRequestDto.address());
                user.setAddress(address);
            }

            User updatedUser = userRepository.save(user);
            return mapToUserResponse(updatedUser);
        } catch (DataAccessException ex) {
            LOGGER.error("Database error while creating user", ex);
            throw ex;
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Invalid user creation request", ex);
            throw new BadRequestException(ex.getMessage(), ex);
        }
    }

    /**
     * Assigns an active pet to an active (non-deceased) user from the same address only.
     * Also check if the pet is not assigned to a different users from different addresses. Other wise execution will be failed
     *
     * @param userId the ID of the user to assign the pet to (must not be null)
     * @param petId  the ID of the pet to assign (must not be null)
     * @return UserResponseDto containing the updated user information with assigned pet
     * @throws ResourceNotFoundException if: User or Pet doesn't exist or is deceased
     * @throws BadRequestException       if: Either ID is null,Pet is already assigned to user ,Pet is deceased<
     * @throws DataAccessException       if there's a database access problem
     * @see UserResponseDto
     */
    @Transactional
    public UserResponseDto assignPetToUser(Long userId, Long petId) {
        LOGGER.info("Assign a pet {} to a user {} ", petId, userId);
        try {
            // Validation
            validateIds(userId, petId);

            // Entity loading
            User user = loadActiveUserWithAddress(userId);
            Pet pet = loadActivePetWithOwners(petId,true);

            // Business rule validation
            validateAssignmentRules(user, pet);

            // Assignment execution
            executeAssignment(user, pet);

            return mapToUserResponse(user);
        } catch (DataAccessException ex) {
            LOGGER.error("Database error while creating user", ex);
            throw ex;
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Invalid user and pet assignment ", ex);
            throw new BadRequestException(ex.getMessage(), ex);
        }
    }

    /**
     * Removes a pet assignment from a user if the relationship exists and irrespective of the pet deceased status
     * Also checks homonyms user and if a pet is not assigned to a given user then we stop the flow there
     * Validates both user and pet exist and are active (not deceased).
     * Verifies the pet is currently assigned to the user
     * Maintains data integrity by properly clearing bidirectional relationships
     *
     * @param userId ID of the user (must not be null)
     * @param petId  ID of the pet to remove (must not be null)
     * @throws BadRequestException       if: Either ID is null , Pet is not currently assigned to the user
     * @throws ResourceNotFoundException if: User doesn't exist or is deceased , Pet doesn't exist or is deceased
     * @throws DataAccessException       if there's a database access problem during the operation
     */
    @Transactional
    public void removePetFromUser(Long userId, Long petId) {
        LOGGER.info("remove pets{} under a user{} ", petId, userId);
        try {
            // Validate inputs
            Objects.requireNonNull(userId, "User ID cannot be null");
            Objects.requireNonNull(petId, "Pet ID cannot be null");

            User user = loadActiveUserWithAddress(userId);
            Pet pet = loadActivePetWithOwners(petId,false);

            // Check for homonyms
            List<User> homonymsUsers = userRepository.homonymExists(
                    user.getName(), user.getFirstName());

            if (homonymsUsers.size() > 1) {
                // For homonyms, verify pet is assigned to THIS specific user
                if (!pet.getOwners().contains(user)) {
                    throw new BadRequestException(
                            String.format("Ambiguous removal: Pet %s is not assigned to user %s "
                                            + "(found %d users with same name)",
                                    petId, userId, homonymsUsers.size()));
                }

                // Additional verification - address must match
                boolean addressMatch = pet.getOwners().stream()
                        .anyMatch(owner -> owner.getAddress().equals(user.getAddress()));

                if (!addressMatch) {
                    throw new BadRequestException(
                            String.format("Address mismatch for pet removal: "
                                    + "User %s address doesn't match pet's owners", userId));
                }
            }

            // Check if pet is assigned to user
            if (!user.getPets().contains(pet)) {
                throw new BadRequestException(
                        String.format("Pet %s is not assigned to user %s", petId, userId));
            }

            user.getPets().remove(pet);
            userRepository.save(user);
        } catch (DataAccessException ex) {
            LOGGER.error("Database error while removing Pet {} from User {} ", petId, userId, ex);
            throw ex;
        }
    }

    /**
     * Retrieves active users of active pets by given city
     * @param petType :dog,cat etc
     * @param city give city
     * @return list of users
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsersByPetTypeAndCity(String petType, String city) {
        return userRepository.findUserByPetTypeAndCity(petType, city)
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    /**
     * Maps user data fetched from table to UserResponseDto so that it can be passed on to the client
     *
     * @param user contains user details
     * @return UserResponseDto generated userresponse
     */
    private UserResponseDto mapToUserResponse(User user) {
        Set<PetResponseDto> petResponses = user.getPets().stream()
                .map(petService::mapToPetResponseDto)
                .collect(Collectors.toSet());

        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getFirstName(),
                new AddressDto(
                        user.getAddress().getCity(),
                        user.getAddress().getType(),
                        user.getAddress().getAddressName(),
                        user.getAddress().getNumber()
                ),
                user.getAge(),
                user.getGender(),
                user.isDeceased(),
                petResponses
        );
    }

    private void validateUserRequest(UserRequestDto dto) {

        if (dto.name() == null || dto.name().trim().isEmpty()) {
            throw new BadRequestException("User name is required");
        }
        if (dto.age() != null && dto.age() <= 0) {
            throw new BadRequestException("Age must be positive");
        }
        if (dto.gender() == null || !Set.of("MALE", "FEMALE", "OTHER").contains(dto.gender().toUpperCase())) {
            throw new BadRequestException("Invalid gender value");
        }
    }

    // ===== Helper Methods ===== //

    private void validateIds(Long userId, Long petId) {
        if (userId == null || petId == null) {
            throw new BadRequestException("User ID and Pet ID cannot be null");
        }
    }

    private User loadActiveUserWithAddress(Long userId) {
        return userRepository.findActiveUserWithAddress(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active user not found with id: " + userId +
                                " (either doesn't exist or is deceased)"));
    }

    private Pet loadActivePetWithOwners(Long petId, boolean activePetRequired) {
        return petService.findActivePetWithOwners(petId, activePetRequired);
    }

    private void validateAssignmentRules(User user, Pet pet) {
        if (isAlreadyAssigned(user, pet)) {
            throw new BadRequestException(
                    String.format("Pet %s already assigned to user %s",
                            pet.getId(), user.getId()));
        }

        if (hasExistingOwnersWithDifferentAddress(user, pet)) {
            throw new BadRequestException(
                    "Cannot assign pet - address doesn't match existing owners");
        }
    }

    private boolean isAlreadyAssigned(User user, Pet pet) {
        return user.getPets().contains(pet);
    }

    private boolean hasExistingOwnersWithDifferentAddress(User user, Pet pet) {
        return !pet.getOwners().isEmpty() &&
                pet.getOwners().stream()
                        .noneMatch(owner -> owner.getAddress().equals(user.getAddress()));
    }

    private void executeAssignment(User user, Pet pet) {
        user.addPet(pet);
        userRepository.save(user);
        logAssignment(user, pet);
    }

    private void logAssignment(User user, Pet pet) {
        LOGGER.info("Assigned pet {} to user {} at address {}",
                pet.getId(), user.getId(), user.getAddress().getId());
    }
}