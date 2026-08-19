package com.example.aiworkshop.tasks.task_6_chat.model;

import com.example.aiworkshop.tasks.task_2_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_2_document_agent.model.QualityAssessment;
import com.example.aiworkshop.tasks.task_1_first_agent.model.MatchConfidence;
import com.example.aiworkshop.tasks.task_2_document_agent.model.ExtractedField;
import com.example.aiworkshop.tasks.task_2_document_agent.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_6_chat.model.DocumentForChat;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.tasks.task_2_document_agent.model.QualityAssessment.Quality;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentForChatTest {
    @Test
    void itSaysWhatTheDocumentIsAndWhatItCountsAs() {
        String rendered = DocumentForChat.of(aReceipt(Quality.POOR, false), true).toString();

        assertThat(rendered).isEqualTo("receipt.jpg — receipt — counts as \"receipt\" — quality POOR");
    }

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
                "hash-of-" + filename,
                new DocumentAnalysis(
                        category, summary, fields, matchedRequiredDocument, MatchConfidence.HIGH, quality, null),
                reviewed);
    }
}
