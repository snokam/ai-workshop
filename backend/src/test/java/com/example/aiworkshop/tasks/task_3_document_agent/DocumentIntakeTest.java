package com.example.aiworkshop.tasks.task_3_document_agent;

import org.springframework.context.ApplicationEventPublisher;
import com.example.aiworkshop.tasks.task_3_document_agent.agent.DocumentAnalyzer;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentStore;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentFiles;
import com.example.aiworkshop.tasks.task_3_document_agent.store.FileType;
import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_3_document_agent.model.QualityAssessment;
import com.example.aiworkshop.tasks.task_1_first_agent.model.MatchConfidence;
import com.example.aiworkshop.tasks.task_3_document_agent.model.ExtractedField;
import com.example.aiworkshop.tasks.task_3_document_agent.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_1_first_agent.store.CaseStore;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseType;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Case;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiworkshop.tasks.task_3_document_agent.model.QualityAssessment.Quality;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

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
    /** Intake announces that a document was stored; nothing in this test cares who hears it. */
    private final ApplicationEventPublisher events = event -> {};

    @TempDir
    Path directory;

    private DocumentFiles files;
    private DocumentIntake intake;

    @BeforeEach
    void theClaimantHasACase() throws IOException {
        cases.save(new Case(
                CASE_ID, "CASE-2026-001", CaseType.HOME_CONTENTS, List.of("proof of identity", "receipt")));
        files = new DocumentFiles(directory);
        intake = new DocumentIntake(analyzer, store, cases, events, files);
    }

    @Test
    void theUploadedBytesAreKeptSoAnAgentCanLookAgain() throws IOException {
        when(analyzer.analyse(anyList(), anyList())).thenReturn(UNREADABLE);

        UploadedDocument document = intake.accept(CASE_ID, image("blurry.jpg"));

        assertThat(files.read(document.id())).isEqualTo(new byte[] {1, 2, 3});
    }

    @Test
    void anUploadNamingACaseThatDoesNotExistIsRefused() {
        assertThatThrownBy(() -> intake.accept("no-such-case", image("receipt.jpg")))
                .isInstanceOf(DocumentIntake.UnknownCaseException.class)
                .hasMessageContaining("no-such-case");
    }

    @Test
    void anUploadedDocumentRecordsTheCaseItBelongsTo() throws IOException {
        when(analyzer.analyse(anyList(), anyList())).thenReturn(UNREADABLE);

        UploadedDocument document = intake.accept(CASE_ID, image("receipt.jpg"));

        assertThat(document.caseId()).isEqualTo(CASE_ID);
    }

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

    @Test
    void aPoorDocumentIsStillAttachedToItsCase() throws IOException {
        when(analyzer.analyse(anyList(), anyList())).thenReturn(UNREADABLE);

        UploadedDocument document = intake.accept(CASE_ID, image("blurry.jpg"));

        assertThat(store.findByCaseId(CASE_ID)).containsExactly(document);
    }

    /**
     * There is no cache. The same file uploaded twice is read twice, deliberately — skipping the
     * second call would be cheaper, and it would put a cache in front of the one line in this class
     * worth reading. What the second upload does record is the same content hash, which is how task
     * 5 notices it.
     */
    @Test
    void everyUploadIsRead() throws IOException {
        when(analyzer.analyse(anyList(), anyList())).thenReturn(MATCHED_RECEIPT);

        UploadedDocument first = intake.accept(CASE_ID, image("receipt.jpg"));
        UploadedDocument second = intake.accept(CASE_ID, image("receipt.jpg"));

        verify(analyzer, times(2)).analyse(anyList(), anyList());
        assertThat(second.contentHash()).isEqualTo(first.contentHash());
        assertThat(store.findByCaseId(CASE_ID)).hasSize(2);
    }

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

    @Test
    void anUnhelpfulContentTypeFallsBackToTheExtension() throws IOException {
        when(analyzer.analyse(anyList(), anyList())).thenReturn(UNREADABLE);

        UploadedDocument document = intake.accept(CASE_ID, 
                new MockMultipartFile("file", "scan.pdf", "application/octet-stream", "%PDF-1.4".getBytes()));

        assertThat(document.contentType()).isEqualTo("application/pdf");
    }

    @Test
    void aPhotoFromAnIPhoneIsAcceptedWhateverTheBrowserCallsIt() throws IOException {
        when(analyzer.analyse(anyList(), anyList())).thenReturn(UNREADABLE);

        UploadedDocument document = intake.accept(
                CASE_ID,
                new MockMultipartFile("file", "IMG_4021.heic", "application/octet-stream", new byte[] {1, 2, 3}));

        assertThat(document.contentType()).isEqualTo("image/heic");
    }

    @Test
    void aFileTheModelCannotLookAtIsRejected() {
        assertThatThrownBy(() ->
                        intake.accept(CASE_ID, new MockMultipartFile("file", "notes.docx", null, "irrelevant".getBytes())))
                .isInstanceOf(FileType.UnsupportedDocumentException.class);
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
