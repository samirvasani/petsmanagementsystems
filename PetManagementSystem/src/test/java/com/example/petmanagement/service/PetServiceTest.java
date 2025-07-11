package com.example.petmanagement.service;

import com.example.petmanagement.dto.PetRequestDto;
import com.example.petmanagement.dto.PetResponseDto;
import com.example.petmanagement.exception.BadRequestException;
import com.example.petmanagement.exception.ResourceNotFoundException;
import com.example.petmanagement.model.Pet;
import com.example.petmanagement.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private PetService petService;

    private Pet pet;
    private PetRequestDto petRequestDto;

    @BeforeEach
    void setUp() {
        pet = new Pet();
        pet.setId(1L);
        pet.setName("Fido");
        pet.setAge(3);
        pet.setType("dog");
        pet.setDeceased(false);

        petRequestDto = new PetRequestDto("Fido", 3, "dog");
    }

    @Test
    void createPet_ValidRequest_ReturnsPetResponse() {
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        PetResponseDto result = petService.createPet(petRequestDto);

        assertNotNull(result);
        assertEquals(pet.getId(), result.id());
        assertEquals(pet.getName(), result.name());
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    void createPet_NullRequest_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> petService.createPet(null));
    }

    @Test
    void createPet_InvalidName_ThrowsBadRequestException() {
        PetRequestDto invalidDto = new PetRequestDto("", 3, "dog");
        assertThrows(BadRequestException.class, () -> petService.createPet(invalidDto));
    }

    @Test
    void getPetsByOwnerId_ValidId_ReturnsPetList() {
        when(petRepository.findByOwnerId(1L)).thenReturn(List.of(pet));

        List<PetResponseDto> result = petService.getPetsByOwnerId(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(petRepository, times(1)).findByOwnerId(1L);
    }

    @Test
    void getPetsByCity_ValidCity_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        when(petRepository.findByCity("Paris", pageable)).thenReturn(new PageImpl<>(List.of(pet)));

        Page<PetResponseDto> result = petService.getPetsByCity("Paris", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(petRepository, times(1)).findByCity("Paris", pageable);
    }

    @Test
    void getPetsByWomenInCity_ValidCity_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        when(petRepository.findPetsByWomenOwnersInCity("London", pageable))
                .thenReturn(new PageImpl<>(List.of(pet)));

        Page<PetResponseDto> result = petService.getPetsByWomenInCity("London", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(petRepository, times(1)).findPetsByWomenOwnersInCity("London", pageable);
    }

    @Test
    void updatePet_ValidRequest_ReturnsUpdatedPet() {
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        PetResponseDto result = petService.updatePet(1L, petRequestDto);

        assertNotNull(result);
        assertEquals(pet.getId(), result.id());
        verify(petRepository, times(1)).findById(1L);
        verify(petRepository, times(1)).save(pet);
    }

    @Test
    void updatePet_NonExistingId_ThrowsResourceNotFoundException() {
        when(petRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> petService.updatePet(1L, petRequestDto));
    }

    @Test
    void markPetAsDeceased_ValidId_MarksPetAsDeceased() {
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        petService.markPetAsDeceased(1L);

        assertTrue(pet.isDeceased());
        verify(petRepository, times(1)).findById(1L);
        verify(petRepository, times(1)).save(pet);
    }
}

