package com.example.aiworkshop.tasks.task_3_document_agent.progress;

import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_3_document_agent.model.QualityAssessment;
import com.example.aiworkshop.tasks.task_1_first_agent.model.MatchConfidence;
import com.example.aiworkshop.tasks.task_3_document_agent.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimType;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimStatus;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.tasks.task_3_document_agent.model.QualityAssessment.Quality;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClaimDocumentsTest {
    private static final Claim THE_CLAIM =
            new Claim("c-1", "CLAIM-2026-001", ClaimType.HOME_CONTENTS, List.of("proof of identity", "receipt"));

    private static final Instant AT_NINE = Instant.parse("2026-08-15T09:00:00Z");
    private static final Instant AT_TEN = Instant.parse("2026-08-15T10:00:00Z");

    @Test
    void aClaimWithNothingUploadedIsAwaitingDocuments() {
        assertThat(ClaimDocuments.statusOf(THE_CLAIM, List.of())).isEqualTo(ClaimStatus.AWAITING_DOCUMENTS);
    }

    @Test
    void aClaimIsReadyForDecisionOnceEveryRequiredDocumentHasArrived() {
        List<UploadedDocument> documents =
                List.of(document("proof of identity", Quality.GOOD), document("receipt", Quality.GOOD));

        assertThat(ClaimDocuments.statusOf(THE_CLAIM, documents)).isEqualTo(ClaimStatus.READY_FOR_DECISION);
    }

    @Test
    void oneRequiredDocumentStillMissingHoldsTheClaimAtAwaitingDocuments() {
        List<UploadedDocument> documents = List.of(document("proof of identity", Quality.GOOD));

        assertThat(ClaimDocuments.statusOf(THE_CLAIM, documents)).isEqualTo(ClaimStatus.AWAITING_DOCUMENTS);
    }

    @Test
    void aRequiredDocumentTooPoorToWorkWithHoldsTheClaimAtNeedsReview() {
        List<UploadedDocument> documents =
                List.of(document("proof of identity", Quality.GOOD), document("receipt", Quality.POOR));

        assertThat(ClaimDocuments.statusOf(THE_CLAIM, documents)).isEqualTo(ClaimStatus.NEEDS_REVIEW);
    }

    @Test
    void anAcceptableDocumentDoesNotBlockTheClaim() {
        List<UploadedDocument> documents =
                List.of(document("proof of identity", Quality.GOOD), document("receipt", Quality.ACCEPTABLE));

        assertThat(ClaimDocuments.statusOf(THE_CLAIM, documents)).isEqualTo(ClaimStatus.READY_FOR_DECISION);
    }

    @Test
    void awaitingDocumentsOutranksNeedsReview() {
        List<UploadedDocument> documents = List.of(document("receipt", Quality.POOR));

        assertThat(ClaimDocuments.statusOf(THE_CLAIM, documents)).isEqualTo(ClaimStatus.AWAITING_DOCUMENTS);
    }

    @Test
    void aReviewedDocumentNoLongerBlocksTheClaim() {
        List<UploadedDocument> documents = List.of(
                document("proof of identity", Quality.GOOD),
                document("receipt", Quality.POOR).markReviewed());

        assertThat(ClaimDocuments.statusOf(THE_CLAIM, documents)).isEqualTo(ClaimStatus.READY_FOR_DECISION);
    }

    @Test
    void aBetterReUploadClearsTheBlockWithoutAReview() {
        List<UploadedDocument> documents = List.of(
                document("proof of identity", Quality.GOOD),
                document("receipt", Quality.POOR, AT_NINE),
                document("receipt", Quality.GOOD, AT_TEN));

        assertThat(ClaimDocuments.statusOf(THE_CLAIM, documents)).isEqualTo(ClaimStatus.READY_FOR_DECISION);
    }

    @Test
    void theMostRecentUploadForARequiredDocumentIsTheOneThatCounts() {
        List<UploadedDocument> documents = List.of(
                document("proof of identity", Quality.GOOD),
                document("receipt", Quality.GOOD, AT_NINE),
                document("receipt", Quality.POOR, AT_TEN));

        assertThat(ClaimDocuments.statusOf(THE_CLAIM, documents)).isEqualTo(ClaimStatus.NEEDS_REVIEW);
    }

    @Test
    void aDocumentMatchingNoRequiredDocumentIsIgnoredByTheStatus() {
        List<UploadedDocument> documents = List.of(
                document("proof of identity", Quality.GOOD),
                document("receipt", Quality.GOOD),
                document(null, Quality.POOR));

        assertThat(ClaimDocuments.statusOf(THE_CLAIM, documents)).isEqualTo(ClaimStatus.READY_FOR_DECISION);
    }

    @Test
    void onlyTheNewestMatchCountsForARequiredDocument() {
        UploadedDocument superseded = document("receipt", Quality.POOR, AT_NINE);
        UploadedDocument newest = document("receipt", Quality.GOOD, AT_TEN);
        List<UploadedDocument> documents =
                List.of(document("proof of identity", Quality.GOOD), superseded, newest);

        assertThat(ClaimDocuments.countingDocuments(THE_CLAIM, documents)).contains(newest).doesNotContain(superseded);
    }

    @Test
    void lowConfidenceInAMatchDoesNotBlockTheClaim() {
        List<UploadedDocument> documents = List.of(
                hedged(document("proof of identity", Quality.GOOD)), hedged(document("receipt", Quality.GOOD)));

        assertThat(ClaimDocuments.statusOf(THE_CLAIM, documents)).isEqualTo(ClaimStatus.READY_FOR_DECISION);
    }

    private static UploadedDocument document(String matchedRequiredDocument, Quality verdict) {
        return document(matchedRequiredDocument, verdict, AT_NINE);
    }

    private static UploadedDocument document(String matchedRequiredDocument, Quality verdict, Instant uploadedAt) {
        return new UploadedDocument(
                matchedRequiredDocument + "@" + uploadedAt,
                THE_CLAIM.id(),
                "scan.pdf",
                "application/pdf",
                1024,
                uploadedAt,
                "hash-of-" + matchedRequiredDocument,
                new DocumentAnalysis(
                        "some document",
                        "What the document says.",
                        List.of(),
                        matchedRequiredDocument,
                        MatchConfidence.HIGH,
                        new QualityAssessment(verdict, "A sentence about the file.", List.of()),
                        null),
                false);
    }

    private static UploadedDocument hedged(UploadedDocument document) {
        DocumentAnalysis analysis = document.analysis();
        return new UploadedDocument(
                document.id(),
                document.claimId(),
                document.filename(),
                document.contentType(),
                document.sizeBytes(),
                document.uploadedAt(),
                document.contentHash(),
                new DocumentAnalysis(
                        analysis.category(),
                        analysis.summary(),
                        analysis.fields(),
                        analysis.matchedRequiredDocument(),
                        MatchConfidence.LOW,
                        analysis.quality(),
                        analysis.manipulationAttempt()),
                document.reviewed());
    }
}
