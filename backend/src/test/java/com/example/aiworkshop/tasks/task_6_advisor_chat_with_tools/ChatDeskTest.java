package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools;

import com.example.aiworkshop.tasks.task_5_claim_summary_using_memory.SummaryDesk;
import com.example.aiworkshop.tasks.task_1_first_agent.ClaimDesk;
import com.example.aiworkshop.tasks.task_3_document_agent.progress.DocumentReview;
import com.example.aiworkshop.tasks.task_3_document_agent.progress.DocumentProgress;
import com.example.aiworkshop.tasks.task_5_claim_summary_using_memory.agent.ClaimSummarizer;
import com.example.aiworkshop.tasks.task_5_claim_summary_using_memory.agent.ClaimStatusWriter;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.agent.ClaimChatAgent;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentStore;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentFiles;
import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_3_document_agent.model.QualityAssessment;
import com.example.aiworkshop.tasks.task_1_first_agent.model.MatchConfidence;
import com.example.aiworkshop.tasks.task_3_document_agent.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.agent.DocumentReader;
import com.example.aiworkshop.tasks.task_5_claim_summary_using_memory.store.ClaimSummaryStore;
import com.example.aiworkshop.tasks.task_1_first_agent.store.ClaimStore;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.store.ProposalStore;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.proposals.ProposalState;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.proposals.ProposalCard;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.store.DocumentRequestStore;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.proposals.DocumentRequest;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimType;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimStatus;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimOverview;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.model.ChatTurn;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.model.ChatAnswer;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.store.ClaimChatStore;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.model.ClaimAtAGlance;
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

import com.example.aiworkshop.tasks.task_3_document_agent.model.QualityAssessment.Quality;
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

class ChatDeskTest {
    private static final String CLAIM_ID = "c-1";

    private static final Instant AT_NINE = Instant.parse("2026-08-15T09:00:00Z");
    private static final Instant AT_TEN = Instant.parse("2026-08-15T10:00:00Z");

    private static final byte[] SCAN = {1, 2, 3};

    private final ClaimStore claims = new ClaimStore();
    private final DocumentStore documents = new DocumentStore();
    private final ClaimSummaryStore summaries = new ClaimSummaryStore();
    private final ProposalStore proposals = new ProposalStore();
    private final DocumentRequestStore requests = new DocumentRequestStore();
    private final ClaimChatStore chats = new ClaimChatStore();
    private final ClaimSummarizer summarizer = mock(ClaimSummarizer.class);
    private final ClaimStatusWriter statusWriter = mock(ClaimStatusWriter.class);
    private final ClaimChatAgent chatAgent = mock(ClaimChatAgent.class);
    private final DocumentReader reader = mock(DocumentReader.class);

    @TempDir
    Path directory;

    private DocumentFiles files;
    private ChatDesk desk;
    private ClaimDesk caseDesk;
    private ClaimFile caseFile;

    @BeforeEach
    void aClaimHeldUpByOneUnreadableDocument() throws IOException {
        claims.save(new Claim(CLAIM_ID, "CLAIM-2026-001", ClaimType.HOME_CONTENTS, List.of("receipt")));
        documents.save(document("d-1", "blurry.jpg", "receipt", Quality.POOR));
        when(summarizer.summarise(anyString(), anyString(), anyList()))
                .thenReturn("What the documents say, taken together.");
        files = new DocumentFiles(directory);
        SummaryDesk summaryDesk = new SummaryDesk(summaries, summarizer, statusWriter);
        caseDesk = new ClaimDesk(claims, new DocumentProgress(documents));
        caseFile = new ClaimFile(caseDesk, documents, requests, summaryDesk);
        desk = new ChatDesk(
                claims,
                documents,
                files,
                proposals,
                requests,
                chats,
                chatAgent,
                reader,
                summaryDesk,
                caseDesk,
                new DocumentReview(documents));
    }

    @Test
    void aProposedReviewIsRecordedAndMovesNothing() {
        ProposalCard proposed = desk.proposeReview(CLAIM_ID, "blurry.jpg", "The total is legible despite the shadow.");

        assertThat(desk.proposalsOn(CLAIM_ID)).containsExactly(proposed);
        assertThat(proposed.state()).isEqualTo(ProposalState.PROPOSED);
        assertThat(statusOfTheClaim()).isEqualTo(ClaimStatus.NEEDS_REVIEW);
    }

