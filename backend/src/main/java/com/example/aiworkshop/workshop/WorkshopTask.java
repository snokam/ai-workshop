package com.example.aiworkshop.workshop;

import com.example.aiworkshop.tasks.task_1_first_agent.CaseIntake;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Case;
import com.example.aiworkshop.tasks.task_1_first_agent.agent.CaseTypeClassifier;
import com.example.aiworkshop.tasks.task_1_first_agent.agent.VertexAiConfig;
import com.example.aiworkshop.tasks.task_3_document_agent.agent.DocumentAnalyzer;
import com.example.aiworkshop.tasks.task_2_guardrails.guardrails.Guardrails;
import com.example.aiworkshop.tasks.task_5_fraud_detection.FraudScreener;
import com.example.aiworkshop.tasks.task_6_case_summary.agent.CaseSummarizer;
import com.example.aiworkshop.tasks.task_7_advisor_chat.agent.CaseChatAgent;
import com.example.aiworkshop.tasks.task_8_create_case_chat.agent.CaseIntakeInterviewer;

public enum WorkshopTask {
    FIRST_AGENT(
            1,
            "Your first agent",
            "tasks/task_1_first_agent/",
            "Build the ChatModel in VertexAiConfig, write the @SystemMessage in CaseTypeClassifier,"
                    + " and open the case its answer describes in CaseIntake."),
    GUARDRAILS(
            2,
            "Is this even a claim?",
            "tasks/task_2_guardrails/guardrails/Guardrails.java",
            "Refuse text that nobody could open a case from, before it reaches the model."),
    DOCUMENT_AGENT(
            3,
            "Give it a file",
            "tasks/task_3_document_agent/agent/DocumentAnalyzer.java",
            "Write the agent that reads an uploaded PDF or photo."),
    EVALUATION(
            4,
            "How would you know?",
            "tasks/task_4_evaluation/",
            "Build the sets four evaluations run over, then read what they say: a category is a"
                    + " comparison, facts are coverage, an attack set has a right answer."),
    FRAUD_DETECTION(
            5,
            "Case: Fraud detection",
            "tasks/task_5_fraud_detection/FraudScreener.java",
            "Write the checks that run in Java after the agent has answered."),
    CASE_SUMMARY(
            6,
            "Case: Claim summary",
            "tasks/task_6_case_summary/agent/CaseSummarizer.java",
            "Write the agent that reads every document on a case at once."),
    ADVISOR_CHAT(
            7,
            "Case: Advisor chat",
            "tasks/task_7_advisor_chat/agent/CaseChatAgent.java",
            "Write the chat agent and its tools."),
    CREATE_CASE_CHAT(
            8,
            "Case: File claim with AI chat",
            "tasks/task_8_create_case_chat/agent/CaseIntakeInterviewer.java",
            "Write the @SystemMessage in CaseIntakeInterviewer so it can ask before it commits.");


    private static final String SOURCE_ROOT = "backend/src/main/java/com/example/aiworkshop/";

    private final int number;
    private final String title;
    private final String path;
    private final String todo;

    WorkshopTask(int number, String title, String path, String todo) {
        this.number = number;
        this.title = title;
        this.path = path;
        this.todo = todo;
    }

    public int number() {
        return number;
    }

    public String title() {
        return title;
    }

    public String file() {
        return SOURCE_ROOT + path;
    }

    public String todo() {
        return todo;
    }

    public String brief() {
        return "docs/tasks/task_%d_%s.md".formatted(number, name().toLowerCase());
    }
}
