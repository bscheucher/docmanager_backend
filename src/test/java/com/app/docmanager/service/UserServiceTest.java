package com.app.docmanager.service;

import com.app.docmanager.entity.User;
import com.app.docmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(1L);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findById(1L);
    }

    @Test
    void createUser_WhenValidUser_ShouldCreateUser() {
        // Given
        User newUser = User.builder()
                .username("newuser")
                .email("new@example.com")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        User result = userService.createUser(newUser);

        // Then
        assertThat(result.getUsername()).isEqualTo("newuser");
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).save(newUser);
    }

    @Test
    void createUser_WhenUsernameExists_ShouldThrowException() {
        // Given
        User newUser = User.builder()
                .username("existinguser")
                .email("new@example.com")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(newUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowException() {
        // Given
        User newUser = User.builder()
                .username("newuser")
                .email("existing@example.com")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(newUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldThrowException() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(1L);
    }
}