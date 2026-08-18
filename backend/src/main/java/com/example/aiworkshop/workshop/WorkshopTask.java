package com.example.aiworkshop.workshop;


public enum WorkshopTask {
    FIRST_AGENT(
            1,
            "Your first agent",
            "tasks/task_1_first_agent/",
            "Build the ChatModel in VertexAiConfig, write the @SystemMessage in CaseTypeClassifier,"
                    + " then set IMPLEMENTED to true."),
    DOCUMENT_AGENT(
            2,
            "Give it a file",
            "tasks/task_2_document_agent/DocumentAnalyzer.java",
            "Write the agent that reads an uploaded PDF or photo, then set IMPLEMENTED to true."),
    GUARDRAILS(
            3,
            "Don't be talked round",
            "tasks/task_3_guardrails/Guardrails.java",
            "Write the input and output guardrails, then set IMPLEMENTED to true."),
    POSTPROCESSING(
            4,
            "What the model cannot know",
            "tasks/task_4_postprocessing/FraudScreener.java",
            "Write the checks that run in Java after the agent has answered, then set IMPLEMENTED to true."),
    CHAT(
            5,
            "Tools and memory",
            "tasks/task_5_chat/CaseChatAgent.java",
            "Write the chat agent and its tools, then set IMPLEMENTED to true."),
    SUMMARY(
            6,
            "Across documents",
            "tasks/task_6_summary/CaseSummarizer.java",
            "Write the agent that reads every document on a case at once, then set IMPLEMENTED to true.");

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
