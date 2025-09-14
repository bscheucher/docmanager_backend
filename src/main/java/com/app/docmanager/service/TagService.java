package com.app.docmanager.service;

import com.app.docmanager.entity.Tag;
import com.app.docmanager.exception.DuplicateResourceException;
import com.app.docmanager.exception.ResourceNotFoundException;
import com.app.docmanager.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }

    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findByName(name.toLowerCase().trim());
    }

    public List<Tag> getTagsByUserId(Long userId) {
        return tagRepository.findTagsByUserId(userId);
    }

    public List<Tag> searchTags(String query) {
        return tagRepository.findByNameContainingIgnoreCase(query.toLowerCase().trim());
    }

    @Transactional
    public Tag createTag(String tagName) {
        String normalizedName = tagName.toLowerCase().trim();

        if (tagRepository.existsByName(normalizedName)) {
            throw new DuplicateResourceException("Tag", "name", normalizedName);
        }

        Tag tag = Tag.builder()
                .name(normalizedName)
                .build();

        Tag savedTag = tagRepository.save(tag);
        log.info("Created new tag: {}", savedTag.getName());
        return savedTag;
    }

    @Transactional
    public Tag createOrGetTag(String tagName) {
        String normalizedName = tagName.toLowerCase().trim();

        return tagRepository.findByName(normalizedName)
                .orElseGet(() -> {
                    Tag newTag = Tag.builder()
                            .name(normalizedName)
                            .build();
                    return tagRepository.save(newTag);
                });
    }

    @Transactional
    public Set<Tag> createOrGetTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> normalizedNames = tagNames.stream()
                .map(name -> name.toLowerCase().trim())
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toSet());

        if (normalizedNames.isEmpty()) {
            return new HashSet<>();
        }

        // Find existing tags
        List<Tag> existingTags = tagRepository.findByNameIn(normalizedNames);
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        // Create new tags for names that don't exist
        Set<String> newTagNames = normalizedNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .collect(Collectors.toSet());

        List<Tag> newTags = newTagNames.stream()
                .map(name -> Tag.builder().name(name).build())
                .collect(Collectors.toList());

        if (!newTags.isEmpty()) {
            List<Tag> savedNewTags = tagRepository.saveAll(newTags);
            existingTags.addAll(savedNewTags);
        }

        return new HashSet<>(existingTags);
    }

    @Transactional
    public Tag updateTag(Long id, String newName) {
        String normalizedName = newName.toLowerCase().trim();

        Tag existingTag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));

        // Check if the new name is already taken by another tag
        if (!existingTag.getName().equals(normalizedName)
                && tagRepository.existsByName(normalizedName)) {
            throw new DuplicateResourceException("Tag", "name", normalizedName);
        }

        existingTag.setName(normalizedName);
        Tag savedTag = tagRepository.save(existingTag);
        log.info("Updated tag: {} to {}", existingTag.getName(), savedTag.getName());
        return savedTag;
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));

        // Note: Due to the ManyToMany relationship with documents,
        // we need to handle the relationship cleanup
        if (!tag.getDocuments().isEmpty()) {
            log.warn("Deleting tag '{}' that is associated with {} documents",
                    tag.getName(), tag.getDocuments().size());
        }

        tagRepository.delete(tag);
        log.info("Deleted tag: {}", tag.getName());
    }

    @Transactional
    public void deleteUnusedTags() {
        List<Tag> unusedTags = tagRepository.findUnusedTags();
        if (!unusedTags.isEmpty()) {
            tagRepository.deleteAll(unusedTags);
            log.info("Deleted {} unused tags", unusedTags.size());
        }
    }

    public boolean existsByName(String name) {
        return tagRepository.existsByName(name.toLowerCase().trim());
    }

    /**
     * Get tag statistics
     */
    public TagStats getTagStats() {
        long totalTags = tagRepository.count();
        long unusedTags = tagRepository.findUnusedTags().size();

        return TagStats.builder()
                .totalTags(totalTags)
                .unusedTags(unusedTags)
                .usedTags(totalTags - unusedTags)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class TagStats {
        private long totalTags;
        private long usedTags;
        private long unusedTags;
    }
}