package com.app.docmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"tags", "user"})
@ToString(exclude = {"tags", "user"})
public class Document extends BaseEntity {

    @NotBlank(message = "Document title cannot be blank")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Column(name = "title", nullable = false)
    private String title;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    @Column(name = "category", length = 100)
    private String category;

    @Size(max = 500, message = "File path cannot exceed 500 characters")
    @Column(name = "file_path", length = 500)
    private String filePath;

    @Size(max = 50, message = "File type cannot exceed 50 characters")
    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Lob
    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "document_date")
    private LocalDate documentDate;

    @NotNull(message = "Document must belong to a user")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "document_tags",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    // Custom constructors
    public Document(String title, User user) {
        this.title = title;
        this.user = user;
        this.tags = new HashSet<>();
    }

    public Document(String title, String category, User user) {
        this.title = title;
        this.category = category;
        this.user = user;
        this.tags = new HashSet<>();
    }

    public Document(String title, String category, String filePath, User user) {
        this.title = title;
        this.category = category;
        this.filePath = filePath;
        this.user = user;
        this.tags = new HashSet<>();
    }

    // Utility methods for managing tags
    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getDocuments().add(this);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getDocuments().remove(this);
    }

    public void clearTags() {
        for (Tag tag : new HashSet<>(tags)) {
            removeTag(tag);
        }
    }

    // Utility methods for user relationship
    public void setUser(User user) {
        // Remove from previous user if exists
        if (this.user != null) {
            this.user.getDocuments().remove(this);
        }

        this.user = user;

        // Add to new user if not null
        if (user != null && !user.getDocuments().contains(this)) {
            user.getDocuments().add(this);
        }
    }

    // Convenience method to get user's username
    public String getUserUsername() {
        return user != null ? user.getUsername() : null;
    }

    // Convenience method to get user's full name
    public String getUserFullName() {
        return user != null ? user.getFullName() : null;
    }
}