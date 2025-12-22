package com.pipemasters.demoadmink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipemasters.demoadmink.dto.UserDto;
import com.pipemasters.demoadmink.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void getAllUsers_shouldReturnAllUsers() throws Exception {
        // Given
        List<UserDto> users = List.of(
                UserDto.builder().id(1L).name("John").email("john@test.com").age(30).build(),
                UserDto.builder().id(2L).name("Jane").email("jane@test.com").age(25).build()
        );
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[1].name").value("Jane"));
    }

    @Test
    void getUserById_shouldReturnUser_whenExists() throws Exception {
        // Given
        UserDto user = UserDto.builder().id(1L).name("John").email("john@test.com").age(30).build();
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void getUserById_shouldReturn404_whenNotExists() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_shouldReturn201AndCreatedUser() throws Exception {
        // Given
        UserDto inputUser = UserDto.builder().name("New User").email("new@test.com").age(28).build();
        UserDto createdUser = UserDto.builder().id(3L).name("New User").email("new@test.com").age(28).build();
        when(userService.createUser(any(UserDto.class))).thenReturn(createdUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("New User"));
    }

    @Test
    void updateUser_shouldReturnUpdatedUser_whenExists() throws Exception {
        // Given
        UserDto inputUser = UserDto.builder().name("Updated").email("updated@test.com").age(35).build();
        UserDto updatedUser = UserDto.builder().id(1L).name("Updated").email("updated@test.com").age(35).build();
        when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(Optional.of(updatedUser));

        // When & Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void updateUser_shouldReturn404_whenNotExists() throws Exception {
        // Given
        UserDto inputUser = UserDto.builder().name("Updated").build();
        when(userService.updateUser(eq(999L), any(UserDto.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_shouldReturn204_whenExists() throws Exception {
        // Given
        when(userService.deleteUser(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_shouldReturn404_whenNotExists() throws Exception {
        // Given
        when(userService.deleteUser(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound());
    }
}
