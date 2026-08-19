package com.example.aiworkshop.cases.model;

import com.example.aiworkshop.documents.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_2_document_agent.model.QualityAssessment;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record Case(String id, String reference, CaseType type, List<String> requiredDocuments) {
    public CaseStatus status(List<UploadedDocument> documents) {
        if (!unmatchedRequiredDocuments(documents).isEmpty()) {
            return CaseStatus.AWAITING_DOCUMENTS;
        }
        return blockedDocuments(documents).isEmpty() ? CaseStatus.READY_FOR_DECISION : CaseStatus.NEEDS_REVIEW;
    }

    public List<String> unmatchedRequiredDocuments(List<UploadedDocument> documents) {
        return requiredDocuments.stream()
                .filter(required -> countingDocument(documents, required).isEmpty())
                .toList();
    }

    public List<UploadedDocument> countingDocuments(List<UploadedDocument> documents) {
        return requiredDocuments.stream()
                .flatMap(required -> countingDocument(documents, required).stream())
                .toList();
    }

    public List<UploadedDocument> blockedDocuments(List<UploadedDocument> documents) {
        return countingDocuments(documents).stream()
                .filter(Case::tooPoorToWorkWith)
                .toList();
    }

    private static Optional<UploadedDocument> countingDocument(List<UploadedDocument> documents, String required) {
        return documents.stream()
                .filter(document -> required.equals(document.analysis().matchedRequiredDocument()))
                .max(Comparator.comparing(UploadedDocument::uploadedAt));
    }

    private static boolean tooPoorToWorkWith(UploadedDocument document) {
        return document.analysis().quality().verdict() == QualityAssessment.Quality.POOR && !document.reviewed();
    }
}
