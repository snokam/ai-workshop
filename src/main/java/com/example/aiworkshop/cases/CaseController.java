package com.example.aiworkshop.cases;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Case Handler's side of the API, plus the Case list the upload screen needs to let a Claimant
 * pick where their file is going.
 *
 * <p>The split between the two GETs is the point: {@code GET /api/cases} is a cheap lookup a screen
 * can poll, {@code GET /api/cases/{id}} costs two model calls. Only the second one blocks.
 */
@RestController
@RequestMapping("/api/cases")
class CaseController {

    private static final Logger log = LoggerFactory.getLogger(CaseController.class);

    private final CaseDesk desk;
    private final CaseIntake intake;

    CaseController(CaseDesk desk, CaseIntake intake) {
        this.desk = desk;
        this.intake = intake;
    }

    @GetMapping
    List<CaseOverview> list() {
        return desk.list();
    }

    /**
     * The kinds of insurance a Claimant can open a Case for, for the front page to show up front. Read
     * straight off {@link CaseType} so the page never drifts from what the classifier can actually
     * pick; {@link CaseType#OTHER} is left out because it is the fallback, not a product.
     */
    @GetMapping("/types")
    List<SupportedType> types() {
        return java.util.Arrays.stream(CaseType.values())
                .filter(type -> type != CaseType.OTHER)
                .map(type -> new SupportedType(type.label(), type.description()))
                .toList();
    }

    /** One supported insurance type as the front page shows it. */
    record SupportedType(String label, String description) {}

    /**
     * Opens a Case from what the Claimant typed. Blocks for the one classifier call, then returns the
     * Case that was created — the same failure mode as the other model-backed endpoints, so the
     * {@code RuntimeException} handler below surfaces the real cause.
     */
    @PostMapping
    ResponseEntity<CreatedCase> create(@RequestBody NewCaseRequest request) {
        CreatedCase created = intake.open(request.description());
        log.info("Opened case {} as '{}' ({})", created.reference(), created.typeLabel(), created.confidence());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** What the claimant screen posts: the free-text description of what they need help with. */
    record NewCaseRequest(String description) {}

    /** Blocks for both model calls. Same caveat as uploading: this is where streaming would go. */
    @GetMapping("/{id}")
    CaseDetail open(@PathVariable String id) {
        return desk.open(id);
    }

    @PostMapping("/documents/{documentId}/review")
    ResponseEntity<Void> review(@PathVariable String documentId) {
        log.info("Document {} reviewed by a case handler", documentId);
        desk.review(documentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * One turn of the Case Chat. Blocks for one model call plus whatever the agent's tools cost it,
     * which is the one endpoint here that can run several.
     */
    @PostMapping("/{id}/chat")
    ChatAnswer chat(@PathVariable String id, @RequestBody Question question) {
        ChatAnswer answered = desk.chat(id, question.question());
        log.info(
                "Case chat on {}: {} tool call(s), {} proposal(s) raised",
                id,
                answered.turn().toolCalls().size(),
                answered.turn().proposalIds().size());
        return answered;
    }

    /** The click that turns a Proposal into a write. The only path by which one ever does. */
    @PostMapping("/proposals/{proposalId}/confirm")
    ProposalCard confirm(@PathVariable String proposalId) {
        log.info("Proposal {} confirmed by a case handler", proposalId);
        return desk.confirm(proposalId);
    }

    @PostMapping("/proposals/{proposalId}/decline")
    ProposalCard decline(@PathVariable String proposalId) {
        log.info("Proposal {} declined by a case handler", proposalId);
        return desk.decline(proposalId);
    }

    /** What a Case Handler types. A record rather than a raw string so the JSON has a name in it. */
    record Question(String question) {}

    @ExceptionHandler({CaseDesk.UnknownCaseException.class, CaseDesk.UnknownProposalException.class})
    ResponseEntity<Map<String, String>> notFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    /**
     * Opening a Case runs two agents and a chat turn runs at least one more, so the same failure
     * mode as upload applies — show the real cause rather than leaving a blank panel on screen.
     */
    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<Map<String, String>> agentFailed(RuntimeException e) {
        log.error("Case could not be read", e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("message", "The case could not be read: " + e.getMessage()));
    }
}
