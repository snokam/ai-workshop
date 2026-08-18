package com.example.aiworkshop.workshop;

import com.example.aiworkshop.tasks.task_1_first_agent.CaseTypeClassifier;
import com.example.aiworkshop.tasks.task_2_document_agent.DocumentAnalyzer;
import com.example.aiworkshop.tasks.task_3_guardrails.Guardrails;
import com.example.aiworkshop.tasks.task_4_postprocessing.checks.FraudCheck;
import com.example.aiworkshop.tasks.task_5_chat.CaseChatAgent;
import com.example.aiworkshop.tasks.task_6_summary.CaseSummarizer;
import com.example.aiworkshop.tasks.task_7_create_case_chat.CaseIntakeInterviewer;
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

    private final List<FraudCheck> checks;

    TaskProgress(List<FraudCheck> checks) {
        this.checks = checks;
    }

    public boolean isDone(WorkshopTask task) {
        return switch (task) {
            case FIRST_AGENT -> UnfinishedTasks.promptWritten(CaseTypeClassifier.class);
            case DOCUMENT_AGENT -> UnfinishedTasks.promptWritten(DocumentAnalyzer.class);
            case CHAT -> UnfinishedTasks.promptWritten(CaseChatAgent.class);
            case SUMMARY -> UnfinishedTasks.promptWritten(CaseSummarizer.class);
            case CREATE_CASE_CHAT -> UnfinishedTasks.promptWritten(CaseIntakeInterviewer.class);
            case GUARDRAILS -> UnfinishedTasks.written(Guardrails::beforeTheCall);
            case POSTPROCESSING -> checks.stream().anyMatch(check -> UnfinishedTasks.written(() -> check.screen(null)));
        };
    }
}
