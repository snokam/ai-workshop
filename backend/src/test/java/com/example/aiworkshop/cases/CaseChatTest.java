package com.example.aiworkshop.cases;

import com.example.aiworkshop.tasks.task_6_summary.CaseSummarizer;
import com.example.aiworkshop.tasks.task_6_summary.CaseStatusWriter;
import com.example.aiworkshop.tasks.task_5_chat.CaseChatAgent;
import com.example.aiworkshop.tasks.task_4_postprocessing.FraudScreener;
import com.example.aiworkshop.documents.store.DocumentStore;
import com.example.aiworkshop.documents.store.DocumentFiles;
import com.example.aiworkshop.documents.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_2_document_agent.model.QualityAssessment;
import com.example.aiworkshop.documents.model.MatchConfidence;
import com.example.aiworkshop.tasks.task_2_document_agent.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_5_chat.DocumentReader;
import com.example.aiworkshop.tasks.task_6_summary.CaseSummaryStore;
import com.example.aiworkshop.cases.store.CaseStore;
import com.example.aiworkshop.cases.proposals.ProposalStore;
import com.example.aiworkshop.cases.proposals.ProposalState;
import com.example.aiworkshop.cases.proposals.ProposalCard;
import com.example.aiworkshop.cases.proposals.DocumentRequestStore;
import com.example.aiworkshop.cases.proposals.DocumentRequest;
import com.example.aiworkshop.cases.model.CaseType;
import com.example.aiworkshop.cases.model.CaseStatus;
import com.example.aiworkshop.cases.model.CaseOverview;
import com.example.aiworkshop.cases.model.Case;
import com.example.aiworkshop.cases.chat.ChatTurn;
import com.example.aiworkshop.cases.chat.ChatAnswer;
import com.example.aiworkshop.cases.chat.CaseChatStore;
import com.example.aiworkshop.tasks.task_5_chat.model.CaseAtAGlance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.aiworkshop.tasks.task_2_document_agent.model.QualityAssessment.Quality;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.Result;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

class CaseChatTest {
    private static final String CASE_ID = "c-1";

    private static final Instant AT_NINE = Instant.parse("2026-08-15T09:00:00Z");
    private static final Instant AT_TEN = Instant.parse("2026-08-15T10:00:00Z");

    private static final byte[] SCAN = {1, 2, 3};

    private final CaseStore cases = new CaseStore();
    private final DocumentStore documents = new DocumentStore();
    private final CaseSummaryStore summaries = new CaseSummaryStore();
    private final ProposalStore proposals = new ProposalStore();
    private final DocumentRequestStore requests = new DocumentRequestStore();
    private final CaseChatStore chats = new CaseChatStore();
    private final CaseSummarizer summarizer = mock(CaseSummarizer.class);
    private final CaseStatusWriter statusWriter = mock(CaseStatusWriter.class);
    private final CaseChatAgent chatAgent = mock(CaseChatAgent.class);
    private final DocumentReader reader = mock(DocumentReader.class);

    @TempDir
    Path directory;

    private DocumentFiles files;
    private CaseDesk desk;

    @BeforeEach
    void aCaseHeldUpByOneUnreadableDocument() throws IOException {
        cases.save(new Case(CASE_ID, "CASE-2026-001", CaseType.HOME_CONTENTS, List.of("receipt")));
        documents.save(document("d-1", "blurry.jpg", "receipt", Quality.POOR));
        when(summarizer.summarise(anyString(), anyList()))
                .thenReturn("What the documents say, taken together.");
        files = new DocumentFiles(directory);
        desk = new CaseDesk(
                cases,
                documents,
                summaries,
                proposals,
                requests,
                chats,
                files,
                summarizer,
                statusWriter,
                new FraudScreener(List.of()),
                chatAgent,
                reader);
    }

    @Test
    void aProposedReviewIsRecordedAndMovesNothing() {
        ProposalCard proposed = desk.proposeReview(CASE_ID, "blurry.jpg", "The total is legible despite the shadow.");

        assertThat(desk.open(CASE_ID).proposals()).containsExactly(proposed);
        assertThat(proposed.state()).isEqualTo(ProposalState.PROPOSED);
        assertThat(statusOfTheCase()).isEqualTo(CaseStatus.NEEDS_REVIEW);
    }

