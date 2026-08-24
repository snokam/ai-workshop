package com.example.aiworkshop.tasks.task_3_document_agent.progress;

import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimStatus;
import com.example.aiworkshop.tasks.task_3_document_agent.model.QualityAssessment;
import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * What the uploads add up to, measured against what the claim asked for.
 *
 * <p>Every method here needs an answer from the analyser you write in task 3, which is why none of
 * them lives on Claim. "Is this the police report?" is not a fact about the claim; it is a claim the
 * agent made about a file, recorded in {@code analysis().matchedRequiredDocument()}.
 *
 * <p>The newest upload wins for each required document. Someone who uploads a better photo of the
 * same receipt has replaced it, not added to it.
 */
public final class ClaimDocuments {
    private ClaimDocuments() {}

    public static ClaimStatus statusOf(Claim theClaim, List<UploadedDocument> documents) {
        if (!unmatchedRequiredDocuments(theClaim, documents).isEmpty()) {
            return ClaimStatus.AWAITING_DOCUMENTS;
        }
        return blockedDocuments(theClaim, documents).isEmpty()
                ? ClaimStatus.READY_FOR_DECISION
                : ClaimStatus.NEEDS_REVIEW;
    }

    public static List<String> unmatchedRequiredDocuments(Claim theClaim, List<UploadedDocument> documents) {
        return theClaim.requiredDocuments().stream()
                .filter(required -> countingDocument(documents, required).isEmpty())
                .toList();
    }

    public static List<UploadedDocument> countingDocuments(Claim theClaim, List<UploadedDocument> documents) {
        return theClaim.requiredDocuments().stream()
                .flatMap(required -> countingDocument(documents, required).stream())
                .toList();
    }

    public static List<UploadedDocument> blockedDocuments(Claim theClaim, List<UploadedDocument> documents) {
        return countingDocuments(theClaim, documents).stream()
                .filter(ClaimDocuments::tooPoorToWorkWith)
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
