package com.app.docmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(exclude = "documents")
public class Tag extends BaseEntity {

    @NotBlank(message = "Tag name cannot be blank")
    @Size(max = 100, message = "Tag name cannot exceed 100 characters")
    @Column(name = "name", unique = true, nullable = false, length = 100)
    @EqualsAndHashCode.Include
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Document> documents = new HashSet<>();

    // Constructor for just name
    public Tag(String name) {
        this.name = name;
    }

    // Utility methods for managing relationships
    public void addDocument(Document document) {
        this.documents.add(document);
        document.getTags().add(this);
    }

    public void removeDocument(Document document) {
        this.documents.remove(document);
        document.getTags().remove(this);
    }
}
