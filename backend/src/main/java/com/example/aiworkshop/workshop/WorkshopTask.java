package com.example.aiworkshop.workshop;

import com.example.aiworkshop.tasks.task_1_first_agent.ClaimIntake;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_1_first_agent.agent.ClaimTypeClassifier;
import com.example.aiworkshop.tasks.task_1_first_agent.agent.VertexAiConfig;
import com.example.aiworkshop.tasks.task_3_document_agent.agent.DocumentAnalyzer;
import com.example.aiworkshop.tasks.task_2_guardrails.Guardrails;
import com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.agent.ClaimSummarizer;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.agent.ClaimChatAgent;

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
    CLAIM_SUMMARY_CHOOSING_MODELS(
            5,
            "Claim summary, and which model each job needs",
            "tasks/task_5_claim_summary_choosing_models/agent/SummaryConfig.java",
            "Two agents, two jobs of very different difficulty. Decide which model each one needs,"
                    + " and read what the calls cost before you do."),
    ADVISOR_CHAT_WITH_TOOLS_AND_MEMORY(
            6,
            "Advisor chat with tools and memory",
            "tasks/task_6_advisor_chat_with_tools_and_memory/agent/ClaimChatTools.java",
            "Describe two of the tools so the model knows when to reach for them, hand the tools to the"
                    + " agent, and give each claim its own conversation."),
    STREAMING_FORM_HELP(
            7,
            "Help while you type, streamed",
            "tasks/task_7_streaming_form_help/StreamedHelp.java",
            "Carry a TokenStream to the browser, so somebody filling in the form is helped while they"
                    + " write rather than after they submit.");


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
