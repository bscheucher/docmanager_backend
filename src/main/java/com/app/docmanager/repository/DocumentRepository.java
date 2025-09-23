package com.app.docmanager.repository;

import com.app.docmanager.entity.Document;
import com.app.docmanager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUser(User user);
    List<Document> findByUserIdAndCategory(Long userId, String category);
    List<Document> findByUserIdOrderByCreatedAtDesc(Long userId);

    // New paginated methods
    Page<Document> findByUserId(Long userId, Pageable pageable);

    Page<Document> findByUserIdAndCategory(Long userId, String category, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.user.id = :userId ORDER BY d.createdAt DESC")
    Page<Document> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.user.id = :userId AND " +
            "LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Document> searchByUserIdAndTitle(@Param("userId") Long userId,
                                          @Param("query") String query,
                                          Pageable pageable);

    // Global search (for admins)
    @Query("SELECT d FROM Document d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Document> searchByTitle(@Param("query") String query, Pageable pageable);
}