package com.app.docmanager.service;

import com.app.docmanager.dto.DocumentDTO;
import com.app.docmanager.entity.Document;
import com.app.docmanager.entity.Tag;
import com.app.docmanager.entity.User;
import com.app.docmanager.exception.ResourceNotFoundException;
import com.app.docmanager.repository.DocumentRepository;
import com.app.docmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final TagService tagService;

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    public List<Document> getDocumentsByUser(User user) {
        return documentRepository.findByUser(user);
    }

    public List<Document> getDocumentsByUserId(Long userId) {
        return documentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Document> getDocumentsByUserAndCategory(Long userId, String category) {
        return documentRepository.findByUserIdAndCategory(userId, category);
    }

    /**
     * Create a document for a specific user with tag resolution
     */
    @Transactional
    public Document createDocument(Document document, Long userId, Set<String> tagNames) {
        // Fetch and validate the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Set the user on the document
        document.setUser(user);

        // Handle tags if present
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<Tag> managedTags = tagService.createOrGetTags(tagNames);
            document.setTags(managedTags);
        }

        return documentRepository.save(document);
    }

    /**
     * Update document with proper tag resolution
     */
    @Transactional
    public Document updateDocument(Long id, Document updatedDocument, Set<String> tagNames) {
        return documentRepository.findById(id)
                .map(existingDocument -> {
                    // Update basic fields
                    existingDocument.setTitle(updatedDocument.getTitle());
                    existingDocument.setCategory(updatedDocument.getCategory());
                    existingDocument.setFilePath(updatedDocument.getFilePath());
                    existingDocument.setFileType(updatedDocument.getFileType());
                    existingDocument.setFileSize(updatedDocument.getFileSize());
                    existingDocument.setExtractedText(updatedDocument.getExtractedText());
                    existingDocument.setDocumentDate(updatedDocument.getDocumentDate());

                    // Handle tags update
                    if (tagNames != null) {
                        // Clear existing tags
                        existingDocument.clearTags();

                        if (!tagNames.isEmpty()) {
                            Set<Tag> managedTags = tagService.createOrGetTags(tagNames);
                            existingDocument.setTags(managedTags);
                        }
                    }

                    return documentRepository.save(existingDocument);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
    }

    @Transactional
    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Document", "id", id);
        }
        documentRepository.deleteById(id);
    }

    // Business logic methods
    public long countDocumentsByUser(Long userId) {
        return documentRepository.findByUserIdOrderByCreatedAtDesc(userId).size();
    }

    public List<Document> searchDocumentsByTitle(String title) {
        return documentRepository.findAll().stream()
                .filter(doc -> doc.getTitle().toLowerCase().contains(title.toLowerCase()))
                .toList();
    }

    // NEW: Paginated methods
    public Page<Document> getAllDocumentsPaginated(Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    public Page<Document> getDocumentsByUserIdPaginated(Long userId, Pageable pageable) {
        return documentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<Document> getDocumentsByUserAndCategoryPaginated(Long userId, String category, Pageable pageable) {
        return documentRepository.findByUserIdAndCategory(userId, category, pageable);
    }

    public Page<Document> searchDocumentsByTitlePaginated(String title, Pageable pageable) {
        return documentRepository.searchByTitle(title, pageable);
    }

    public Page<Document> searchDocumentsByUserAndTitlePaginated(Long userId, String title, Pageable pageable) {
        return documentRepository.searchByUserIdAndTitle(userId, title, pageable);
    }
}