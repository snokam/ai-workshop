package com.example.aiworkshop.workshop;

import com.example.aiworkshop.tasks.task_1_first_agent.ClaimIntake;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_1_first_agent.agent.ClaimTypeClassifier;
import com.example.aiworkshop.tasks.task_1_first_agent.agent.VertexAiConfig;
import com.example.aiworkshop.tasks.task_3_document_agent.agent.DocumentAnalyzer;
import com.example.aiworkshop.tasks.task_2_guardrails.Guardrails;
import com.example.aiworkshop.tasks.task_5_claim_summary.agent.ClaimSummarizer;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.agent.ClaimChatAgent;
import com.example.aiworkshop.tasks.task_7_streaming_file_claim_chat.agent.ClaimIntakeInterviewer;

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
            "Claim summary",
            "tasks/task_5_claim_summary/agent/ClaimSummarizer.java",
            "Write the agent that reads every document on a claim at once, give it a memory of what it"
                    + " said last time, then the rubric that says whether its answer holds up."),
    ADVISOR_CHAT_WITH_TOOLS(
            6,
            "Advisor chat with tools",
            "tasks/task_6_advisor_chat_with_tools/agent/ClaimChatTools.java",
            "Describe two of the tools so the model knows when to reach for them, then hand the tools"
                    + " to the agent."),
    STREAMING_FILE_CLAIM_CHAT(
            7,
            "File a claim with a streaming chat",
            "tasks/task_7_streaming_file_claim_chat/InterviewNarration.java",
            "Carry a TokenStream to the browser, so the claimant reads the answer as it is written.");


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

    /** The task's own README, beside the code it changes. */
    public String brief() {
        return SOURCE_ROOT + "tasks/task_%d_%s/README.md".formatted(number, name().toLowerCase());
    }
}
