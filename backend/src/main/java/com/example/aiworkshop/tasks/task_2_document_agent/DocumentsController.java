package com.example.aiworkshop.tasks.task_2_document_agent;

import com.example.aiworkshop.tasks.task_2_document_agent.DocumentIntake;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.workshop.TaskNotImplementedAdvice;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.DocumentForClaimant;
import com.example.aiworkshop.tasks.task_2_document_agent.store.DocumentStore;
import com.example.aiworkshop.tasks.task_2_document_agent.store.DocumentFiles;
import com.example.aiworkshop.documents.model.UploadedDocument;
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

@RestController
@RequestMapping("/api/documents")
class DocumentsController {
    private static final Logger log = LoggerFactory.getLogger(DocumentsController.class);

    private final DocumentIntake intake;
    private final DocumentStore store;
    private final DocumentFiles files;

    DocumentsController(DocumentIntake intake, DocumentStore store, DocumentFiles files) {
        this.intake = intake;
        this.store = store;
        this.files = files;
    }

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

    @GetMapping("/{id}/file")
    ResponseEntity<byte[]> file(@PathVariable String id) {
        return store.findById(id)
                .map(document -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(document.contentType()))
                        .body(files.read(id)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

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
