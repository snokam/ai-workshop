package com.example.aiworkshop.cases;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.document.DocumentAnalysis;
import com.example.aiworkshop.document.MatchConfidence;
import com.example.aiworkshop.document.QualityAssessment;
import com.example.aiworkshop.document.QualityAssessment.Quality;
import com.example.aiworkshop.document.UploadedDocument;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Case Status is derived, never stored — so this is a pure function over a Case's Required Documents
 * and the Documents attached to it. No Spring, no mocks, no model.
 */
class CaseStatusTest {

    private static final Case THE_CASE = new Case("c-1", "CASE-2026-001", List.of("proof of identity", "receipt"));

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

    /** Three verdicts exist so that an imperfect scan is not treated as a failure. Only POOR blocks. */
    @Test
    void anAcceptableDocumentDoesNotBlockTheCase() {
        List<UploadedDocument> documents =
                List.of(document("proof of identity", Quality.GOOD), document("receipt", Quality.ACCEPTABLE));

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.READY_FOR_DECISION);
    }

    /** A missing Required Document outranks a poor one: the Case is not the handler's problem yet. */
    @Test
    void awaitingDocumentsOutranksNeedsReview() {
        List<UploadedDocument> documents = List.of(document("receipt", Quality.POOR));

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.AWAITING_DOCUMENTS);
    }

    /** The handler could read the file even though the agent could not; their judgement wins. */
    @Test
    void aReviewedDocumentNoLongerBlocksTheCase() {
        List<UploadedDocument> documents = List.of(
                document("proof of identity", Quality.GOOD),
                document("receipt", Quality.POOR).markReviewed());

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.READY_FOR_DECISION);
    }

    /** The other way out of NEEDS_REVIEW: the Claimant sends a better copy and the Case unsticks itself. */
    @Test
    void aBetterReUploadClearsTheBlockWithoutAReview() {
        List<UploadedDocument> documents = List.of(
                document("proof of identity", Quality.GOOD),
                document("receipt", Quality.POOR, AT_NINE),
                document("receipt", Quality.GOOD, AT_TEN));

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.READY_FOR_DECISION);
    }

    /** Newest, not best: a Claimant replacing a good scan with a poor one has made the Case worse. */
    @Test
    void theMostRecentUploadForARequiredDocumentIsTheOneThatCounts() {
        List<UploadedDocument> documents = List.of(
                document("proof of identity", Quality.GOOD),
                document("receipt", Quality.GOOD, AT_NINE),
                document("receipt", Quality.POOR, AT_TEN));

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.NEEDS_REVIEW);
    }

    /**
     * An audience uploads whatever is on their desktop. Under any stricter rule every one of those
     * files would jam the Case it landed in, so a Document matching nothing is attached and ignored.
     */
    @Test
    void aDocumentMatchingNoRequiredDocumentIsIgnoredByTheStatus() {
        List<UploadedDocument> documents = List.of(
                document("proof of identity", Quality.GOOD),
                document("receipt", Quality.GOOD),
                document(null, Quality.POOR));

        assertThat(THE_CASE.status(documents)).isEqualTo(CaseStatus.READY_FOR_DECISION);
    }

    /**
     * The same rule the status is derived from, named so a Case Handler can be shown which of two
     * receipts the status came from. Superseded Documents are still attached — only inert.
     */
    @Test
    void onlyTheNewestMatchCountsForARequiredDocument() {
        UploadedDocument superseded = document("receipt", Quality.POOR, AT_NINE);
        UploadedDocument newest = document("receipt", Quality.GOOD, AT_TEN);
        List<UploadedDocument> documents =
                List.of(document("proof of identity", Quality.GOOD), superseded, newest);

        assertThat(THE_CASE.countingDocuments(documents)).contains(newest).doesNotContain(superseded);
    }

    /**
     * A Case stalling because a model hedged is worse than a visibly wrong match a handler can
     * correct in a second. Confidence is shown on the screen and kept out of the derivation.
     */
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
                new DocumentAnalysis(
                        "some document",
                        "What the document says.",
                        List.of(),
                        matchedRequiredDocument,
                        MatchConfidence.HIGH,
                        new QualityAssessment(verdict, "A sentence about the file.", List.of())),
                false);
    }

    /** The same Document, with the agent unsure it matched the right Required Document. */
    private static UploadedDocument hedged(UploadedDocument document) {
        DocumentAnalysis analysis = document.analysis();
        return new UploadedDocument(
                document.id(),
                document.caseId(),
                document.filename(),
                document.contentType(),
                document.sizeBytes(),
                document.uploadedAt(),
                new DocumentAnalysis(
                        analysis.category(),
                        analysis.summary(),
                        analysis.fields(),
                        analysis.matchedRequiredDocument(),
                        MatchConfidence.LOW,
                        analysis.quality()),
                document.reviewed());
    }
}
