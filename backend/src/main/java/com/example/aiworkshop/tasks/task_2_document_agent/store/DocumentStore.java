package com.example.aiworkshop.tasks.task_2_document_agent.store;

import com.example.aiworkshop.tasks.task_2_document_agent.model.UploadedDocument;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Comparator;
import org.springframework.stereotype.Component;

@Component
public class DocumentStore {
    private final ConcurrentMap<String, UploadedDocument> documents = new ConcurrentHashMap<>();

    public void save(UploadedDocument document) {
        documents.put(document.id(), document);
    }

    public Optional<UploadedDocument> findById(String id) {
        return Optional.ofNullable(documents.get(id));
    }

    public List<UploadedDocument> findAll() {
        return documents.values().stream()
                .sorted(Comparator.comparing(UploadedDocument::uploadedAt).reversed())
                .toList();
    }

    public List<UploadedDocument> findByCaseId(String caseId) {
        return findAll().stream()
                .filter(document -> document.caseId().equals(caseId))
                .toList();
    }

    public void deleteAll() {
        documents.clear();
    }
}
