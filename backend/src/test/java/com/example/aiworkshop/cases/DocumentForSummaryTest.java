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
import com.example.aiworkshop.tasks.task_6_summary.CaseSummarizer;

/**
 * The one place the text handed to {@link CaseSummarizer} is pinned.
 *
 * <p>Asserted here rather than at {@link CaseDeskTest} because this rendering is the whole job of the
 * type: a change to it is a change to a prompt, and it should fail under a name that says so.
 */
class DocumentForSummaryTest {

    @Test
    void itRendersWhatTheDocumentSays() {
        String rendered = DocumentForSummary.of(aReceipt()).toString();

        assertThat(rendered)
                .contains("receipt.jpg")
                .contains("receipt")
                .contains("A receipt from MENY.")
                .contains("Butikk", "MENY Storo")
                .contains("Summa", "unreadable")
                .contains("POOR");
    }

    /**
     * The failure this type was introduced for. A Document carries plenty the summarizer has no use
     * for, and under the old record dump all of it reached the model — including the Quality
     * Assessment's own prose, which the agent then paraphrased back into the Case Summary beside the
     * Document that already said it.
     */
    @Test
    void itLeavesOutTheInternals() {
        String rendered = DocumentForSummary.of(aReceipt()).toString();

        assertThat(rendered)
                .doesNotContain("d-1")
                .doesNotContain("c-1")
                .doesNotContain("image/jpeg")
                .doesNotContain("4821004")
                .doesNotContain("2026-08-15T09:00:00Z")
                .doesNotContain("Shadows across the lower half.")
                .doesNotContain("total cut off");
    }

    private static UploadedDocument aReceipt() {
        return new UploadedDocument(
                "d-1",
                "c-1",
                "receipt.jpg",
                "image/jpeg",
                4821004,
                Instant.parse("2026-08-15T09:00:00Z"),
                "hash-of-receipt",
                new DocumentAnalysis(
                        "receipt",
                        "A receipt from MENY.",
                        List.of(new ExtractedField("Butikk", "MENY Storo"), new ExtractedField("Summa", "unreadable")),
                        "receipt",
                        MatchConfidence.HIGH,
                        new QualityAssessment(Quality.POOR, "Shadows across the lower half.", List.of("total cut off")),
                        null),
                false);
    }
}
