package com.app.docmanager.service;

import com.app.docmanager.entity.Document;
import com.app.docmanager.entity.User;
import com.app.docmanager.repository.DocumentRepository;
import com.app.docmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

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

    @Transactional
    public Document createDocument(Document document) {
        // Validate that the user exists
        if (document.getUser() == null || document.getUser().getId() == null) {
            throw new RuntimeException("Document must be associated with a user");
        }

        User user = userRepository.findById(document.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + document.getUser().getId()));

        document.setUser(user);
        return documentRepository.save(document);
    }

    @Transactional
    public Document updateDocument(Long id, Document updatedDocument) {
        return documentRepository.findById(id)
                .map(existingDocument -> {
                    existingDocument.setTitle(updatedDocument.getTitle());
                    existingDocument.setCategory(updatedDocument.getCategory());
                    existingDocument.setFilePath(updatedDocument.getFilePath());
                    existingDocument.setFileType(updatedDocument.getFileType());
                    existingDocument.setFileSize(updatedDocument.getFileSize());
                    existingDocument.setExtractedText(updatedDocument.getExtractedText());
                    existingDocument.setDocumentDate(updatedDocument.getDocumentDate());

                    // Handle user change if provided
                    if (updatedDocument.getUser() != null && updatedDocument.getUser().getId() != null) {
                        User newUser = userRepository.findById(updatedDocument.getUser().getId())
                                .orElseThrow(() -> new RuntimeException("User not found with id: " + updatedDocument.getUser().getId()));
                        existingDocument.setUser(newUser);
                    }

                    return documentRepository.save(existingDocument);
                })
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
    }

    @Transactional
    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new RuntimeException("Document not found with id: " + id);
        }
        documentRepository.deleteById(id);
    }

    // Business logic methods
    public long countDocumentsByUser(Long userId) {
        return documentRepository.findByUserIdOrderByCreatedAtDesc(userId).size();
    }

    public List<Document> searchDocumentsByTitle(String title) {
        // This would need a custom query method in the repository
        // For now, returning all documents (you'd implement actual search logic)
        return documentRepository.findAll().stream()
                .filter(doc -> doc.getTitle().toLowerCase().contains(title.toLowerCase()))
                .toList();
    }
}