package com.app.docmanager.repository;

import com.app.docmanager.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    List<Tag> findByNameIn(Set<String> names);

    // New paginated methods
    @Query("SELECT t FROM Tag t WHERE t.name LIKE %:name%")
    Page<Tag> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT t FROM Tag t JOIN t.documents d WHERE d.user.id = :userId")
    Page<Tag> findTagsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Tag t WHERE SIZE(t.documents) = 0")
    Page<Tag> findUnusedTags(Pageable pageable);

    @Query("SELECT t FROM Tag t WHERE t.name LIKE %:name%")
    List<Tag> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT t FROM Tag t JOIN t.documents d WHERE d.user.id = :userId")
    List<Tag> findTagsByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Tag t WHERE SIZE(t.documents) = 0")
    List<Tag> findUnusedTags();
}