    @Test
    void confirmingAProposedReviewMovesTheClaim() {
        ProposalCard proposed = desk.proposeReview(CLAIM_ID, "blurry.jpg", "The total is legible despite the shadow.");

        desk.confirm(proposed.id());

        assertThat(statusOfTheClaim()).isEqualTo(ClaimStatus.READY_FOR_DECISION);
        assertThat(desk.proposalsOn(CLAIM_ID))
                .extracting(ProposalCard::state)
                .containsExactly(ProposalState.CONFIRMED);
    }

    @Test
    void decliningAProposedReviewLeavesTheClaimWhereItWas() {
        ProposalCard proposed = desk.proposeReview(CLAIM_ID, "blurry.jpg", "The total is legible despite the shadow.");

        desk.decline(proposed.id());

        assertThat(statusOfTheClaim()).isEqualTo(ClaimStatus.NEEDS_REVIEW);
        assertThat(desk.proposalsOn(CLAIM_ID))
                .extracting(ProposalCard::state)
                .containsExactly(ProposalState.DECLINED);
    }

    @Test
    void aConfirmedDocumentRequestReachesTheClaimant() {
        ProposalCard proposed = desk.proposeDocumentRequest(
                CLAIM_ID, "the second page of the receipt", "The total is on a page that did not arrive.");

        desk.confirm(proposed.id());

        assertThat(caseFile.open(CLAIM_ID, List.of(), List.of()).documentRequests())
                .extracting(DocumentRequest::label)
                .containsExactly("the second page of the receipt");
    }

    @Test
    void anUnconfirmedDocumentRequestReachesNobody() {
        desk.proposeDocumentRequest(CLAIM_ID, "the second page of the receipt", "The total did not arrive.");

        assertThat(caseFile.open(CLAIM_ID, List.of(), List.of()).documentRequests()).isEmpty();
    }

    @Test
    void aConfirmedDocumentRequestDoesNotChangeWhatTheClaimRequires() {
        ProposalCard proposed =
                desk.proposeDocumentRequest(CLAIM_ID, "the second page of the receipt", "The total did not arrive.");

        desk.confirm(proposed.id());

        assertThat(overviewOfTheClaim().requiredDocuments()).containsExactly("receipt");
        assertThat(overviewOfTheClaim().outstanding()).isEmpty();
        assertThat(statusOfTheClaim()).isEqualTo(ClaimStatus.NEEDS_REVIEW);
    }

    @Test
    void aProposalAlreadyAnsweredIsNotAnsweredAgain() {
        ProposalCard proposed =
                desk.proposeDocumentRequest(CLAIM_ID, "the second page of the receipt", "The total did not arrive.");

        desk.confirm(proposed.id());
        desk.confirm(proposed.id());

        assertThat(caseFile.open(CLAIM_ID, List.of(), List.of()).documentRequests()).hasSize(1);
    }

    @Test
    void aDeclinedProposalCannotBeConfirmedAfterTheFact() {
        ProposalCard proposed = desk.proposeReview(CLAIM_ID, "blurry.jpg", "The total is legible despite the shadow.");
        desk.decline(proposed.id());

        desk.confirm(proposed.id());

        assertThat(statusOfTheClaim()).isEqualTo(ClaimStatus.NEEDS_REVIEW);
        assertThat(desk.proposalsOn(CLAIM_ID))
                .extracting(ProposalCard::state)
                .containsExactly(ProposalState.DECLINED);
    }

    @Test
    void theChatIsBoundToTheClaimItWasAskedIn() {
        theAgentReplies("This claim is waiting on a review of blurry.jpg.");

        ChatAnswer answer = desk.chat(CLAIM_ID, "What is this waiting on?");

        assertThat(answer.turn().answer()).isEqualTo("This claim is waiting on a review of blurry.jpg.");
        verify(chatAgent).answer(eq(CLAIM_ID), eq("What is this waiting on?"), any());
    }

    @Test
    void theChatReusesTheClaimSummaryTheHandlerHasAlreadyPaidFor() {
        theAgentReplies("Waiting on a review.");
        caseFile.open(CLAIM_ID, List.of(), List.of());

        desk.chat(CLAIM_ID, "What is this waiting on?");

        verify(summarizer, times(1)).summarise(anyString(), anyString(), anyList());
    }

