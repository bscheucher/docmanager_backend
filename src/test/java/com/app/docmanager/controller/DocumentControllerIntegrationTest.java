package com.app.docmanager.controller;

import com.app.docmanager.dto.AuthDTO;
import com.app.docmanager.dto.DocumentDTO;
import com.app.docmanager.entity.User;
import com.app.docmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class DocumentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String jwtToken;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Create and save a test user
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        testUser = userRepository.save(testUser);

        // Login to get JWT token
        AuthDTO.LoginRequest loginRequest = AuthDTO.LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        AuthDTO.JwtAuthResponse authResponse = objectMapper.readValue(response, AuthDTO.JwtAuthResponse.class);
        jwtToken = authResponse.getAccessToken();
    }

    @Test
    void createDocument_ValidRequest_ShouldCreateDocument() throws Exception {
        DocumentDTO.CreateDocumentRequest request = new DocumentDTO.CreateDocumentRequest();
        request.setTitle("Test Document");
        request.setCategory("Test Category");
        request.setFileType("application/pdf");
        request.setFileSize(1024L);
        request.setDocumentDate(LocalDate.now());
        request.setTags(Set.of("test", "document"));
        request.setUserId(testUser.getId());

        mockMvc.perform(post("/api/documents")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Document"))
                .andExpect(jsonPath("$.category").value("Test Category"))
                .andExpect(jsonPath("$.user.username").value("testuser"));
    }

    @Test
    void getAllDocuments_WithValidToken_ShouldReturnUserDocuments() throws Exception {
        // First create a document
        DocumentDTO.CreateDocumentRequest request = new DocumentDTO.CreateDocumentRequest();
        request.setTitle("My Document");
        request.setCategory("Personal");
        request.setUserId(testUser.getId());

        mockMvc.perform(post("/api/documents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then get all documents
        mockMvc.perform(get("/api/documents")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("My Document"));
    }

    @Test
    void uploadDocument_ValidFile_ShouldCreateDocumentWithFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Test content".getBytes());

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .param("title", "Uploaded Document")
                        .param("category", "Uploads")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Uploaded Document"))
                .andExpect(jsonPath("$.category").value("Uploads"))
                .andExpect(jsonPath("$.fileType").value("text/plain"))
                .andExpect(jsonPath("$.fileSize").value(12L));
    }

    @Test
    void getDocument_NonExistentId_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/documents/999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateDocument_ValidRequest_ShouldUpdateDocument() throws Exception {
        // First create a document
        DocumentDTO.CreateDocumentRequest createRequest = new DocumentDTO.CreateDocumentRequest();
        createRequest.setTitle("Original Title");
        createRequest.setCategory("Original Category");
        createRequest.setUserId(testUser.getId());

        MvcResult createResult = mockMvc.perform(post("/api/documents")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        DocumentDTO createdDocument = objectMapper.readValue(createResponse, DocumentDTO.class);

        // Then update it
        DocumentDTO.UpdateDocumentRequest updateRequest = new DocumentDTO.UpdateDocumentRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setCategory("Updated Category");

        mockMvc.perform(put("/api/documents/" + createdDocument.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.category").value("Updated Category"));
    }

    @Test
    void deleteDocument_ValidId_ShouldDeleteDocument() throws Exception {
        // First create a document
        DocumentDTO.CreateDocumentRequest createRequest = new DocumentDTO.CreateDocumentRequest();
        createRequest.setTitle("To Delete");
        createRequest.setUserId(testUser.getId());

        MvcResult createResult = mockMvc.perform(post("/api/documents")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        DocumentDTO createdDocument = objectMapper.readValue(createResponse, DocumentDTO.class);

        // Then delete it
        mockMvc.perform(delete("/api/documents/" + createdDocument.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get("/api/documents/" + createdDocument.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchDocuments_WithQuery_ShouldReturnMatchingDocuments() throws Exception {
        // Create some documents
        DocumentDTO.CreateDocumentRequest request1 = new DocumentDTO.CreateDocumentRequest();
        request1.setTitle("Java Programming Guide");
        request1.setUserId(testUser.getId());

        DocumentDTO.CreateDocumentRequest request2 = new DocumentDTO.CreateDocumentRequest();
        request2.setTitle("Python Tutorial");
        request2.setUserId(testUser.getId());

        mockMvc.perform(post("/api/documents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(post("/api/documents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        // Search for Java documents
        mockMvc.perform(get("/api/documents/search")
                        .param("query", "Java")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Java Programming Guide"));
    }

    @Test
    void getDocumentStats_WithValidToken_ShouldReturnStats() throws Exception {
        // Create some documents first
        DocumentDTO.CreateDocumentRequest request = new DocumentDTO.CreateDocumentRequest();
        request.setTitle("Stats Test Document");
        request.setUserId(testUser.getId());

        mockMvc.perform(post("/api/documents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/documents/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDocuments").value(1));
    }

    @Test
    void accessDocument_WithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void filterDocumentsByCategory_ShouldReturnFilteredResults() throws Exception {
        // Create documents with different categories
        DocumentDTO.CreateDocumentRequest request1 = new DocumentDTO.CreateDocumentRequest();
        request1.setTitle("Work Document");
        request1.setCategory("Work");
        request1.setUserId(testUser.getId());

        DocumentDTO.CreateDocumentRequest request2 = new DocumentDTO.CreateDocumentRequest();
        request2.setTitle("Personal Document");
        request2.setCategory("Personal");
        request2.setUserId(testUser.getId());

        mockMvc.perform(post("/api/documents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(post("/api/documents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        // Filter by Work category
        mockMvc.perform(get("/api/documents")
                        .param("category", "Work")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Work"));
    }
}