package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public UserDto getById(Long id) {
        log.info("Get user by id: " + id);
        return userRepository.findById(id).map(userMapper::mapToDto).orElse(null);
    }

    public List<UserDto> getAll() {
        log.info("Get all users");
        return userRepository.findAll().stream()
                .map(userMapper::mapToDto)
                .toList();
    }

    public UserDto create(UserDto userDto) {
        log.info("Create new User");
        if (userRepository.findByPassportNumber(userDto.getPassportNumber()).isPresent()) {
            return null;
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        user.setPassportNumber(userDto.getPassportNumber());
        Set<Role> userRoles = new HashSet<>(roleRepository.findByNameIn(userDto.getRoles()));
        user.setRoles(userRoles);
        user.setCards(new ArrayList<>());

        return userMapper.mapToDto(userRepository.save(user));
    }

    public UserDto update(UserDto userDto) {
        User user = userRepository.findById(userDto.getId()).orElse(null);
        if (user == null) {
            return null;
        }
        if (userDto.getUsername() != null) {
            user.setUsername(userDto.getUsername());
        }
        if (userDto.getPassword() != null) {
            user.setPassword(userDto.getPassword());
        }
        if (userDto.getPassportNumber() != null) {
            user.setPassportNumber(userDto.getPassportNumber());
        }
        if (userDto.getRoles() != null) {
            Set<Role> userRoles = new HashSet<>(roleRepository.findByNameIn(userDto.getRoles()));
            user.setRoles(userRoles);
        }
        user = userRepository.save(user);
        log.info("User " + user.getId() + " updated");
        return userMapper.mapToDto(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
        log.info("User " + id + " deleted");
    }
}
