package com.example.aiworkshop.workshop;

import com.example.aiworkshop.tasks.task_1_first_agent.agent.ClaimTypeClassifier;
import com.example.aiworkshop.tasks.task_3_document_agent.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_2_guardrails.Guardrails;
import com.example.aiworkshop.tasks.task_6_advisor_chat.agent.ClaimChatTools;
import com.example.aiworkshop.tasks.task_5_claim_summary.agent.ClaimSummarizer;
import com.example.aiworkshop.tasks.task_5_claim_summary.evaluation.SummaryRubric;
import com.example.aiworkshop.tasks.task_4_evaluation.GuardrailProbe;
import com.example.aiworkshop.tasks.task_4_evaluation.LabelledClaim;
import com.example.aiworkshop.tasks.task_7_create_claim_chat.InterviewBudget;
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


    public boolean isDone(WorkshopTask task) {
        return switch (task) {
            case FIRST_AGENT -> UnfinishedTasks.promptWritten(ClaimTypeClassifier.class);
            // Task 3's first part is readable without running anything. Its second is two lines in the
            // middle of DocumentIntake.accept, and the only way to ask whether they are written is to
            // run them — which would mean the progress bar paying for a model call to draw a screen. So
            // this gate is part 1, and part 2 announces itself: intake throws until it is written, and
            // the upload comes back as the 501 the screen already knows how to show.
            case DOCUMENT_AGENT -> UnfinishedTasks.descriptionsWritten(DocumentAnalysis.class);
            // Task 6's prompt is given; what is written is the tool descriptions and the one builder
            // call that hands them over. Only the first can be read without building the agent, and an
            // agent wired without tools is not broken — it answers, it just never looks anything up,
            // which is the point of part 2 and something you see rather than something a gate reports.
            case ADVISOR_CHAT -> UnfinishedTasks.toolDescriptionsWritten(ClaimChatTools.class);
            case CLAIM_SUMMARY -> UnfinishedTasks.promptWritten(ClaimSummarizer.class)
                    && !SummaryRubric.yours().isEmpty();
            case CREATE_CLAIM_CHAT -> UnfinishedTasks.promptWritten(ClaimIntakeInterviewer.class)
                    && UnfinishedTasks.written(() -> InterviewBudget.withinBudget("", 0));
            // Task 4 has no code to gate: nothing on a screen waits on it, and there is no prompt to
            // write. It counts as done when both sets have rows of yours in them — the three that ship
            // with each are the worked examples, and adding your own is the exercise.
            case EVALUATION -> !LabelledClaim.yours().isEmpty() && !GuardrailProbe.yours().isEmpty();
            case GUARDRAILS -> UnfinishedTasks.written(() -> Guardrails.againstPromptInjection(null))
                    && UnfinishedTasks.written(() -> Guardrails.againstWastedCalls(null));
        };
    }
}
