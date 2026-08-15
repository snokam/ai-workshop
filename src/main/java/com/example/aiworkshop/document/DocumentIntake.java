package com.example.aiworkshop.document;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.TextContent;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Accepts an upload, hands the raw file to the intake agent, and stores the result.
 *
 * <p>Every upload is kept, whatever the agent thinks of it. {@link QualityAssessment} is advice
 * attached to a stored document, never a reason to refuse one.
 */
@Service
public class DocumentIntake {

    private final DocumentAnalyzer analyzer;
    private final DocumentStore store;

    DocumentIntake(DocumentAnalyzer analyzer, DocumentStore store) {
        this.analyzer = analyzer;
        this.store = store;
    }

    public UploadedDocument accept(MultipartFile file) throws IOException {
        String mimeType = resolveMimeType(file);
        UploadedDocument document = new UploadedDocument(
                UUID.randomUUID().toString(),
                file.getOriginalFilename(),
                mimeType,
                file.getSize(),
                Instant.now(),
                analyzer.analyse(promptFor(file, mimeType)));
        store.save(document);
        return document;
    }

    /**
     * The file goes to the model as-is: a PDF as a PDF, a photo as a photo. Both providers accept
     * both, so nothing is parsed to text on the way — and nothing can be, if the agent is to say
     * whether a scan is legible.
     *
     * <p>The filename is deliberately left out of the prompt. A file called {@code invoice.pdf}
     * would lead the categorisation before the model had looked at a single pixel, and a filename is
     * user-supplied text going into a prompt, which is not somewhere user-supplied text belongs.
     */
    private List<Content> promptFor(MultipartFile file, String mimeType) throws IOException {
        String base64 = Base64.getEncoder().encodeToString(file.getBytes());
        Content fileContent = mimeType.equals("application/pdf")
                ? PdfFileContent.from(base64, mimeType)
                : ImageContent.from(base64, mimeType);
        return List.of(TextContent.from("Analyse the attached file."), fileContent);
    }

    /**
     * Browsers are unreliable about MIME types — some send {@code application/octet-stream} for a
     * PDF, some send nothing at all — so the extension is the fallback.
     */
    private String resolveMimeType(MultipartFile file) {
        String declared = file.getContentType();
        if (declared != null && (declared.equals("application/pdf") || declared.startsWith("image/"))) {
            return declared;
        }
        String name = file.getOriginalFilename() == null
                ? ""
                : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        return switch (name.substring(name.lastIndexOf('.') + 1)) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> throw new UnsupportedDocumentException(
                    "Only PDFs and images can be analysed. Received: " + declared);
        };
    }

    /** Thrown for a file the agent cannot look at. Mapped to 415 by the controller. */
    public static class UnsupportedDocumentException extends RuntimeException {
        UnsupportedDocumentException(String message) {
            super(message);
        }
    }
}
