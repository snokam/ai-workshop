package com.example.aiworkshop.cases;

import com.example.aiworkshop.documents.model.UploadedDocument;
import com.example.aiworkshop.documents.model.QualityAssessment;
import com.example.aiworkshop.documents.model.MatchConfidence;
import com.example.aiworkshop.documents.model.ExtractedField;
import com.example.aiworkshop.documents.model.DocumentAnalysis;
import com.example.aiworkshop.cases.chat.DocumentInDetail;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.documents.model.QualityAssessment.Quality;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentInDetailTest {
    @Test
    void itCarriesEverythingTheIndexLeftOut() {
        String rendered = DocumentInDetail.of(aReceipt()).toString();

        assertThat(rendered)
                .contains("receipt.jpg")
                .contains("A receipt from MENY.")
                .contains("Butikk: MENY Storo")
                .contains("Summa: unreadable")
                .contains("Shadows across the lower half.")
                .contains("total cut off");
    }

    @Test
    void itQuotesFieldNamesAsTheyAppearOnTheDocument() {
        String rendered = DocumentInDetail.of(aReceipt()).toString();

        assertThat(rendered).contains("Butikk").doesNotContain("Store").doesNotContain("Shop");
    }

    @Test
    void aDocumentNothingCouldBeReadOffSaysThatRatherThanNothing() {
        String rendered = DocumentInDetail.of(anUnreadableScan()).toString();

        assertThat(rendered).contains("nothing could be read off this document");
    }

    @Test
    void itIsNotTheFile() {
        String rendered = DocumentInDetail.of(aReceipt()).toString();

        assertThat(rendered).doesNotContain("image/jpeg").doesNotContain("4821004");
    }

    private static UploadedDocument aReceipt() {
        return document(
                List.of(new ExtractedField("Butikk", "MENY Storo"), new ExtractedField("Summa", "unreadable")),
                new QualityAssessment(Quality.POOR, "Shadows across the lower half.", List.of("total cut off")));
    }

    private static UploadedDocument anUnreadableScan() {
        return document(List.of(), new QualityAssessment(Quality.POOR, "Out of focus throughout.", List.of()));
    }

    private static UploadedDocument document(List<ExtractedField> fields, QualityAssessment quality) {
        return new UploadedDocument(
                "d-1",
                "c-1",
                "receipt.jpg",
                "image/jpeg",
                4821004,
                Instant.parse("2026-08-15T09:00:00Z"),
                "hash-of-receipt",
                new DocumentAnalysis(
                        "receipt", "A receipt from MENY.", fields, "receipt", MatchConfidence.HIGH, quality, null),
                false);
    }
}
