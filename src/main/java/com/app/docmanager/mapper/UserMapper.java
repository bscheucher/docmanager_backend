package com.app.docmanager.mapper;

import com.app.docmanager.dto.UserDTO;
import com.app.docmanager.dto.DocumentDTO;
import com.app.docmanager.entity.User;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

import java.util.List;

public class UserMapper {
    public List<UserDTO> toDtoList(List<User> users) {
        return null;
    }

    public Object toDto(User user) {
        return null;
    }

    public User toEntity(UserDTO.@Valid CreateUserRequest request) {
        return null;
    }

    public User updateEntity(User user, UserDTO.@Valid UpdateUserRequest request) {
        return null;
    }
}
