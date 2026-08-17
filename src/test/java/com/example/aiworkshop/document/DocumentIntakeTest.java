package com.example.aiworkshop.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiworkshop.cases.Case;
import com.example.aiworkshop.cases.CaseStore;
import com.example.aiworkshop.document.QualityAssessment.Quality;
import com.example.aiworkshop.tasks.task_2_postprocessing.FraudScreener;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

/** Covers intake with the agent mocked out, so the whole class runs without credentials. */
class DocumentIntakeTest {

    private static final String CASE_ID = "c-1";

    private static final DocumentAnalysis UNREADABLE = new DocumentAnalysis(
            "unknown",
            "Could not be read.",
            List.of(),
            null,
            MatchConfidence.LOW,
            new QualityAssessment(Quality.POOR, "The scan is too blurry to read.", List.of("out of focus")),
            null);

    private static final DocumentAnalysis MATCHED_RECEIPT = new DocumentAnalysis(
            "receipt",
            "A receipt for a replacement window.",
            List.of(new ExtractedField("Total", "4 200 kr")),
            "receipt",
            MatchConfidence.HIGH,
            new QualityAssessment(Quality.GOOD, "Fully legible.", List.of()),
            null);

    private final DocumentAnalyzer analyzer = mock(DocumentAnalyzer.class);
    private final DocumentStore store = new DocumentStore();
    private final CaseStore cases = new CaseStore();
    private final FraudScreener screener = new FraudScreener(List.of());
    private final DocumentIntake intake = new DocumentIntake(analyzer, store, cases, screener);

    @BeforeEach
    void theClaimantHasACase() {
        cases.save(new Case(CASE_ID, "CASE-2026-001", List.of("proof of identity", "receipt")));
    }

    /**
     * The one thing intake refuses on the Case side. An upload is always accepted, but a Case that
     * does not exist is a broken client rather than a poor document.
     */
    @Test
    void anUploadNamingACaseThatDoesNotExistIsRefused() {
        assertThatThrownBy(() -> intake.accept("no-such-case", image("receipt.jpg")))
                .isInstanceOf(DocumentIntake.UnknownCaseException.class)
                .hasMessageContaining("no-such-case");
    }

    /** A Document uploaded into nothing has nowhere for the agent's work to go. */
    @Test
    void anUploadedDocumentRecordsTheCaseItBelongsTo() throws IOException {
        when(analyzer.analyse(anyList(), anyList())).thenReturn(UNREADABLE);

        UploadedDocument document = intake.accept(CASE_ID, image("receipt.jpg"));

        assertThat(document.caseId()).isEqualTo(CASE_ID);
    }

    /**
     * Matching the upload to what the Case asked for is the whole reason the agent is told about the
     * Case at all — and it happens in the call that already classifies, extracts and assesses, so an
     * upload is still one model call rather than two.
     */
    @Test
    void theAgentIsToldWhatTheCaseIsWaitingFor() throws IOException {
        when(analyzer.analyse(anyList(), anyList())).thenReturn(UNREADABLE);

        intake.accept(CASE_ID, image("receipt.jpg"));

        assertThat(capturedRequiredDocuments()).containsExactly("proof of identity", "receipt");
    }

    @Test
    void theMatchTheAgentReportedIsStoredOnTheDocument() throws IOException {
        when(analyzer.analyse(anyList(), anyList())).thenReturn(MATCHED_RECEIPT);

        UploadedDocument document = intake.accept(CASE_ID, image("receipt.jpg"));

        assertThat(store.findById(document.id()))
                .get()
                .extracting(stored -> stored.analysis().matchedRequiredDocument())
                .isEqualTo("receipt");
    }

    @Test
    void poorQualityDocumentsAreStillAccepted() throws IOException {
        when(analyzer.analyse(anyList(), anyList())).thenReturn(UNREADABLE);

        UploadedDocument document = intake.accept(CASE_ID, image("blurry.jpg"));

        assertThat(document.analysis().quality().verdict()).isEqualTo(Quality.POOR);
        assertThat(store.findById(document.id())).contains(document);
    }

    /**
     * The other half of "always accepted": a file the agent could not read still lands in the Case,
     * where it holds the Case at NEEDS_REVIEW rather than vanishing.
     */
    @Test
    void aPoorDocumentIsStillAttachedToItsCase() throws IOException {
        when(analyzer.analyse(anyList(), anyList())).thenReturn(UNREADABLE);

        UploadedDocument document = intake.accept(CASE_ID, image("blurry.jpg"));

        assertThat(store.findByCaseId(CASE_ID)).containsExactly(document);
    }

    /** Nothing a Claimant sent is thrown away, so a better re-upload adds a Document rather than replacing one. */
    @Test
    void aBetterReUploadIsKeptAlongsideTheEarlierOne() throws IOException {
        when(analyzer.analyse(anyList(), anyList())).thenReturn(UNREADABLE, MATCHED_RECEIPT);

        intake.accept(CASE_ID, image("blurry.jpg"));
        intake.accept(CASE_ID, image("better.jpg"));

        assertThat(store.findByCaseId(CASE_ID)).hasSize(2);
    }

    @Test
    void aPdfReachesTheModelAsAPdf() throws IOException {
        when(analyzer.analyse(anyList(), anyList())).thenReturn(UNREADABLE);

        intake.accept(CASE_ID, new MockMultipartFile("file", "scan.pdf", "application/pdf", "%PDF-1.4".getBytes()));

        assertThat(capturedContent()).hasAtLeastOneElementOfType(PdfFileContent.class);
    }

    @Test
    void anImageReachesTheModelAsAnImage() throws IOException {
        when(analyzer.analyse(anyList(), anyList())).thenReturn(UNREADABLE);

        intake.accept(CASE_ID, image("receipt.jpg"));

        assertThat(capturedContent()).hasAtLeastOneElementOfType(ImageContent.class);
    }

    /** Browsers routinely mislabel a PDF as a generic binary, so the extension has to win. */
    @Test
    void anUnhelpfulContentTypeFallsBackToTheExtension() throws IOException {
        when(analyzer.analyse(anyList(), anyList())).thenReturn(UNREADABLE);

        UploadedDocument document = intake.accept(CASE_ID, 
                new MockMultipartFile("file", "scan.pdf", "application/octet-stream", "%PDF-1.4".getBytes()));

        assertThat(document.contentType()).isEqualTo("application/pdf");
    }

    @Test
    void aFileTheModelCannotLookAtIsRejected() {
        assertThatThrownBy(() ->
                        intake.accept(CASE_ID, new MockMultipartFile("file", "notes.docx", null, "irrelevant".getBytes())))
                .isInstanceOf(DocumentIntake.UnsupportedDocumentException.class);
    }

    private List<Content> capturedContent() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Content>> captor = ArgumentCaptor.forClass(List.class);
        verify(analyzer).analyse(captor.capture(), anyList());
        return captor.getValue();
    }

    private List<String> capturedRequiredDocuments() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(analyzer).analyse(anyList(), captor.capture());
        return captor.getValue();
    }

    private static MockMultipartFile image(String name) {
        return new MockMultipartFile("file", name, "image/jpeg", new byte[] {1, 2, 3});
    }
}
