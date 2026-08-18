package com.example.aiworkshop.document;

import com.example.aiworkshop.cases.Case;
import com.example.aiworkshop.cases.CaseStore;
import com.example.aiworkshop.tasks.task_2_postprocessing.FraudScreener;
import com.example.aiworkshop.tasks.task_2_postprocessing.FraudScreener.Upload;
import com.example.aiworkshop.tasks.task_1_guardrails.Guardrails;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.TextContent;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Accepts an upload, hands the raw file to the intake agent, stores the result, and screens the
 * bytes before they go out of scope.
 *
 * <p>Every upload is kept, whatever the agent thinks of it. {@link QualityAssessment} is advice
 * attached to a stored document, never a reason to refuse one.
 */
@Service
public class DocumentIntake {

    private static final Logger log = LoggerFactory.getLogger(DocumentIntake.class);

    private final DocumentAnalyzer analyzer;
    private final DocumentStore store;
    private final CaseStore cases;
    private final FraudScreener screener;
    private final DocumentFiles files;

    DocumentIntake(
            DocumentAnalyzer analyzer,
            DocumentStore store,
            CaseStore cases,
            FraudScreener screener,
            DocumentFiles files) {
        this.analyzer = analyzer;
        this.store = store;
        this.cases = cases;
        this.screener = screener;
        this.files = files;
    }

    public UploadedDocument accept(String caseId, MultipartFile file) throws IOException {
        Case theCase =
                cases.findById(caseId).orElseThrow(() -> new UnknownCaseException("No such case: " + caseId));
        String mimeType = resolveMimeType(file);
        String id = UUID.randomUUID().toString();
        // Kept before the agent runs, at the point the bytes are already in hand. See ADR 0004.
        files.save(id, file.getBytes());

        String contentHash = hashOf(file.getBytes());
        Optional<DocumentAnalysis> alreadyRead = alreadyReadOnThisCase(caseId, contentHash);
        if (alreadyRead.isPresent()) {
            log.info("{} is byte-identical to a document already on case {}; not reading it again",
                    file.getOriginalFilename(), caseId);
        }
        DocumentAnalysis analysis = alreadyRead.isPresent()
                ? alreadyRead.get()
                : analyzer.analyse(promptFor(file, mimeType), theCase.requiredDocuments());

        UploadedDocument document = new UploadedDocument(
                id,
                caseId,
                file.getOriginalFilename(),
                mimeType,
                file.getSize(),
                Instant.now(),
                contentHash,
                analysis,
                false);
        store.save(document);

        screener.screen(new Upload(
                id, caseId, file.getOriginalFilename(), mimeType, file.getBytes(), contentHash, analysis));
        return document;
    }

    /**
     * The reading this Case already has of these exact bytes, if it has one.
     *
     * <p>A Claimant who uploads the same file twice — a double-click, or sending it again because
     * nothing seemed to happen — should not cost a second model call, and should not get a second
     * opinion. Asking the model again is not free and not deterministic: the same licence read twice
     * came back GOOD once and ACCEPTABLE once, which on the handler's screen looks like the agent
     * contradicting itself about one file.
     *
     * <p>Scoped to the Case on purpose. The same bytes on a *different* Case is the interesting case
     * and gets read on its own merits, because the question there is whether one expense is being
     * claimed twice — which is the screening's job to raise, not intake's to quietly optimise away.
     */
    private Optional<DocumentAnalysis> alreadyReadOnThisCase(String caseId, String contentHash) {
        return store.findByCaseId(caseId).stream()
                .filter(document -> contentHash.equals(document.contentHash()))
                .map(UploadedDocument::analysis)
                .findFirst();
    }

    private static String hashOf(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required of every JVM", e);
        }
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
        return List.of(
                TextContent.from(Guardrails.INTAKE_INSTRUCTION), DocumentFiles.contentOf(file.getBytes(), mimeType));
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

    /**
     * Thrown when the upload names a Case that does not exist. Mapped to 404 by the controller.
     *
     * <p>The only refusal on the Case side. A poor document is still accepted — this is a broken
     * client, not a judgement about the file.
     */
    public static class UnknownCaseException extends RuntimeException {
        UnknownCaseException(String message) {
            super(message);
        }
    }
}
