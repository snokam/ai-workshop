package com.example.aiworkshop.cases;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.aiworkshop.document.DocumentAnalysis;
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

    private final CaseStore cases = new CaseStore();
    private final DocumentStore documents = new DocumentStore();
    private final CaseSummarizer summarizer = mock(CaseSummarizer.class);
    private final CaseStatusWriter statusWriter = mock(CaseStatusWriter.class);
    private final CaseDesk desk = new CaseDesk(cases, documents, summarizer, statusWriter);

    @BeforeEach
    void aCaseWithOneUnreadableDocument() {
        cases.save(new Case(CASE_ID, "CASE-2026-001", List.of("receipt")));
        documents.save(document("blurry.jpg", "receipt", Quality.POOR));
    }

    @Test
    void reviewingABlockedDocumentLetsTheCaseProceed() {
        assertThat(statusOfTheCase()).isEqualTo(CaseStatus.NEEDS_REVIEW);

        desk.review("blurry.jpg");

        assertThat(statusOfTheCase()).isEqualTo(CaseStatus.READY_FOR_DECISION);
    }

    /** The expensive agent: it needs the Documents themselves, which is why it is not the same agent. */
    @Test
    void theSummarizerIsHandedTheCasesDocuments() {
        desk.open(CASE_ID);

        assertThat(capturedDocuments()).containsExactlyElementsOf(documents.findByCaseId(CASE_ID));
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

    private List<UploadedDocument> capturedDocuments() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UploadedDocument>> captor = ArgumentCaptor.forClass(List.class);
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
        return new UploadedDocument(
                id,
                CASE_ID,
                id,
                "image/jpeg",
                1024,
                Instant.parse("2026-08-15T09:00:00Z"),
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
