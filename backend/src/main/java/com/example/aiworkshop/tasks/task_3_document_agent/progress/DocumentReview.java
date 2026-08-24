package com.example.aiworkshop.tasks.task_3_document_agent.progress;

import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentStore;
import org.springframework.stereotype.Service;

/**
 * A handler overriding the agent.
 *
 * <p>When the analyser calls a document too poor to work with, the claim stops. A human who can read
 * it anyway says so here, and the claim moves on. Every agent that blocks something needs one of
 * these, or the first wrong answer is the last thing that ever happens on that claim.
 */
@Service
public class DocumentReview {
    private final DocumentStore documents;

    public DocumentReview(DocumentStore documents) {
        this.documents = documents;
    }

    public void markReviewed(String documentId) {
        documents.findById(documentId).map(UploadedDocument::markReviewed).ifPresent(documents::save);
    }
}
