package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        UserDto userDto = service.getById(id);
        if (userDto == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User with ID " + id + " not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(userDto);
    }

    @GetMapping
    public ResponseEntity<Collection<UserDto>> getAllUsers() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = service.create(userDto);
        if (createdUser == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "User with this passport number already exists");
            return new ResponseEntity<>(error, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        userDto.setId(id);
        UserDto updatedUser = service.update(userDto);
        if (updatedUser == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "User with ID " + userDto.getId() + " not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        if (service.getById(id) == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "User with ID " + id + " not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        service.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
