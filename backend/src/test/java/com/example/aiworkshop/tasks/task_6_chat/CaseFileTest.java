package com.example.aiworkshop.tasks.task_6_chat;

import com.example.aiworkshop.tasks.task_1_first_agent.CaseDesk;
import com.example.aiworkshop.tasks.task_2_document_agent.DocumentReview;
import com.example.aiworkshop.tasks.task_2_document_agent.DocumentProgress;
import com.example.aiworkshop.tasks.task_1_first_agent.CaseProgress;
import com.example.aiworkshop.tasks.task_5_summary.SummaryDesk;
import com.example.aiworkshop.tasks.task_5_summary.agent.CaseSummarizer;
import com.example.aiworkshop.tasks.task_5_summary.agent.CaseStatusWriter;
import com.example.aiworkshop.tasks.task_6_chat.agent.CaseChatAgent;
import com.example.aiworkshop.tasks.task_4_postprocessing.FraudScreener;
import com.example.aiworkshop.tasks.task_2_document_agent.store.DocumentStore;
import com.example.aiworkshop.tasks.task_2_document_agent.store.DocumentFiles;
import com.example.aiworkshop.tasks.task_2_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_2_document_agent.model.QualityAssessment;
import com.example.aiworkshop.tasks.task_1_first_agent.model.MatchConfidence;
import com.example.aiworkshop.tasks.task_2_document_agent.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_6_chat.agent.DocumentReader;
import com.example.aiworkshop.tasks.task_5_summary.store.CaseSummaryStore;
import com.example.aiworkshop.tasks.task_1_first_agent.store.CaseStore;
import com.example.aiworkshop.tasks.task_6_chat.store.ProposalStore;
import com.example.aiworkshop.tasks.task_6_chat.store.DocumentRequestStore;
import com.example.aiworkshop.tasks.task_5_summary.DocumentForSummary;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseType;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseStatus;
import com.example.aiworkshop.tasks.task_6_chat.model.CaseDetail;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Case;
import com.example.aiworkshop.tasks.task_6_chat.store.CaseChatStore;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiworkshop.tasks.task_2_document_agent.model.QualityAssessment.Quality;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CaseFileTest {
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
    private final FraudScreener screener = new FraudScreener(List.of());
    private final SummaryDesk summaryDesk = new SummaryDesk(summaries, summarizer, statusWriter);

    private final CaseProgress progress = new DocumentProgress(documents);
    private final CaseDesk desk = new CaseDesk(cases, progress);
    private final DocumentReview review = new DocumentReview(documents);

    private final CaseFile file = new CaseFile(desk, documents, requests, summaryDesk, screener);

    @BeforeEach
    void aCaseWithOneUnreadableDocument() {
        cases.save(new Case(CASE_ID, "CASE-2026-001", CaseType.HOME_CONTENTS, List.of("receipt")));
        documents.save(document("blurry.jpg", "receipt", Quality.POOR));
        when(summarizer.summarise(anyString(), anyList())).thenReturn("What the documents say, taken together.");
    }

    @Test
    void reviewingABlockedDocumentLetsTheCaseProceed() {
        assertThat(statusOfTheCase()).isEqualTo(CaseStatus.NEEDS_REVIEW);

        review.markReviewed("blurry.jpg");

        assertThat(statusOfTheCase()).isEqualTo(CaseStatus.READY_FOR_DECISION);
    }

    @Test
    void theCaseDetailSaysWhichDocumentCounts() {
        documents.save(document("better.jpg", "receipt", Quality.GOOD, AT_TEN));

        CaseDetail detail = file.open(CASE_ID, List.of(), List.of());

        assertThat(detail.countingDocumentIds()).containsExactly("better.jpg");
        assertThat(detail.documents()).extracting(UploadedDocument::id).contains("blurry.jpg", "better.jpg");
    }

    @Test
    void onlyTheDocumentsHoldingTheCaseUpAreMarkedAsBlocking() {
        documents.save(document("holiday-photo.png", null, Quality.POOR));
        documents.save(document("blurrier.jpg", "receipt", Quality.POOR, AT_TEN));

        CaseDetail detail = file.open(CASE_ID, List.of(), List.of());

        assertThat(detail.blockedDocumentIds()).containsExactly("blurrier.jpg");
        assertThat(detail.countingDocumentIds()).doesNotContain("holiday-photo.png");
    }

    @Test
    void theSummarizerIsHandedTheCasesDocuments() {
        documents.save(document("better.jpg", "receipt", Quality.GOOD, AT_TEN));

        file.open(CASE_ID, List.of(), List.of());

        assertThat(capturedProjections())
                .extracting(DocumentForSummary::filename)
                .containsExactly("better.jpg", "blurry.jpg");
    }

    @Test
    void theStatusWriterIsHandedTheDerivedFactsAndNothingElse() {
        file.open(CASE_ID, List.of(), List.of());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> blocked = ArgumentCaptor.forClass(List.class);
        verify(statusWriter).write(anyString(), eq(CaseStatus.NEEDS_REVIEW), eq(List.of()), blocked.capture());
        assertThat(blocked.getValue()).singleElement(as(STRING)).contains("blurry.jpg");
    }

    @Test
    void openingTheSameCaseTwiceWritesTheSummaryOnce() {
        file.open(CASE_ID, List.of(), List.of());
        file.open(CASE_ID, List.of(), List.of());

        verify(summarizer, times(1)).summarise(anyString(), anyList());
    }

    @Test
    void aNewDocumentMakesTheNextOpenWriteTheSummaryAgain() {
        file.open(CASE_ID, List.of(), List.of());

        documents.save(document("better.jpg", "receipt", Quality.GOOD, AT_TEN));
        file.open(CASE_ID, List.of(), List.of());

        verify(summarizer, times(2)).summarise(anyString(), anyList());
    }

    @Test
    void aReviewDoesNotMakeTheSummaryStale() {
        file.open(CASE_ID, List.of(), List.of());

        review.markReviewed("blurry.jpg");
        file.open(CASE_ID, List.of(), List.of());

        verify(summarizer, times(1)).summarise(anyString(), anyList());
    }

    @Test
    void theStatusNoteIsWrittenOnEveryOpen() {
        file.open(CASE_ID, List.of(), List.of());
        file.open(CASE_ID, List.of(), List.of());

        verify(statusWriter, times(2)).write(anyString(), any(), anyList(), anyList());
    }

    private List<DocumentForSummary> capturedProjections() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DocumentForSummary>> captor = ArgumentCaptor.forClass(List.class);
        verify(summarizer).summarise(anyString(), captor.capture());
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
                "hash-of-" + id,
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
}
