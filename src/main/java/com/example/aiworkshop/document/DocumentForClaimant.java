package com.example.aiworkshop.document;

import java.time.Instant;
import java.util.List;

public record DocumentForClaimant(
        String id,
        String caseId,
        String filename,
        String contentType,
        long sizeBytes,
        Instant uploadedAt,
        AnalysisForClaimant analysis,
        boolean reviewed) {
    public static DocumentForClaimant of(UploadedDocument document) {
        DocumentAnalysis analysis = document.analysis();
        return new DocumentForClaimant(
                document.id(),
                document.caseId(),
                document.filename(),
                document.contentType(),
                document.sizeBytes(),
                document.uploadedAt(),
                new AnalysisForClaimant(
                        analysis.category(),
                        analysis.summary(),
                        analysis.fields(),
                        analysis.matchedRequiredDocument(),
                        analysis.matchConfidence(),
                        analysis.quality()),
                document.reviewed());
    }

    public static List<DocumentForClaimant> of(List<UploadedDocument> documents) {
        return documents.stream().map(DocumentForClaimant::of).toList();
    }

    public record AnalysisForClaimant(
            String category,
            String summary,
            List<ExtractedField> fields,
            String matchedRequiredDocument,
            MatchConfidence matchConfidence,
            QualityAssessment quality) {}
}
