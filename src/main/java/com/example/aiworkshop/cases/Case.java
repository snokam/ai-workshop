package com.example.aiworkshop.cases;

import com.example.aiworkshop.document.QualityAssessment;
import com.example.aiworkshop.document.UploadedDocument;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The unit of work a Document belongs to, and the thing that has a status.
 *
 * <p>Cases do not come in kinds. The Required Documents live on the Case itself, so if Cases later
 * differ by kind, the list is what varies rather than a new noun appearing.
 *
 * <p>The Documents are not held here — they live in the document store, each carrying the
 * identifier of the Case it belongs to. {@link #status} is therefore handed the Case's Documents
 * rather than reaching for them, which is also what keeps it a pure function.
 *
 * @param id opaque identifier, used on the API and on every Document that belongs to this Case
 * @param reference the human-readable name a Case Handler recognises the Case by
 * @param requiredDocuments what this Case needs before it can be decided, as plain labels — the four
 *     Document Types are not named yet, so these are described rather than typed (see ADR 0001)
 */
public record Case(String id, String reference, List<String> requiredDocuments) {

    /** Derived on read from the Documents attached to this Case. */
    public CaseStatus status(List<UploadedDocument> documents) {
        if (!unmatchedRequiredDocuments(documents).isEmpty()) {
            return CaseStatus.AWAITING_DOCUMENTS;
        }
        return blockedDocuments(documents).isEmpty() ? CaseStatus.READY_FOR_DECISION : CaseStatus.NEEDS_REVIEW;
    }

    /** What the Case is still waiting for — the Required Documents nothing has matched yet. */
    public List<String> unmatchedRequiredDocuments(List<UploadedDocument> documents) {
        return requiredDocuments.stream()
                .filter(required -> countingDocument(documents, required).isEmpty())
                .toList();
    }

    /**
     * The Documents the status was derived from: one per Required Document that has a match.
     *
     * <p>Everything else attached is either superseded by a newer upload of the same Required
     * Document or matched nothing at all. Both stay attached — this only says which ones count.
     */
    public List<UploadedDocument> countingDocuments(List<UploadedDocument> documents) {
        return requiredDocuments.stream()
                .flatMap(required -> countingDocument(documents, required).stream())
                .toList();
    }

    /** The Documents holding this Case at {@code NEEDS_REVIEW}, and so the ones a Review would free. */
    public List<UploadedDocument> blockedDocuments(List<UploadedDocument> documents) {
        return countingDocuments(documents).stream()
                .filter(Case::tooPoorToWorkWith)
                .toList();
    }

    /**
     * The one Document that counts for a Required Document: the most recently uploaded match.
     *
     * <p>Every upload is kept, so a Required Document can have several. Taking the newest is what
     * lets a Claimant fix a bad scan by sending a better one — and, deliberately, lets them make the
     * Case worse by sending a worse one. Newest, not best: the Case Handler works from what the
     * Claimant last said was the document, not from whichever copy flatters the Case most.
     */
    private static Optional<UploadedDocument> countingDocument(List<UploadedDocument> documents, String required) {
        return documents.stream()
                .filter(document -> required.equals(document.analysis().matchedRequiredDocument()))
                .max(Comparator.comparing(UploadedDocument::uploadedAt));
    }

    private static boolean tooPoorToWorkWith(UploadedDocument document) {
        return document.analysis().quality().verdict() == QualityAssessment.Quality.POOR && !document.reviewed();
    }
}
