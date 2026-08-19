package com.example.aiworkshop.tasks.task_6_chat.model;

import com.example.aiworkshop.tasks.task_1_first_agent.model.Case;
import com.example.aiworkshop.tasks.task_2_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_2_document_agent.model.QualityAssessment;
import com.example.aiworkshop.tasks.task_1_first_agent.model.MatchConfidence;
import com.example.aiworkshop.tasks.task_2_document_agent.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_6_chat.proposals.ProposalState;
import com.example.aiworkshop.tasks.task_6_chat.proposals.ProposalKind;
import com.example.aiworkshop.tasks.task_6_chat.proposals.ProposalCard;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseStatus;
import com.example.aiworkshop.tasks.task_6_chat.model.DocumentForChat;
import com.example.aiworkshop.tasks.task_6_chat.model.CaseAtAGlance;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.tasks.task_2_document_agent.model.QualityAssessment.Quality;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class CaseAtAGlanceTest {
    @Test
    void itRendersTheCaseTheHandlerHasOpen() {
        String rendered =
                aCase(List.of(DocumentForChat.of(aBlurryReceipt(), true)), List.of()).toString();

        assertThat(rendered)
                .contains("Case CASE-2026-001 — Home contents claim")
                .contains("Status: NEEDS_REVIEW")
                .contains("Required documents: receipt")
                .contains("blurry.jpg — receipt — counts as \"receipt\" — quality POOR")
                .contains("What the documents say, taken together.");
    }

    @Test
    void anEmptyListSaysNoneRatherThanNothing() {
        String rendered = aCase(List.of(), List.of()).toString();

        assertThat(rendered).contains("Still outstanding: none").contains("  none");
    }

    @Test
    void itListsWhatTheAgentHasAlreadySuggestedAndWhatBecameOfIt() {
        String rendered = aCase(
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
        String rendered = aCase(
                        List.of(),
                        List.of(new ProposalCard(
                                "p-1", ProposalKind.REVIEW, "blurry.jpg", "Legible enough.", ProposalState.PROPOSED)))
                .toString();

        assertThat(rendered).doesNotContain("p-1");
    }

    private static CaseAtAGlance aCase(List<DocumentForChat> documents, List<ProposalCard> proposals) {
        return new CaseAtAGlance(
                "CASE-2026-001",
                "Home contents claim",
                CaseStatus.NEEDS_REVIEW,
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
