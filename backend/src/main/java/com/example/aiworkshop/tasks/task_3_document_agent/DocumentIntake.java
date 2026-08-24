package com.example.aiworkshop.tasks.task_3_document_agent;

import com.example.aiworkshop.workshop.WorkshopTask;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
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
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * What happens to a file between the upload button and the case it lands on.
 *
 * <p>Everything that decides anything is in {@link #accept}, in order: save the bytes, show them to
 * the agent, write down what it said, announce it. The two questions that are not about this task —
 * what kind of file is this, and what are its bytes worth as an identity — are answered in {@code
 * store/FileType} and {@code DocumentFiles.hashOf} so they stay out of the way.
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

        // The same bytes on the same case were read once already, and that answer still holds.
        DocumentAnalysis analysis = analysisAlreadyOnCase(caseId, contentHash)
                .orElseGet(() -> analyzer.analyse(promptFor(content, mimeType), theCase.requiredDocuments()));

        UploadedDocument document = new UploadedDocument(
                id, caseId, file.getOriginalFilename(), mimeType,
                file.getSize(), Instant.now(), contentHash, analysis, false);
        store.save(document);
        events.publishEvent(new DocumentStored(
                id, caseId, file.getOriginalFilename(), mimeType, content, contentHash, analysis));
        return document;
    }

    private List<Content> promptFor(byte[] content, String mimeType) {
        // TODO — task 3, part 3. Send the file as itself.
        //
        // Return the List<Content> the model is sent. Two elements, in this order:
        //
        //   1. TextContent.from(INTAKE_INSTRUCTION)
        //   2. DocumentFiles.contentOf(content, mimeType)
        //
        // contentOf decides between PdfFileContent and ImageContent from the mime type. Nothing extracts
        // text first — the model is handed the document itself, which is the whole idea of the task.
        //
        // The text has to be exactly INTAKE_INSTRUCTION and nothing else.

        throw new TaskNotImplementedException(WorkshopTask.DOCUMENT_AGENT);
    }

    private Optional<DocumentAnalysis> analysisAlreadyOnCase(String caseId, String contentHash) {
        return store.findByCaseId(caseId).stream()
                .filter(document -> contentHash.equals(document.contentHash()))
                .map(UploadedDocument::analysis)
                .findFirst();
    }

    public static class UnknownCaseException extends RuntimeException {
        UnknownCaseException(String message) {
            super(message);
        }
    }
}
