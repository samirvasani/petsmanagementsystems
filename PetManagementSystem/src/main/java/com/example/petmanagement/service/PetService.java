package com.example.petmanagement.service;

import com.example.petmanagement.dto.PetRequestDto;
import com.example.petmanagement.dto.PetResponseDto;
import com.example.petmanagement.exception.BadRequestException;
import com.example.petmanagement.exception.ResourceNotFoundException;
import com.example.petmanagement.model.Pet;
import com.example.petmanagement.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 *
 */
@Service
@RequiredArgsConstructor
public class PetService {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PetService.class);

    private final PetRepository petRepository;

    /**
     * This method create pet.If pet validation failes then it throws BadRequestException
     *
     * @param petRequestDto contains pet details comes from client
     * @return PetResponseDto which contains response to be send back to the client
     */
    @Transactional
    public PetResponseDto createPet(PetRequestDto petRequestDto) {
        LOGGER.info("Create Pet for the given {}", petRequestDto);
        try {
            Objects.requireNonNull(petRequestDto, "Pet request DTO cannot be null");
            validatePetRequest(petRequestDto);

            Pet pet = new Pet();
            pet.setName(petRequestDto.name());
            pet.setAge(petRequestDto.age());
            pet.setType(petRequestDto.type());

            Pet savedPet = petRepository.save(pet);
            return mapToPetResponseDto(savedPet);
        } catch (DataAccessException ex) {
            LOGGER.error("Database error while creating Pet", ex);
            throw ex;
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Invalid pet creation request", ex);
            throw new BadRequestException(ex.getMessage(), ex);
        }
    }

    /**
     * Retrieves a list of pets by their owner's ID.
     *
     * @param userId the ID of the pet's owner
     * @return a list of PetResponseDto objects representing the pets owned by the specified owner
     * @throws BadRequestException if ownerId is null
     */
    @Transactional(readOnly = true)
    public List<PetResponseDto> getPetsByOwnerId(Long userId) {
        LOGGER.info("Retrieving pets for user with ID: {}", userId);
        if (userId == null) {
            throw new BadRequestException("userId cannot be null");
        }
        return petRepository.findByOwnerId(userId)
                .stream()
                .map(this::mapToPetResponseDto)
                .toList();
    }


    /**
     * etrieves a paginated list of pets located in the specified city, sorted by pet name.
     *
     * @param city The city name to filter pets by (case-sensitive). Must not be blank or empty.
     * @param page The zero-based page index (0 = first page). Must not be negative.
     * @param size The number of pets to include per page. Must be greater than 0
     * @throws BadRequestException if an attempt is made to update a deceased pet
     */
    @Transactional(readOnly = true)
    public Page<PetResponseDto> getPetsByCity(String city, int page, int size) {
        LOGGER.info("Retrieving pets by city: {}", city);
        if (city.isEmpty() || city.isBlank()) {
            throw new BadRequestException("City cannot be empty");
        }
        return petRepository.findByCity(
                        city,
                        PageRequest.of(page, size, Sort.by("name")))
                .map(this::mapToPetResponseDto);
    }

    /**
     * Fetch pets for FEMALE for a given city .Since the data can be huge so it return few pages of the given size
     *
     * @param city The city name to filter pets by (case-sensitive). Must not be blank or empty.
     * @param page The zero-based page index (0 = first page). Must not be negative.
     * @param size The number of pets to include per page. Must be greater than 0
     * @return PetResponseDto with the given page size
     * @throws ResourceNotFoundException if the pet with the given ID does not exist
     * @throws BadRequestException       if an attempt is made to update a deceased pet
     */
    @Transactional(readOnly = true)
    public Page<PetResponseDto> getPetsByWomenInCity(String city, int page, int size) {
        LOGGER.info("Retreiving Pets by Women in city: {}", city);
        if (city != null && city.trim().isEmpty()) {
            throw new BadRequestException("City cannot be empty");
        }
        return petRepository.findPetsByWomenOwnersInCity(
                city,
                PageRequest.of(page, size, Sort.by("name"))
        ).map(this::mapToPetResponseDto);
    }

    /**
     * /**
     * Updates the details of an existing pet.
     * This method allows updating the name, age, and type of a pet.
     *
     * @param id            the ID of the pet to be updated
     * @param petRequestDto the DTO containing the new details for the pet
     * @return the updated PetResponseDto
     * @throws ResourceNotFoundException if the pet with the given ID does not exist
     * @throws BadRequestException       if id,petdto is null
     * @throws DataAccessException       if there is a database access problem
     */
    @Transactional
    public PetResponseDto updatePet(Long id, PetRequestDto petRequestDto) {
        LOGGER.info("Updating pet with ID: {} with details: {}", id, petRequestDto);
        try {
            Pet pet = petRepository.findById(id)
                    .filter(u -> !u.isDeceased())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Active pet not found with id: " + id +
                                    " (either doesn't exist or is deceased)"));
            if (petRequestDto.name() != null) pet.setName(petRequestDto.name());
            if (petRequestDto.age() != null) pet.setAge(petRequestDto.age());
            if (petRequestDto.type() != null) pet.setType(petRequestDto.type());

            Pet updatedPet = petRepository.save(pet);
            return mapToPetResponseDto(updatedPet);
        } catch (DataAccessException ex) {
            LOGGER.error("Database error while creating Pet", ex);
            throw ex;
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Invalid Pet creation request", ex);
            throw new BadRequestException(ex.getMessage(), ex);
        }
    }

    /**
     * Marks a pet as deceased.
     * This method updates the deceased status of a pet to true.
     *
     * @param id the ID of the pet to be marked as deceased
     * @throws ResourceNotFoundException if the pet with the given ID does not exist
     * @throws BadRequestException       if an unexpected error occurs during the operation
     */
    @Transactional
    public void markPetAsDeceased(Long id) {
        LOGGER.info("Mark pet as deceased for the given pet id {}", id);
        try {
            Pet pet = petRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + id));
            pet.setDeceased(true);
            petRepository.save(pet);
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred while marking pet as deceased for pet ID {}: {}", id, e.getMessage(), e);
            throw new BadRequestException("Failed to mark pet as deceased due to an unexpected error.", e);
        }
    }

    /**
     * Find active pet by petid
     *
     * @param petid to find the active pet
     * @return Pet found result
     * @throws ResourceNotFoundException if no Pet found
     */
    @Transactional(readOnly = true)
    public Pet findActivePetWithOwners(Long petId, boolean activePetRequired) {
        LOGGER.info("Retrieve  pets along with address underneath for petId {} based on the active or inactive pets requirements", petId);

        if (activePetRequired) {
            return petRepository.findActivePetWithOwners(petId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Active pet not found with id: " + petId +
                                    " (either doesn't exist or is deceased)"));
        } else {
            return petRepository.findById(petId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Pet not found with id: " + petId +
                                    " (either doesn't exist or is deceased)"));
        }
    }

    public PetResponseDto mapToPetResponseDto(Pet pet) {
        return new PetResponseDto(
                pet.getId(),
                pet.getName(),
                pet.getAge(),
                pet.getType(),
                pet.isDeceased()
        );
    }

    /**
     * Pet validation for the given input dto
     *
     * @param dto given dto that contains pet details sent from client
     */
    private void validatePetRequest(PetRequestDto dto) {
        if (dto.name() == null || dto.name().trim().isEmpty()) {
            throw new BadRequestException("Pet name is required");
        }
        if (dto.age() != null && dto.age() <= 0) {
            throw new BadRequestException("Age must be positive");
        }
    }
}
