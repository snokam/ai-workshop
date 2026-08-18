package com.example.aiworkshop.cases;

import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.workshop.TaskNotImplementedAdvice;
import com.example.aiworkshop.tasks.task_1_first_agent.CaseIntake;
import com.example.aiworkshop.cases.proposals.ProposalCard;
import com.example.aiworkshop.cases.model.CreatedCase;
import com.example.aiworkshop.cases.model.CaseType;
import com.example.aiworkshop.cases.model.CaseOverview;
import com.example.aiworkshop.cases.model.CaseDetail;
import com.example.aiworkshop.cases.chat.ChatAnswer;
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

    @GetMapping("/types")
    List<SupportedType> types() {
        return java.util.Arrays.stream(CaseType.values())
                .filter(type -> type != CaseType.OTHER)
                .map(type -> new SupportedType(type.label(), type.description()))
                .toList();
    }

    record SupportedType(String label, String description) {}

    @PostMapping
    ResponseEntity<CreatedCase> create(@RequestBody NewCaseRequest request) {
        CreatedCase created = intake.open(request.description());
        log.info("Opened case {} as '{}' ({})", created.reference(), created.typeLabel(), created.confidence());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    record NewCaseRequest(String description) {}

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

    record Question(String question) {}

    @ExceptionHandler({CaseDesk.UnknownCaseException.class, CaseDesk.UnknownProposalException.class})
    ResponseEntity<Map<String, String>> notFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(TaskNotImplementedException.class)
    ResponseEntity<Map<String, Object>> taskNotDone(TaskNotImplementedException e) {
        return TaskNotImplementedAdvice.response(e);
    }

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<Map<String, String>> agentFailed(RuntimeException e) {
        log.error("Case could not be read", e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("message", "The case could not be read: " + e.getMessage()));
    }
}
