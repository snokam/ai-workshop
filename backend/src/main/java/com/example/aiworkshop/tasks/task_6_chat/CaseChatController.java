package com.example.aiworkshop.tasks.task_6_chat;

import com.example.aiworkshop.tasks.task_1_first_agent.model.Case;
import com.example.aiworkshop.tasks.task_6_chat.proposals.Proposal;
import com.example.aiworkshop.tasks.task_1_first_agent.CaseDesk;
import com.example.aiworkshop.tasks.task_6_chat.model.ChatAnswer;
import com.example.aiworkshop.tasks.task_6_chat.model.CaseDetail;
import com.example.aiworkshop.tasks.task_6_chat.proposals.DocumentRequest;
import com.example.aiworkshop.tasks.task_6_chat.proposals.ProposalCard;
import com.example.aiworkshop.workshop.TaskNotImplementedAdvice;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * One case as the handler reads it, and the conversation about it.
 *
 * <p>The detail endpoint is here rather than with task 1 because it is the only place that needs
 * all three: the case from task 1, the summary from task 5, the conversation from task 6. A
 * composition belongs with the last thing it composes.
 */
@RestController
@RequestMapping("/api/cases")
class CaseChatController {
    private static final Logger log = LoggerFactory.getLogger(CaseChatController.class);

    private final CaseFile file;
    private final ChatDesk chat;

    CaseChatController(CaseFile file, ChatDesk chat) {
        this.file = file;
        this.chat = chat;
    }

    @GetMapping("/{id}")
    CaseDetail open(@PathVariable String id) {
        return file.open(id, chat.proposalsOn(id), chat.turnsOn(id));
    }

    /**
     * What the case handler has asked the claimant for.
     *
     * <p>Its own endpoint, because the claimant's page wants these and nothing else on it. Reading
     * the whole case would summarise it, and paying an agent to write a summary nobody on that page
     * reads is the sort of cost that only shows up on the bill.
     */
    @GetMapping("/{id}/document-requests")
    List<DocumentRequest> documentRequests(@PathVariable String id) {
        return chat.requestsOn(id);
    }

    @PostMapping("/{id}/chat")
    ChatAnswer chat(@PathVariable String id, @RequestBody Question question) {
        ChatAnswer answered = chat.chat(id, question.question());
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
        return chat.confirm(proposalId);
    }

    @PostMapping("/proposals/{proposalId}/decline")
    ProposalCard decline(@PathVariable String proposalId) {
        log.info("Proposal {} declined by a case handler", proposalId);
        return chat.decline(proposalId);
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
