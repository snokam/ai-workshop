package com.example.aiworkshop.workshop;

import com.example.aiworkshop.tasks.task_1_first_agent.agent.CaseTypeClassifier;
import com.example.aiworkshop.tasks.task_1_first_agent.agent.VertexAiConfig;
import com.example.aiworkshop.tasks.task_2_document_agent.agent.DocumentAnalyzer;
import com.example.aiworkshop.tasks.task_3_guardrails.guardrails.Guardrails;
import com.example.aiworkshop.tasks.task_4_fraud_detection.FraudScreener;
import com.example.aiworkshop.tasks.task_5_summary.agent.CaseSummarizer;
import com.example.aiworkshop.tasks.task_6_chat.agent.CaseChatAgent;
import com.example.aiworkshop.tasks.task_7_create_case_chat.agent.CaseIntakeInterviewer;

public enum WorkshopTask {
    FIRST_AGENT(
            1,
            "Your first agent",
            "tasks/task_1_first_agent/",
            "Build the ChatModel in VertexAiConfig, write the @SystemMessage in CaseTypeClassifier,"
                    + " and open the case its answer describes in CaseIntake."),
    DOCUMENT_AGENT(
            2,
            "Give it a file",
            "tasks/task_2_document_agent/agent/DocumentAnalyzer.java",
            "Write the agent that reads an uploaded PDF or photo."),
    GUARDRAILS(
            3,
            "Don't be talked round",
            "tasks/task_3_guardrails/guardrails/Guardrails.java",
            "Write the input and output guardrails."),
    FRAUD_DETECTION(
            4,
            "What the model cannot know",
            "tasks/task_4_fraud_detection/FraudScreener.java",
            "Write the checks that run in Java after the agent has answered."),
    SUMMARY(
            5,
            "Across documents",
            "tasks/task_5_summary/agent/CaseSummarizer.java",
            "Write the agent that reads every document on a case at once."),
    CHAT(
            6,
            "Tools and memory",
            "tasks/task_6_chat/agent/CaseChatAgent.java",
            "Write the chat agent and its tools."),
    CREATE_CASE_CHAT(
            7,
            "Report with AI chat",
            "tasks/task_7_create_case_chat/agent/CaseIntakeInterviewer.java",
            "Write the @SystemMessage in CaseIntakeInterviewer so it can ask before it commits."),
    EVALUATION(
            8,
            "How would you know?",
            "tasks/task_8_evaluation/",
            "Label the descriptions you would argue about, run the classifier over them, and decide"
                    + " what the disagreements mean.");


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
