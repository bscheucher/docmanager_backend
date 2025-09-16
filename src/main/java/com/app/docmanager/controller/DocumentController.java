package com.app.docmanager.controller;

import com.app.docmanager.dto.DocumentDTO;
import com.app.docmanager.entity.Document;
import com.app.docmanager.exception.ResourceNotFoundException;
import com.app.docmanager.mapper.DocumentMapper;
import com.app.docmanager.security.CurrentUser;
import com.app.docmanager.security.CustomUserDetails;
import com.app.docmanager.service.DocumentService;
import com.app.docmanager.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;
    private final FileStorageService fileStorageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<DocumentDTO>> getAllDocuments(
            @CurrentUser CustomUserDetails currentUser,
            @RequestParam(required = false) String category) {

        List<Document> documents;
        if (category != null && !category.isEmpty()) {
            documents = documentService.getDocumentsByUserAndCategory(currentUser.getId(), category);
        } else {
            documents = documentService.getDocumentsByUserId(currentUser.getId());
        }

        List<DocumentDTO> documentDTOs = documentMapper.toDtoList(documents);
        return ResponseEntity.ok(documentDTOs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DocumentDTO> getDocumentById(
            @PathVariable Long id,
            @CurrentUser CustomUserDetails currentUser) {

        return documentService.getDocumentById(id)
                .filter(document -> document.getUser().getId().equals(currentUser.getId())
                        || currentUser.getRoles().stream().anyMatch(role -> role.name().equals("ROLE_ADMIN")))
                .map(document -> ResponseEntity.ok(documentMapper.toDto(document)))
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DocumentDTO> createDocument(
            @Valid @RequestBody DocumentDTO.CreateDocumentRequest request,
            @CurrentUser CustomUserDetails currentUser) {

        // Map request to document entity (without user)
        Document document = documentMapper.toEntityWithoutUser(request);

        // Service handles user assignment and tag resolution securely
        Document savedDocument = documentService.createDocument(document, currentUser.getId(), request.getTags());
        DocumentDTO documentDTO = documentMapper.toDto(savedDocument);

        return ResponseEntity.status(HttpStatus.CREATED).body(documentDTO);
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DocumentDTO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) String tagsParam,
            @CurrentUser CustomUserDetails currentUser) {

        try {
            // Store the file
            String fileName = fileStorageService.storeFile(file);

            // Parse tags from request parameter (if provided)
            Set<String> tags = null;
            if (tagsParam != null && !tagsParam.trim().isEmpty()) {
                tags = Arrays.stream(tagsParam.split(","))
                        .map(String::trim)
                        .filter(tag -> !tag.isEmpty())
                        .collect(Collectors.toSet());
            }

            // Create document record without user
            DocumentDTO.CreateDocumentRequest request = new DocumentDTO.CreateDocumentRequest();
            request.setTitle(title);
            request.setCategory(category);
            request.setFilePath(fileName);
            request.setFileType(file.getContentType());
            request.setFileSize(file.getSize());
            request.setTags(tags);

            Document document = documentMapper.toEntityWithoutUser(request);
            Document savedDocument = documentService.createDocument(document, currentUser.getId(), tags);
            DocumentDTO documentDTO = documentMapper.toDto(savedDocument);

            log.info("File uploaded successfully: {} by user: {}", fileName, currentUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(documentDTO);

        } catch (Exception e) {
            log.error("Error uploading file", e);
            throw new RuntimeException("Could not upload file: " + e.getMessage());
        }
    }
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            @CurrentUser CustomUserDetails currentUser) {

        Document document = documentService.getDocumentById(id)
                .filter(doc -> doc.getUser().getId().equals(currentUser.getId())
                        || currentUser.getRoles().stream().anyMatch(role -> role.name().equals("ROLE_ADMIN")))
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        if (document.getFilePath() == null) {
            throw new RuntimeException("No file associated with this document");
        }

        try {
            Path filePath = fileStorageService.getFileStorageLocation().resolve(document.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = fileStorageService.getContentType(document.getFilePath());

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + document.getTitle() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("File not found: " + document.getFilePath());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + document.getFilePath());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DocumentDTO> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentDTO.UpdateDocumentRequest request,
            @CurrentUser CustomUserDetails currentUser) {

        // Check if document exists and user has permission
        Document existingDocument = documentService.getDocumentById(id)
                .filter(doc -> doc.getUser().getId().equals(currentUser.getId())
                        || currentUser.getRoles().stream().anyMatch(role -> role.name().equals("ROLE_ADMIN")))
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        // Map the update request to document entity
        Document updatedDocumentData = documentMapper.updateEntity(existingDocument, request);

        // Update the document with tag resolution in service layer
        Document savedDocument = documentService.updateDocument(id, updatedDocumentData, request.getTags());
        DocumentDTO documentDTO = documentMapper.toDto(savedDocument);

        return ResponseEntity.ok(documentDTO);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            @CurrentUser CustomUserDetails currentUser) {

        // Check if document exists and user has permission
        Document document = documentService.getDocumentById(id)
                .filter(doc -> doc.getUser().getId().equals(currentUser.getId())
                        || currentUser.getRoles().stream().anyMatch(role -> role.name().equals("ROLE_ADMIN")))
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        // Delete associated file if exists
        if (document.getFilePath() != null) {
            fileStorageService.deleteFile(document.getFilePath());
        }

        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<DocumentDTO>> searchDocuments(
            @RequestParam String query,
            @CurrentUser CustomUserDetails currentUser) {

        List<Document> documents = documentService.searchDocumentsByTitle(query);

        // Filter by current user (unless admin)
        if (!currentUser.getRoles().stream().anyMatch(role -> role.name().equals("ROLE_ADMIN"))) {
            documents = documents.stream()
                    .filter(doc -> doc.getUser().getId().equals(currentUser.getId()))
                    .toList();
        }

        List<DocumentDTO> documentDTOs = documentMapper.toDtoList(documents);
        return ResponseEntity.ok(documentDTOs);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DocumentStatsResponse> getDocumentStats(
            @CurrentUser CustomUserDetails currentUser) {

        long totalDocuments = documentService.countDocumentsByUser(currentUser.getId());

        DocumentStatsResponse stats = DocumentStatsResponse.builder()
                .totalDocuments(totalDocuments)
                .build();

        return ResponseEntity.ok(stats);
    }

    // Inner class for stats response
    @lombok.Data
    @lombok.Builder
    public static class DocumentStatsResponse {
        private long totalDocuments;
    }
}