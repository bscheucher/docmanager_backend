package com.app.docmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDTO {

    private Long id;

    @NotBlank(message = "Document title cannot be blank")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    private String filePath;
    private String fileType;
    private Long fileSize;
    private String extractedText;
    private LocalDate documentDate;

    // User information (nested)
    private UserInfo user;
    private Set<String> tags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String fullName;
    }

    // Request DTOs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDocumentRequest {
        @NotBlank(message = "Document title cannot be blank")
        @Size(max = 255, message = "Title cannot exceed 255 characters")
        private String title;

        @Size(max = 100, message = "Category cannot exceed 100 characters")
        private String category;

        private String filePath;
        private String fileType;
        private Long fileSize;
        private String extractedText;
        private LocalDate documentDate;

        @NotNull(message = "User ID is required")
        private Long userId;

        private Set<String> tags;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDocumentRequest {
        @NotBlank(message = "Document title cannot be blank")
        @Size(max = 255, message = "Title cannot exceed 255 characters")
        private String title;

        @Size(max = 100, message = "Category cannot exceed 100 characters")
        private String category;

        private String filePath;
        private String fileType;
        private Long fileSize;
        private String extractedText;
        private LocalDate documentDate;

        private Set<String> tags;
    }
}