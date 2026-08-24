package com.example.aiworkshop.workshop;

import com.example.aiworkshop.tasks.task_1_first_agent.agent.ClaimTypeClassifier;
import com.example.aiworkshop.tasks.task_3_document_agent.DocumentIntake;
import com.example.aiworkshop.tasks.task_3_document_agent.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_2_guardrails.Guardrails;
import com.example.aiworkshop.tasks.task_6_advisor_chat.agent.ClaimChatAgent;
import com.example.aiworkshop.tasks.task_5_claim_summary.agent.ClaimSummarizer;
import com.example.aiworkshop.tasks.task_4_evaluation.GuardrailProbe;
import com.example.aiworkshop.tasks.task_4_evaluation.LabelledClaim;
import com.example.aiworkshop.tasks.task_7_create_claim_chat.agent.ClaimIntakeInterviewer;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * How far the workshop has got, worked out from the code rather than declared.
 *
 * <p>An agent is unwritten while its prompt is still the paragraph it shipped with. Everything else
 * is unwritten while it still throws {@link TaskNotImplementedException} when called, which is what
 * the probes below are for — they ask the code, and the code is the only thing that can be wrong
 * about this.
 */
@Component
public class TaskProgress {

    private final DocumentIntake intake;

    TaskProgress(DocumentIntake intake) {
        this.intake = intake;
    }

    public boolean isDone(WorkshopTask task) {
        return switch (task) {
            case FIRST_AGENT -> UnfinishedTasks.promptWritten(ClaimTypeClassifier.class);
            // Task 3 is two parts and neither is a prompt: the field descriptions on DocumentAnalysis,
            // and the content list intake sends. Both are checked without calling anything — probing the
            // second by running it would mean the progress bar paid for a model call to ask a question.
            case DOCUMENT_AGENT -> UnfinishedTasks.descriptionsWritten(DocumentAnalysis.class)
                    && UnfinishedTasks.written(() -> intake.promptFor(new byte[0], "image/png"));
            case ADVISOR_CHAT -> UnfinishedTasks.promptWritten(ClaimChatAgent.class);
            case CLAIM_SUMMARY -> UnfinishedTasks.promptWritten(ClaimSummarizer.class);
            case CREATE_CLAIM_CHAT -> UnfinishedTasks.promptWritten(ClaimIntakeInterviewer.class);
            // Task 4 has no code to gate: nothing on a screen waits on it, and there is no prompt to
            // write. It counts as done when both sets have rows of yours in them — the three that ship
            // with each are the worked examples, and adding your own is the exercise.
            case EVALUATION -> !LabelledClaim.yours().isEmpty() && !GuardrailProbe.yours().isEmpty();
            case GUARDRAILS -> UnfinishedTasks.written(() -> Guardrails.againstPromptInjection(null))
                    && UnfinishedTasks.written(() -> Guardrails.againstWastedCalls(null));
        };
    }
}
