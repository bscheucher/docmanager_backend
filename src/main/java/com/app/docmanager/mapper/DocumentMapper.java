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
     * Convert CreateDocumentRequest to Document entity WITHOUT user
     * Tags will be resolved in the service layer
     */
    public Document toEntityWithoutUser(DocumentDTO.CreateDocumentRequest request) {
        if (request == null) {
            return null;
        }

        return Document.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .filePath(request.getFilePath())
                .fileType(request.getFileType())
                .fileSize(request.getFileSize())
                .extractedText(request.getExtractedText())
                .documentDate(request.getDocumentDate())
                .build();
        // Note: Tags will be handled in the service layer
    }

    /**
     * Update existing Document entity with data from UpdateDocumentRequest
     * Tags will be resolved in the service layer
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

        // Note: Tags will be handled in the service layer
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
}