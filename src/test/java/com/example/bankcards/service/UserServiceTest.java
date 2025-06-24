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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;


    @Test
    @DisplayName("Get user by ID when user exists")
    public void testGetByIdIfFound() {
        Long userId = 1L;
        String username = "user";

        User user = new User();
        user.setId(userId);
        user.setUsername(username);

        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setUsername(username);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.mapToDto(user)).thenReturn(userDto);

        UserDto actualCardDto = userService.getById(userId);

        assertEquals(userId, actualCardDto.getId());
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).mapToDto(user);
    }

    @Test
    @DisplayName("Get user by ID when user is not found")
    public void testGetByIdIfUserNotFound() {
        Long cardId = 1L;

        when(userRepository.findById(cardId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getById(cardId));
        verify(userRepository, times(1)).findById(cardId);
    }

    @Test
    @DisplayName("Get all users")
    public void testGetAll() {
        Long firstUserId = 1L;
        Long secondUserId = 2L;
        String username1 = "username1";
        String username2 = "username2";

        User user1 = new User();
        user1.setId(firstUserId);
        user1.setUsername(username1);

        User user2 = new User();
        user2.setId(secondUserId);
        user2.setUsername(username2);

        UserDto dto1 = new UserDto();
        dto1.setId(firstUserId);
        dto1.setUsername(username1);

        UserDto dto2 = new UserDto();
        dto2.setId(secondUserId);
        dto2.setUsername(username2);

        List<User> cardList = List.of(user1, user2);
        List<UserDto> expectedCardDtoList = List.of(dto1, dto2);

        when(userRepository.findAll()).thenReturn(cardList);
        when(userMapper.mapToDto(user1)).thenReturn(dto1);
        when(userMapper.mapToDto(user2)).thenReturn(dto2);

        List<UserDto> actualCardDtoList = userService.getAll();

        assertEquals(expectedCardDtoList.size(), actualCardDtoList.size());
        assertTrue(actualCardDtoList.containsAll(expectedCardDtoList));
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).mapToDto(user1);
        verify(userMapper, times(1)).mapToDto(user2);
    }

    @Test
    @DisplayName("Create user if user has unique username")
    void testCreateIfNewUsername() {
        String username = "user";
        String password = "qwerty";
        String encodedPassword = "<encrypted>";
        Set<String> roles = Set.of("USER");

        UserDto inputDto = new UserDto();
        inputDto.setUsername(username);
        inputDto.setPassword(password);
        inputDto.setRoles(roles);

        Long savedUserId = 1L;
        Role role = new Role("USER");

        User savedUser = new User();
        savedUser.setId(savedUserId);
        savedUser.setUsername(username);
        savedUser.setPassword(encodedPassword);
        savedUser.setRoles(Set.of(role));

        UserDto expectedDto = new UserDto();
        expectedDto.setId(savedUserId);
        expectedDto.setUsername(username);
        expectedDto.setPassword(encodedPassword);
        expectedDto.setRoles(roles);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(roleRepository.findByNameIn(roles)).thenReturn(Set.of(role));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.mapToDto(savedUser)).thenReturn(expectedDto);

        UserDto actual = userService.create(inputDto);

        assertEquals(expectedDto.getId(), actual.getId());
        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).encode(password);
        verify(roleRepository, times(1)).findByNameIn(roles);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).mapToDto(savedUser);
    }

    @Test
    @DisplayName("Create user if user has duplicate username")
    void testCreateIfDuplicateUsername() {
        String username = "user";

        UserDto inputDto = new UserDto();
        inputDto.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));
        assertThrows(DuplicateUsernameException.class, () -> userService.create(inputDto));
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Update user if exists")
    void testUpdateIfUserFound() {
        Long userId = 1L;
        String newUsername = "newUsername";
        String newPassword = "newPass";
        String encodedNewPassword = "encodedNewPassword";
        String newRoleName = "ADMIN";
        Role adminRole = new Role(newRoleName);

        UserUpdateDto inputDto = new UserUpdateDto();
        inputDto.setUsername(newUsername);
        inputDto.setPassword(newPassword);
        inputDto.setRoles(Set.of(newRoleName));

        User oldUser = new User();
        oldUser.setId(userId);
        oldUser.setUsername("oldUser");
        oldUser.setPassword("encodedOldPass");
        oldUser.setRoles(Set.of(new Role("USER")));

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setUsername(newUsername);
        updatedUser.setPassword(encodedNewPassword);
        updatedUser.setRoles(Set.of(adminRole));

        UserDto expectedUserDto = new UserDto();
        expectedUserDto.setId(userId);
        expectedUserDto.setUsername(newUsername);
        expectedUserDto.setPassword(encodedNewPassword);
        expectedUserDto.setRoles(Set.of(newRoleName));

        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        when(roleRepository.findByNameIn(Set.of(newRoleName))).thenReturn(Set.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.mapToDto(updatedUser)).thenReturn(expectedUserDto);

        UserDto actual = userService.update(userId, inputDto);

        assertEquals(expectedUserDto, actual);
        verify(userRepository, times(1)).findById(userId);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(roleRepository, times(1)).findByNameIn(Set.of(newRoleName));
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).mapToDto(updatedUser);
    }

    @Test
    @DisplayName("Update user when not found")
    void testUpdateIfUserNotFound() {
        Long userId = 1L;
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("newUsername");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.update(userId, updateDto));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Delete user by ID when exists")
    void testDeleteByIdIfUserFound() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        userService.deleteById(userId);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("Delete user by ID when user is not found")
    void testDeleteByIdIfCardNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.deleteById(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).deleteById(any());
    }
}
