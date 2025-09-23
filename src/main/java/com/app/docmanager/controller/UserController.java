package com.app.docmanager.controller;

import com.app.docmanager.dto.UserDTO;
import com.app.docmanager.entity.User;
import com.app.docmanager.exception.ResourceNotFoundException;
import com.app.docmanager.exception.DuplicateResourceException;
import com.app.docmanager.mapper.UserMapper;
import com.app.docmanager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOs = userMapper.toDtoList(users);
        return ResponseEntity.ok(userDTOs);
    }

    // NEW: Paginated endpoint
    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllUsersPaginated(
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC)
            Pageable pageable) {

        Page<User> users = userService.getAllUsersPaginated(pageable);
        Page<UserDTO> userDTOs = users.map(userMapper::toDto);
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(userMapper.toDto(user)))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(user -> ResponseEntity.ok(userMapper.toDto(user)))
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO.CreateUserRequest request) {
        try {
            User user = userMapper.toEntity(request);
            User savedUser = userService.createUser(user);
            UserDTO userDTO = userMapper.toDto(savedUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
        } catch (RuntimeException e) {
            // This will be caught by GlobalExceptionHandler if we use proper exceptions
            throw new DuplicateResourceException(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO.UpdateUserRequest request) {

        // Get the existing user first
        User existingUser = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Update the existing user with new data
        User updatedUserData = userMapper.updateEntity(existingUser, request);

        try {
            User savedUser = userService.updateUser(id, updatedUserData);
            UserDTO userDTO = userMapper.toDto(savedUser);
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            throw new DuplicateResourceException(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // Check if user exists first
        userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Additional endpoints
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Boolean> checkUsernameExists(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}