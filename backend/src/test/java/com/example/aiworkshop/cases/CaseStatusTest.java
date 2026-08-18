package com.example.aiworkshop.cases;

import com.example.aiworkshop.document.model.UploadedDocument;
import com.example.aiworkshop.document.model.QualityAssessment;
import com.example.aiworkshop.document.model.MatchConfidence;
import com.example.aiworkshop.document.model.DocumentAnalysis;
import com.example.aiworkshop.cases.model.CaseType;
import com.example.aiworkshop.cases.model.CaseStatus;
import com.example.aiworkshop.cases.model.Case;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.document.model.QualityAssessment.Quality;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class CaseStatusTest {
    private static final Case THE_CASE =
            new Case("c-1", "CASE-2026-001", CaseType.HOME_CONTENTS, List.of("proof of identity", "receipt"));

    private static final Instant AT_NINE = Instant.parse("2026-08-15T09:00:00Z");
    private static final Instant AT_TEN = Instant.parse("2026-08-15T10:00:00Z");

    @Test
    void aCaseWithNothingUploadedIsAwaitingDocuments() {
        assertThat(THE_CASE.status(List.of())).isEqualTo(CaseStatus.AWAITING_DOCUMENTS);
    }

    @Test
    void aCaseIsReadyForDecisionOnceEveryRequiredDocumentHasArrived() {
        List<UploadedDocument> documents =
                List.of(document("proof of identity", Quality.GOOD), document("receipt", Quality.GOOD));

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.READY_FOR_DECISION);
    }

    @Test
    void oneRequiredDocumentStillMissingHoldsTheCaseAtAwaitingDocuments() {
        List<UploadedDocument> documents = List.of(document("proof of identity", Quality.GOOD));

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.AWAITING_DOCUMENTS);
    }

    @Test
    void aRequiredDocumentTooPoorToWorkWithHoldsTheCaseAtNeedsReview() {
        List<UploadedDocument> documents =
                List.of(document("proof of identity", Quality.GOOD), document("receipt", Quality.POOR));

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.NEEDS_REVIEW);
    }

    @Test
    void anAcceptableDocumentDoesNotBlockTheCase() {
        List<UploadedDocument> documents =
                List.of(document("proof of identity", Quality.GOOD), document("receipt", Quality.ACCEPTABLE));

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.READY_FOR_DECISION);
    }

    @Test
    void awaitingDocumentsOutranksNeedsReview() {
        List<UploadedDocument> documents = List.of(document("receipt", Quality.POOR));

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.AWAITING_DOCUMENTS);
    }

    @Test
    void aReviewedDocumentNoLongerBlocksTheCase() {
        List<UploadedDocument> documents = List.of(
                document("proof of identity", Quality.GOOD),
                document("receipt", Quality.POOR).markReviewed());

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.READY_FOR_DECISION);
    }

    @Test
    void aBetterReUploadClearsTheBlockWithoutAReview() {
        List<UploadedDocument> documents = List.of(
                document("proof of identity", Quality.GOOD),
                document("receipt", Quality.POOR, AT_NINE),
                document("receipt", Quality.GOOD, AT_TEN));

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.READY_FOR_DECISION);
    }

    @Test
    void theMostRecentUploadForARequiredDocumentIsTheOneThatCounts() {
        List<UploadedDocument> documents = List.of(
                document("proof of identity", Quality.GOOD),
                document("receipt", Quality.GOOD, AT_NINE),
                document("receipt", Quality.POOR, AT_TEN));

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.NEEDS_REVIEW);
    }

    @Test
    void aDocumentMatchingNoRequiredDocumentIsIgnoredByTheStatus() {
        List<UploadedDocument> documents = List.of(
                document("proof of identity", Quality.GOOD),
                document("receipt", Quality.GOOD),
                document(null, Quality.POOR));

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.READY_FOR_DECISION);
    }

    @Test
    void onlyTheNewestMatchCountsForARequiredDocument() {
        UploadedDocument superseded = document("receipt", Quality.POOR, AT_NINE);
        UploadedDocument newest = document("receipt", Quality.GOOD, AT_TEN);
        List<UploadedDocument> documents =
                List.of(document("proof of identity", Quality.GOOD), superseded, newest);

        assertThat(THE_CASE.countingDocuments(documents)).contains(newest).doesNotContain(superseded);
    }

    @Test
    void lowConfidenceInAMatchDoesNotBlockTheCase() {
        List<UploadedDocument> documents = List.of(
                hedged(document("proof of identity", Quality.GOOD)), hedged(document("receipt", Quality.GOOD)));

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.READY_FOR_DECISION);
    }

    private static UploadedDocument document(String matchedRequiredDocument, Quality verdict) {
        return document(matchedRequiredDocument, verdict, AT_NINE);
    }

    private static UploadedDocument document(String matchedRequiredDocument, Quality verdict, Instant uploadedAt) {
        return new UploadedDocument(
                matchedRequiredDocument + "@" + uploadedAt,
                THE_CASE.id(),
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
                document.caseId(),
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
