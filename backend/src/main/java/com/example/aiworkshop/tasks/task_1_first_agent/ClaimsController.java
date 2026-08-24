package com.example.aiworkshop.tasks.task_1_first_agent;

import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimOverview;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimType;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CreatedClaim;
import com.example.aiworkshop.workshop.TaskNotImplementedAdvice;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
import dev.langchain4j.guardrail.InputGuardrailException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Opening a claim and listing them: everything task 1 puts on the wire. */
@RestController
@RequestMapping("/api/claims")
class ClaimsController {
    private static final Logger log = LoggerFactory.getLogger(ClaimsController.class);

    private final ClaimDesk desk;
    private final ClaimIntake intake;

    ClaimsController(ClaimDesk desk, ClaimIntake intake) {
        this.desk = desk;
        this.intake = intake;
    }

    @GetMapping
    List<ClaimOverview> list() {
        return desk.list();
    }

    @GetMapping("/types")
    List<SupportedType> types() {
        return java.util.Arrays.stream(ClaimType.values())
                .map(type -> new SupportedType(type.label(), type.description()))
                .toList();
    }

    record SupportedType(String label, String description) {}

    @PostMapping
    ResponseEntity<CreatedClaim> create(@RequestBody NewCaseRequest request) {
        CreatedClaim created = intake.open(request.description());
        log.info("Opened claim {} as '{}' ({})", created.reference(), created.typeLabel(), created.confidence());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    record NewCaseRequest(String description) {}

    @ExceptionHandler({ClaimDesk.UnknownClaimException.class, ClaimDesk.UnknownProposalException.class})
    ResponseEntity<Map<String, String>> notFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    /**
     * A guardrail said no, and what it said is for the person who typed the text.
     *
     * <p>400 rather than 502: nothing failed. The request was refused, deliberately, before anything
     * was spent on it, and the reason is written to be read by a claimant. Without this the refusal
     * fell to the catch-all below and arrived as "The claim could not be read:" followed by the
     * guardrail's internal class name.
     */
    @ExceptionHandler(InputGuardrailException.class)
    ResponseEntity<Map<String, String>> refused(InputGuardrailException e) {
        String reason = e.getMessage() == null ? "" : e.getMessage();
        int said = reason.indexOf("failed with this message: ");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "message",
                        said < 0 ? reason : reason.substring(said + "failed with this message: ".length())));
    }

    /**
     * We read it, and it is not something this insurer covers.
     *
     * <p>422 rather than 502: nothing failed. The agent did its job and the answer was no, and the
     * sentence it wrote is the reason, addressed to the person who typed it. Telling them costs a
     * moment; opening a claim that somebody closes in silence costs them the chance to take it
     * somewhere that can help.
     */
    @ExceptionHandler(ClaimIntake.NothingWeCoverException.class)
    ResponseEntity<Map<String, String>> nothingWeCover(ClaimIntake.NothingWeCoverException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(TaskNotImplementedException.class)
    ResponseEntity<Map<String, Object>> taskNotDone(TaskNotImplementedException e) {
        return TaskNotImplementedAdvice.response(e);
    }

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<Map<String, String>> agentFailed(RuntimeException e) {
        log.error("Claim could not be read", e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("message", "The claim could not be read: " + e.getMessage()));
    }
}
