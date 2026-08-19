package com.example.aiworkshop.tasks.task_1_first_agent;

import com.example.aiworkshop.cases.CaseDesk;
import com.example.aiworkshop.cases.model.CaseOverview;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseType;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CreatedCase;
import com.example.aiworkshop.workshop.TaskNotImplementedAdvice;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Opening a case and listing them: everything task 1 puts on the wire. */
@RestController
@RequestMapping("/api/cases")
class CasesController {
    private static final Logger log = LoggerFactory.getLogger(CasesController.class);

    private final CaseDesk desk;
    private final CaseIntake intake;

    CasesController(CaseDesk desk, CaseIntake intake) {
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

    @PostMapping("/documents/{documentId}/review")
    ResponseEntity<Void> review(@PathVariable String documentId) {
        log.info("Document {} reviewed by a case handler", documentId);
        desk.review(documentId);
        return ResponseEntity.noContent().build();
    }


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
