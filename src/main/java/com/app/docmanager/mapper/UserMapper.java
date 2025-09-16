package com.app.docmanager.mapper;

import com.app.docmanager.dto.UserDTO;
import com.app.docmanager.dto.DocumentDTO;
import com.app.docmanager.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    /**
     * Convert User entity to UserDTO
     */
    public UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .documentCount(user.getDocumentCount())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Convert CreateUserRequest to User entity
     */
    public User toEntity(UserDTO.CreateUserRequest request) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
    }

    /**
     * Update existing User entity with data from UpdateUserRequest
     */
    public User updateEntity(User existingUser, UserDTO.UpdateUserRequest request) {
        if (request == null || existingUser == null) {
            return existingUser;
        }

        // Only update non-null values
        if (request.getUsername() != null) {
            existingUser.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            existingUser.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            existingUser.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            existingUser.setLastName(request.getLastName());
        }

        return existingUser;
    }

    /**
     * Convert list of User entities to list of UserDTOs
     */
    public List<UserDTO> toDtoList(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert User entity to DocumentDTO.UserInfo (for nested user info in documents)
     */
    public DocumentDTO.UserInfo toUserInfo(User user) {
        if (user == null) {
            return null;
        }

        return DocumentDTO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .build();
    }
}