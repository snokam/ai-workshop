package com.example.aiworkshop.tasks.task_7_create_case_chat;

import com.example.aiworkshop.tasks.task_7_create_case_chat.model.CaseScenario;
import com.example.aiworkshop.tasks.task_7_create_case_chat.model.InterviewTurn;
import com.example.aiworkshop.tasks.task_1_first_agent.CaseIntake;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CreatedCase;
import com.example.aiworkshop.tasks.task_7_create_case_chat.agent.CaseIntakeInterviewer;
import com.example.aiworkshop.workshop.TaskNotImplementedAdvice;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The conversational intake endpoint (task 7), kept beside task 1's {@code CaseController} rather
 * than in the task folder — the endpoint is plumbing, only the agent it calls is the exercise.
 * Stateless: the client sends the running transcript each turn, the agent reads it and either asks
 * more or decides, and only on a decision is a Case actually opened.
 *
 * <p>The path sits under {@code /api/cases} but is a distinct POST, so it lives alongside the one-shot
 * {@code POST /api/cases} of task 1 without touching it.
 */
@RestController
@RequestMapping("/api/cases/interview")
class CaseInterviewController {

    private static final Logger log = LoggerFactory.getLogger(CaseInterviewController.class);

    private final CaseIntakeInterviewer interviewer;
    private final InterviewCaseOpener opener;

    CaseInterviewController(CaseIntakeInterviewer interviewer, InterviewCaseOpener opener) {
        this.interviewer = interviewer;
        this.opener = opener;
    }

    @PostMapping
    InterviewResponse next(@RequestBody InterviewRequest request) {
        InterviewTurn turn = interviewer.next(CaseScenario.catalog(), transcriptOf(request));

        if (turn.decision() == InterviewTurn.Decision.NEEDS_INFO) {
            List<String> questions = turn.questions() == null ? List.of() : turn.questions();
            return InterviewResponse.needsInfo(questions, turn.rationale());
        }

        if (turn.scenario() == null) {
            throw new CaseIntake.NothingWeCoverException(turn.rationale());
        }
        CaseScenario scenario = turn.scenario();
        CreatedCase created = opener.open(scenario, turn.confidence(), turn.rationale());
        log.info("Interview opened case {} as '{}' ({})", created.reference(), created.typeLabel(), created.confidence());
        return InterviewResponse.decided(created);
    }

    /** Flattens the description and any question/answer pairs into the plain transcript the agent reads. */
    private static String transcriptOf(InterviewRequest request) {
        StringBuilder transcript = new StringBuilder("Description: ").append(request.description());
        if (request.answers() != null) {
            for (InterviewRequest.Answer answer : request.answers()) {
                transcript
                        .append("\n\nQ: ")
                        .append(answer.question())
                        .append("\nA: ")
                        .append(answer.answer());
            }
        }
        return transcript.toString();
    }

    /** What the AI-chat screen posts: the first description, plus every question already answered. */
    record InterviewRequest(String description, List<Answer> answers) {
        record Answer(String question, String answer) {}
    }

    /**
     * Either the next questions to ask, or the case that was opened. {@code status} tells the two
     * apart so the screen can keep the conversation going or move on to uploading.
     */
    record InterviewResponse(String status, List<String> questions, String rationale, CreatedCase createdCase) {

        static InterviewResponse needsInfo(List<String> questions, String rationale) {
            return new InterviewResponse("NEEDS_INFO", questions, rationale, null);
        }

        static InterviewResponse decided(CreatedCase createdCase) {
            return new InterviewResponse("DECIDED", List.of(), null, createdCase);
        }
    }

    /** Until task 7 is written the agent is a stub; surface that as the 501 the screen knows how to show. */
    /**
     * We read it, and it is not something this insurer covers.
     *
     * <p>422 rather than 502: nothing failed. The agent did its job and the answer was no, and the
     * sentence it wrote is the reason, addressed to the person who typed it. Telling them costs a
     * moment; opening a case that somebody closes in silence costs them the chance to take it
     * somewhere that can help.
     */
    @ExceptionHandler(CaseIntake.NothingWeCoverException.class)
    ResponseEntity<Map<String, String>> nothingWeCover(CaseIntake.NothingWeCoverException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(TaskNotImplementedException.class)
    ResponseEntity<Map<String, Object>> taskNotDone(TaskNotImplementedException e) {
        return TaskNotImplementedAdvice.response(e);
    }

    /** Running the interview agent can fail like any model call — surface the real cause to the screen. */
    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<Map<String, String>> agentFailed(RuntimeException e) {
        log.error("Interview could not continue", e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("message", "The chat could not continue: " + e.getMessage()));
    }
}
