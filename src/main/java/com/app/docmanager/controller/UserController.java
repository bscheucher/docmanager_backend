package com.app.docmanager.controller;

import com.app.docmanager.dto.UserDTO;
import com.app.docmanager.entity.User;
import com.app.docmanager.mapper.UserMapper;
import com.app.docmanager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(userMapper.toDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(user -> ResponseEntity.ok(userMapper.toDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO.CreateUserRequest request) {
        try {
            User user = userMapper.toEntity(request);
            User savedUser = userService.createUser(user);
            UserDTO userDTO = userMapper.toDto(savedUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO.UpdateUserRequest request) {
        try {
            User updatedUser = userMapper.updateEntity(new User(), request);
            updatedUser = userService.updateUser(id, updatedUser);
            UserDTO userDTO = userMapper.toDto(updatedUser);
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
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