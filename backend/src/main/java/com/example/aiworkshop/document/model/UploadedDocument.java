package com.example.aiworkshop.document.model;

import java.time.Instant;

public record UploadedDocument(
        String id,
        String caseId,
        String filename,
        String contentType,
        long sizeBytes,
        Instant uploadedAt,
        String contentHash,
        DocumentAnalysis analysis,
        boolean reviewed) {
    public UploadedDocument markReviewed() {
        return new UploadedDocument(
                id, caseId, filename, contentType, sizeBytes, uploadedAt, contentHash, analysis, true);
    }
}
