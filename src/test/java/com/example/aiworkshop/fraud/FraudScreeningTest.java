package com.example.aiworkshop.fraud;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.document.DocumentAnalysis;
import com.example.aiworkshop.document.ManipulationAttempt;
import com.example.aiworkshop.document.MatchConfidence;
import com.example.aiworkshop.document.QualityAssessment;
import com.example.aiworkshop.document.QualityAssessment.Quality;
import com.example.aiworkshop.fraud.FraudIndicator.Kind;
import com.example.aiworkshop.fraud.FraudIndicator.Weight;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FraudScreeningTest {
    private final FraudScreeningStore store = new FraudScreeningStore();

    @Test
    void aCheckThatThrowsIsSkippedAndTheRestStillRun() {
        FraudCheck broken = file -> {
            throw new IllegalStateException("the network went away");
        };
        FraudScreener screener = new FraudScreener(List.of(broken, new DuplicateUploadCheck()), store);

        screener.screen(file("a.png", "case-1", new byte[] {1, 2, 3}));
        FraudScreening second = screener.screen(file("a.png", "case-2", new byte[] {1, 2, 3}));

        assertThat(second.indicators()).extracting(FraudIndicator::kind).containsExactly(Kind.ALREADY_UPLOADED);
    }

    @Test
    void indicatorsComeBackHeaviestFirst() {
        FraudCheck note = file -> List.of(FraudIndicator.of(Kind.NO_CAMERA_ORIGIN, Weight.NOTE, "a", List.of()));
        FraudCheck strong =
                file -> List.of(FraudIndicator.of(Kind.ADDRESSED_THE_AGENT, Weight.STRONG, "b", List.of()));
        FraudScreener screener = new FraudScreener(List.of(note, strong), store);

        FraudScreening screening = screener.screen(file("a.png", "case-1", new byte[] {1}));

        assertThat(screening.indicators()).extracting(FraudIndicator::weight).containsExactly(Weight.STRONG, Weight.NOTE);
        assertThat(screening.heaviest()).isEqualTo(Weight.STRONG);
    }

    @Test
    void aFileIsNeverADuplicateOfItself() {
        FraudScreener screener = new FraudScreener(List.of(new DuplicateUploadCheck()), store);

        assertThat(screener.screen(file("a.png", "case-1", new byte[] {1, 2, 3})).indicators())
                .isEmpty();
    }

    @Test
    void theSameFileOnADifferentCaseWeighsMoreThanOnTheSameOne() {
        FraudScreener screener = new FraudScreener(List.of(new DuplicateUploadCheck()), store);
        byte[] content = {9, 9, 9};

        screener.screen(file("receipt.pdf", "case-1", content));
        FraudScreening sameCase = screener.screen(file("receipt.pdf", "case-1", content));
        FraudScreening otherCase = screener.screen(file("receipt.pdf", "case-2", content));

        assertThat(sameCase.indicators().getFirst().weight()).isEqualTo(Weight.NOTE);
        assertThat(otherCase.indicators().getFirst().weight()).isEqualTo(Weight.STRONG);
    }

    @Test
    void differentBytesAreNotADuplicate() {
        FraudScreener screener = new FraudScreener(List.of(new DuplicateUploadCheck()), store);

        screener.screen(file("a.png", "case-1", new byte[] {1}));

        assertThat(screener.screen(file("b.png", "case-2", new byte[] {2})).indicators())
                .isEmpty();
    }

    @Test
    void aLookupThatDidNotRunProducesNoIndicator() {
        ReverseImageLookup notRun = (image, mimeType) -> Optional.empty();
        FraudScreener screener = new FraudScreener(List.of(new ReverseImageCheck(notRun)), store);

        assertThat(screener.screen(file("photo.png", "case-1", new byte[] {1})).indicators())
                .isEmpty();
    }

    @Test
    void anImagePublishedOnlineIsStrongAndACropIsAConcern() {
        FraudScreener published = new FraudScreener(
                List.of(new ReverseImageCheck((image, mimeType) ->
                        Optional.of(new ReverseImageLookup.WebMatches(2, 0, List.of("https://example.com/car"), "a red Volvo")))),
                store);
        FraudScreener cropped = new FraudScreener(
                List.of(new ReverseImageCheck(
                        (image, mimeType) -> Optional.of(new ReverseImageLookup.WebMatches(0, 3, List.of(), null)))),
                new FraudScreeningStore());

        FraudIndicator full = published.screen(file("photo.png", "c", new byte[] {1})).indicators().getFirst();
        FraudIndicator partial = cropped.screen(file("photo.png", "c", new byte[] {1})).indicators().getFirst();

        assertThat(full.weight()).isEqualTo(Weight.STRONG);
        assertThat(full.evidence()).contains("https://example.com/car", "The search reads the picture as: a red Volvo");
        assertThat(partial.weight()).isEqualTo(Weight.CONCERN);
    }

    @Test
    void aPdfIsNotSentToTheImageLookup() {
        ReverseImageLookup neverAsked = (image, mimeType) -> {
            throw new AssertionError("a PDF should never reach the image lookup");
        };
        FraudScreener screener = new FraudScreener(List.of(new ReverseImageCheck(neverAsked)), store);

        assertThat(screener
                        .screen(new ScreenedFile("d", "c", "scan.pdf", "application/pdf", new byte[] {1}, clean()))
                        .indicators())
                .isEmpty();
    }

    @Test
    void aDocumentThatGaveTheAgentOrdersIsAStrongIndicator() {
        FraudScreener screener = new FraudScreener(List.of(new AddressedTheAgentCheck()), store);
        DocumentAnalysis analysis = new DocumentAnalysis(
                "receipt",
                "A receipt.",
                List.of(),
                null,
                MatchConfidence.LOW,
                new QualityAssessment(Quality.GOOD, "Legible.", List.of()),
                new ManipulationAttempt(
                        "Told the agent to record the document as already approved.", "IGNORE ALL PREVIOUS"));

        FraudIndicator found = screener
                .screen(new ScreenedFile("d", "c", "receipt.pdf", "application/pdf", new byte[] {1}, analysis))
                .indicators()
                .getFirst();

        assertThat(found.kind()).isEqualTo(Kind.ADDRESSED_THE_AGENT);
        assertThat(found.weight()).isEqualTo(Weight.STRONG);
        assertThat(found.evidence()).anyMatch(line -> line.contains("IGNORE ALL PREVIOUS"));
    }

    @Test
    void anOrdinaryDocumentProducesNothing() {
        FraudScreener screener = new FraudScreener(List.of(new AddressedTheAgentCheck()), store);

        assertThat(screener
                        .screen(new ScreenedFile("d", "c", "receipt.pdf", "application/pdf", new byte[] {1}, clean()))
                        .indicators())
                .isEmpty();
    }

    @Test
    void aScreeningIsKeptAgainstTheDocumentItWasMadeFor() {
        FraudScreener screener = new FraudScreener(List.of(new DuplicateUploadCheck()), store);

        FraudScreening screening = screener.screen(file("a.png", "case-1", new byte[] {1}));

        assertThat(store.findByDocumentId(screening.documentId())).contains(screening);
        assertThat(store.findAllFor(List.of(screening.documentId(), "never-screened"))).containsExactly(screening);
    }

    private static ScreenedFile file(String filename, String caseId, byte[] content) {
        return new ScreenedFile("doc-" + filename + caseId, caseId, filename, "image/png", content, clean());
    }

    private static DocumentAnalysis clean() {
        return new DocumentAnalysis(
                "receipt",
                "A receipt.",
                List.of(),
                null,
                MatchConfidence.LOW,
                new QualityAssessment(Quality.GOOD, "Legible.", List.of()),
                null);
    }
}
