package com.example.aiworkshop.cases;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiworkshop.document.DocumentAnalysis;
import com.example.aiworkshop.document.DocumentFiles;
import com.example.aiworkshop.document.DocumentReader;
import com.example.aiworkshop.document.DocumentStore;
import com.example.aiworkshop.document.MatchConfidence;
import com.example.aiworkshop.document.QualityAssessment;
import com.example.aiworkshop.document.QualityAssessment.Quality;
import com.example.aiworkshop.document.UploadedDocument;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * The handler side, with both agents mocked out the way {@code DocumentAnalyzer} already is.
 *
 * <p>Deliberately thin. What each status means is settled at {@link CaseStatusTest}, and this seam
 * should not become a second home for it — only what the agents are handed, and that a Review
 * actually moves the Case, are asserted here.
 */
class CaseDeskTest {

    private static final String CASE_ID = "c-1";

    private static final Instant AT_NINE = Instant.parse("2026-08-15T09:00:00Z");
    private static final Instant AT_TEN = Instant.parse("2026-08-15T10:00:00Z");

    private final CaseStore cases = new CaseStore();
    private final DocumentStore documents = new DocumentStore();
    private final CaseSummaryStore summaries = new CaseSummaryStore();
    private final ProposalStore proposals = new ProposalStore();
    private final DocumentRequestStore requests = new DocumentRequestStore();
    private final CaseSummarizer summarizer = mock(CaseSummarizer.class);
    private final CaseStatusWriter statusWriter = mock(CaseStatusWriter.class);
    private final CaseDesk desk = new CaseDesk(
            cases,
            documents,
            summaries,
            proposals,
            requests,
            new CaseChatStore(),
            mock(DocumentFiles.class),
            summarizer,
            statusWriter,
            mock(CaseChatAgent.class),
            mock(DocumentReader.class));

    @BeforeEach
    void aCaseWithOneUnreadableDocument() {
        cases.save(new Case(CASE_ID, "CASE-2026-001", List.of("receipt")));
        documents.save(document("blurry.jpg", "receipt", Quality.POOR));
        when(summarizer.summarise(anyList())).thenReturn("What the documents say, taken together.");
    }

    @Test
    void reviewingABlockedDocumentLetsTheCaseProceed() {
        assertThat(statusOfTheCase()).isEqualTo(CaseStatus.NEEDS_REVIEW);

        desk.review("blurry.jpg");

        assertThat(statusOfTheCase()).isEqualTo(CaseStatus.READY_FOR_DECISION);
    }

    /**
     * Two receipts, and the handler is looking at both. Which one the status came from is derived
     * here rather than left for the screen to work out again.
     */
    @Test
    void theCaseDetailSaysWhichDocumentCounts() {
        documents.save(document("better.jpg", "receipt", Quality.GOOD, AT_TEN));

        CaseDetail detail = desk.open(CASE_ID);

        assertThat(detail.countingDocumentIds()).containsExactly("better.jpg");
        assertThat(detail.documents()).extracting(UploadedDocument::id).contains("blurry.jpg", "better.jpg");
    }

    /**
     * Three Documents the agent could not read, one Case held up. Offering a Review on the other two
     * would be offering a fix for a problem the Case does not have.
     */
    @Test
    void onlyTheDocumentsHoldingTheCaseUpAreMarkedAsBlocking() {
        documents.save(document("holiday-photo.png", null, Quality.POOR));
        documents.save(document("blurrier.jpg", "receipt", Quality.POOR, AT_TEN));

        CaseDetail detail = desk.open(CASE_ID);

        assertThat(detail.blockedDocumentIds()).containsExactly("blurrier.jpg");
        assertThat(detail.countingDocumentIds()).doesNotContain("holiday-photo.png");
    }

    /**
     * The expensive agent: it needs what the Documents say, which is why it is not the same agent.
     * One projection per attached Document, newest first — what each one renders as is pinned at
     * {@link DocumentForSummaryTest}, not here.
     */
    @Test
    void theSummarizerIsHandedTheCasesDocuments() {
        documents.save(document("better.jpg", "receipt", Quality.GOOD, AT_TEN));

        desk.open(CASE_ID);

        assertThat(capturedProjections())
                .extracting(DocumentForSummary::filename)
                .containsExactly("better.jpg", "blurry.jpg");
    }

    /**
     * The cheap agent: derived facts only. Its signature is what keeps Document contents out of a
     * call made every time a Case is opened — and what keeps it from inventing a status of its own.
     */
    @Test
    void theStatusWriterIsHandedTheDerivedFactsAndNothingElse() {
        desk.open(CASE_ID);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> blocked = ArgumentCaptor.forClass(List.class);
        verify(statusWriter).write(eq(CaseStatus.NEEDS_REVIEW), eq(List.of()), blocked.capture());
        assertThat(blocked.getValue()).singleElement(as(STRING)).contains("blurry.jpg");
    }

    /**
     * The point of splitting the two agents: the expensive one is not re-run for a handler reading
     * the same Case twice, because nothing it was written over has changed.
     */
    @Test
    void openingTheSameCaseTwiceWritesTheSummaryOnce() {
        desk.open(CASE_ID);
        desk.open(CASE_ID);

        verify(summarizer, times(1)).summarise(anyList());
    }

    /** And it does go stale: a Document arriving is the one thing the summary is written over. */
    @Test
    void aNewDocumentMakesTheNextOpenWriteTheSummaryAgain() {
        desk.open(CASE_ID);

        documents.save(document("better.jpg", "receipt", Quality.GOOD, AT_TEN));
        desk.open(CASE_ID);

        verify(summarizer, times(2)).summarise(anyList());
    }

    /** A Review changes a handler's judgement of a Document, not a word of what it says. */
    @Test
    void aReviewDoesNotMakeTheSummaryStale() {
        desk.open(CASE_ID);

        desk.review("blurry.jpg");
        desk.open(CASE_ID);

        verify(summarizer, times(1)).summarise(anyList());
    }

    /** The cheap agent is not cached: the derived facts it writes up change under it. */
    @Test
    void theStatusNoteIsWrittenOnEveryOpen() {
        desk.open(CASE_ID);
        desk.open(CASE_ID);

        verify(statusWriter, times(2)).write(any(), anyList(), anyList());
    }

    private List<DocumentForSummary> capturedProjections() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DocumentForSummary>> captor = ArgumentCaptor.forClass(List.class);
        verify(summarizer).summarise(captor.capture());
        return captor.getValue();
    }

    private CaseStatus statusOfTheCase() {
        return desk.list().stream()
                .filter(overview -> overview.id().equals(CASE_ID))
                .findFirst()
                .orElseThrow()
                .status();
    }

    private static UploadedDocument document(String id, String matchedRequiredDocument, Quality verdict) {
        return document(id, matchedRequiredDocument, verdict, AT_NINE);
    }

    private static UploadedDocument document(
            String id, String matchedRequiredDocument, Quality verdict, Instant uploadedAt) {
        return new UploadedDocument(
                id,
                CASE_ID,
                id,
                "image/jpeg",
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
}
