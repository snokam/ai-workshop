package com.example.aiworkshop.cases.chat;

import dev.langchain4j.service.tool.ToolExecution;

public record ToolCall(String name, String arguments) {
    public static ToolCall of(ToolExecution execution) {
        return new ToolCall(execution.request().name(), execution.request().arguments());
    }
}
