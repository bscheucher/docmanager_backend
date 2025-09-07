package com.app.docmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(exclude = "documents")
public class User extends BaseEntity {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    @EqualsAndHashCode.Include
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    @EqualsAndHashCode.Include
    private String email;

    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Column(name = "first_name", length = 100)
    private String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Column(name = "last_name", length = 100)
    private String lastName;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Document> documents = new ArrayList<>();

    // Custom constructors
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.documents = new ArrayList<>();
    }

    public User(String username, String email, String firstName, String lastName) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.documents = new ArrayList<>();
    }

    // Utility methods for managing documents
    public void addDocument(Document document) {
        documents.add(document);
        document.setUser(this);
    }

    public void removeDocument(Document document) {
        documents.remove(document);
        document.setUser(null);
    }

    public void clearDocuments() {
        for (Document document : new ArrayList<>(documents)) {
            removeDocument(document);
        }
    }

    // Convenience method to get full name
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return username;
        }
    }

    // Method to get document count
    public int getDocumentCount() {
        return documents != null ? documents.size() : 0;
    }
}