package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.service.UserService;
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
public class UserController {
    private final UserService service;

    @GetMapping(path = "/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id, Authentication authentication) {
        log.info("User '{}' requested user ID: {}", authentication.getName(), id);
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Collection<UserDto>> getAllUsers(Authentication authentication) {
        log.info("User '{}' requested all users", authentication.getName());
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto, Authentication authentication) {
        log.info("User '{}' requested creating of new user", authentication.getName());
        return new ResponseEntity<>(service.create(userDto), HttpStatus.CREATED);
    }

    @PatchMapping(path = "/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id,
                                              @Valid @RequestBody UserUpdateDto userDto,
                                              Authentication authentication) {
        log.info("User '{}' requested updating user ID: {}", authentication.getName(), id);
        return ResponseEntity.ok(service.update(id, userDto));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<UserDto> deleteById(@PathVariable Long id, Authentication authentication) {
        log.info("User '{}' requested deleting of user ID: {}", authentication.getName(), id);
        service.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
