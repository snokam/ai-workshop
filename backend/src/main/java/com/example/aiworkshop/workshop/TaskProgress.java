package com.example.aiworkshop.workshop;

import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TaskProgress {
    private final Map<WorkshopTask, Boolean> done = new EnumMap<>(WorkshopTask.class);

    public TaskProgress() {
        done.put(WorkshopTask.FIRST_AGENT, com.example.aiworkshop.tasks.task_1_first_agent.CaseTypeClassifier.IMPLEMENTED);
        done.put(WorkshopTask.DOCUMENT_AGENT, com.example.aiworkshop.tasks.task_2_document_agent.DocumentAnalyzer.IMPLEMENTED);
        done.put(WorkshopTask.GUARDRAILS, com.example.aiworkshop.tasks.task_3_guardrails.Guardrails.IMPLEMENTED);
        done.put(WorkshopTask.POSTPROCESSING, com.example.aiworkshop.tasks.task_4_postprocessing.FraudScreener.IMPLEMENTED);
        done.put(WorkshopTask.CHAT, com.example.aiworkshop.tasks.task_5_chat.CaseChatAgent.IMPLEMENTED);
        done.put(WorkshopTask.SUMMARY, com.example.aiworkshop.tasks.task_6_summary.CaseSummarizer.IMPLEMENTED);
    }

    public boolean isDone(WorkshopTask task) {
        return done.getOrDefault(task, false);
    }
}
