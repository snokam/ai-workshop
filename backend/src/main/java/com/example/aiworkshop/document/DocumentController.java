package com.example.aiworkshop.document;

import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.workshop.TaskNotImplementedAdvice;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.DocumentForClaimant;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** The API the upload screen talks to. No more than a Claimant needs, plus the file itself. */
@RestController
@RequestMapping("/api/documents")
class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentIntake intake;
    private final DocumentStore store;
    private final DocumentFiles files;

    DocumentController(DocumentIntake intake, DocumentStore store, DocumentFiles files) {
        this.intake = intake;
        this.store = store;
        this.files = files;
    }

    /**
     * Blocks for as long as the model takes — several seconds for a large scan. Acceptable while the
     * screen shows one upload at a time; the moment the agent becomes a chain of steps, this wants to
     * stream them back instead of going quiet until the last one finishes.
     */
    @PostMapping
    ResponseEntity<DocumentForClaimant> upload(
            @RequestParam("caseId") String caseId, @RequestParam("file") MultipartFile file) throws IOException {
        UploadedDocument document = intake.accept(caseId, file);
        log.info(
                "Analysed {} ({}) for case {}: category={}, matched={}, quality={}",
                document.filename(),
                document.contentType(),
                document.caseId(),
                document.analysis().category(),
                document.analysis().matchedRequiredDocument(),
                document.analysis().quality().verdict());
        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentForClaimant.of(document));
    }

    @GetMapping
    List<DocumentForClaimant> list() {
        return DocumentForClaimant.of(store.findAll());
    }

    /**
     * The file itself, as it arrived.
     *
     * <p>The screens used to preview an upload from the {@code File} the browser still had in hand,
     * which meant the preview lasted exactly as long as the tab did: reload the page and the document
     * you had just sent was a filename and nothing else. The bytes are kept now (ADR 0004), so the
     * preview can come from where the file actually is — which also gives the Case Handler's screen
     * one, having never had one at all.
     */
    @GetMapping("/{id}/file")
    ResponseEntity<byte[]> file(@PathVariable String id) {
        return store.findById(id)
                .map(document -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(document.contentType()))
                        .body(files.read(id)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** The directory is emptied on startup, so a Document can outlive its file. Not an error worth a 500. */
    @ExceptionHandler(DocumentFiles.MissingFileException.class)
    ResponseEntity<Void> missingFile(DocumentFiles.MissingFileException e) {
        log.info("{}", e.getMessage());
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    ResponseEntity<DocumentForClaimant> byId(@PathVariable String id) {
        return store.findById(id)
                .map(DocumentForClaimant::of)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(DocumentIntake.UnsupportedDocumentException.class)
    ResponseEntity<Map<String, String>> unsupported(DocumentIntake.UnsupportedDocumentException e) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(DocumentIntake.UnknownCaseException.class)
    ResponseEntity<Map<String, String>> unknownCase(DocumentIntake.UnknownCaseException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    /**
     * A failed model call is the most likely error on workshop day — a missing API key, a deployment
     * that rejects PDFs, a reply the parser could not read. Surface the real message rather than a
     * bare 500, so the screen can show what actually went wrong.
     */
    /** An unfinished task is not a failure: it is the workshop, so it says what to open. */
    @ExceptionHandler(TaskNotImplementedException.class)
    ResponseEntity<Map<String, Object>> taskNotDone(TaskNotImplementedException e) {
        return TaskNotImplementedAdvice.response(e);
    }

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<Map<String, String>> analysisFailed(RuntimeException e) {
        log.error("Analysis failed", e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("message", "The document could not be analysed: " + e.getMessage()));
    }
}
