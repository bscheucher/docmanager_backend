package com.app.docmanager.mapper;

import com.app.docmanager.dto.DocumentDTO;
import com.app.docmanager.entity.Document;
import com.app.docmanager.entity.Tag;
import com.app.docmanager.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DocumentMapper {

    private final UserMapper userMapper;

    /**
     * Convert Document entity to DocumentDTO
     */
    public DocumentDTO toDto(Document document) {
        if (document == null) {
            return null;
        }

        return DocumentDTO.builder()
                .id(document.getId())
                .title(document.getTitle())
                .category(document.getCategory())
                .filePath(document.getFilePath())
                .fileType(document.getFileType())
                .fileSize(document.getFileSize())
                .extractedText(document.getExtractedText())
                .documentDate(document.getDocumentDate())
                .user(userMapper.toUserInfo(document.getUser()))
                .tags(mapTagsToStrings(document.getTags()))
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    /**
     * Convert CreateDocumentRequest to Document entity
     */
    public Document toEntity(DocumentDTO.CreateDocumentRequest request) {
        if (request == null) {
            return null;
        }

        Document.DocumentBuilder builder = Document.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .filePath(request.getFilePath())
                .fileType(request.getFileType())
                .fileSize(request.getFileSize())
                .extractedText(request.getExtractedText())
                .documentDate(request.getDocumentDate());

        // Set user if provided
        if (request.getUserId() != null) {
            User user = User.builder()
                    .id(request.getUserId())
                    .build();
            builder.user(user);
        }

        Document document = builder.build();

        // Handle tags - convert strings to Tag entities
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = request.getTags().stream()
                    .map(tagName -> Tag.builder()
                            .name(tagName.trim().toLowerCase())
                            .build())
                    .collect(Collectors.toSet());
            document.setTags(tags);
        }

        return document;
    }

    /**
     * Update existing Document entity with data from UpdateDocumentRequest
     */
    public Document updateEntity(Document existingDocument, DocumentDTO.UpdateDocumentRequest request) {
        if (request == null || existingDocument == null) {
            return existingDocument;
        }

        existingDocument.setTitle(request.getTitle());
        existingDocument.setCategory(request.getCategory());
        existingDocument.setFilePath(request.getFilePath());
        existingDocument.setFileType(request.getFileType());
        existingDocument.setFileSize(request.getFileSize());
        existingDocument.setExtractedText(request.getExtractedText());
        existingDocument.setDocumentDate(request.getDocumentDate());

        // Handle tags update
        if (request.getTags() != null) {
            // Clear existing tags
            existingDocument.clearTags();

            // Add new tags
            if (!request.getTags().isEmpty()) {
                Set<Tag> newTags = request.getTags().stream()
                        .map(tagName -> Tag.builder()
                                .name(tagName.trim().toLowerCase())
                                .build())
                        .collect(Collectors.toSet());
                existingDocument.setTags(newTags);
            }
        }

        return existingDocument;
    }

    /**
     * Convert list of Document entities to list of DocumentDTOs
     */
    public List<DocumentDTO> toDtoList(List<Document> documents) {
        if (documents == null) {
            return null;
        }

        return documents.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to convert Set<Tag> to Set<String>
     */
    private Set<String> mapTagsToStrings(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }

        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Helper method to convert Set<String> to Set<Tag>
     */
    private Set<Tag> mapStringsToTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return null;
        }

        return tagNames.stream()
                .map(tagName -> Tag.builder()
                        .name(tagName.trim().toLowerCase())
                        .build())
                .collect(Collectors.toSet());
    }
}