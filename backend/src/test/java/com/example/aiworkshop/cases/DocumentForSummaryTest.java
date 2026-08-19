package com.example.aiworkshop.cases;

import com.example.aiworkshop.documents.model.UploadedDocument;
import com.example.aiworkshop.documents.model.QualityAssessment;
import com.example.aiworkshop.documents.model.MatchConfidence;
import com.example.aiworkshop.documents.model.ExtractedField;
import com.example.aiworkshop.documents.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_6_summary.DocumentForSummary;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.documents.model.QualityAssessment.Quality;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

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
