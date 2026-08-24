package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.model;

import dev.langchain4j.service.tool.ToolExecution;

public record ToolCall(String name, String arguments) {
    public static ToolCall of(ToolExecution execution) {
        return new ToolCall(execution.request().name(), execution.request().arguments());
    }
}
