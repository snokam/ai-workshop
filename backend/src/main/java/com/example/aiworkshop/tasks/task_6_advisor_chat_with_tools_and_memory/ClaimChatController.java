package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory;

import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.proposals.Proposal;
import com.example.aiworkshop.tasks.task_1_first_agent.ClaimDesk;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.model.ChatAnswer;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.model.ClaimDetail;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.proposals.DocumentRequest;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.proposals.ProposalCard;
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
 * One claim as the handler reads it, and the conversation about it.
 *
 * <p>The detail endpoint is here rather than with task 1 because it is the only place that needs
 * all three: the claim from task 1, the summary from task 5, the conversation from task 6. A
 * composition belongs with the last thing it composes.
 */
@RestController
@RequestMapping("/api/claims")
class ClaimChatController {
    private static final Logger log = LoggerFactory.getLogger(ClaimChatController.class);

    private final ClaimFile file;
    private final ChatDesk chat;

    ClaimChatController(ClaimFile file, ChatDesk chat) {
        this.file = file;
        this.chat = chat;
    }

    @GetMapping("/{id}")
    ClaimDetail open(@PathVariable String id) {
        return file.open(id, chat.proposalsOn(id), chat.turnsOn(id));
    }

    /**
     * What the claim handler has asked the claimant for.
     *
     * <p>Its own endpoint, because the claimant's page wants these and nothing else on it. Reading
     * the whole claim would summarise it, and paying an agent to write a summary nobody on that page
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
                "Claim chat on {}: {} tool call(s), {} proposal(s) raised",
                id,
                answered.turn().toolCalls().size(),
                answered.turn().proposalIds().size());
        return answered;
    }

    @PostMapping("/proposals/{proposalId}/confirm")
    ProposalCard confirm(@PathVariable String proposalId) {
        log.info("Proposal {} confirmed by a claim handler", proposalId);
        return chat.confirm(proposalId);
    }

    @PostMapping("/proposals/{proposalId}/decline")
    ProposalCard decline(@PathVariable String proposalId) {
        log.info("Proposal {} declined by a claim handler", proposalId);
        return chat.decline(proposalId);
    }

    record Question(String question) {}

    @ExceptionHandler({ClaimDesk.UnknownClaimException.class, ClaimDesk.UnknownProposalException.class})
    ResponseEntity<Map<String, String>> notFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
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
