package com.example.aiworkshop.tasks.task_3_document_agent.model;

import java.time.Instant;

public record UploadedDocument(
        String id,
        String claimId,
        String filename,
        String contentType,
        long sizeBytes,
        Instant uploadedAt,
        String contentHash,
        DocumentAnalysis analysis,
        boolean reviewed) {
    public UploadedDocument markReviewed() {
        return new UploadedDocument(
                id, claimId, filename, contentType, sizeBytes, uploadedAt, contentHash, analysis, true);
    }
}
