package com.example.petmanagement.controller;

import com.example.petmanagement.dto.PetRequestDto;
import com.example.petmanagement.model.Address;
import com.example.petmanagement.model.Pet;
import com.example.petmanagement.model.User;
import com.example.petmanagement.repository.AddressRepository;
import com.example.petmanagement.repository.PetRepository;
import com.example.petmanagement.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class PetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    private Pet testPet;

    @BeforeEach
    void setUp() {
        testPet = new Pet();
        testPet.setName("TestPet");
        testPet.setAge(2);
        testPet.setType("cat");
        testPet = petRepository.save(testPet);
    }

    @Test
    void createPet_ValidRequest_ReturnsCreated() throws Exception {
        PetRequestDto request = new PetRequestDto("NewPet", 3, "dog");

        mockMvc.perform(post("/api/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("NewPet")));
    }

    @Test
    void getPetsByCity_ValidCity_ReturnsPets() throws Exception {
        mockMvc.perform(get("/api/pets/by-city")
                        .param("city", "London")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    void getPetsByWomenInCity_ValidCity_ReturnsPets() throws Exception {
        mockMvc.perform(get("/api/pets/by-women-in-city")
                        .param("city", "London")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    void updatePet_ValidRequest_ReturnsUpdatedPet() throws Exception {
        PetRequestDto request = new PetRequestDto("UpdatedPet", 4, "dog");

        mockMvc.perform(patch("/api/pets/{id}", testPet.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.name", is("UpdatedPet")));
    }

    @Test
    void markPetAsDeceased_ValidId_ReturnsNoContent() throws Exception {
        mockMvc.perform(put("/api/pets/{id}/deceased", testPet.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getUserPets_ValidUserId_ReturnsPets() throws Exception {
        Address address = createAddress("Paris");
        User user = createUser("Owner", "Test", address, "MALE");
        Pet pet = createPet("TestPet", "dog", user);
        addressRepository.save(address);
        user = userRepository.save(user);
        pet.getOwners().add(user);
        petRepository.save(pet);

        mockMvc.perform(get("/api/pets/{userId}/pets", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // Helper methods
    private Address createAddress(String city) {
        Address address = new Address();
        address.setCity(city);
        address.setType("street");
        address.setAddressName("Main");
        address.setNumber("123");
        return address;
    }

    private User createUser(String name, String firstName, Address address, String gender) {
        User user = new User();
        user.setName(name);
        user.setFirstName(firstName);
        user.setAddress(address);
        user.setAge(30);
        user.setGender(gender);
        user.setDeceased(false);
        return user;
    }

    private Pet createPet(String name, String type, User owner) {
        Pet pet = new Pet();
        pet.setName(name);
        pet.setAge(1);
        pet.setType(type);
        pet.setDeceased(false);
        pet.getOwners().add(owner);
        owner.getPets().add(pet); // Maintain bidirectional relationship
        return pet;
    }


}
