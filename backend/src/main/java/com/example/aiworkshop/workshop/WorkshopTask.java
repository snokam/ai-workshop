package com.example.aiworkshop.workshop;

import com.example.aiworkshop.tasks.task_1_first_agent.ClaimIntake;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_1_first_agent.agent.ClaimTypeClassifier;
import com.example.aiworkshop.tasks.task_1_first_agent.agent.VertexAiConfig;
import com.example.aiworkshop.tasks.task_3_document_agent.agent.DocumentAnalyzer;
import com.example.aiworkshop.tasks.task_2_guardrails.Guardrails;
import com.example.aiworkshop.tasks.task_5_claim_summary.agent.ClaimSummarizer;
import com.example.aiworkshop.tasks.task_6_advisor_chat.agent.ClaimChatAgent;
import com.example.aiworkshop.tasks.task_7_create_claim_chat.agent.ClaimIntakeInterviewer;

public enum WorkshopTask {
    FIRST_AGENT(
            1,
            "Your first agent",
            "tasks/task_1_first_agent/",
            "Build the ChatModel in VertexAiConfig, write the @SystemMessage in ClaimTypeClassifier,"
                    + " and open the claim its answer describes in ClaimIntake."),
    GUARDRAILS(
            2,
            "Is this even a claim?",
            "tasks/task_2_guardrails/Guardrails.java",
            "Refuse text nobody could open a claim from, and text that is instructing the system"
                    + " rather than describing a situation, before either reaches the model."),
    DOCUMENT_AGENT(
            3,
            "Give it a file",
            "tasks/task_3_document_agent/model/DocumentAnalysis.java",
            "Say what two of the fields mean, then send the file to the model as a file."),
    EVALUATION(
            4,
            "How would you know?",
            "tasks/task_4_evaluation/",
            "Build the two sets, then run ./mvnw test -Pevaluate and read what it says about the"
                    + " classifier from task 1 and the guardrails from task 2."),
    CLAIM_SUMMARY(
            5,
            "Claim: Claim summary",
            "tasks/task_5_claim_summary/agent/ClaimSummarizer.java",
            "Write the agent that reads every document on a claim at once."),
    ADVISOR_CHAT(
            6,
            "Claim: Advisor chat",
            "tasks/task_6_advisor_chat/agent/ClaimChatAgent.java",
            "Write the chat agent and its tools."),
    CREATE_CLAIM_CHAT(
            7,
            "Claim: File claim with AI chat",
            "tasks/task_7_create_claim_chat/agent/ClaimIntakeInterviewer.java",
            "Write the @SystemMessage in ClaimIntakeInterviewer so it can ask before it commits.");


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
