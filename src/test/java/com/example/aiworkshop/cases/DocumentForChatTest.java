package com.example.aiworkshop.cases;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.document.DocumentAnalysis;
import com.example.aiworkshop.document.ExtractedField;
import com.example.aiworkshop.document.MatchConfidence;
import com.example.aiworkshop.document.QualityAssessment;
import com.example.aiworkshop.document.QualityAssessment.Quality;
import com.example.aiworkshop.document.UploadedDocument;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The one place the Case Chat agent's index of a Case is pinned.
 *
 * <p>Asserted here rather than at {@link CaseChatTest} for the reason {@link DocumentForSummaryTest}
 * gives: this rendering is the whole job of the type, a change to it is a change to a prompt, and it
 * should fail under a name that says so.
 *
 * <p>What is absent matters more here than in any other projection. The agent has tools to fetch a
 * Document's Extraction and the Quality Assessment's reasoning; an index that already carried them
 * would make those tools pointless, the prompt enormous, and the tool strip under the answer a lie.
 */
class DocumentForChatTest {

    @Test
    void itSaysWhatTheDocumentIsAndWhatItCountsAs() {
        String rendered = DocumentForChat.of(aReceipt(Quality.POOR, false), true).toString();

        assertThat(rendered).isEqualTo("receipt.jpg — receipt — counts as \"receipt\" — quality POOR");
    }

    /** The failure the detail tool exists to prevent: an index that is already the documents. */
    @Test
    void itLeavesOutEverythingATooIsForFetching() {
        String rendered = DocumentForChat.of(aReceipt(Quality.POOR, false), true).toString();

        assertThat(rendered)
                .doesNotContain("A receipt from MENY.")
                .doesNotContain("Butikk", "MENY Storo")
                .doesNotContain("Summa", "unreadable")
                .doesNotContain("Shadows across the lower half.")
                .doesNotContain("total cut off")
                .doesNotContain("d-1")
                .doesNotContain("image/jpeg");
    }

    /**
     * A Document that lost to a later upload of the same thing counts as nothing, and the agent has
     * to know which of two files with the same story is the live one.
     */
    @Test
    void aSupersededDocumentSaysWhatSupersededIt() {
        String rendered = DocumentForChat.of(aReceipt(Quality.GOOD, false), false).toString();

        assertThat(rendered).contains("superseded by a later \"receipt\"");
    }

    @Test
    void aDocumentThatMatchedNothingSaysSo() {
        String rendered = DocumentForChat.of(aHolidayPhoto(), false).toString();

        assertThat(rendered).contains("counts as nothing this case requires");
    }

    /**
     * Without this the agent reads POOR off a Document a handler has already cleared and suggests a
     * Review that has already happened.
     */
    @Test
    void aReviewedDocumentSaysItHasBeenReviewed() {
        String rendered = DocumentForChat.of(aReceipt(Quality.POOR, true), true).toString();

        assertThat(rendered).contains("quality POOR, already reviewed by a case handler");
    }

    private static UploadedDocument aReceipt(Quality verdict, boolean reviewed) {
        return document(
                "receipt.jpg",
                "receipt",
                "A receipt from MENY.",
                List.of(new ExtractedField("Butikk", "MENY Storo"), new ExtractedField("Summa", "unreadable")),
                "receipt",
                new QualityAssessment(verdict, "Shadows across the lower half.", List.of("total cut off")),
                reviewed);
    }

    private static UploadedDocument aHolidayPhoto() {
        return document(
                "holiday.png",
                "photograph",
                "A photograph of a beach.",
                List.of(),
                null,
                new QualityAssessment(Quality.GOOD, "Sharp and well lit.", List.of()),
                false);
    }

    private static UploadedDocument document(
            String filename,
            String category,
            String summary,
            List<ExtractedField> fields,
            String matchedRequiredDocument,
            QualityAssessment quality,
            boolean reviewed) {
        return new UploadedDocument(
                "d-1",
                "c-1",
                filename,
                "image/jpeg",
                4821004,
                Instant.parse("2026-08-15T09:00:00Z"),
                new DocumentAnalysis(
                        category, summary, fields, matchedRequiredDocument, MatchConfidence.HIGH, quality),
                reviewed);
    }
}
