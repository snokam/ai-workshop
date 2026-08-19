package com.example.aiworkshop.tasks.task_4_postprocessing.checks;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.documents.model.DocumentAnalysis;
import com.example.aiworkshop.documents.model.ExtractedField;
import com.example.aiworkshop.documents.model.MatchConfidence;
import com.example.aiworkshop.documents.model.QualityAssessment;
import com.example.aiworkshop.documents.model.QualityAssessment.Quality;
import com.example.aiworkshop.tasks.task_4_postprocessing.FraudScreener.Upload;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.FraudScreening.Indicator;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.FraudScreening.Kind;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The check that reads what the agent extracted rather than the file.
 *
 * <p>Everything here is arithmetic on values a model wrote down, which is the point: the reading
 * came from a model and the conclusion does not.
 */
class FiguresCheckTest {

    private final FiguresCheck check = new FiguresCheck();

    @Test
    void saysNothingWhenTheFiguresAgree() {
        List<Indicator> found = check.screen(receiptWith(
                field("Varer", "100,00"), field("Frakt", "49,50"), field("Totalt", "149,50")));

        assertThat(found).isEmpty();
    }

    @Test
    void noticesWhenTheStatedTotalIsNotTheSum() {
        List<Indicator> found = check.screen(receiptWith(
                field("Varer", "100,00"), field("Frakt", "49,50"), field("Totalt", "1495,00")));

        assertThat(found).singleElement().satisfies(indicator -> {
            assertThat(indicator.kind()).isEqualTo(Kind.FIGURES_DISAGREE);
            assertThat(indicator.detail()).contains("1495.00").contains("149.50");
        });
    }

    @Test
    void saysNothingWhenThereIsNoTotalToCheckAgainst() {
        assertThat(check.screen(receiptWith(field("Varer", "100,00"), field("Frakt", "49,50"))))
                .isEmpty();
    }

    @Test
    void saysNothingWhenTheValuesAreNotAmounts() {
        assertThat(check.screen(receiptWith(
                        field("Butikk", "MENY Grünerløkka"),
                        field("Dato", "12.03.2026"),
                        field("Totalt", "ikke oppgitt"))))
                .isEmpty();
    }

    @Test
    void ignoresTheNumbersOnAReceiptThatAreNotMoney() {
        List<Indicator> found = check.screen(receiptWith(
                field("Org.nr", "912 345 678"),
                field("Kvittering No.", "40219"),
                field("Dato", "12.03.2026"),
                field("Delsum", "16375,00 kr"),
                field("MVA", "4093,75 kr"),
                field("Totalt", "20468,75 kr")));

        assertThat(found)
                .describedAs("an organisation number is not an amount, and summing it accuses a document"
                        + " nobody has read")
                .isEmpty();
    }

    private static ExtractedField field(String name, String value) {
        return new ExtractedField(name, value);
    }

    private static Upload receiptWith(ExtractedField... fields) {
        DocumentAnalysis analysis = new DocumentAnalysis(
                "receipt",
                "A receipt.",
                List.of(fields),
                null,
                MatchConfidence.LOW,
                new QualityAssessment(Quality.GOOD, "Legible.", List.of()),
                null);
        return new Upload("d", "c", "receipt.png", "image/png", new byte[] {1}, "hash", analysis);
    }
}
