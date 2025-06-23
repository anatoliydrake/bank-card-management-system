package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.DuplicateUsernameException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDto getById(Long id) {
        log.info("Retrieving user by ID: {}", id);
        return userRepository.findById(id)
                .map(userMapper::mapToDto)
                .orElseThrow(() -> {
                    log.warn("User ID {} not found", id);
                    return new UserNotFoundException(id);
                });
    }

    public List<UserDto> getAll() {
        log.info("Retrieving all users");
        return userRepository.findAll().stream()
                .map(userMapper::mapToDto)
                .toList();
    }

    @Transactional
    public UserDto create(UserDto userDto) {
        String username = userDto.getUsername();
        log.info("Creating new user '{}'", username);
        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("User '{}' already exists", username);
            throw new DuplicateUsernameException(username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        Set<Role> userRoles = new HashSet<>(roleRepository.findByNameIn(userDto.getRoles()));
        user.setRoles(userRoles);
        user.setCards(new ArrayList<>());

        User saved = userRepository.save(user);
        log.info("User created successfully with ID {}", saved.getId());
        return userMapper.mapToDto(saved);
    }

    @Transactional
    public UserDto update(Long userId, UserUpdateDto userDto) {
        log.info("Updating user ID {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User ID {} not found", userId);
                    return new UserNotFoundException(userId);
                });

        if (userDto.getUsername() != null) {
            user.setUsername(userDto.getUsername());
        }
        if (userDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        if (userDto.getRoles() != null) {
            Set<Role> userRoles = new HashSet<>(roleRepository.findByNameIn(userDto.getRoles()));
            user.setRoles(userRoles);
        }
        user = userRepository.save(user);
        log.info("User ID {} successfully updated", userId);
        return userMapper.mapToDto(user);
    }

    @Transactional
    public void deleteById(Long id) {
        log.info("Deleting user ID {}", id);
        if (userRepository.findById(id).isEmpty()) {
            log.warn("User ID {} not found", id);
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
        log.info("User ID {} deleted successfully", id);
    }
}
