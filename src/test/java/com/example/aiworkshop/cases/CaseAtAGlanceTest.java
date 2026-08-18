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
 * The one place the Case Chat system message's contents are pinned.
 *
 * <p>Not the instructions around it — ADR 0002 is explicit that asserting a system message contains
 * a particular word only restates the code. What is pinned is the text the model receives about this
 * Case, the same thing {@link DocumentForSummaryTest} pins for the Case Summary agent.
 */
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

    /** "Nothing" has to read as nothing. An empty line where a list should be reads as a bug. */
    @Test
    void anEmptyListSaysNoneRatherThanNothing() {
        String rendered = aCase(List.of(), List.of()).toString();

        assertThat(rendered).contains("Still outstanding: none").contains("  none");
    }

    /**
     * What stops the agent suggesting the same thing twice. Declined and outstanding both, and
     * confirmed too — a suggestion already carried out is one it should not make again either.
     */
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

    /** The handle a Case Handler's click travels on is of no use to an agent that cannot click. */
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

    /** One line of it only. What a Document renders as is pinned at {@link DocumentForChatTest}. */
    private static UploadedDocument aBlurryReceipt() {
        return new UploadedDocument(
                "d-1",
                "c-1",
                "blurry.jpg",
                "image/jpeg",
                1024,
                Instant.parse("2026-08-15T09:00:00Z"),
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
