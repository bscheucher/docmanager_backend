package com.app.docmanager.mapper;

import com.app.docmanager.dto.UserDTO;
import com.app.docmanager.dto.DocumentDTO;
import com.app.docmanager.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper userMapper;
    private User testUser;
    private UserDTO.CreateUserRequest createRequest;
    private UserDTO.UpdateUserRequest updateRequest;



    @Test
    void toDto_WhenUserProvided_ShouldMapCorrectly() {
        // When
        UserDTO result = userMapper.toDto(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getFullName()).isEqualTo("Test User");
        assertThat(result.getDocumentCount()).isEqualTo(0);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void toDto_WhenUserIsNull_ShouldReturnNull() {
        // When
        UserDTO result = userMapper.toDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEntity_WhenCreateRequestProvided_ShouldMapCorrectly() {
        // When
        User result = userMapper.toEntity(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getFirstName()).isEqualTo("New");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getId()).isNull(); // Should be null for new entities
    }

    @Test
    void toEntity_WhenCreateRequestIsNull_ShouldReturnNull() {
        // When
        User result = userMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void updateEntity_WhenUpdateRequestProvided_ShouldUpdateExistingUser() {
        // When
        User result = userMapper.updateEntity(testUser, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L); // ID should remain the same
        assertThat(result.getUsername()).isEqualTo("updateduser");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getFirstName()).isEqualTo("Updated");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getCreatedAt()).isNotNull(); // Should preserve timestamps
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateEntity_WhenUpdateRequestIsNull_ShouldReturnOriginalUser() {
        // When
        User result = userMapper.updateEntity(testUser, null);

        // Then
        assertThat(result).isSameAs(testUser);
    }

    @Test
    void updateEntity_WhenExistingUserIsNull_ShouldReturnNull() {
        // When
        User result = userMapper.updateEntity(null, updateRequest);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toDtoList_WhenUserListProvided_ShouldMapAllUsers() {
        // Given
        User user2 = User.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .build();

        List<User> users = Arrays.asList(testUser, user2);

        // When
        List<UserDTO> result = userMapper.toDtoList(users);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
        assertThat(result.get(1).getUsername()).isEqualTo("user2");
    }

    @Test
    void toDtoList_WhenUserListIsNull_ShouldReturnNull() {
        // When
        List<UserDTO> result = userMapper.toDtoList(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toUserInfo_WhenUserProvided_ShouldMapToUserInfo() {
        // When
        DocumentDTO.UserInfo result = userMapper.toUserInfo(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getFullName()).isEqualTo("Test User");
    }

    @Test
    void toUserInfo_WhenUserIsNull_ShouldReturnNull() {
        // When
        DocumentDTO.UserInfo result = userMapper.toUserInfo(null);

        // Then
        assertThat(result).isNull();
    }
}