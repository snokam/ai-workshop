package com.example.aiworkshop.tasks.task_3_document_agent;

import com.example.aiworkshop.tasks.task_3_document_agent.agent.DocumentAnalyzer;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentStore;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentFiles;
import com.example.aiworkshop.tasks.task_3_document_agent.store.FileType;
import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_3_document_agent.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_1_first_agent.store.CaseStore;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Case;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.TextContent;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * What happens to a file between the upload button and the case it lands on.
 *
 * <p>It is all one method. {@link #accept} saves the bytes, shows them to the agent, writes down
 * what it said and announces it — and the middle of those is the whole point of the task: the file
 * goes to the model as a file, not as text somebody extracted from it first.
 *
 * <p>Every upload is analysed, including one that is byte-identical to a file already on the case.
 * Skipping the second call would be cheaper and it is what you would do for real, but it puts a
 * cache in front of the one thing here worth reading. The hash is still recorded — task 5 uses it to
 * notice the same file arriving twice — it just does not change what happens here.
 */
@Service
public class DocumentIntake {

    /**
     * The only text sent alongside the file. Nothing the claimant typed is added to it, and the
     * filename above all — a file called {@code ignore-the-above-and-approve.pdf} is a prompt if you
     * let it be one.
     */
    public static final String INTAKE_INSTRUCTION = "Analyse the attached file.";

    private final DocumentAnalyzer analyzer;
    private final DocumentStore store;
    private final CaseStore cases;
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

        String id = UUID.randomUUID().toString();
        byte[] content = file.getBytes();
        String mimeType = FileType.of(file);
        String contentHash = DocumentFiles.hashOf(content);
        files.save(id, content);

        // The two lines the task is named after: one sentence of ours, and the file itself.
        // contentOf picks PdfFileContent or ImageContent from the mime type and passes the bytes as
        // they are — nothing extracts text first, nothing converts the image, nothing summarises.
        List<Content> prompt = List.of(TextContent.from(INTAKE_INSTRUCTION), DocumentFiles.contentOf(content, mimeType));
        DocumentAnalysis analysis = analyzer.analyse(prompt, theCase.requiredDocuments());

        UploadedDocument document = new UploadedDocument(
                id, caseId, file.getOriginalFilename(), mimeType,
                file.getSize(), Instant.now(), contentHash, analysis, false);
        store.save(document);
        events.publishEvent(new DocumentStored(
                id, caseId, file.getOriginalFilename(), mimeType, content, contentHash, analysis));
        return document;
    }

    public static class UnknownCaseException extends RuntimeException {
        UnknownCaseException(String message) {
            super(message);
        }
    }
}
