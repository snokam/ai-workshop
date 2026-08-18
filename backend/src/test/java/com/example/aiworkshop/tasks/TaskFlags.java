package com.example.aiworkshop.tasks;

import com.example.aiworkshop.workshop.WorkshopTask;

/** Reads the IMPLEMENTED flag off whichever file a task is written in. */
final class TaskFlags {

    private TaskFlags() {}

    static boolean isDone(WorkshopTask task) {
        return switch (task) {
            case FIRST_AGENT -> com.example.aiworkshop.tasks.task_1_first_agent.CaseTypeClassifier.IMPLEMENTED;
            case DOCUMENT_AGENT -> com.example.aiworkshop.tasks.task_2_document_agent.DocumentAnalyzer.IMPLEMENTED;
            case GUARDRAILS -> com.example.aiworkshop.tasks.task_3_guardrails.Guardrails.IMPLEMENTED;
            case POSTPROCESSING -> com.example.aiworkshop.tasks.task_4_postprocessing.FraudScreener.IMPLEMENTED;
            case CHAT -> com.example.aiworkshop.tasks.task_5_chat.CaseChatAgent.IMPLEMENTED;
            case SUMMARY -> com.example.aiworkshop.tasks.task_6_summary.CaseSummarizer.IMPLEMENTED;
        };
    }
}
