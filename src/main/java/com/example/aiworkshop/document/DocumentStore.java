package com.example.aiworkshop.document;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Comparator;
import org.springframework.stereotype.Component;

/**
 * Where uploaded documents live. In memory, so everything is lost on restart.
 *
 * <p>Kept to four methods on purpose: this is the one class to replace when documents need to
 * outlive the process. Nothing above it knows how storage works, so swapping in a repository is a
 * change to this file and its constructor call, and nowhere else.
 */
@Component
public class DocumentStore {

    private final ConcurrentMap<String, UploadedDocument> documents = new ConcurrentHashMap<>();

    public void save(UploadedDocument document) {
        documents.put(document.id(), document);
    }

    public Optional<UploadedDocument> findById(String id) {
        return Optional.ofNullable(documents.get(id));
    }

    /** Newest first — the list is a feed, and the thing you just uploaded should be at the top. */
    public List<UploadedDocument> findAll() {
        return documents.values().stream()
                .sorted(Comparator.comparing(UploadedDocument::uploadedAt).reversed())
                .toList();
    }

    /** Everything attached to one Case, newest first. Every upload is here, including superseded ones. */
    public List<UploadedDocument> findByCaseId(String caseId) {
        return findAll().stream()
                .filter(document -> document.caseId().equals(caseId))
                .toList();
    }

    public void deleteAll() {
        documents.clear();
    }
}
