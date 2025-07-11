package com.example.petmanagement.controller;

import com.example.petmanagement.dto.AddressDto;
import com.example.petmanagement.dto.PetResponseDto;
import com.example.petmanagement.dto.UserRequestDto;
import com.example.petmanagement.dto.UserResponseDto;
import com.example.petmanagement.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = com.example.petmanagement.PetManagementApplication.class,
        properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser_ValidRequest_ReturnsCreated() throws Exception {
        UserRequestDto request = new UserRequestDto(
                "Doe",
                "John",
                new AddressDto("Paris", "street", "Main", "123"),
                30,
                "MALE"
        );

        UserResponseDto response = new UserResponseDto(
                1L,
                "Doe",
                "John",
                new AddressDto("Paris", "street", "Main", "123"),
                30,
                "MALE",
                false,
                Collections.emptySet()
        );

        when(userService.createUser(any(UserRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Doe")));
    }


    @Test
    void updateUser_ValidRequest_ReturnsUpdatedUser() throws Exception {
        UserRequestDto request = new UserRequestDto(
                "Doe",
                "John",
                new AddressDto("London", "avenue", "Baker", "221B"),
                31,
                "MALE"
        );

        UserResponseDto response = new UserResponseDto(
                1L,
                "Doe",
                "John",
                new AddressDto("London", "avenue", "Baker", "221B"),
                31,
                "MALE",
                false,
                Collections.emptySet()
        );

        when(userService.updateUser(anyLong(), any(UserRequestDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address.city", is("London")));
    }

    @Test
    void assignPetToUser_ValidIds_ReturnsUserWithPet() throws Exception {
        UserResponseDto response = new UserResponseDto(
                1L,
                "Doe",
                "John",
                new AddressDto("Paris", "street", "Main", "123"),
                30,
                "MALE",
                false,
                Collections.singleton(new PetResponseDto(1L, "Fido", 3, "dog", false))
        );

        when(userService.assignPetToUser(anyLong(), anyLong())).thenReturn(response);

        mockMvc.perform(post("/api/users/1/pets/1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pets[0].name", is("Fido")));
    }

    @Test
    void markUserAsDeceased_ExistingUser_ReturnsNoContent() throws Exception {
        mockMvc.perform(put("/api/users/1/deceased"))
                .andExpect(status().isNoContent());

        verify(userService).markUserAsDeceased(1L);
    }

    @Test
    void getUsersByPetTypeAndCity_ValidParams_ReturnsUsers() throws Exception {
        UserResponseDto user = new UserResponseDto(
                1L,
                "Doe",
                "John",
                new AddressDto("Paris", "street", "Main", "123"),
                30,
                "MALE",
                false,
                Collections.emptySet()
        );

        when(userService.getUsersByPetTypeAndCity(anyString(), anyString()))
                .thenReturn(Collections.singletonList(user));

        mockMvc.perform(get("/api/users/by-pet-and-city")
                        .param("petType", "dog")
                        .param("city", "Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Doe")));
    }
}