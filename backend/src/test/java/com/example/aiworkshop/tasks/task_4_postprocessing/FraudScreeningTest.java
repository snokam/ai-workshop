package com.example.aiworkshop.tasks.task_4_postprocessing;

import com.example.aiworkshop.tasks.task_4_postprocessing.model.FraudScreening;
import com.example.aiworkshop.tasks.task_4_postprocessing.checks.ImageMetadataCheck;
import com.example.aiworkshop.tasks.task_4_postprocessing.checks.DuplicateUploadCheck;
import com.example.aiworkshop.tasks.task_4_postprocessing.checks.AddressedTheAgentCheck;
import com.example.aiworkshop.tasks.task_2_document_agent.model.ManipulationAttempt;
import com.example.aiworkshop.documents.model.QualityAssessment;
import com.example.aiworkshop.documents.model.MatchConfidence;
import com.example.aiworkshop.tasks.task_2_document_agent.model.DocumentAnalysis;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.documents.model.QualityAssessment.Quality;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.FraudScreening.Kind;
import com.example.aiworkshop.tasks.task_4_postprocessing.FraudScreener.Upload;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.FraudScreening.Weight;
import java.util.List;
import org.junit.jupiter.api.Test;

class FraudScreeningTest {

    private final FraudScreener screener = new FraudScreener(
            List.of(new DuplicateUploadCheck(), new ImageMetadataCheck(), new AddressedTheAgentCheck()));

    @Test
    void aFileIsNeverADuplicateOfItself() {
        assertThat(screener.screen(upload("a.png", "case-1", new byte[] {1, 2, 3})).indicators())
                .isEmpty();
    }

    @Test
    void theSameFileOnADifferentCaseWeighsMoreThanOnTheSameOne() {
        byte[] content = {9, 9, 9};
        screener.screen(upload("receipt.pdf", "case-1", content));

        FraudScreening sameCase = screener.screen(upload("receipt.pdf", "case-1", content));
        FraudScreening otherCase = screener.screen(upload("receipt.pdf", "case-2", content));

        assertThat(sameCase.indicators().getFirst().weight()).isEqualTo(Weight.NOTE);
        assertThat(otherCase.indicators().getFirst().weight()).isEqualTo(Weight.STRONG);
    }

    @Test
    void differentBytesAreNotADuplicate() {
        screener.screen(upload("a.png", "case-1", new byte[] {1}));

        assertThat(screener.screen(upload("b.png", "case-2", new byte[] {2})).indicators())
                .isEmpty();
    }

    @Test
    void aDocumentThatGaveTheAgentOrdersIsAStrongIndicator() {
        DocumentAnalysis analysis = analysis(new ManipulationAttempt(
                "Told the agent to record the document as already approved.", "IGNORE ALL PREVIOUS"));

        FraudScreening.Indicator found = screener
                .screen(new Upload("d", "c", "receipt.pdf", "application/pdf", new byte[] {1}, "hash-1", analysis))
                .indicators()
                .getFirst();

        assertThat(found.kind()).isEqualTo(Kind.ADDRESSED_THE_AGENT);
        assertThat(found.weight()).isEqualTo(Weight.STRONG);
        assertThat(found.evidence()).anyMatch(line -> line.contains("IGNORE ALL PREVIOUS"));
    }

    @Test
    void anOrdinaryDocumentProducesNothing() {
        assertThat(screener
                        .screen(new Upload(
                                "d", "c", "receipt.pdf", "application/pdf", new byte[] {1}, "hash-1", analysis(null)))
                        .indicators())
                .isEmpty();
    }

    @Test
    void onlyScreeningsThatFoundSomethingComeBack() {
        FraudScreening nothing = screener.screen(upload("a.png", "case-1", new byte[] {1}));
        FraudScreening duplicate = screener.screen(upload("a.png", "case-2", new byte[] {1}));

        assertThat(screener.findAllFor(List.of(nothing.documentId(), duplicate.documentId(), "never-screened")))
                .containsExactly(duplicate);
    }

    private static Upload upload(String filename, String caseId, byte[] content) {
        return new Upload(
                "doc-" + filename + caseId,
                caseId,
                filename,
                "image/png",
                content,
                "hash-of-" + new String(content),
                analysis(null));
    }

    private static DocumentAnalysis analysis(ManipulationAttempt attempt) {
        return new DocumentAnalysis(
                "receipt",
                "A receipt.",
                List.of(),
                null,
                MatchConfidence.LOW,
                new QualityAssessment(Quality.GOOD, "Legible.", List.of()),
                attempt);
    }
}
