package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.model;

import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_3_document_agent.model.QualityAssessment;
import com.example.aiworkshop.tasks.task_1_first_agent.model.MatchConfidence;
import com.example.aiworkshop.tasks.task_3_document_agent.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.proposals.ProposalState;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.proposals.ProposalKind;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.proposals.ProposalCard;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimStatus;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.tasks.task_3_document_agent.model.QualityAssessment.Quality;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClaimAtAGlanceTest {
    @Test
    void itRendersTheClaimTheHandlerHasOpen() {
        String rendered =
                aClaim(List.of(DocumentForChat.of(aBlurryReceipt(), true)), List.of()).toString();

        assertThat(rendered)
                .contains("Claim CLAIM-2026-001 — Home contents claim")
                .contains("Status: NEEDS_REVIEW")
                .contains("Required documents: receipt")
                .contains("blurry.jpg — receipt — counts as \"receipt\" — quality POOR")
                .contains("What the documents say, taken together.");
    }

    @Test
    void anEmptyListSaysNoneRatherThanNothing() {
        String rendered = aClaim(List.of(), List.of()).toString();

        assertThat(rendered).contains("Still outstanding: none").contains("  none");
    }

    @Test
    void itListsWhatTheAgentHasAlreadySuggestedAndWhatBecameOfIt() {
        String rendered = aClaim(
                        List.of(),
                        List.of(
                                new ProposalCard(
                                        "p-1",
                                        ProposalKind.REVIEW,
                                        "blurry.jpg",
                                        "The total is legible despite the shadow.",
                                        ProposalState.DECLINED),
                                new ProposalCard(
                                        "p-2",
                                        ProposalKind.DOCUMENT_REQUEST,
                                        "the second page of the receipt",
                                        "The total is on a page that did not arrive.",
                                        ProposalState.PROPOSED)))
                .toString();

        assertThat(rendered)
                .contains("REVIEW blurry.jpg [DECLINED] — The total is legible despite the shadow.")
                .contains("DOCUMENT_REQUEST the second page of the receipt [PROPOSED] —");
    }

    @Test
    void itDoesNotHandTheAgentProposalIdentifiers() {
        String rendered = aClaim(
                        List.of(),
                        List.of(new ProposalCard(
                                "p-1", ProposalKind.REVIEW, "blurry.jpg", "Legible enough.", ProposalState.PROPOSED)))
                .toString();

        assertThat(rendered).doesNotContain("p-1");
    }

    private static ClaimAtAGlance aClaim(List<DocumentForChat> documents, List<ProposalCard> proposals) {
        return new ClaimAtAGlance(
                "CLAIM-2026-001",
                "Home contents claim",
                ClaimStatus.NEEDS_REVIEW,
                List.of("receipt"),
                List.of(),
                "What the documents say, taken together.",
                documents,
                proposals);
    }

    private static UploadedDocument aBlurryReceipt() {
        return new UploadedDocument(
                "d-1",
                "c-1",
                "blurry.jpg",
                "image/jpeg",
                1024,
                Instant.parse("2026-08-15T09:00:00Z"),
                "hash-of-blurry",
                new DocumentAnalysis(
                        "receipt",
                        "A receipt from MENY.",
                        List.of(),
                        "receipt",
                        MatchConfidence.HIGH,
                        new QualityAssessment(Quality.POOR, "Shadows across the lower half.", List.of()),
                        null),
                false);
    }
}
