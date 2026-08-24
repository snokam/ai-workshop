package com.example.aiworkshop.workshop;

import com.example.aiworkshop.tasks.task_1_first_agent.agent.CaseTypeClassifier;
import com.example.aiworkshop.tasks.task_3_document_agent.agent.DocumentAnalyzer;
import com.example.aiworkshop.tasks.task_2_guardrails.Guardrails;
import com.example.aiworkshop.tasks.task_6_advisor_chat.agent.CaseChatAgent;
import com.example.aiworkshop.tasks.task_5_claim_summary.agent.CaseSummarizer;
import com.example.aiworkshop.tasks.task_4_evaluation.GuardrailProbe;
import com.example.aiworkshop.tasks.task_4_evaluation.LabelledCase;
import com.example.aiworkshop.tasks.task_7_create_case_chat.agent.CaseIntakeInterviewer;
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
            case FIRST_AGENT -> UnfinishedTasks.promptWritten(CaseTypeClassifier.class);
            case DOCUMENT_AGENT -> UnfinishedTasks.promptWritten(DocumentAnalyzer.class);
            case ADVISOR_CHAT -> UnfinishedTasks.promptWritten(CaseChatAgent.class);
            case CLAIM_SUMMARY -> UnfinishedTasks.promptWritten(CaseSummarizer.class);
            case CREATE_CASE_CHAT -> UnfinishedTasks.promptWritten(CaseIntakeInterviewer.class);
            // Task 4 has no code to gate: nothing on a screen waits on it, and there is no prompt to
            // write. It counts as done when both sets have rows of yours in them — the three that ship
            // with each are the worked examples, and adding your own is the exercise.
            case EVALUATION -> !LabelledCase.yours().isEmpty() && !GuardrailProbe.yours().isEmpty();
            case GUARDRAILS -> UnfinishedTasks.written(() -> Guardrails.againstPromptInjection(null))
                    && UnfinishedTasks.written(() -> Guardrails.againstWastedCalls(null));
        };
    }
}
