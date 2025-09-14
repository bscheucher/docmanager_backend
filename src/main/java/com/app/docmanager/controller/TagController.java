package com.app.docmanager.controller;

import com.app.docmanager.entity.Tag;
import com.app.docmanager.exception.ResourceNotFoundException;
import com.app.docmanager.security.CurrentUser;
import com.app.docmanager.security.CustomUserDetails;
import com.app.docmanager.service.TagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Slf4j
public class TagController {

    private final TagService tagService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<TagResponse>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        List<TagResponse> tagResponses = tags.stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(tagResponses);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<TagResponse>> getMyTags(
            @CurrentUser CustomUserDetails currentUser) {
        List<Tag> tags = tagService.getTagsByUserId(currentUser.getId());
        List<TagResponse> tagResponses = tags.stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(tagResponses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TagResponse> getTagById(@PathVariable Long id) {
        return tagService.getTagById(id)
                .map(tag -> ResponseEntity.ok(mapToResponse(tag)))
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<TagResponse>> searchTags(@RequestParam String query) {
        List<Tag> tags = tagService.searchTags(query);
        List<TagResponse> tagResponses = tags.stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(tagResponses);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TagResponse> createTag(
            @Valid @RequestBody CreateTagRequest request) {
        Tag tag = tagService.createTag(request.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapToResponse(tag));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTagRequest request) {
        Tag tag = tagService.updateTag(id, request.getName());
        return ResponseEntity.ok(mapToResponse(tag));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/unused")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteUnusedTags() {
        tagService.deleteUnusedTags();
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Unused tags deleted successfully")
                .success(true)
                .build());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagService.TagStats> getTagStats() {
        TagService.TagStats stats = tagService.getTagStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/check/{name}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Boolean> checkTagExists(@PathVariable String name) {
        boolean exists = tagService.existsByName(name);
        return ResponseEntity.ok(exists);
    }

    // Helper method to map Tag entity to response DTO
    private TagResponse mapToResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .documentCount(tag.getDocuments().size())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }

    // Inner classes for request/response DTOs
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class TagResponse {
        private Long id;
        private String name;
        private int documentCount;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CreateTagRequest {
        @NotBlank(message = "Tag name is required")
        private String name;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class UpdateTagRequest {
        @NotBlank(message = "Tag name is required")
        private String name;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class MessageResponse {
        private String message;
        private boolean success;
    }
}