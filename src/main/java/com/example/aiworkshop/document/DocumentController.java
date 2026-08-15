package com.example.aiworkshop.document;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** The API the frontend talks to. Three endpoints, no more than the upload screen needs. */
@RestController
@RequestMapping("/api/documents")
class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentIntake intake;
    private final DocumentStore store;

    DocumentController(DocumentIntake intake, DocumentStore store) {
        this.intake = intake;
        this.store = store;
    }

    /**
     * Blocks for as long as the model takes — several seconds for a large scan. Acceptable while the
     * screen shows one upload at a time; the moment the agent becomes a chain of steps, this wants to
     * stream them back instead of going quiet until the last one finishes.
     */
    @PostMapping
    ResponseEntity<UploadedDocument> upload(@RequestParam("file") MultipartFile file) throws IOException {
        UploadedDocument document = intake.accept(file);
        log.info(
                "Analysed {} ({}): category={}, quality={}",
                document.filename(),
                document.contentType(),
                document.analysis().category(),
                document.analysis().quality().verdict());
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }

    @GetMapping
    List<UploadedDocument> list() {
        return store.findAll();
    }

    @GetMapping("/{id}")
    ResponseEntity<UploadedDocument> byId(@PathVariable String id) {
        return store.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(DocumentIntake.UnsupportedDocumentException.class)
    ResponseEntity<Map<String, String>> unsupported(DocumentIntake.UnsupportedDocumentException e) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(Map.of("message", e.getMessage()));
    }

    /**
     * A failed model call is the most likely error on workshop day — a missing API key, a deployment
     * that rejects PDFs, a reply the parser could not read. Surface the real message rather than a
     * bare 500, so the screen can show what actually went wrong.
     */
    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<Map<String, String>> analysisFailed(RuntimeException e) {
        log.error("Analysis failed", e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("message", "The document could not be analysed: " + e.getMessage()));
    }
}
