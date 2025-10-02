package com.app.docmanager.controller;

import com.app.docmanager.dto.AuthDTO;
import com.app.docmanager.dto.UserDTO;
import com.app.docmanager.entity.User;
import com.app.docmanager.mapper.UserMapper;
import com.app.docmanager.security.CurrentUser;
import com.app.docmanager.security.CustomUserDetails;
import com.app.docmanager.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<AuthDTO.JwtAuthResponse> login(@Valid @RequestBody AuthDTO.LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsernameOrEmail());
        AuthDTO.JwtAuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDTO.JwtAuthResponse> register(@Valid @RequestBody AuthDTO.RegisterRequest registerRequest) {
        log.info("Registration attempt for username: {}", registerRequest.getUsername());
        AuthDTO.JwtAuthResponse response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthDTO.JwtAuthResponse> refreshToken(@Valid @RequestBody AuthDTO.RefreshTokenRequest request) {
        AuthDTO.JwtAuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthDTO.MessageResponse> logout() {
        AuthDTO.MessageResponse response = authService.logout();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthDTO.MessageResponse> changePassword(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody AuthDTO.ChangePasswordRequest request) {

        log.info("Password change request for user: {}", currentUser.getUsername());
        AuthDTO.MessageResponse response = authService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthDTO.UserInfo> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @GetMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthDTO.MessageResponse> validateToken() {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .message("Token is valid")
                .success(true)
                .build());
    }
}