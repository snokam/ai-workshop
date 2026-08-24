package com.example.aiworkshop.tasks.task_6_advisor_chat;

import com.example.aiworkshop.tasks.task_1_first_agent.ClaimDesk;
import com.example.aiworkshop.tasks.task_3_document_agent.progress.DocumentReview;
import com.example.aiworkshop.tasks.task_3_document_agent.progress.DocumentProgress;
import com.example.aiworkshop.tasks.task_1_first_agent.ClaimProgress;
import com.example.aiworkshop.tasks.task_5_claim_summary.SummaryDesk;
import com.example.aiworkshop.tasks.task_5_claim_summary.agent.ClaimSummarizer;
import com.example.aiworkshop.tasks.task_5_claim_summary.agent.ClaimStatusWriter;
import com.example.aiworkshop.tasks.task_6_advisor_chat.agent.ClaimChatAgent;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentStore;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentFiles;
import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_3_document_agent.model.QualityAssessment;
import com.example.aiworkshop.tasks.task_1_first_agent.model.MatchConfidence;
import com.example.aiworkshop.tasks.task_3_document_agent.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_6_advisor_chat.agent.DocumentReader;
import com.example.aiworkshop.tasks.task_5_claim_summary.store.ClaimSummaryStore;
import com.example.aiworkshop.tasks.task_1_first_agent.store.ClaimStore;
import com.example.aiworkshop.tasks.task_6_advisor_chat.store.ProposalStore;
import com.example.aiworkshop.tasks.task_6_advisor_chat.store.DocumentRequestStore;
import com.example.aiworkshop.tasks.task_5_claim_summary.DocumentForSummary;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimType;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimStatus;
import com.example.aiworkshop.tasks.task_6_advisor_chat.model.ClaimDetail;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_6_advisor_chat.store.ClaimChatStore;
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

import com.example.aiworkshop.tasks.task_3_document_agent.model.QualityAssessment.Quality;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ClaimFileTest {
    private static final String CASE_ID = "c-1";

    private static final Instant AT_NINE = Instant.parse("2026-08-15T09:00:00Z");
    private static final Instant AT_TEN = Instant.parse("2026-08-15T10:00:00Z");

    private final ClaimStore claims = new ClaimStore();
    private final DocumentStore documents = new DocumentStore();
    private final ClaimSummaryStore summaries = new ClaimSummaryStore();
    private final ProposalStore proposals = new ProposalStore();
    private final DocumentRequestStore requests = new DocumentRequestStore();
    private final ClaimSummarizer summarizer = mock(ClaimSummarizer.class);
    private final ClaimStatusWriter statusWriter = mock(ClaimStatusWriter.class);
    private final SummaryDesk summaryDesk = new SummaryDesk(summaries, summarizer, statusWriter);

    private final ClaimProgress progress = new DocumentProgress(documents);
    private final ClaimDesk desk = new ClaimDesk(claims, progress);
    private final DocumentReview review = new DocumentReview(documents);

    private final ClaimFile file = new ClaimFile(desk, documents, requests, summaryDesk);

    @BeforeEach
    void aCaseWithOneUnreadableDocument() {
        claims.save(new Claim(CASE_ID, "CASE-2026-001", ClaimType.HOME_CONTENTS, List.of("receipt")));
        documents.save(document("blurry.jpg", "receipt", Quality.POOR));
        when(summarizer.summarise(anyString(), anyList())).thenReturn("What the documents say, taken together.");
    }

    @Test
    void reviewingABlockedDocumentLetsTheCaseProceed() {
        assertThat(statusOfTheCase()).isEqualTo(ClaimStatus.NEEDS_REVIEW);

        review.markReviewed("blurry.jpg");

        assertThat(statusOfTheCase()).isEqualTo(ClaimStatus.READY_FOR_DECISION);
    }

    @Test
    void theCaseDetailSaysWhichDocumentCounts() {
        documents.save(document("better.jpg", "receipt", Quality.GOOD, AT_TEN));

        ClaimDetail detail = file.open(CASE_ID, List.of(), List.of());

        assertThat(detail.countingDocumentIds()).containsExactly("better.jpg");
        assertThat(detail.documents()).extracting(UploadedDocument::id).contains("blurry.jpg", "better.jpg");
    }

    @Test
    void onlyTheDocumentsHoldingTheCaseUpAreMarkedAsBlocking() {
        documents.save(document("holiday-photo.png", null, Quality.POOR));
        documents.save(document("blurrier.jpg", "receipt", Quality.POOR, AT_TEN));

        ClaimDetail detail = file.open(CASE_ID, List.of(), List.of());

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
        verify(statusWriter)
                .write(anyString(), eq(ClaimStatus.NEEDS_REVIEW), eq(List.of()), blocked.capture());
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

    private ClaimStatus statusOfTheCase() {
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
