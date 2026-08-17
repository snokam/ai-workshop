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
 * The other half of {@link DocumentForChatTest}: what the detail tool hands back.
 *
 * <p>Everything the index deliberately omits appears here, which is what makes omitting it from the
 * index a division of labour rather than a loss. This text is a tool result and therefore a prompt.
 */
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

    /** Field names and values are quoted off the Document and stay in its own language (ADR 0002). */
    @Test
    void itQuotesFieldNamesAsTheyAppearOnTheDocument() {
        String rendered = DocumentInDetail.of(aReceipt()).toString();

        assertThat(rendered).contains("Butikk").doesNotContain("Store").doesNotContain("Shop");
    }

    /**
     * An empty field list is a finding, not an absence: the intake agent is told that inventing a
     * value is worse than returning none. Rendered as a blank it reads as a bug.
     */
    @Test
    void aDocumentNothingCouldBeReadOffSaysThatRatherThanNothing() {
        String rendered = DocumentInDetail.of(anUnreadableScan()).toString();

        assertThat(rendered).contains("nothing could be read off this document");
    }

    /** Still not the file. What this cannot answer is what the reader agent is for. */
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
                new DocumentAnalysis(
                        "receipt", "A receipt from MENY.", fields, "receipt", MatchConfidence.HIGH, quality),
                false);
    }
}