    @Test
    void confirmingAProposedReviewMovesTheCase() {
        ProposalCard proposed = desk.proposeReview(CASE_ID, "blurry.jpg", "The total is legible despite the shadow.");

        desk.confirm(proposed.id());

        assertThat(statusOfTheCase()).isEqualTo(CaseStatus.READY_FOR_DECISION);
        assertThat(desk.open(CASE_ID).proposals())
                .extracting(ProposalCard::state)
                .containsExactly(ProposalState.CONFIRMED);
    }

    @Test
    void decliningAProposedReviewLeavesTheCaseWhereItWas() {
        ProposalCard proposed = desk.proposeReview(CASE_ID, "blurry.jpg", "The total is legible despite the shadow.");

        desk.decline(proposed.id());

        assertThat(statusOfTheCase()).isEqualTo(CaseStatus.NEEDS_REVIEW);
        assertThat(desk.open(CASE_ID).proposals())
                .extracting(ProposalCard::state)
                .containsExactly(ProposalState.DECLINED);
    }

    @Test
    void aConfirmedDocumentRequestReachesTheClaimant() {
        ProposalCard proposed = desk.proposeDocumentRequest(
                CASE_ID, "the second page of the receipt", "The total is on a page that did not arrive.");

        desk.confirm(proposed.id());

        assertThat(overviewOfTheCase().documentRequests())
                .extracting(DocumentRequest::label)
                .containsExactly("the second page of the receipt");
    }

    @Test
    void anUnconfirmedDocumentRequestReachesNobody() {
        desk.proposeDocumentRequest(CASE_ID, "the second page of the receipt", "The total did not arrive.");

        assertThat(overviewOfTheCase().documentRequests()).isEmpty();
    }

    @Test
    void aConfirmedDocumentRequestDoesNotChangeWhatTheCaseRequires() {
        ProposalCard proposed =
                desk.proposeDocumentRequest(CASE_ID, "the second page of the receipt", "The total did not arrive.");

        desk.confirm(proposed.id());

        assertThat(overviewOfTheCase().requiredDocuments()).containsExactly("receipt");
        assertThat(overviewOfTheCase().outstanding()).isEmpty();
        assertThat(statusOfTheCase()).isEqualTo(CaseStatus.NEEDS_REVIEW);
    }

    @Test
    void aProposalAlreadyAnsweredIsNotAnsweredAgain() {
        ProposalCard proposed =
                desk.proposeDocumentRequest(CASE_ID, "the second page of the receipt", "The total did not arrive.");

        desk.confirm(proposed.id());
        desk.confirm(proposed.id());

        assertThat(overviewOfTheCase().documentRequests()).hasSize(1);
    }

    @Test
    void aDeclinedProposalCannotBeConfirmedAfterTheFact() {
        ProposalCard proposed = desk.proposeReview(CASE_ID, "blurry.jpg", "The total is legible despite the shadow.");
        desk.decline(proposed.id());

        desk.confirm(proposed.id());

        assertThat(statusOfTheCase()).isEqualTo(CaseStatus.NEEDS_REVIEW);
        assertThat(desk.open(CASE_ID).proposals())
                .extracting(ProposalCard::state)
                .containsExactly(ProposalState.DECLINED);
    }

    @Test
    void theChatIsBoundToTheCaseItWasAskedIn() {
        theAgentReplies("This case is waiting on a review of blurry.jpg.");

        ChatAnswer answer = desk.chat(CASE_ID, "What is this waiting on?");

        assertThat(answer.turn().answer()).isEqualTo("This case is waiting on a review of blurry.jpg.");
        verify(chatAgent).answer(eq(CASE_ID), eq("What is this waiting on?"), any());
    }

    @Test
    void theChatReusesTheCaseSummaryTheHandlerHasAlreadyPaidFor() {
        theAgentReplies("Waiting on a review.");
        desk.open(CASE_ID);

        desk.chat(CASE_ID, "What is this waiting on?");

        verify(summarizer, times(1)).summarise(anyString(), anyList());
    }

    @Test
    void whatTheHandlerHasAlreadyAnsweredReachesTheAgent() {
        theAgentReplies("Nothing further.");
        ProposalCard declined = desk.proposeReview(CASE_ID, "blurry.jpg", "The total is legible despite the shadow.");
        desk.decline(declined.id());
        desk.proposeDocumentRequest(CASE_ID, "the second page of the receipt", "The total did not arrive.");

        desk.chat(CASE_ID, "Should I review the receipt?");

        assertThat(capturedGlance().proposals())
                .extracting(ProposalCard::subject, ProposalCard::state)
                .containsExactly(
                        tuple("blurry.jpg", ProposalState.DECLINED),
                        tuple("the second page of the receipt", ProposalState.PROPOSED));
    }

