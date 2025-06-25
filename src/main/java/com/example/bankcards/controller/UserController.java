package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management operations")
public class UserController {
    private final UserService service;

    @Operation(summary = "Get user by ID", description = "Get user details by ID",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"User with ID: 10 not found\"}"))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
    content = @Content(mediaType = "application/json",
        schema = @Schema(example = "{\"message\": \"Access Denied\"}")))
    })
    @GetMapping(path = "/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id, Authentication authentication) {
        log.info("User '{}' requested user ID: {}", authentication.getName(), id);
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Get all users", description = "List of all users",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            value = """
                                    [
                                      {
                                        "id": 1,
                                        "username": "admin",
                                        "password": "<admin_encrypted_password>",
                                        "roles": ["ADMIN"],
                                        "cards": []
                                      },
                                      {
                                        "id": 2,
                                        "username": "user",
                                        "password": "<user_encrypted_password>",
                                        "roles": ["USER"],
                                        "cards": [
                                          {
                                            "id": 2,
                                            "number": "**** **** **** 5013",
                                            "holderName": "user",
                                            "expirationDate": "2030-06-22",
                                            "status": "BLOCKED",
                                            "balance": 42000.00
                                          },
                                          {
                                            "id": 4,
                                            "number": "**** **** **** 3201",
                                            "holderName": "user",
                                            "expirationDate": "2030-06-23",
                                            "status": "ACTIVE",
                                            "balance": 37000.00
                                          }
                                        ]
                                      }
                                    ]
                                    """
                    )
            )),
            @ApiResponse(responseCode = "403", description = "Forbidden",
    content = @Content(mediaType = "application/json",
        schema = @Schema(example = "{\"message\": \"Access Denied\"}")))
    })
    @GetMapping
    public ResponseEntity<Collection<UserDto>> getAllUsers(Authentication authentication) {
        log.info("User '{}' requested all users", authentication.getName());
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Create user", description = "Create new user",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "409", description = "Conflict",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Username 'username' already exists\"}"))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
    content = @Content(mediaType = "application/json",
        schema = @Schema(example = "{\"message\": \"Access Denied\"}")))
    })
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json",
                    examples = {@io.swagger.v3.oas.annotations.media.ExampleObject(
                            value = """
                                    {
                                      "username": "<username>",
                                      "password": "<password>",
                                      "roles": [
                                        "USER"
                                      ]
                                    }"""
                    )
                    })) @RequestBody UserDto userDto, Authentication authentication) {
        log.info("User '{}' requested creating of new user", authentication.getName());
        return new ResponseEntity<>(service.create(userDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Update user", description = "Update user details",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"User with ID: 10 not found\"}"))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
    content = @Content(mediaType = "application/json",
        schema = @Schema(example = "{\"message\": \"Access Denied\"}")))
    })
    @PatchMapping(path = "/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id,
                                              @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                      required = true,
                                                      content = @Content(mediaType = "application/json",
                                                              examples = {@io.swagger.v3.oas.annotations.media.ExampleObject(
                                                                      value = """
                                                                              {
                                                                                "username": "username",
                                                                                "password": "<password>",
                                                                                "roles": [
                                                                                  "USER"
                                                                                ]
                                                                              }"""
                                                              )
                                                              })) @RequestBody UserUpdateDto userDto,
                                              Authentication authentication) {
        log.info("User '{}' requested updating user ID: {}", authentication.getName(), id);
        return ResponseEntity.ok(service.update(id, userDto));
    }

    @Operation(summary = "Delete user", description = "Delete user by ID",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"User with ID: 10 not found\"}"))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
    content = @Content(mediaType = "application/json",
        schema = @Schema(example = "{\"message\": \"Access Denied\"}")))
    })
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id, Authentication authentication) {
        log.info("User '{}' requested deleting of user ID: {}", authentication.getName(), id);
        service.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
