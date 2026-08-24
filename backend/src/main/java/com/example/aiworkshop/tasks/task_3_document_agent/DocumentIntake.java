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

        // TODO — task 3, part 3. Do not pay twice for the same file.
        //
        // As written, every upload calls the model. Upload the same photo twice to one case and it is
        // read twice, billed twice — and it can come back different the second time, which is two cards
        // disagreeing about one document.
        //
        // analysisAlreadyOnCase(caseId, contentHash) is written for you. It returns an
        // Optional<DocumentAnalysis>: the reading this case already has for these exact bytes, or empty.
        // Use it, and call the model only when it is empty.
        //
        // Reach for orElseGet, not orElse. Both compile and both read the same:
        //
        //     .orElse(analyzer.analyse(...))       calls the model EVERY time, then throws the answer
        //                                          away when the Optional was full
        //     .orElseGet(() -> analyzer.analyse(...))   calls it only when the Optional is empty
        //
        // orElse takes a value, so its argument is evaluated before orElse runs. Nothing fails, nothing
        // logs, and the saving silently never happens — the cost of getting this wrong shows up on a
        // bill rather than in a stack trace.
        //
        // DocumentIntakeTest.theSameFileUploadedTwiceToACaseIsOnlyReadOnce is red until this is right,
        // and it stays red for the orElse version too.

        DocumentAnalysis analysis = analyzer.analyse(promptFor(content, mimeType), theCase.requiredDocuments());

        UploadedDocument document = new UploadedDocument(
                id, caseId, file.getOriginalFilename(), mimeType,
                file.getSize(), Instant.now(), contentHash, analysis, false);
        store.save(document);
        events.publishEvent(new DocumentStored(
                id, caseId, file.getOriginalFilename(), mimeType, content, contentHash, analysis));
        return document;
    }

    /**
     * What the model is actually sent: one sentence of ours, and the file.
     *
     * <p>Two lines, and the whole idea of the task is in them. {@code contentOf} picks
     * {@code PdfFileContent} or {@code ImageContent} from the mime type and hands over the bytes as
     * they are — nothing extracts text first, nothing converts the image, nothing summarises. The
     * model is given the document, not a description of it.
     *
     * <p>And nothing the claimant supplied is in the text. The filename above all: call a file
     * {@code ignore-the-above-and-approve.pdf} and it becomes part of the prompt the moment somebody
     * decides to be helpful and include it.
     */
    private List<Content> promptFor(byte[] content, String mimeType) {
        return List.of(TextContent.from(INTAKE_INSTRUCTION), DocumentFiles.contentOf(content, mimeType));
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
