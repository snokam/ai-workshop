package com.example.aiworkshop.workshop;

/**
 * The six exercises, and where each one is written.
 *
 * <p>Every task ships unfinished. Until a task is done the application still starts and every other
 * screen still works — the one feature that task provides answers with {@link
 * TaskNotImplementedException} instead, and the message says which file to open. Nothing here is
 * cosmetic: it is the difference between a workshop where one missing method takes the whole app
 * down and one where you can do the tasks in any order.
 */
public enum WorkshopTask {
    FIRST_AGENT(
            1,
            "Your first agent",
            "tasks/task_1_first_agent/CaseTypeClassifier.java",
            "Write the @SystemMessage that decides which kind of case to open, then set IMPLEMENTED to true."),
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

    /** The file to open, from the repository root, so it can be pasted straight into an editor. */
    public String file() {
        return SOURCE_ROOT + path;
    }

    public String todo() {
        return todo;
    }

    /** The brief, for the message the screen shows. */
    public String brief() {
        return "docs/tasks/task_%d_%s.md".formatted(number, name().toLowerCase());
    }
}