    @Test
    void whatTheHandlerHasAlreadyAnsweredReachesTheAgent() {
        theAgentReplies("Nothing further.");
        ProposalCard declined = desk.proposeReview(CLAIM_ID, "blurry.jpg", "The total is legible despite the shadow.");
        desk.decline(declined.id());
        desk.proposeDocumentRequest(CLAIM_ID, "the second page of the receipt", "The total did not arrive.");

        desk.chat(CLAIM_ID, "Should I review the receipt?");

        assertThat(capturedGlance().proposals())
                .extracting(ProposalCard::subject, ProposalCard::state)
                .containsExactly(
                        tuple("blurry.jpg", ProposalState.DECLINED),
                        tuple("the second page of the receipt", ProposalState.PROPOSED));
    }

    @Test
    void theConversationIsStillThereWhenTheClaimIsReopened() {
        theAgentReplies("It is waiting on a review of blurry.jpg.");

        desk.chat(CLAIM_ID, "What is this waiting on?");

        assertThat(desk.turnsOn(CLAIM_ID))
                .extracting(ChatTurn::question, ChatTurn::answer)
                .containsExactly(tuple("What is this waiting on?", "It is waiting on a review of blurry.jpg."));
    }

    @Test
    void aTurnRemembersTheSuggestionsItRaised() {
        when(chatAgent.answer(any(), any(), any())).thenAnswer(invocation -> {
            desk.proposeReview(CLAIM_ID, "blurry.jpg", "The total is legible despite the shadow.");
            return Result.<String>builder()
                    .content("I would review it.")
                    .toolExecutions(List.of())
                    .build();
        });

        ChatAnswer answer = desk.chat(CLAIM_ID, "Can I work with the receipt?");

        assertThat(answer.proposals()).hasSize(1);
        assertThat(answer.turn().proposalIds())
                .containsExactly(answer.proposals().getFirst().id());
    }

    @Test
    void answeringWithoutTheReadToolNeverOpensAFile() {
        theAgentReplies("It is waiting on a review of blurry.jpg.");

        desk.chat(CLAIM_ID, "What is this waiting on?");

        verifyNoInteractions(reader);
    }

    @Test
    void readingADocumentHandsTheOriginalFileToTheReaderAgent() {
        files.save("d-1", SCAN);
        when(reader.read(anyList(), any())).thenReturn("The total reads 4 200 kr.");

        String read = desk.readDocument(CLAIM_ID, "blurry.jpg", "What is the total at the bottom?");

        assertThat(read).isEqualTo("The total reads 4 200 kr.");
        assertThat(capturedFile()).hasAtLeastOneElementOfType(ImageContent.class);
    }

    @Test
    void aFilenameUsedTwiceResolvesToTheNewerDocument() {
        documents.save(document("d-2", "blurry.jpg", "receipt", Quality.GOOD, AT_TEN));

        assertThat(desk.documentDetail(CLAIM_ID, "blurry.jpg")).contains("Quality: GOOD");
    }

    @Test
    void aFilenameFromAnotherClaimIsNotFound() {
        assertThatThrownBy(() -> desk.documentDetail(CLAIM_ID, "someone-elses.pdf"))
                .isInstanceOf(ClaimDesk.UnknownDocumentException.class)
                .hasMessageContaining("someone-elses.pdf");
    }

    private List<Content> capturedFile() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Content>> captor = ArgumentCaptor.forClass(List.class);
        verify(reader).read(captor.capture(), any());
        return captor.getValue();
    }

    private ClaimAtAGlance capturedGlance() {
        ArgumentCaptor<ClaimAtAGlance> captor = ArgumentCaptor.forClass(ClaimAtAGlance.class);
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

    private ClaimOverview overviewOfTheClaim() {
        return caseDesk.list().stream()
                .filter(overview -> overview.id().equals(CLAIM_ID))
                .findFirst()
                .orElseThrow();
    }

    private ClaimStatus statusOfTheClaim() {
        return caseDesk.list().stream()
                .filter(overview -> overview.id().equals(CLAIM_ID))
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
                CLAIM_ID,
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
