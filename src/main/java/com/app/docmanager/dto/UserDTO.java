package com.app.docmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    private String fullName;
    private int documentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Request DTOs for different operations
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUserRequest {
        @NotBlank(message = "Username cannot be blank")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        private String email;

        private String firstName;
        private String lastName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateUserRequest {
        @NotBlank(message = "Username cannot be blank")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        private String email;

        private String firstName;
        private String lastName;
    }
}