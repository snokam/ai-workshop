package com.example.aiworkshop.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.aiworkshop.document.QualityAssessment.Quality;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

/** Covers intake with the agent mocked out, so the whole class runs without credentials. */
class DocumentIntakeTest {

    private static final DocumentAnalysis UNREADABLE = new DocumentAnalysis(
            "unknown",
            "Could not be read.",
            List.of(),
            new QualityAssessment(Quality.POOR, "The scan is too blurry to read.", List.of("out of focus")));

    private final DocumentAnalyzer analyzer = mock(DocumentAnalyzer.class);
    private final DocumentStore store = new DocumentStore();
    private final DocumentIntake intake = new DocumentIntake(analyzer, store);

    @Test
    void poorQualityDocumentsAreStillAccepted() throws IOException {
        when(analyzer.analyse(anyList())).thenReturn(UNREADABLE);

        UploadedDocument document = intake.accept(image("blurry.jpg"));

        assertThat(document.analysis().quality().verdict()).isEqualTo(Quality.POOR);
        assertThat(store.findById(document.id())).contains(document);
    }

    @Test
    void aPdfReachesTheModelAsAPdf() throws IOException {
        when(analyzer.analyse(anyList())).thenReturn(UNREADABLE);

        intake.accept(new MockMultipartFile("file", "scan.pdf", "application/pdf", "%PDF-1.4".getBytes()));

        assertThat(capturedContent()).hasAtLeastOneElementOfType(PdfFileContent.class);
    }

    @Test
    void anImageReachesTheModelAsAnImage() throws IOException {
        when(analyzer.analyse(anyList())).thenReturn(UNREADABLE);

        intake.accept(image("receipt.jpg"));

        assertThat(capturedContent()).hasAtLeastOneElementOfType(ImageContent.class);
    }

    /** Browsers routinely mislabel a PDF as a generic binary, so the extension has to win. */
    @Test
    void anUnhelpfulContentTypeFallsBackToTheExtension() throws IOException {
        when(analyzer.analyse(anyList())).thenReturn(UNREADABLE);

        UploadedDocument document = intake.accept(
                new MockMultipartFile("file", "scan.pdf", "application/octet-stream", "%PDF-1.4".getBytes()));

        assertThat(document.contentType()).isEqualTo("application/pdf");
    }

    @Test
    void aFileTheModelCannotLookAtIsRejected() {
        assertThatThrownBy(() ->
                        intake.accept(new MockMultipartFile("file", "notes.docx", null, "irrelevant".getBytes())))
                .isInstanceOf(DocumentIntake.UnsupportedDocumentException.class);
    }

    private List<Content> capturedContent() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Content>> captor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(analyzer).analyse(captor.capture());
        return captor.getValue();
    }

    private static MockMultipartFile image(String name) {
        return new MockMultipartFile("file", name, "image/jpeg", new byte[] {1, 2, 3});
    }
}