    @Test
    void theConversationIsStillThereWhenTheCaseIsReopened() {
        theAgentReplies("It is waiting on a review of blurry.jpg.");

        desk.chat(CASE_ID, "What is this waiting on?");

        assertThat(desk.open(CASE_ID).conversation())
                .extracting(ChatTurn::question, ChatTurn::answer)
                .containsExactly(tuple("What is this waiting on?", "It is waiting on a review of blurry.jpg."));
    }

    @Test
    void aTurnRemembersTheSuggestionsItRaised() {
        when(chatAgent.answer(any(), any(), any())).thenAnswer(invocation -> {
            desk.proposeReview(CASE_ID, "blurry.jpg", "The total is legible despite the shadow.");
            return Result.<String>builder()
                    .content("I would review it.")
                    .toolExecutions(List.of())
                    .build();
        });

        ChatAnswer answer = desk.chat(CASE_ID, "Can I work with the receipt?");

        assertThat(answer.proposals()).hasSize(1);
        assertThat(answer.turn().proposalIds())
                .containsExactly(answer.proposals().getFirst().id());
    }

    @Test
    void answeringWithoutTheReadToolNeverOpensAFile() {
        theAgentReplies("It is waiting on a review of blurry.jpg.");

        desk.chat(CASE_ID, "What is this waiting on?");

        verifyNoInteractions(reader);
    }

    @Test
    void readingADocumentHandsTheOriginalFileToTheReaderAgent() {
        files.save("d-1", SCAN);
        when(reader.read(anyList(), any())).thenReturn("The total reads 4 200 kr.");

        String read = desk.readDocument(CASE_ID, "blurry.jpg", "What is the total at the bottom?");

        assertThat(read).isEqualTo("The total reads 4 200 kr.");
        assertThat(capturedFile()).hasAtLeastOneElementOfType(ImageContent.class);
    }

    @Test
    void aFilenameUsedTwiceResolvesToTheNewerDocument() {
        documents.save(document("d-2", "blurry.jpg", "receipt", Quality.GOOD, AT_TEN));

        assertThat(desk.documentDetail(CASE_ID, "blurry.jpg")).contains("Quality: GOOD");
    }

    @Test
    void aFilenameFromAnotherCaseIsNotFound() {
        assertThatThrownBy(() -> desk.documentDetail(CASE_ID, "someone-elses.pdf"))
                .isInstanceOf(CaseDesk.UnknownDocumentException.class)
                .hasMessageContaining("someone-elses.pdf");
    }

    private List<Content> capturedFile() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Content>> captor = ArgumentCaptor.forClass(List.class);
        verify(reader).read(captor.capture(), any());
        return captor.getValue();
    }

    private CaseAtAGlance capturedGlance() {
        ArgumentCaptor<CaseAtAGlance> captor = ArgumentCaptor.forClass(CaseAtAGlance.class);
        verify(chatAgent).answer(any(), any(), captor.capture());
        return captor.getValue();
    }

    private void theAgentReplies(String answer) {
        when(chatAgent.answer(any(), any(), any()))
                .thenReturn(Result.<String>builder()
                        .content(answer)
                        .toolExecutions(List.of())
                        .build());
    }

    private CaseOverview overviewOfTheCase() {
        return desk.list().stream()
                .filter(overview -> overview.id().equals(CASE_ID))
                .findFirst()
                .orElseThrow();
    }

    private CaseStatus statusOfTheCase() {
        return desk.list().stream()
                .filter(overview -> overview.id().equals(CASE_ID))
                .findFirst()
                .orElseThrow()
                .status();
    }

    private static UploadedDocument document(
            String id, String filename, String matchedRequiredDocument, Quality verdict) {
        return document(id, filename, matchedRequiredDocument, verdict, AT_NINE);
    }

    private static UploadedDocument document(
            String id, String filename, String matchedRequiredDocument, Quality verdict, Instant uploadedAt) {
        return new UploadedDocument(
                id,
                CASE_ID,
                filename,
                "image/jpeg",
                1024,
                uploadedAt,
                "hash-of-" + filename,
                new DocumentAnalysis(
                        "receipt",
                        "What the document says.",
                        List.of(),
                        matchedRequiredDocument,
                        MatchConfidence.HIGH,
                        new QualityAssessment(verdict, "Shadows across the lower half.", List.of("total cut off")),
                        null),
                false);
    }
}
