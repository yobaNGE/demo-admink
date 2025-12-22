package com.pipemasters.demoadmink.service;

import com.pipemasters.demoadmink.dto.UserDto;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        userService = new UserService(meterRegistry);
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        assertEquals(2, users.size());
    }

    @Test
    void getUserById_shouldReturnUser_whenExists() {
        Long existingId = 1L;
        Optional<UserDto> result = userService.getUserById(existingId);
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    void getUserById_shouldReturnEmpty_whenNotExists() {
        Long nonExistingId = 999L;
        Optional<UserDto> result = userService.getUserById(nonExistingId);
        assertTrue(result.isEmpty());
    }

    @Test
    void createUser_shouldCreateAndReturnUser() {
        UserDto newUser = UserDto.builder()
                .name("New User")
                .email("new@example.com")
                .age(35)
                .build();
        UserDto created = userService.createUser(newUser);
        assertNotNull(created.getId());
        assertEquals("New User", created.getName());
        assertEquals("new@example.com", created.getEmail());
        assertEquals(35, created.getAge());
    }

    @Test
    void updateUser_shouldUpdateAndReturnUser_whenExists() {
        Long existingId = 1L;
        UserDto updatedUser = UserDto.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .age(40)
                .build();
        Optional<UserDto> result = userService.updateUser(existingId, updatedUser);
        assertTrue(result.isPresent());
        assertEquals(existingId, result.get().getId());
        assertEquals("Updated Name", result.get().getName());
    }

    @Test
    void updateUser_shouldReturnEmpty_whenNotExists() {
        Long nonExistingId = 999L;
        UserDto updatedUser = UserDto.builder().name("Updated").build();
        Optional<UserDto> result = userService.updateUser(nonExistingId, updatedUser);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteUser_shouldReturnTrue_whenExists() {
        Long existingId = 1L;
        boolean result = userService.deleteUser(existingId);
        assertTrue(result);
        assertTrue(userService.getUserById(existingId).isEmpty());
    }

    @Test
    void deleteUser_shouldReturnFalse_whenNotExists() {
        Long nonExistingId = 999L;
        boolean result = userService.deleteUser(nonExistingId);
        assertFalse(result);
    }

    @Test
    void createUser_shouldIncrementCounter() {
        UserDto newUser = UserDto.builder().name("Test").email("test@test.com").age(20).build();
        double initialCount = meterRegistry.counter("users_created_total").count();
        userService.createUser(newUser);
        assertEquals(initialCount + 1, meterRegistry.counter("users_created_total").count());
    }

    @Test
    void deleteUser_shouldIncrementCounter_whenSuccess() {
        Long existingId = 1L;
        double initialCount = meterRegistry.counter("users_deleted_total").count();
        userService.deleteUser(existingId);
        assertEquals(initialCount + 1, meterRegistry.counter("users_deleted_total").count());
    }
}
