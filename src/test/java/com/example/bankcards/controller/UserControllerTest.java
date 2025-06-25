package com.example.bankcards.controller;

import com.example.bankcards.TestSecurityConfig;
import com.example.bankcards.config.JwtConfig;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.exception.DuplicateUsernameException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private JwtConfig jwtConfig;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

    @Test
    @DisplayName("GET /api/users/{id} - returns 200 OK with user details")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUserById_ReturnsUserDto_WhenExists() throws Exception {
        Long userId = 1L;
        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setUsername("user");

        when(userService.getById(userId)).thenReturn(userDto);
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("user"));
        verify(userService, times(1)).getById(userId);
    }

    @Test
    @DisplayName("GET /api/users/{id} - returns 404 Not Found when user does not exist")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUserById_ReturnsNotFound_WhenUserMissing() throws Exception {
        Long userId = 1L;
        when(userService.getById(userId)).thenThrow(new UserNotFoundException(userId));
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with ID: 1 not found"));
        verify(userService, times(1)).getById(userId);
    }

    @Test
    @DisplayName("GET /api/users/{id} - returns 403 Forbidden for unauthorized user")
    @WithMockUser(username = "user", roles = "USER")
    void getUserById_ReturnsForbidden_WhenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/users - returns 200 OK with list of users")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllUsers_ReturnsListOfUsers_WhenAuthenticated() throws Exception {
        UserDto admin = new UserDto();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRoles(Set.of("ADMIN"));

        UserDto user = new UserDto();
        user.setId(2L);
        user.setUsername("user");
        user.setRoles(Set.of("USER"));

        when(userService.getAll()).thenReturn(List.of(admin, user));
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(admin.getId()))
                .andExpect(jsonPath("$[0].username").value(admin.getUsername()))
                .andExpect(jsonPath("$[1].id").value(user.getId()))
                .andExpect(jsonPath("$[1].username").value(user.getUsername()));
        verify(userService, times(1)).getAll();
    }

    @Test
    @DisplayName("GET /api/users - returns 403 Forbidden for unauthorized user")
    @WithMockUser(username = "admin", roles = "USER")
    void getAllUsers_ReturnsForbidden_WhenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/users - returns 201 Created with new user")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createUser_ReturnsCreatedUser_WhenValid() throws Exception {
        Long userId = 1L;
        String role = "USER";

        UserUpdateDto inputDto = new UserUpdateDto();
        inputDto.setUsername("user");
        inputDto.setPassword("password");
        inputDto.setRoles(Set.of(role));

        UserDto responseDto = new UserDto();
        responseDto.setId(userId);
        responseDto.setUsername(inputDto.getUsername());
        responseDto.setRoles(inputDto.getRoles());

        when(userService.create(any(UserDto.class))).thenReturn(responseDto);
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.username").value(responseDto.getUsername()))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value(role));
        verify(userService, times(1)).create(any(UserDto.class));
    }

    @Test
    @DisplayName("POST /api/users - returns 409 Conflict when username exists")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createUser_ReturnsConflict_WhenUsernameExists() throws Exception {
        UserUpdateDto inputDto = new UserUpdateDto();
        inputDto.setUsername("user");
        inputDto.setPassword("password");
        inputDto.setRoles(Set.of("USER"));

        when(userService.create(any(UserDto.class)))
                .thenThrow(new DuplicateUsernameException(inputDto.getUsername()));
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Username '" + inputDto.getUsername() + "' already exists"));
        verify(userService, times(1)).create(any(UserDto.class));
    }

    @Test
    @DisplayName("POST /api/users - returns 403 Forbidden for USER")
    @WithMockUser(username = "user", roles = "USER")
    void createUser_ReturnsForbidden_WhenRoleIsNotAdmin() throws Exception {
        UserUpdateDto inputDto = new UserUpdateDto();
        inputDto.setUsername("user");
        inputDto.setPassword("password");
        inputDto.setRoles(Set.of("USER"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/users/{id} - returns 200 OK with updated user")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateUser_ReturnsUpdatedUser_WhenValid() throws Exception {
        Long userId = 1L;
        String role = "USER";

        UserUpdateDto inputDto = new UserUpdateDto();
        inputDto.setUsername("user");
        inputDto.setPassword("password");
        inputDto.setRoles(Set.of(role));

        UserDto updatedUser = new UserDto();
        updatedUser.setId(userId);
        updatedUser.setUsername(inputDto.getUsername());
        updatedUser.setRoles(inputDto.getRoles());

        Mockito.when(userService.update(eq(userId), any(UserUpdateDto.class)))
                .thenReturn(updatedUser);
        mockMvc.perform(patch("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(updatedUser.getId()))
                .andExpect(jsonPath("$.username").value(updatedUser.getUsername()))
                .andExpect(jsonPath("$.roles[0]").value(role));
    }

    @Test
    @DisplayName("PATCH /api/users/{id} - returns 404 Not Found when user doesn't exist")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateUser_ReturnsNotFound_WhenUserMissing() throws Exception {
        Long userId = 1L;
        String role = "USER";

        UserUpdateDto inputDto = new UserUpdateDto();
        inputDto.setUsername("user");
        inputDto.setPassword("password");
        inputDto.setRoles(Set.of(role));

        Mockito.when(userService.update(eq(userId), any(UserUpdateDto.class)))
                .thenThrow(new UserNotFoundException(userId));
        mockMvc.perform(patch("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("User with ID: " + userId + " not found"));
    }

    @Test
    @DisplayName("PATCH /api/users/{id} - returns 403 Forbidden for USER")
    @WithMockUser(username = "user", roles = "USER")
    void updateUser_ReturnsForbidden_WhenRoleIsNotAdmin() throws Exception {
        UserUpdateDto inputDto = new UserUpdateDto();
        inputDto.setUsername("user");
        inputDto.setPassword("password");
        inputDto.setRoles(Set.of("USER"));

        mockMvc.perform(patch("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - returns 204 No Content when user is deleted")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteById_ReturnsNoContent_WhenUserExists() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());
        verify(userService, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - returns 404 Not Found when user doesn't exist")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteById_ReturnsNotFound_WhenUserMissing() throws Exception {
        Long userId = 1L;

        Mockito.doThrow(new UserNotFoundException(userId))
                .when(userService).deleteById(userId);
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("User with ID: " + userId + " not found"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - returns 403 Forbidden for USER")
    @WithMockUser(username = "user", roles = "USER")
    void deleteById_ReturnsForbidden_WhenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isForbidden());
    }
}
