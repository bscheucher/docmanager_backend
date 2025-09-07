package com.app.docmanager.repository;

import com.app.docmanager.entity.Document;
import com.app.docmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUser(User user);
    List<Document> findByUserIdAndCategory(Long userId, String category);
    List<Document> findByUserIdOrderByCreatedAtDesc(Long userId);
}