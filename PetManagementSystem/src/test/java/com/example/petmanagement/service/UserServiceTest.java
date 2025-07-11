package com.example.petmanagement.service;

import com.example.petmanagement.dto.AddressDto;
import com.example.petmanagement.dto.UserRequestDto;
import com.example.petmanagement.dto.UserResponseDto;

import com.example.petmanagement.exception.BadRequestException;
import com.example.petmanagement.exception.ResourceNotFoundException;
import com.example.petmanagement.model.User;
import com.example.petmanagement.model.Pet;
import com.example.petmanagement.model.Address;
import com.example.petmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressService addressService;

    @Mock
    private PetService petService;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequestDto userRequestDto;
    private AddressDto addressDto;
    private Pet pet;
    private Address address;

    @BeforeEach
    void setUp() {
        // Initialize Address
        address = new Address();
        address.setId(1L);
        address.setCity("Paris");
        address.setType("street");
        address.setAddressName("Main");
        address.setNumber("123");

        // Initialize User
        user = new User();
        user.setId(1L);
        user.setName("Vasani");
        user.setFirstName("Samir");
        user.setAddress(address);
        user.setAge(30);
        user.setGender("MALE");
        user.setDeceased(false);

        // Initialize Address DTO
        addressDto = new AddressDto("Paris", "street", "Main", "123");

        // Initialize UserRequest DTO
        userRequestDto = new UserRequestDto("Vasani", "Samir", addressDto, 30, "MALE");

        // Initialize Pet
        pet = new Pet();
        pet.setId(1L);
        pet.setName("Fido");
        pet.setAge(3);
        pet.setType("dog");
        pet.setDeceased(false);
    }

    private List<User> createHomonyms(String name, String firstName, int count, boolean sameAddress) {
        List<User> homonyms = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            User homonym = new User();
            homonym.setId((long) (i + 1));
            homonym.setName(name);
            homonym.setFirstName(firstName);
            homonym.setAge(30 + i);
            homonym.setGender("MALE");

            if (sameAddress) {
                homonym.setAddress(address);
            } else if (i > 0) {
                Address differentAddress = new Address();
                differentAddress.setCity("London");
                homonym.setAddress(differentAddress);
            } else {
                homonym.setAddress(address);
            }

            homonyms.add(homonym);
        }
        return homonyms;
    }

    @Nested
    class CreateUserTests {
        @Test
        void createUser_WithNoHomonyms_ShouldSucceed() {
            when(userRepository.homonymExists("Vasani", "Samir")).thenReturn(List.of());
            when(addressService.getOrCreateAddress(any())).thenReturn(address);
            when(userRepository.save(any())).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setId(1L);
                return savedUser;
            });

            UserResponseDto response = userService.createUser(userRequestDto);

            assertNotNull(response);
            assertEquals(1L, response.id());
            verify(userRepository).homonymExists("Vasani", "Samir");
        }

        @Test
        void createUser_WithHomonyms_ShouldLogWarningButSucceed() {
            List<User> homonyms = createHomonyms("Vasani", "Samir", 2, true);

            when(userRepository.homonymExists("Vasani", "Samir")).thenReturn(homonyms);
            when(addressService.getOrCreateAddress(any())).thenReturn(address);
            when(userRepository.save(any())).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setId(3L); // New user ID
                return savedUser;
            });

            UserResponseDto response = userService.createUser(userRequestDto);

            assertNotNull(response);
            assertEquals(3L, response.id());
            // Verify warning log would be checked here if you had a way to verify logs
        }
    }

    @Nested
    class RemovePetTests {
        @Test
        void removePet_WithNoHomonyms_ShouldSucceed() {
            user.getPets().add(pet);
            pet.getOwners().add(user);

            when(userRepository.findActiveUserWithAddress(1L)).thenReturn(Optional.of(user));
            when(petService.findActivePetWithOwners(1L, false)).thenReturn(pet);
            when(userRepository.homonymExists("Vasani", "Samir")).thenReturn(List.of(user));

            userService.removePetFromUser(1L, 1L);

            assertFalse(user.getPets().contains(pet));
            verify(userRepository).save(user);
        }

        @Test
        void removePet_WithHomonymsSameAddress_ShouldSucceed() {
            List<User> homonyms = createHomonyms("Vasani", "Samir", 2, true);
            User currentUser = homonyms.get(0);
            currentUser.getPets().add(pet);
            pet.getOwners().add(currentUser);

            when(userRepository.findActiveUserWithAddress(1L)).thenReturn(Optional.of(currentUser));
            when(petService.findActivePetWithOwners(1L, false)).thenReturn(pet);
            when(userRepository.homonymExists("Vasani", "Samir")).thenReturn(homonyms);

            userService.removePetFromUser(1L, 1L);

            assertFalse(currentUser.getPets().contains(pet));
            verify(userRepository).save(currentUser);
        }

        @Test
        void removePet_WithHomonymsDifferentAddress_ShouldThrow() {
            List<User> homonyms = createHomonyms("Vasani", "Samir", 2, false);
            User currentUser = homonyms.get(0);
            User petOwner = homonyms.get(1);
            petOwner.getPets().add(pet);
            pet.getOwners().add(petOwner);

            when(userRepository.findActiveUserWithAddress(1L)).thenReturn(Optional.of(currentUser));
            when(petService.findActivePetWithOwners(1L, false)).thenReturn(pet);
            when(userRepository.homonymExists("Vasani", "Samir")).thenReturn(homonyms);

            assertThrows(BadRequestException.class, () ->
                            userService.removePetFromUser(1L, 1L),
                    "Address mismatch for pet removal");
        }

        @Test
        void removePet_WhenNotAssigned_ShouldThrow() {
            when(userRepository.findActiveUserWithAddress(1L)).thenReturn(Optional.of(user));
            when(petService.findActivePetWithOwners(1L, false)).thenReturn(pet);
            when(userRepository.homonymExists("Vasani", "Samir")).thenReturn(List.of(user));

            assertThrows(BadRequestException.class, () ->
                            userService.removePetFromUser(1L, 1L),
                    "Pet not assigned to user");
        }
    }

    @Nested
    class UpdateUserTests {
        @Test
        void updateUser_ShouldSucceed() {
            when(userRepository.findActiveUserWithAddress(1L)).thenReturn(Optional.of(user));
            when(addressService.getOrCreateAddress(any())).thenReturn(address);
            when(userRepository.save(any())).thenReturn(user);

            UserResponseDto result = userService.updateUser(1L, userRequestDto);

            assertNotNull(result);
            assertEquals(1L, result.id());
            verify(userRepository).save(user);
        }

        @Test
        void updateUser_WhenUserNotFound_ShouldThrow() {
            when(userRepository.findActiveUserWithAddress(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    userService.updateUser(1L, userRequestDto));
        }
    }

    @Nested
    class MarkUserAsDeceasedTests {
        @Test
        void markUserAsDeceased_ShouldSucceed() {
            when(userRepository.findActiveUserWithAddress(1L)).thenReturn(Optional.of(user));

            userService.markUserAsDeceased(1L);

            assertTrue(user.isDeceased());
            verify(userRepository).save(user);
        }
    }
}