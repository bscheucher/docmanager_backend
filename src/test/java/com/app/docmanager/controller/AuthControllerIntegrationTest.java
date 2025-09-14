package com.app.docmanager.controller;

import com.app.docmanager.dto.AuthDTO;
import com.app.docmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void register_ValidRequest_ShouldCreateUser() throws Exception {
        AuthDTO.RegisterRequest registerRequest = AuthDTO.RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user.username").value("newuser"))
                .andExpect(jsonPath("$.user.email").value("newuser@example.com"));
    }

    @Test
    void register_DuplicateUsername_ShouldReturnConflict() throws Exception {
        // First registration
        AuthDTO.RegisterRequest firstRequest = AuthDTO.RegisterRequest.builder()
                .username("duplicate")
                .email("first@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Second registration with same username
        AuthDTO.RegisterRequest secondRequest = AuthDTO.RegisterRequest.builder()
                .username("duplicate")
                .email("second@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already taken!"));
    }

    @Test
    void login_ValidCredentials_ShouldReturnToken() throws Exception {
        // Register user first
        AuthDTO.RegisterRequest registerRequest = AuthDTO.RegisterRequest.builder()
                .username("logintest")
                .email("login@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Login
        AuthDTO.LoginRequest loginRequest = AuthDTO.LoginRequest.builder()
                .usernameOrEmail("logintest")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user.username").value("logintest"));
    }

    @Test
    void login_InvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        AuthDTO.LoginRequest loginRequest = AuthDTO.LoginRequest.builder()
                .usernameOrEmail("nonexistent")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}