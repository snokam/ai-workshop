package com.example.aiworkshop.tasks.task_3_document_agent;

import com.example.aiworkshop.tasks.task_1_first_agent.model.Case;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseStatus;
import com.example.aiworkshop.tasks.task_3_document_agent.model.QualityAssessment;
import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * What the uploads add up to, measured against what the case asked for.
 *
 * <p>Every method here needs an answer from the analyser you write in task 2, which is why none of
 * them lives on Case. "Is this the police report?" is not a fact about the case; it is a claim the
 * agent made about a file, recorded in {@code analysis().matchedRequiredDocument()}.
 *
 * <p>The newest upload wins for each required document. Someone who uploads a better photo of the
 * same receipt has replaced it, not added to it.
 */
public final class CaseDocuments {
    private CaseDocuments() {}

    public static CaseStatus statusOf(Case theCase, List<UploadedDocument> documents) {
        if (!unmatchedRequiredDocuments(theCase, documents).isEmpty()) {
            return CaseStatus.AWAITING_DOCUMENTS;
        }
        return blockedDocuments(theCase, documents).isEmpty()
                ? CaseStatus.READY_FOR_DECISION
                : CaseStatus.NEEDS_REVIEW;
    }

    public static List<String> unmatchedRequiredDocuments(Case theCase, List<UploadedDocument> documents) {
        return theCase.requiredDocuments().stream()
                .filter(required -> countingDocument(documents, required).isEmpty())
                .toList();
    }

    public static List<UploadedDocument> countingDocuments(Case theCase, List<UploadedDocument> documents) {
        return theCase.requiredDocuments().stream()
                .flatMap(required -> countingDocument(documents, required).stream())
                .toList();
    }

    public static List<UploadedDocument> blockedDocuments(Case theCase, List<UploadedDocument> documents) {
        return countingDocuments(theCase, documents).stream()
                .filter(CaseDocuments::tooPoorToWorkWith)
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
