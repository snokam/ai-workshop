package com.example.aiworkshop.tasks.task_3_document_agent;

import com.example.aiworkshop.workshop.WorkshopTask;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.tasks.task_3_document_agent.agent.DocumentAnalyzer;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentStore;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentFiles;
import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_3_document_agent.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_1_first_agent.store.CaseStore;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Case;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.TextContent;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentIntake {
    private static final Logger log = LoggerFactory.getLogger(DocumentIntake.class);

    private final DocumentAnalyzer analyzer;
    private final DocumentStore store;
    private final CaseStore cases;
    private final Map<String, Object> arrivals = new ConcurrentHashMap<>();
    /**
     * The only text sent alongside the file. Task 3's input guardrail refuses a message carrying
     * anything else, which is why it lives here rather than there: this is what intake sends, and a
     * guardrail checks what it was given.
     */
    public static final String INTAKE_INSTRUCTION = "Analyse the attached file.";

    private final ApplicationEventPublisher events;
    private final DocumentFiles files;

    DocumentIntake(
            DocumentAnalyzer analyzer,
            DocumentStore store,
            CaseStore cases,
            ApplicationEventPublisher events,
            DocumentFiles files) {
        this.analyzer = analyzer;
        this.store = store;
        this.cases = cases;
        this.events = events;
        this.files = files;
    }

    public UploadedDocument accept(String caseId, MultipartFile file) throws IOException {
        Case theCase =
                cases.findById(caseId).orElseThrow(() -> new UnknownCaseException("No such case: " + caseId));
        String mimeType = resolveMimeType(file);
        String id = UUID.randomUUID().toString();
        files.save(id, file.getBytes());
        String contentHash = hashOf(file.getBytes());

        synchronized (arrivalOf(caseId, contentHash)) {
            DocumentAnalysis analysis = analysisFor(theCase, file, mimeType, contentHash);
            return store(id, caseId, file, mimeType, contentHash, analysis);
        }
    }

    private DocumentAnalysis analysisFor(Case theCase, MultipartFile file, String mimeType, String contentHash)
            throws IOException {
        Optional<DocumentAnalysis> alreadyRead = alreadyReadOnThisCase(theCase.id(), contentHash);
        if (alreadyRead.isPresent()) {
            log.info(
                    "{} is byte-identical to a document already on case {}; not reading it again",
                    file.getOriginalFilename(),
                    theCase.id());
            return alreadyRead.get();
        }
        return analyzer.analyse(promptFor(file, mimeType), theCase.requiredDocuments());
    }

    private UploadedDocument store(
            String id,
            String caseId,
            MultipartFile file,
            String mimeType,
            String contentHash,
            DocumentAnalysis analysis)
            throws IOException {
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

        events.publishEvent(new DocumentStored(
                id, caseId, file.getOriginalFilename(), mimeType, file.getBytes(), contentHash, analysis));
        return document;
    }

    private Object arrivalOf(String caseId, String contentHash) {
        return arrivals.computeIfAbsent(caseId + "/" + contentHash, key -> new Object());
    }

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

    private List<Content> promptFor(MultipartFile file, String mimeType) throws IOException {
        // TODO — task 3, part 3. Send the file as itself.
        //
        // Return the List<Content> the model is sent. Two elements, in this order:
        //
        //   1. TextContent.from(INTAKE_INSTRUCTION)
        //   2. DocumentFiles.contentOf(file.getBytes(), mimeType)
        //
        // contentOf decides between PdfFileContent and ImageContent from the mime type. Nothing extracts
        // text first — the model is handed the document itself, which is the whole idea of the task.
        //
        // The text has to be exactly INTAKE_INSTRUCTION and nothing else.

        throw new TaskNotImplementedException(WorkshopTask.DOCUMENT_AGENT);
    }

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
            // Every photo taken on an iPhone since 2017. Browsers vary on whether they declare a
            // type for it, so the extension has to be enough — and the model reads it as it is,
            // which is the point: nothing here converts a file before the model sees it.
            case "heic", "heif" -> "image/heic";
            default -> throw new UnsupportedDocumentException(
                    "Only PDFs and images can be analysed. Received: " + declared);
        };
    }

    public static class UnsupportedDocumentException extends RuntimeException {
        UnsupportedDocumentException(String message) {
            super(message);
        }
    }

    public static class UnknownCaseException extends RuntimeException {
        UnknownCaseException(String message) {
            super(message);
        }
    }
}
